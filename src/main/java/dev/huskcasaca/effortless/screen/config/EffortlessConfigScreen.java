package dev.huskcasaca.effortless.screen.config;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.config.ConfigManager;
import dev.huskcasaca.effortless.config.EffortlessConfig;
import dev.huskcasaca.effortless.config.PreviewConfig;
import dev.huskcasaca.effortless.render.BlockRenderOptions;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntSliderBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EffortlessConfigScreen {

    private static final Function<Boolean, Component> yesNoTextSupplier = bool -> {
        if (bool) return new TranslatableComponent(Effortless.MOD_ID + ".settings.toggle.on").withStyle(ChatFormatting.GREEN);
        else return new TranslatableComponent(Effortless.MOD_ID + ".settings.toggle.off").withStyle(ChatFormatting.RED);
    };

    static String getSettingsNamespace() {
        return String.join(".", Effortless.MOD_ID, "settings");
    }

    static String getSettingsNamespaceTooltip(String path) {
        return String.join(".", Effortless.MOD_ID, "settings", path, "tooltip");
    }

    static String getSettingsNamespaceTooltip(String path, int ordinal) {
        return String.join(".", Effortless.MOD_ID, "settings", path, "tooltip" + "_" + ordinal);
    }

    public static Screen createConfigScreen(Screen parentScreen) {

        final var defaults = new EffortlessConfig();

        final var manager = new ConfigManager();
        final var config = manager.getConfig();

        final var builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(new TranslatableComponent(Effortless.MOD_ID + ".settings.title"))
                .transparentBackground()
                .setDoesConfirmSave(true)
                .setSavingRunnable(() -> {
                    config.validate();
                    manager.writeConfig(false);
                    ConfigManager.INSTANCE.readConfig(false);
                });


        final var configCategory = builder.getOrCreateCategory(new TranslatableComponent(String.join(".", Effortless.MOD_ID, "settings", "category", "config", "title")));

        final var entryBuilder = builder.entryBuilder();

        final var previewSubCat = entryBuilder.startSubCategory(new TranslatableComponent(String.join(".", Effortless.MOD_ID, "settings", "category", "config", "preview", "title")));

        final var showBuildInfo = new BooleanEntryData("show_build_info", defaults.getPreviewConfig().isShowBuildInfo(), config.getPreviewConfig().isShowBuildInfo(), config.getPreviewConfig()::setShowBuildInfo);
        final var alwaysShowBlockPreview = new BooleanEntryData("always_show_block_preview", defaults.getPreviewConfig().isAlwaysShowBlockPreview(), config.getPreviewConfig().isAlwaysShowBlockPreview(), config.getPreviewConfig()::setAlwaysShowBlockPreview);
//        final var useShaders = new BooleanEntryData("use_shaders", defaults.getPreviewConfig().isUseShaders(), config.getPreviewConfig().isUseShaders(), config.getPreviewConfig()::setUseShaders);
//        final var shaderThreshold = new SliderEntryData("shader_threshold", defaults.getPreviewConfig().getShaderThreshold(), config.getPreviewConfig().getShaderThreshold(), PreviewConfig.MIN_SHADER_THRESHOLD, PreviewConfig.MAX_SHADER_THRESHOLD, config.getPreviewConfig()::setShaderThreshold);
        final var dissolveTimeMultiplier = new SliderEntryData("shader_dissolve_time_multiplier", defaults.getPreviewConfig().getShaderDissolveTimeMultiplier(), config.getPreviewConfig().getShaderDissolveTimeMultiplier(), PreviewConfig.MIN_SHADER_DISSOLVE_TIME_MULTIPLIER, PreviewConfig.MAX_SHADER_DISSOLVE_TIME_MULTIPLIER, config.getPreviewConfig()::setShaderDissolveTimeMultiplier);


        previewSubCat.add(
                entryBuilder.startBooleanToggle(new TranslatableComponent(showBuildInfo.getTitleKey()), showBuildInfo.currentValue)
                        .setTooltip(new TranslatableComponent(showBuildInfo.getTooltipKey()))
                        .setDefaultValue(showBuildInfo.defaultValue)
                        .setSaveConsumer(showBuildInfo.saveConsumer)
                        .setYesNoTextSupplier(yesNoTextSupplier)
                        .build()
        );
        previewSubCat.add(
                entryBuilder.startBooleanToggle(new TranslatableComponent(alwaysShowBlockPreview.getTitleKey()), alwaysShowBlockPreview.currentValue)
                        .setTooltip(new TranslatableComponent(alwaysShowBlockPreview.getTooltipKey()))
                        .setDefaultValue(alwaysShowBlockPreview.defaultValue)
                        .setSaveConsumer(alwaysShowBlockPreview.saveConsumer)
                        .setYesNoTextSupplier(yesNoTextSupplier)
                        .build()
        );
        previewSubCat.add(
                entryBuilder.startEnumSelector(new TextComponent("Block Preview Mode"), BlockRenderOptions.class, config.getPreviewConfig().getBlockPreviewMode())
                        .setTooltip(new TranslatableComponent("effortless.settings.block_preview_type.tooltip"))
                        .setDefaultValue(defaults.getPreviewConfig().getBlockPreviewMode())
                        .setSaveConsumer(config.getPreviewConfig()::setBlockPreviewMode)
                        .setEnumNameProvider(anEnum -> new TranslatableComponent(((BlockRenderOptions) anEnum).getNameKey()))
                        .build()
        );
//        previewSubCat.add(
//                entryBuilder.startBooleanToggle(Component.translatable(useShaders.getTitleKey()), useShaders.currentValue)
//                        .setTooltip(Component.translatable(useShaders.getTooltipKey()))
//                        .setDefaultValue(useShaders.defaultValue)
//                        .setSaveConsumer(useShaders.saveConsumer)
//                        .setYesNoTextSupplier(yesNoTextSupplier)
//                        .build()
//        );
//        previewSubCat.add(
//                entryBuilder.startIntSlider(Component.translatable(shaderThreshold.getTitleKey()), shaderThreshold.currentValue, shaderThreshold.minValue, shaderThreshold.maxValue)
//                        .setTooltip(
//                                Component.translatable(getSettingsNamespaceTooltip(shaderThreshold.name()))
//                        )
//                        .setDefaultValue(shaderThreshold.defaultValue)
//                        .setSaveConsumer((integer) -> {
//                            int rounded = Math.toIntExact(Math.round(integer / 1000.0));
//                            config.getPreviewConfig().setShaderThreshold(rounded * 1000);
//                        })
//                        .setTextGetter((integer) -> {
//                            // round double
//                            int rounded = Math.toIntExact(Math.round(integer / 1000.0));
//                            return Component.literal(integer <= rounded ? "Disabled" : rounded * 1000 + " blocks");
//                        })
//                        .build()
//        );
        previewSubCat.add(
                entryBuilder.startIntSlider(new TranslatableComponent(dissolveTimeMultiplier.getTitleKey()), dissolveTimeMultiplier.currentValue, dissolveTimeMultiplier.minValue, dissolveTimeMultiplier.maxValue)
                        .setTooltip(
                                new TranslatableComponent(getSettingsNamespaceTooltip(dissolveTimeMultiplier.name(), 0)),
                                new TranslatableComponent(getSettingsNamespaceTooltip(dissolveTimeMultiplier.name(), 1)),
                                new TranslatableComponent(getSettingsNamespaceTooltip(dissolveTimeMultiplier.name(), 2)),
                                new TranslatableComponent(getSettingsNamespaceTooltip(dissolveTimeMultiplier.name(), 3)),
                                new TranslatableComponent(getSettingsNamespaceTooltip(dissolveTimeMultiplier.name(), 4))
                        )
                        .setDefaultValue(dissolveTimeMultiplier.defaultValue)
                        .setSaveConsumer(dissolveTimeMultiplier.saveConsumer)
                        .setTextGetter((integer) -> new TextComponent(integer / 10.0 + "x"))
                        .build()
        );

        var previewSubEntry = previewSubCat.build();
        previewSubEntry.setExpanded(true);
        configCategory.addEntry(previewSubEntry);

        return builder.build();

    }

    interface Entry {

        AbstractConfigListEntry<?> build(ConfigEntryBuilder builder);

        String getName();

        default String getTitleKey() {
            return getSettingsNamespace() + "." + getName() + "." + "title";
        }

        default String getTooltipKey() {
            return getSettingsNamespace() + "." + getName() + "." + "tooltip";
        }

    }

    record SliderEntryData(
            String name,
            int defaultValue,
            int currentValue,
            int minValue,
            int maxValue,
            Consumer<Integer> saveConsumer
    ) implements Entry {

        @Override
        public AbstractConfigListEntry<?> build(ConfigEntryBuilder builder) {
            return builder.startIntSlider(new TranslatableComponent(getTitleKey()), currentValue, minValue, maxValue)
                    .setTooltip(new TranslatableComponent(getTooltipKey()))
                    .setDefaultValue(defaultValue)
                    .setSaveConsumer(saveConsumer)
                    .build();
        }


        public IntSliderBuilder preBuild(ConfigEntryBuilder builder) {
            return builder.startIntSlider(new TranslatableComponent(getTitleKey()), currentValue, minValue, maxValue)
                    .setTooltip(new TranslatableComponent(getTooltipKey()))
                    .setDefaultValue(defaultValue)
                    .setSaveConsumer(saveConsumer);
        }

        @Override
        public String getName() {
            return name();
        }
    }

    record IntegerEntryData(
            String name,
            int defaultValue,
            int currentValue,
            int minValue,
            int maxValue,
            Consumer<Integer> saveConsumer
    ) implements Entry {

        @Override
        public AbstractConfigListEntry<?> build(ConfigEntryBuilder builder) {
            return builder.startIntField(new TranslatableComponent(getTitleKey()), currentValue)
                    .setTooltip(new TranslatableComponent(getTooltipKey()))
                    .setDefaultValue(defaultValue)
                    .setMin(minValue)
                    .setMax(maxValue)
                    .setSaveConsumer(saveConsumer)
                    .build();
        }

        @Override
        public String getName() {
            return name();
        }
    }

    record BooleanEntryData(
            String name,
            boolean defaultValue,
            boolean currentValue,
            Consumer<Boolean> saveConsumer
    ) implements Entry {

        @Override
        public AbstractConfigListEntry<?> build(ConfigEntryBuilder builder) {
            return builder.startBooleanToggle(new TranslatableComponent(getTitleKey()), currentValue)
                    .setTooltip(new TranslatableComponent(getTooltipKey()))
                    .setDefaultValue(defaultValue)
                    .setSaveConsumer(saveConsumer)
                    .setYesNoTextSupplier(yesNoTextSupplier)
                    .build();
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
