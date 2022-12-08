package dev.huskcasaca.effortless.config;

import com.google.gson.annotations.Expose;

public class PreviewConfig extends Config {


    public static final int MIN_SHADER_DISSOLVE_TIME_MULTIPLIER = 1;
    public static final int MAX_SHADER_DISSOLVE_TIME_MULTIPLIER = 40;


    public static final int MIN_SHADER_THRESHOLD = 0;
    public static final int MAX_SHADER_THRESHOLD = 100_000;

    @Expose
    private boolean showBuildInfo = true;
    @Expose
    private boolean alwaysShowBlockPreview = false;
    @Expose
    private boolean useShaders = true;
    @Expose
    private int shaderThreshold = 10_000;
    @Expose
    private int shaderDissolveTimeMultiplier = 10;

    public static double shaderDissolveTimeMultiplier() {
        return ConfigManager.getGlobalPreviewConfig().getShaderDissolveTimeMultiplier() * 0.1;
    }

    public static int shaderThresholdRounded() {
        return ConfigManager.getGlobalPreviewConfig().getShaderThreshold();
    }

    public static boolean useShader() {
        return ConfigManager.getGlobalPreviewConfig().isUseShaders();
    }

    public boolean isShowBuildInfo() {
        return showBuildInfo;
    }

    public void setShowBuildInfo(boolean showBuildInfo) {
        this.showBuildInfo = showBuildInfo;
    }

    public boolean isAlwaysShowBlockPreview() {
        return alwaysShowBlockPreview;
    }

    public void setAlwaysShowBlockPreview(boolean alwaysShowBlockPreview) {
        this.alwaysShowBlockPreview = alwaysShowBlockPreview;
    }

    public boolean isUseShaders() {
        return useShaders;
    }

    public void setUseShaders(boolean useShaders) {
        this.useShaders = useShaders;
    }

    public int getShaderThreshold() {
        return shaderThreshold;
    }

    public void setShaderThreshold(int shaderThreshold) {
        this.shaderThreshold = shaderThreshold;
    }

    public int getShaderDissolveTimeMultiplier() {
        return shaderDissolveTimeMultiplier;
    }

    public void setShaderDissolveTimeMultiplier(int shaderDissolveTimeMultiplier) {
        this.shaderDissolveTimeMultiplier = shaderDissolveTimeMultiplier;
    }

    @Override
    public boolean isValid() {
        return shaderThreshold >= MIN_SHADER_THRESHOLD
                && shaderThreshold <= MAX_SHADER_THRESHOLD
                && shaderThreshold % 1000 == 0
                && shaderDissolveTimeMultiplier >= MIN_SHADER_DISSOLVE_TIME_MULTIPLIER
                && shaderDissolveTimeMultiplier <= MAX_SHADER_DISSOLVE_TIME_MULTIPLIER;
    }

    @Override
    public void validate() {

        // shaderThreshold is times of 1000
        shaderThreshold = Math.max(MIN_SHADER_THRESHOLD, Math.min(shaderThreshold, MAX_SHADER_THRESHOLD));
        shaderThreshold = Math.toIntExact(Math.round(shaderThreshold / 1000.0)) * 1000;
        shaderDissolveTimeMultiplier = Math.max(MIN_SHADER_DISSOLVE_TIME_MULTIPLIER, Math.min(shaderDissolveTimeMultiplier, MAX_SHADER_DISSOLVE_TIME_MULTIPLIER));
    }

}
