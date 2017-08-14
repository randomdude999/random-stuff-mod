package randomdude999.random_stuff.asm;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import randomdude999.random_stuff.config.ConfigHandler;

@SuppressWarnings("unused") // Only used by ASM injected code
public class ASMHooks {

    public static int getFlammability(Block block) {
        if (ConfigHandler.additionalFireBlocks.contains(block))
            return ConfigHandler.customFireFlammability;
        Integer integer = Blocks.FIRE.flammabilities.get(block);
        return integer == null ? 0 : integer;
    }

    public static int getEncouragement(Block block) {
        if (ConfigHandler.additionalFireBlocks.contains(block))
            return ConfigHandler.customFireEncouragement;
        Integer integer = Blocks.FIRE.encouragements.get(block);
        return integer == null ? 0 : integer;
    }

    public static boolean getCanBurn(Material material) {
        if (ConfigHandler.additionalFireMaterials.contains(material))
            return true;
        return material.canBurn;
    }
}
