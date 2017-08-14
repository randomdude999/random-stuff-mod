/*
 * 90% of this file is stolen from Vazkii. Thanks!
 */
package randomdude999.random_stuff.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import randomdude999.random_stuff.config.ConfigHandler;
import randomdude999.random_stuff.core.LoadingPlugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class ClassTransformer implements IClassTransformer {

    private static final String configHandler = "randomdude999/random_stuff/config/ConfigHandler";
    private static final String asmHooks = "randomdude999/random_stuff/asm/ASMHooks";

    private static final Map<String, Transformer> transformers = new HashMap<>();

    static {
        transformers.put("net.minecraft.block.BlockCactus", ClassTransformer::patchCactusReed);
        transformers.put("net.minecraft.block.BlockReed", ClassTransformer::patchCactusReed);
        transformers.put("net.minecraft.block.BlockChorusFlower", ClassTransformer::patchChorusFlower);
        transformers.put("net.minecraft.block.BlockSponge", ClassTransformer::patchSponge);
        transformers.put("net.minecraft.block.BlockLiquid", ClassTransformer::patchBlockLiquid);
        transformers.put("net.minecraft.block.BlockDynamicLiquid", ClassTransformer::patchBlockDynamicLiquid);
        transformers.put("net.minecraft.world.WorldServer", ClassTransformer::patchWorldServer);
        transformers.put("net.minecraft.block.BlockFire", ClassTransformer::patchFire);
        transformers.put("net.minecraft.block.material.Material", ClassTransformer::patchMaterial);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // transformedName is deobf in both deobf and obf envs
        if (transformers.containsKey(transformedName))
            return transformers.get(transformedName).apply(basicClass);

        return basicClass;
    }

    // Can be reused since we need to do the exact same patch on both cactus and reed
    private static byte[] patchCactusReed(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("updateTick", "func_180650_b",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V");
        return transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.ICONST_3,
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.GETSTATIC, configHandler, "maxCactusGrowth", "I"));
                    return true;
                }
        )));
    }

    private static byte[] patchChorusFlower(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("placeDeadFlower", "func_185605_c",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V");
        return transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.ICONST_5,
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.GETSTATIC, configHandler, "deadChorusState", "I"));
                    return true;
                }
        )));
    }

    private static byte[] patchSponge(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("tryAbsorb", "func_176311_e",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V");
        return transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.ICONST_1,
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.GETSTATIC, configHandler, "spongeAbsorbedState", "I"));
                    return true;
                }
        )));
    }

    private static byte[] patchBlockLiquid(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("checkForMixing", "func_176365_e",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z");

        basicClass = transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.GETSTATIC
                        && ((FieldInsnNode) node).owner.equals("net/minecraft/init/Blocks")
                        && (checkField(((FieldInsnNode) node).name, "OBSIDIAN", "field_150343_Z")),
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.GETSTATIC, configHandler, "obsidianReplacement", "Lnet/minecraft/block/Block;"));
                    return true;
                }
        )));

        return transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.GETSTATIC
                        && ((FieldInsnNode) node).owner.equals("net/minecraft/init/Blocks")
                        && checkField(((FieldInsnNode) node).name, "COBBLESTONE", "field_150347_e"),
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.GETSTATIC, configHandler, "cobblestoneReplacement", "Lnet/minecraft/block/Block;"));
                    return true;
                }
        )));
    }

    private static byte[] patchBlockDynamicLiquid(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("updateTick", "func_180650_b",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V");

        return transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.GETSTATIC
                        && ((FieldInsnNode) node).owner.equals("net/minecraft/init/Blocks")
                        && checkField(((FieldInsnNode) node).name, "STONE", "field_150348_b"),
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.GETSTATIC, configHandler, "stoneReplacement", "Lnet/minecraft/block/Block;"));
                    return true;
                }
        )));
    }

    private static byte[] patchWorldServer(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("updateBlocks", "func_147456_g",
                "()V");

        basicClass = transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.GETSTATIC
                        && ((FieldInsnNode) node).owner.equals("net/minecraft/init/Blocks")
                        && checkField(((FieldInsnNode) node).name, "ICE", "field_150432_aD"),
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.GETSTATIC, configHandler, "iceReplacement", "Lnet/minecraft/block/Block;"));
                    return true;
                }
        )));

        return transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.GETSTATIC
                        && ((FieldInsnNode) node).owner.equals("net/minecraft/init/Blocks")
                        && checkField(((FieldInsnNode) node).name, "SNOW_LAYER", "field_150431_aC"),
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.GETSTATIC, configHandler, "snowReplacement", "Lnet/minecraft/block/Block;"));
                    return true;
                }
        )));
    }

    private static byte[] patchFire(byte[] basicClass) {
        if(ConfigHandler.lawfulFireMode)
            return basicClass;
        MethodSignature sig1 = new MethodSignature("getFlammability", "func_176532_c",
                "(Lnet/minecraft/block/Block;)I");
        MethodSignature sig2 = new MethodSignature("getEncouragement", "func_176534_d",
                "(Lnet/minecraft/block/Block;)I");

        basicClass = transform(basicClass, Pair.of(sig1, combine(
                (AbstractInsnNode node) -> true,
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.clear();
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, asmHooks, "getFlammability", "(Lnet/minecraft/block/Block;)I", false));
                    method.instructions.add(new InsnNode(Opcodes.IRETURN));
                    return true;
                }
        )));

        return transform(basicClass, Pair.of(sig2, combine(
                (AbstractInsnNode node) -> true,
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.clear();
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, asmHooks, "getFlammability", "(Lnet/minecraft/block/Block;)I", false));
                    method.instructions.add(new InsnNode(Opcodes.IRETURN));
                    return true;
                }
        )));

    }

    private static byte[] patchMaterial(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("getCanBurn", "func_76217_h",
                "()Z");

        return transform(basicClass, Pair.of(sig, combine(
                (AbstractInsnNode node) -> node.getOpcode() == Opcodes.GETFIELD,
                (MethodNode method, AbstractInsnNode node) -> {
                    method.instructions.set(node, new FieldInsnNode(Opcodes.INVOKESTATIC, asmHooks, "getCanBurn", "(Lnet/minecraft/block/material/Material;)Z"));
                    return true;
                }
        )));
    }

    ///////////////////////////////////////////////
    // BELOW IS VAZKII'S AMAZING ASM BOILERPLATE //
    ///////////////////////////////////////////////

    @SafeVarargs // i guess it's safe?
    private static byte[] transform(byte[] basicClass, Pair<MethodSignature, MethodAction>... methods) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        boolean didAnything = false;

        for(Pair<MethodSignature, MethodAction> pair : methods) {
            log("Applying Transformation to method (" + pair.getLeft() + ")");
            didAnything |= findMethodAndTransform(node, pair.getLeft(), pair.getRight());
        }

        if(didAnything) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    private static boolean findMethodAndTransform(ClassNode node, MethodSignature sig, MethodAction pred) {
        String funcName = sig.funcName;
        if(LoadingPlugin.runtimeDeobfEnabled)
            funcName = sig.srgName;

        for(MethodNode method : node.methods) {
            if((method.name.equals(funcName) || method.name.equals(sig.srgName)) && (method.desc.equals(sig.funcDesc))) {
                log("Located Method, patching...");

                boolean finish = pred.test(method);
                log("Patch result: " + finish);

                return finish;
            }
        }

        log("Failed to locate the method!");
        return false;
    }

    private static MethodAction combine(NodeFilter filter, NodeAction action) {
        return (MethodNode mnode) -> applyOnNode(mnode, filter, action);
    }

    private static boolean applyOnNode(MethodNode method, NodeFilter filter, NodeAction action) {
        Iterator<AbstractInsnNode> iterator = method.instructions.iterator();

        boolean didAny = false;
        while(iterator.hasNext()) {
            AbstractInsnNode anode = iterator.next();
            if(filter.test(anode)) {
                log("Located patch target node " + getNodeString(anode));
                didAny = true;
                if(action.test(method, anode))
                    break;
            }
        }

        return didAny;
    }

    private static void log(String str) {
        LoadingPlugin.LOGGER.printf(Level.INFO,"[RandomStuff ASM] %s", str);
    }

    private static String getNodeString(AbstractInsnNode node) {
        Printer printer = new Textifier();

        TraceMethodVisitor visitor = new TraceMethodVisitor(printer);
        node.accept(visitor);

        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();

        return sw.toString().replaceAll("\n", "").trim();
    }

    private static boolean checkField(String field, String mcp, String srg) {
        return field.equals(mcp) || field.equals(srg);
    }

    private static class MethodSignature {
        String funcName, srgName, funcDesc;

        MethodSignature(String funcName, String srgName, String funcDesc) {
            this.funcName = funcName;
            this.srgName = srgName;
            this.funcDesc = funcDesc;
        }

        @Override
        public String toString() {
            return "Names [" + funcName + ", " + srgName + "] Descriptor " + funcDesc;
        }

    }

    // Basic interface aliases to not have to clutter up the code with generics over and over again
    private interface Transformer extends Function<byte[], byte[]> { }
    private interface MethodAction extends Predicate<MethodNode> { }
    private interface NodeFilter extends Predicate<AbstractInsnNode> { }
    private interface NodeAction extends BiPredicate<MethodNode, AbstractInsnNode> { }
}
