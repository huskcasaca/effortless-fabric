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
    public static final RenderType EF_LINES;
    public static final RenderType EF_PLANES;
    //Between 0 and 7, but dont override vanilla textures
    //Also update dissolve.fsh SamplerX
    private static final int MASK_TEXTURE_INDEX = 2;
    public static final ResourceLocation shaderMaskTextureLocation = new ResourceLocation("textures/shader_mask.png");
    public static ShaderInstance dissolveShaderInstance;
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

        String stateName = "ef_texturing_" + dissolve + "_" + blockPos + "_" + firstPos + "_" + secondPos + "_" + red;
        TexturingStateShard MY_TEXTURING = new TexturingStateShard(stateName, () -> {
            setShaderParameters(dissolveShaderInstance, dissolve, Vec3.atLowerCornerOf(blockPos), Vec3.atLowerCornerOf(firstPos), Vec3.atLowerCornerOf(secondPos), blockPos == secondPos, red);
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.8f);
        }, () -> {
        });

        RenderType.CompositeState renderState = RenderType.CompositeState.builder()
                .setShaderState(RENDER_TYPE_DISSOLVE_SHADER)
                .setTexturingState(MY_TEXTURING)
                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setCullState(RenderStateShard.CULL)
                .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                .createCompositeState(true);
        //Unique name for every combination, otherwise it will reuse the previous one
        String name = "ef_block_previews_" + dissolve + "_" + blockPos + "_" + firstPos + "_" + secondPos + "_" + red;
        return RenderType.create(name,
                DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, renderState);
    }

    private static void setShaderParameters(ShaderInstance shader, final float dissolve, final Vec3 blockpos,
                                            final Vec3 firstpos, final Vec3 secondpos,
                                            final boolean highlight, final boolean red) {
        Uniform percentileUniform = shader.getUniform("dissolve");
        Uniform highlightUniform = shader.getUniform("highlight");
        Uniform redUniform = shader.getUniform("red");
        Uniform blockposUniform = shader.getUniform("blockpos");
        Uniform firstposUniform = shader.getUniform("firstpos");
        Uniform secondposUniform = shader.getUniform("secondpos");

        RenderSystem.setShaderTexture(MASK_TEXTURE_INDEX, shaderMaskTextureLocation);

        if (percentileUniform != null) percentileUniform.set(dissolve);
        else Effortless.log("percentile uniform is null");
        if (highlightUniform != null) highlightUniform.set(highlight ? 1 : 0);
        else Effortless.log("highlight uniform is null");
        if (redUniform != null) redUniform.set(red ? 1 : 0);
        else Effortless.log("redUniform is null");

        if (blockposUniform != null) blockposUniform.set((float) blockpos.x, (float) blockpos.y, (float) blockpos.z);
        else Effortless.log("blockpos uniform is null");
        if (firstposUniform != null) firstposUniform.set((float) firstpos.x, (float) firstpos.y, (float) firstpos.z);
        else Effortless.log("firstpos uniform is null");
        if (secondposUniform != null)
            secondposUniform.set((float) secondpos.x, (float) secondpos.y, (float) secondpos.z);
        else Effortless.log("secondpos uniform is null");
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
