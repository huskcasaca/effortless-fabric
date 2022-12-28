package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.huskcasaca.effortless.Effortless;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.OptionalDouble;

@Environment(EnvType.CLIENT)
public class BuildRenderType extends RenderType {
    private static final RenderType EF_LINES;
    private static final RenderType EF_PLANES;
    //Between 0 and 7, but dont override vanilla textures
    //Also update dissolve.fsh SamplerX
    private static final int MASK_TEXTURE_INDEX = 2;
    private static final ResourceLocation SHADER_MASK_TEXTURE_LOCATION = new ResourceLocation(Effortless.MOD_ID, "textures/shader_mask.png");
    private static ShaderInstance dissolveShaderInstance;
    private static final ShaderStateShard RENDER_TYPE_DISSOLVE_SHADER = new ShaderStateShard(() -> dissolveShaderInstance);

    static {
        RenderType.CompositeState renderState;

        //LINES
        renderState = CompositeState.builder()
                .setLineState(new LineStateShard(OptionalDouble.empty()))
                .setShaderState(RENDERTYPE_LINES_SHADER)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setCullState(NO_CULL)
                .createCompositeState(false);


        EF_LINES = RenderType.create("ef_lines",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.DEBUG_LINES,
                256,
                renderState
        );

        //PLANES
        renderState = CompositeState.builder()
                .setLineState(new LineStateShard(OptionalDouble.empty()))
                .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
//                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(RenderStateShard.NO_CULL)
                .createCompositeState(false);

        EF_PLANES = RenderType.create("ef_planes",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                renderState
        );
    }


    // Dummy constructor needed to make java happy
    public BuildRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    public static RenderType lines() {
        return EF_LINES;
    }

    public static RenderType planes() {
        return EF_PLANES;
    }

    public static RenderType getBlockPreviewRenderType(float dissolve, BlockPos blockPos, BlockPos firstPos, BlockPos secondPos, boolean red) {

        var stateName = "ef_texturing_" + dissolve + "_" + blockPos + "_" + firstPos + "_" + secondPos + "_" + red;
        var texture = new TexturingStateShard(stateName, () -> {
            setShaderParameters(getDissolveShaderInstance(), dissolve, Vec3.atLowerCornerOf(blockPos), Vec3.atLowerCornerOf(firstPos), Vec3.atLowerCornerOf(secondPos), blockPos == secondPos, red);
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.8f);
        }, () -> { });
        var renderState = RenderType.CompositeState.builder()
                .setShaderState(RENDER_TYPE_DISSOLVE_SHADER)
                .setTexturingState(texture)
                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setCullState(RenderStateShard.CULL)
                .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                .createCompositeState(true);
        //Unique name for every combination, otherwise it will reuse the previous one
        var name = "ef_block_previews_" + dissolve + "_" + blockPos + "_" + firstPos + "_" + secondPos + "_" + red;
        return RenderType.create(name, DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, renderState);
    }

    private static void setShaderParameters(ShaderInstance shader, final float dissolve, final Vec3 blockPos,
                                            final Vec3 firstPos, final Vec3 secondPos,
                                            final boolean highlight, final boolean red) {
        var percentileUniform = shader.getUniform("dissolve");
        var highlightUniform = shader.getUniform("highlight");
        var redUniform = shader.getUniform("red");
        var blockPosUniform = shader.getUniform("blockpos");
        var firstPosUniform = shader.getUniform("firstpos");
        var secondPosUniform = shader.getUniform("secondpos");

        RenderSystem.setShaderTexture(MASK_TEXTURE_INDEX, SHADER_MASK_TEXTURE_LOCATION);

        if (percentileUniform != null) {
            percentileUniform.set(dissolve);
        } else {
            Effortless.log("percentile uniform is null");
        }
        if (highlightUniform != null) {
            highlightUniform.set(highlight ? 1 : 0);
        } else {
            Effortless.log("highlight uniform is null");
        }
        if (redUniform != null) {
            redUniform.set(red ? 1 : 0);
        } else {
            Effortless.log("redUniform is null");
        }

        if (blockPosUniform != null) {
            blockPosUniform.set((float) blockPos.x, (float) blockPos.y, (float) blockPos.z);
        } else {
            Effortless.log("blockpos uniform is null");
        }
        if (firstPosUniform != null) {
            firstPosUniform.set((float) firstPos.x, (float) firstPos.y, (float) firstPos.z);
        } else {
            Effortless.log("firstpos uniform is null");
        }
        if (secondPosUniform != null) {
            secondPosUniform.set((float) secondPos.x, (float) secondPos.y, (float) secondPos.z);
        } else {
            Effortless.log("secondpos uniform is null");
        }
    }

    public static ShaderInstance getDissolveShaderInstance() {
        return dissolveShaderInstance;
    }

    public static void setDissolveShaderInstance(ShaderInstance dissolveShaderInstance) {
        BuildRenderType.dissolveShaderInstance = dissolveShaderInstance;
    }

    private record ShaderInfo(
            float dissolve,
            Vec3 blockPos,
            Vec3 firstPos,
            Vec3 secondPos,
            boolean red
    ) {
    }
}
