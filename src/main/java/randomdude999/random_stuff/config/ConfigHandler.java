package randomdude999.random_stuff.config;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import randomdude999.random_stuff.core.LoadingPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("WeakerAccess") // Some fields are only used by ASM injected code
public class ConfigHandler {

    public static Configuration config;

    public static int maxCactusGrowth;
    public static int deadChorusState;
    public static int spongeAbsorbedState;
    public static int pistonPushLimit;

    public static Block obsidianReplacement;
    public static Block cobblestoneReplacement;
    public static Block stoneReplacement;
    public static Block snowReplacement;
    public static Block iceReplacement;

    public static boolean lawfulFireMode;
    public static Set<Block> additionalFireBlocks = new HashSet<>();
    public static Set<Material> additionalFireMaterials = new HashSet<>();
    public static int customFireFlammability;
    public static int customFireEncouragement;

    static {
        File configDir = new File((File) FMLInjectionData.data()[6], "config");
        config = new Configuration(new File(configDir, LoadingPlugin.MOD_ID + ".cfg"));
        initialSyncConfig();
    }

    /**
     * Loads config options that don't need Minecraft code, thus being able to be used while transforming classes
     */
    public static void initialSyncConfig() {
        lawfulFireMode = config.get(Configuration.CATEGORY_GENERAL, "Use alternate fire registering method",
                false, "Use a different way of registering blocks as flammable. This works with other mods better, but has the downside that you need to restart minecraft for the changes to apply, and lava can't ignite the blocks. Use only if some blocks that are supposed to be flammable aren't.").setRequiresMcRestart(true).getBoolean();
    }

    @SuppressWarnings("deprecation")
    public static void syncConfig() {
        initialSyncConfig();

        maxCactusGrowth = config.getInt("Max cactus/sugar cane height", Configuration.CATEGORY_GENERAL,
                3, 0, 256, "Maximum height of cactus / sugar cane. " +
                        "Set to 256 to make them grow infinitely.");
        deadChorusState = config.getBoolean("Infinite chorus fruit", Configuration.CATEGORY_GENERAL,
                false, "Whether to allow chorus fruit to grow infinitely") ? 0 : 5;
        spongeAbsorbedState = config.getBoolean("Never wet sponge", Configuration.CATEGORY_GENERAL,
                false, "If enabled, sponge won't become wet after contact with water.") ? 0 : 1;

        pistonPushLimit = config.getInt("Maximum piston push limit", Configuration.CATEGORY_GENERAL,
                12, 0, 4096, "Maximum number of blocks a piston can push.");


        obsidianReplacement = Block.getBlockFromName(config.getString("Water against lava source reaction",
                Configuration.CATEGORY_GENERAL, "minecraft:obsidian",
                "What block gets generated when a lava source block touches flowing water."));
        if (obsidianReplacement == null)
            obsidianReplacement = Blocks.OBSIDIAN;

        cobblestoneReplacement = Block.getBlockFromName(config.getString("Water against lava reaction",
                Configuration.CATEGORY_GENERAL, "minecraft:cobblestone",
                "What block gets generated when flowing water touches flowing lava."));
        if (cobblestoneReplacement == null)
            cobblestoneReplacement = Blocks.COBBLESTONE;

        stoneReplacement = Block.getBlockFromName(config.getString("Water source against lava reaction",
                Configuration.CATEGORY_GENERAL, "minecraft:stone",
                "What block gets generated when flowing lava touches a water source block."));
        if (stoneReplacement == null)
            stoneReplacement = Blocks.STONE;

        snowReplacement = Block.getBlockFromName(config.getString("Snow replacement",
                Configuration.CATEGORY_GENERAL, "minecraft:snow_layer",
                "What block should the ground be covered in when it snows"));
        if (snowReplacement == null)
            snowReplacement = Blocks.SNOW_LAYER;

        iceReplacement = Block.getBlockFromName(config.getString("Freezing reaction",
                Configuration.CATEGORY_GENERAL, "minecraft:ice",
                "What block should water freeze into"));
        if (iceReplacement == null)
            iceReplacement = Blocks.ICE;

        String[] additionalFlammables = config.get(Configuration.CATEGORY_GENERAL, "Additional flammables",
                new String[]{}, "List of additional blocks that can catch fire.").getStringList();
        int encouragement = config.get(Configuration.CATEGORY_GENERAL, "Additional flammable encouragement", 5,
                "Encouragement for additional blocks. See https://minecraft.gamepedia.com/Fire for more info.").getInt();
        int flammability = config.get(Configuration.CATEGORY_GENERAL, "Additional flammable flammability", 20,
                "Flammability for additional blocks. See https://minecraft.gamepedia.com/Fire for more info.").getInt();

        if (lawfulFireMode) {
            for (String blockName : additionalFlammables) {
                Block block = Block.getBlockFromName(blockName);
                if (block == null)
                    continue;
                Blocks.FIRE.encouragements.put(block, encouragement);
                Blocks.FIRE.flammabilities.put(block, flammability);
            }
        } else {
            customFireFlammability = flammability;
            customFireEncouragement = encouragement;
            additionalFireBlocks.clear();
            additionalFireMaterials.clear();
            for (String blockName : additionalFlammables) {
                Block block = Block.getBlockFromName(blockName);
                if (block == null)
                    continue;
                additionalFireBlocks.add(block);
                additionalFireMaterials.add(block.getMaterial(null));
            }
        }

        if (config.hasChanged())
            config.save();
    }

}
