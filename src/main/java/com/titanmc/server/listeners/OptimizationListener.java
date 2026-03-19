package com.titanmc.server.listeners;

import com.titanmc.server.config.TitanConfig;
import com.titanmc.server.optimization.*;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OptimizationListener implements Listener {

    private final TitanConfig config;
    private final OptimizationManager optimizationManager;
    private final Map<UUID, Long> lastEntityTick = new ConcurrentHashMap<>();
    private final Map<Long, Integer> redstoneUpdatesPerTick = new ConcurrentHashMap<>();
    private long lastRedstoneTick = 0;
    private final Map<Long, List<Item>> itemsByChunk = new ConcurrentHashMap<>();
    private final Map<UUID, int[]> lastPlayerChunk = new ConcurrentHashMap<>();

    public OptimizationListener(TitanConfig config, OptimizationManager optimizationManager) {
        this.config = config;
        this.optimizationManager = optimizationManager;
    }

    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (!config.optimizeEntities) return;

        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER
            || reason == CreatureSpawnEvent.SpawnReason.CUSTOM
            || reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            return;
        }

        Chunk chunk = entity.getLocation().getChunk();
        int entityCount = 0;
        for (Entity e : chunk.getEntities()) {
            if (e instanceof LivingEntity && !(e instanceof Player)) {
                entityCount++;
            }
        }
        if (entityCount >= config.maxEntitiesPerChunk) {
            event.setCancelled(true);
            return;
        }
        double tps = TickOptimizer.getCurrentTPS();
        if (tps < 15.0 && reason == CreatureSpawnEvent.SpawnReason.NATURAL) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                event.setCancelled(true);
            }
        }
    }

    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!config.optimizeItemMerge) return;

        Item newItem = event.getEntity();
        ItemStack stack = newItem.getItemStack();
        if (stack.getMaxStackSize() <= 1) return;
        for (Entity nearby : newItem.getNearbyEntities(3, 2, 3)) {
            if (!(nearby instanceof Item)) continue;
            Item existingItem = (Item) nearby;
            ItemStack existingStack = existingItem.getItemStack();

            if (!existingStack.isSimilar(stack)) continue;

            int totalAmount = existingStack.getAmount() + stack.getAmount();
            if (totalAmount > stack.getMaxStackSize()) continue;
            existingStack.setAmount(totalAmount);
            existingItem.setItemStack(existingStack);
            event.setCancelled(true);
            return;
        }
    }

    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!config.optimizeEntities) return;
        if (event.getEntity() instanceof Player) return;
        if (event.getTarget() == null) return;

        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        double distSq = entity.getLocation().distanceSquared(target.getLocation());
        double maxRange = getActivationRange(entity);

        if (distSq > maxRange * maxRange) {
            event.setCancelled(true);
        }
    }

    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        if (!config.optimizeEntities) return;
        if (event.getBlock().getType().name().contains("PRESSURE_PLATE")) {
            if (!(event.getEntity() instanceof Player)) {
                UUID id = event.getEntity().getUniqueId();
                long now = System.currentTimeMillis();
                Long last = lastEntityTick.get(id);
                if (last != null && now - last < 500) {
                    event.setCancelled(true);
                    return;
                }
                lastEntityTick.put(id, now);
            }
        }
    }

    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (!config.optimizeRedstone) return;

        long currentTick = Bukkit.getCurrentTick();
        if (currentTick != lastRedstoneTick) {
            redstoneUpdatesPerTick.clear();
            lastRedstoneTick = currentTick;
        }

        int cx = event.getBlock().getX() >> 4;
        int cz = event.getBlock().getZ() >> 4;
        long chunkKey = ((long) cx << 32) | (cz & 0xFFFFFFFFL);

        int updates = redstoneUpdatesPerTick.merge(chunkKey, 1, Integer::sum);
        if (updates > config.maxRedstonePerChunk) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!config.optimizeChunks) return;

        Chunk chunk = event.getChunk();
        if (event.isNewChunk()) return;
        Entity[] entities = chunk.getEntities();
        int mobCount = 0;
        List<Entity> removable = new ArrayList<>();

        for (Entity entity : entities) {
            if (entity instanceof Player) continue;
            if (entity instanceof LivingEntity) {
                mobCount++;
                if (mobCount > config.maxEntitiesPerChunk) {
                    removable.add(entity);
                }
            }
        }
        for (Entity entity : removable) {
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!config.optimizeChunks) return;

        int cx = event.getChunk().getX();
        int cz = event.getChunk().getZ();
        long chunkKey = ((long) cx << 32) | (cz & 0xFFFFFFFFL);
        redstoneUpdatesPerTick.remove(chunkKey);
        itemsByChunk.remove(chunkKey);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!config.optimizeChunks) return;
        int fromCX = event.getFrom().getBlockX() >> 4;
        int fromCZ = event.getFrom().getBlockZ() >> 4;
        int toCX = event.getTo().getBlockX() >> 4;
        int toCZ = event.getTo().getBlockZ() >> 4;

        if (fromCX == toCX && fromCZ == toCZ) return;

        UUID uuid = event.getPlayer().getUniqueId();
        int[] prev = lastPlayerChunk.get(uuid);
        lastPlayerChunk.put(uuid, new int[]{toCX, toCZ});
        if (prev != null) {
            int dx = toCX - prev[0];
            int dz = toCZ - prev[1];

            if (dx != 0 || dz != 0) {
                World world = event.getPlayer().getWorld();
                int predictX = toCX + (dx * 2);
                int predictZ = toCZ + (dz * 2);

                if (!world.isChunkLoaded(predictX, predictZ)) {
                    world.loadChunk(predictX, predictZ, false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        int cx = event.getPlayer().getLocation().getBlockX() >> 4;
        int cz = event.getPlayer().getLocation().getBlockZ() >> 4;
        lastPlayerChunk.put(event.getPlayer().getUniqueId(), new int[]{cx, cz});
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastPlayerChunk.remove(uuid);
        lastEntityTick.entrySet().removeIf(e ->
            System.currentTimeMillis() - e.getValue() > 60000);
    }

    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!config.optimizeWorld) return;

        if (event.getEntity() instanceof TNTPrimed) {
            Chunk chunk = event.getEntity().getLocation().getChunk();
            int tntCount = 0;
            for (Entity e : chunk.getEntities()) {
                if (e instanceof TNTPrimed) tntCount++;
            }
            if (tntCount > config.maxTntPerChunk) {
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }
    }

    private double getActivationRange(Entity entity) {
        if (entity instanceof Monster) return config.monsterActivationRange;
        if (entity instanceof Animals) return config.animalActivationRange;
        if (entity instanceof Villager) return config.villagerActivationRange;
        if (entity instanceof WaterMob) return config.waterActivationRange;
        if (entity instanceof Flying) return config.flyingActivationRange;
        return config.miscActivationRange;
    }

    private static final java.util.concurrent.ThreadLocalRandom ThreadLocalRandom =
        java.util.concurrent.ThreadLocalRandom.current();
}
