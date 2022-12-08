package dev.huskcasaca.effortless.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EffortlessConfig extends Config {

    @Expose
    @SerializedName("build")
    private final BuildConfig buildConfig;
    @Expose
    @SerializedName("preview")
    private final PreviewConfig previewConfig;

    public EffortlessConfig(
            BuildConfig buildConfig,
            PreviewConfig previewConfig
    ) {
        this.buildConfig = buildConfig;
        this.previewConfig = previewConfig;
    }

    ;

    public EffortlessConfig() {
        this(new BuildConfig(), new PreviewConfig());
    }

    @Override
    public boolean isValid() {
        return buildConfig.isValid() && previewConfig.isValid();
    }

    @Override
    public void validate() {
        buildConfig.validate();
        previewConfig.validate();
    }

    public BuildConfig getBuildConfig() {
        return buildConfig;
    }

    public PreviewConfig getPreviewConfig() {
        return previewConfig;
    }

}
