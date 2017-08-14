package randomdude999.random_stuff.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import randomdude999.random_stuff.asm.ClassTransformer;

import javax.annotation.Nullable;
import java.util.Map;

// I'd also put a MCVersion annotation here, but it doesn't allow multiple values
@IFMLLoadingPlugin.SortingIndex(1001)
public class LoadingPlugin implements IFMLLoadingPlugin {

    public static final String MOD_ID = "random_stuff";
    public static Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static boolean runtimeDeobfEnabled;

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ClassTransformer.class.getName()};
    }

    @Override
    public void injectData(Map<String, Object> data) {
        runtimeDeobfEnabled = (boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return ModContainer.class.getName();
    }
}
