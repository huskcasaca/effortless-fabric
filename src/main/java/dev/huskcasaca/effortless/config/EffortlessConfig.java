package dev.huskcasaca.effortless.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EffortlessConfig extends Config {

    @Expose
    @SerializedName("preview")
    private final PreviewConfig previewConfig;

    public EffortlessConfig(
            PreviewConfig previewConfig
    ) {
        this.previewConfig = previewConfig;
    }

    public EffortlessConfig() {
        this(new PreviewConfig());
    }

    @Override
    public boolean isValid() {
        return previewConfig.isValid();
    }

    @Override
    public void validate() {
        previewConfig.validate();
    }

    public PreviewConfig getPreviewConfig() {
        return previewConfig;
    }

}
