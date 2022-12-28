package dev.huskcasaca.effortless.config;

import com.google.gson.annotations.Expose;
import dev.huskcasaca.effortless.render.BlockRenderOptions;

public class PreviewConfig extends Config {

    public static final int MIN_SHADER_DISSOLVE_TIME_MULTIPLIER = 1;
    public static final int MAX_SHADER_DISSOLVE_TIME_MULTIPLIER = 40;

    @Expose
    private boolean showBuildInfo = true;
    @Expose
    private boolean alwaysShowBlockPreview = false;
    @Expose
    private int shaderDissolveTimeMultiplier = 10;
    @Expose
    private int blockPreviewMode = BlockRenderOptions.DISSOLVE_SHADER.ordinal();

    public static double shaderDissolveTimeMultiplier() {
        return ConfigManager.getGlobalPreviewConfig().getShaderDissolveTimeMultiplier() * 0.1;
    }

    public static boolean useShader() {
        return ConfigManager.getGlobalPreviewConfig().getBlockPreviewMode() == BlockRenderOptions.DISSOLVE_SHADER;
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

    public int getShaderDissolveTimeMultiplier() {
        return shaderDissolveTimeMultiplier;
    }

    public void setShaderDissolveTimeMultiplier(int shaderDissolveTimeMultiplier) {
        this.shaderDissolveTimeMultiplier = shaderDissolveTimeMultiplier;
    }

    public BlockRenderOptions getBlockPreviewMode() {
        return BlockRenderOptions.values()[blockPreviewMode];
    }

    public void setBlockPreviewMode(BlockRenderOptions blockPreviewMode) {
        this.blockPreviewMode = blockPreviewMode.ordinal();
    }

    @Override
    public boolean isValid() {
         return shaderDissolveTimeMultiplier >= MIN_SHADER_DISSOLVE_TIME_MULTIPLIER &&
                shaderDissolveTimeMultiplier <= MAX_SHADER_DISSOLVE_TIME_MULTIPLIER;
    }

    @Override
    public void validate() {
        shaderDissolveTimeMultiplier = Math.max(MIN_SHADER_DISSOLVE_TIME_MULTIPLIER, Math.min(shaderDissolveTimeMultiplier, MAX_SHADER_DISSOLVE_TIME_MULTIPLIER));
    }
}
