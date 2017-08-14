package randomdude999.random_stuff.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import randomdude999.random_stuff.core.LoadingPlugin;

import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new ConfigGui(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    static class ConfigGui extends GuiConfig {

        ConfigGui(GuiScreen parentScreen)
        {
            super(parentScreen,
                    new ConfigElement(ConfigHandler.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                    LoadingPlugin.MOD_ID, false, false,
                    GuiConfig.getAbridgedConfigPath(ConfigHandler.config.toString()));
        }
    }
}
