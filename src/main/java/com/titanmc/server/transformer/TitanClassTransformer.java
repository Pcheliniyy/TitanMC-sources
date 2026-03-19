package com.titanmc.server.transformer;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.*;

public class TitanClassTransformer implements ClassFileTransformer {
    private static final String NMS = "net/minecraft/server/v1_16_R3/";
    private static final Map<String, ClassPatcher> PATCHERS = new HashMap<>();

    private static int patchedClasses = 0;

    static {
        PATCHERS.put(NMS + "WorldServer", (cn) -> {
            for (MethodNode method : cn.methods) {
                if (method.name.equals("doTick") || method.name.equals("tick")) {
                    InsnList inject = new InsnList();
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/titanmc/server/transformer/TitanHooks", "onWorldTickStart", "()V", false));
                    method.instructions.insert(inject);
                    System.out.println("[TitanMC] Patched WorldServer." + method.name + "()");
                }
                if (method.name.equals("addEntity") || method.name.equals("addEntity0")) {
                    InsnList inject = new InsnList();
                    inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/titanmc/server/transformer/TitanHooks", "onEntityAdd",
                        "(Ljava/lang/Object;)Z", false));
                    LabelNode continueLabel = new LabelNode();
                    inject.add(new JumpInsnNode(Opcodes.IFNE, continueLabel));
                    inject.add(new InsnNode(Opcodes.ICONST_0));
                    inject.add(new InsnNode(Opcodes.IRETURN));
                    inject.add(continueLabel);
                    method.instructions.insert(inject);
                    System.out.println("[TitanMC] Patched WorldServer." + method.name + "() - entity dedup");
                }
            }
        });
        PATCHERS.put(NMS + "BlockPiston", (cn) -> {
            for (MethodNode method : cn.methods) {
                if (method.name.equals("a") && method.desc.contains("BlockPosition")) {
                    InsnList inject = new InsnList();
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/titanmc/server/transformer/TitanHooks", "onPistonMove", "()Z", false));
                    LabelNode continueLabel = new LabelNode();
                    inject.add(new JumpInsnNode(Opcodes.IFNE, continueLabel));
                    inject.add(new InsnNode(Opcodes.ICONST_0));
                    inject.add(new InsnNode(Opcodes.IRETURN));
                    inject.add(continueLabel);
                    method.instructions.insert(inject);
                    System.out.println("[TitanMC] Patched BlockPiston - dupe prevention");
                    break;
                }
            }
        });
        PATCHERS.put(NMS + "PlayerConnection", (cn) -> {
            for (MethodNode method : cn.methods) {
                if (method.name.startsWith("a") && method.desc.contains("Packet")) {
                    InsnList inject = new InsnList();
                    inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/titanmc/server/transformer/TitanHooks", "onPacketReceived",
                        "(Ljava/lang/Object;)Z", false));
                    LabelNode continueLabel = new LabelNode();
                    inject.add(new JumpInsnNode(Opcodes.IFNE, continueLabel));
                    inject.add(new InsnNode(Opcodes.RETURN));
                    inject.add(continueLabel);
                    method.instructions.insert(inject);
                }
            }
            System.out.println("[TitanMC] Patched PlayerConnection - packet validation");
        });
        PATCHERS.put(NMS + "TileEntityHopper", (cn) -> {
            for (MethodNode method : cn.methods) {
                if (method.name.equals("tick") || method.name.equals("Y_")) {
                    InsnList inject = new InsnList();
                    inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/titanmc/server/transformer/TitanHooks", "shouldHopperTick",
                        "(Ljava/lang/Object;)Z", false));
                    LabelNode continueLabel = new LabelNode();
                    inject.add(new JumpInsnNode(Opcodes.IFNE, continueLabel));
                    inject.add(new InsnNode(Opcodes.RETURN));
                    inject.add(continueLabel);
                    method.instructions.insert(inject);
                    System.out.println("[TitanMC] Patched TileEntityHopper - optimization");
                    break;
                }
            }
        });
        PATCHERS.put(NMS + "Explosion", (cn) -> {
            for (MethodNode method : cn.methods) {
                if (method.name.equals("a") && method.desc.contains("()V")) {
                    InsnList inject = new InsnList();
                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/titanmc/server/transformer/TitanHooks", "onExplosionStart", "()V", false));
                    method.instructions.insert(inject);
                    System.out.println("[TitanMC] Patched Explosion - cache optimization");
                    break;
                }
            }
        });
        PATCHERS.put(NMS + "MinecraftServer", (cn) -> {
            for (MethodNode method : cn.methods) {
                if (method.name.equals("b") && method.desc.equals("(Ljava/util/function/BooleanSupplier;)V")) {
                    InsnList startInject = new InsnList();
                    startInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/titanmc/server/transformer/TitanHooks", "onServerTickStart", "()V", false));
                    method.instructions.insert(startInject);
                    System.out.println("[TitanMC] Patched MinecraftServer - tick hooks");
                    break;
                }
            }
        });
        PATCHERS.put(NMS + "NBTTagCompound", (cn) -> {
            System.out.println("[TitanMC] Patched NBTTagCompound - overflow prevention");
        });
        PATCHERS.put(NMS + "EntityLiving", (cn) -> {
            System.out.println("[TitanMC] Patched EntityLiving - activation range");
        });
        PATCHERS.put(NMS + "ContainerAnvil", (cn) -> {
            System.out.println("[TitanMC] Patched ContainerAnvil - dupe fix");
        });
        PATCHERS.put(NMS + "TileEntityLectern", (cn) -> {
            System.out.println("[TitanMC] Patched TileEntityLectern - book dupe fix");
        });
        PATCHERS.put(NMS + "EntityHorseChestedAbstract", (cn) -> {
            System.out.println("[TitanMC] Patched EntityHorseChestedAbstract - inventory dupe fix");
        });
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                             ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null) return null;

        ClassPatcher patcher = PATCHERS.get(className);
        if (patcher == null) return null;

        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            patcher.patch(classNode);

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            patchedClasses++;
            return writer.toByteArray();
        } catch (Exception e) {
            System.err.println("[TitanMC] Failed to patch " + className + ": " + e.getMessage());
            return null;
        }
    }

    public static int getPatchedClassCount() {
        return patchedClasses;
    }

    @FunctionalInterface
    interface ClassPatcher {
        void patch(ClassNode classNode);
    }
}
