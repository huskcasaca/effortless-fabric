package dev.huskcasaca.effortless.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConfigManager {

    public static final ConfigManager INSTANCE = new ConfigManager();
    // TODO: 19/9/22 remove
    public static final Integer quickReplaceMiningLevel = -1;
    private final Gson gson;
    private final File configFile;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private EffortlessConfig config;

    public ConfigManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        this.configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "effortless.config");
        readConfig(false);
    }

    public static EffortlessConfig getGlobalConfig() {
        return INSTANCE.getConfig();
    }

    public static BuildConfig getGlobalBuildConfig() {
        return getGlobalConfig().getBuildConfig();
    }

    public static PreviewConfig getGlobalPreviewConfig() {
        return getGlobalConfig().getPreviewConfig();
    }

    public static void writeGlobalConfig() {
        INSTANCE.writeConfig(false);
    }

    public static int getMaxReachDistance() {
        return getGlobalConfig().getBuildConfig().getMaxReachDistance();
    }

    public void readConfig(boolean async) {

        Runnable task = () -> {
            try {
                //read if exists
                if (configFile.exists()) {
                    String fileContents = FileUtils.readFileToString(configFile, Charset.defaultCharset());
                    config = gson.fromJson(fileContents, EffortlessConfig.class);

                    //If there were any invalid options, write the fixed config
                    if (config.isValid()) writeConfig(true);

                } else { //write new if no config file exists
                    writeNewConfig();
                }

            } catch (Exception e) {
                e.printStackTrace();
                writeNewConfig();
            }
        };

        if (async) executor.execute(task);
        else task.run();
    }

    public void writeNewConfig() {
        config = new EffortlessConfig();
        writeConfig(false);
    }

    public void writeConfig(boolean async) {
        Runnable task = () -> {
            try {
                if (config != null) {
                    String serialized = gson.toJson(config);
                    FileUtils.writeStringToFile(configFile, serialized, Charset.defaultCharset());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        if (async) executor.execute(task);
        else task.run();
    }

    public EffortlessConfig getConfig() {
        return config;
    }

}
