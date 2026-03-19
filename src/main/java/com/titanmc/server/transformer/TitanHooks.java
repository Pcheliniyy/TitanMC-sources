package com.titanmc.server.transformer;

import com.titanmc.server.fixes.*;
import com.titanmc.server.optimization.*;

public final class TitanHooks {

    private static volatile long currentTick = 0;
    private static volatile boolean initialized = false;

    private TitanHooks() {}

    
    public static void onServerTickStart() {
        currentTick++;
        TntDuplicationFix.onTickStart(currentTick);
        RailDuplicationFix.onTickStart(currentTick);
        CarpetDuplicationFix.onTickStart(currentTick);
        GravityBlockDuplicationFix.onTickStart(currentTick);
        BookDuplicationFix.onTickStart(currentTick);
        InventoryDesyncDupeFix.onTickStart(currentTick);
        PistonDuplicationFix.onTickStart(currentTick);
        EntityDuplicationFix.onTickStart(currentTick);
        DeathDuplicationFix.onTickStart(currentTick);
        PortalRollbackDupeFix.onTickStart(currentTick);
        NetherPortalDupeFix.onTickStart(currentTick);
        EndPortalDuplicationFix.onTickStart(currentTick);
        CraftingDuplicationFix.onTickStart(currentTick);
        ExploitFixManager.onTickStart(currentTick);
        ExploitFixManager.resetCrystalCounts();
        EntityOptimizer.onTickStart(currentTick);

        if (!initialized) {
            System.out.println("[TitanMC] All runtime patches active. Server protected.");
            initialized = true;
        }
    }

    
    public static void onWorldTickStart() {
    }

    
    public static boolean onEntityAdd(Object entity) {
        try {
            java.lang.reflect.Method getUUID = entity.getClass().getMethod("getUniqueID");
            java.lang.reflect.Method getWorld = entity.getClass().getMethod("getWorld");

            java.util.UUID uuid = (java.util.UUID) getUUID.invoke(entity);
            Object world = getWorld.invoke(entity);

            int worldId = world.hashCode();
            java.lang.reflect.Field chunkX = findField(entity.getClass(), "chunkX", "u");
            java.lang.reflect.Field chunkZ = findField(entity.getClass(), "chunkZ", "w");

            int cx = 0, cz = 0;
            if (chunkX != null) {
                chunkX.setAccessible(true);
                cx = chunkX.getInt(entity);
            }
            if (chunkZ != null) {
                chunkZ.setAccessible(true);
                cz = chunkZ.getInt(entity);
            }

            return EntityDuplicationFix.registerEntity(uuid, worldId, cx, cz);
        } catch (Exception e) {
            return true;
        }
    }

    
    public static boolean onPistonMove() {
        return true;
    }

    
    public static boolean onPacketReceived(Object playerConnection) {
        try {
            java.lang.reflect.Field playerField = playerConnection.getClass().getDeclaredField("d");
            playerField.setAccessible(true);
            Object player = playerField.get(playerConnection);

            java.lang.reflect.Method getUUID = player.getClass().getMethod("getUniqueID");
            java.util.UUID uuid = (java.util.UUID) getUUID.invoke(player);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    
    public static boolean shouldHopperTick(Object hopper) {
        try {
            java.lang.reflect.Method getPos = hopper.getClass().getMethod("getPosition");
            Object pos = getPos.invoke(hopper);

            java.lang.reflect.Method getX = pos.getClass().getMethod("getX");
            java.lang.reflect.Method getY = pos.getClass().getMethod("getY");
            java.lang.reflect.Method getZ = pos.getClass().getMethod("getZ");

            int x = (int) getX.invoke(pos);
            int y = (int) getY.invoke(pos);
            int z = (int) getZ.invoke(pos);
            long posHash = ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
            return (currentTick + posHash) % 8 == 0;
        } catch (Exception e) {
            return true;
        }
    }

    
    public static void onExplosionStart() {
        WorldOptimizer.clearExplosionBlockCache();
    }

    
    private static java.lang.reflect.Field findField(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
            Class<?> parent = clazz.getSuperclass();
            while (parent != null) {
                try {
                    return parent.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {}
                parent = parent.getSuperclass();
            }
        }
        return null;
    }

    public static long getCurrentTick() {
        return currentTick;
    }
}
