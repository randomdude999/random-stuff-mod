package randomdude999.random_stuff.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import randomdude999.random_stuff.config.ConfigHandler;
import randomdude999.random_stuff.config.GuiFactory;

import java.util.Collections;

public class ModContainer extends DummyModContainer {

    public ModContainer() {
        super(new ModMetadata());
        ModMetadata meta = super.getMetadata();
        meta.modId = LoadingPlugin.MOD_ID;
        meta.name = "Random Stuff";
        meta.version = "@VERSION@";
        meta.authorList = Collections.singletonList("randomdude999");
        meta.description = "Some stuff YouTubers like AntVenom and Phoenix SC made, but this time with forge.\nTODO:\n* More falling blocks\n* Anti-gravity for falling blocks\n* AntVenom's modding experiments";
        meta.credits = "Phoenix SC, for the ideas. Vazkii, for the good ASM code.";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ConfigHandler.syncConfig();
    }

    @Override
    public VersionRange acceptableMinecraftVersionRange() {
        return VersionParser.parseRange("[1.11,)");
    }

    @Override
    public String getGuiClassName() {
        return GuiFactory.class.getName();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(LoadingPlugin.MOD_ID))
            ConfigHandler.syncConfig();
    }
}