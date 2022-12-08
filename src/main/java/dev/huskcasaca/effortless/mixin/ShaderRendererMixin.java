package dev.huskcasaca.effortless.mixin;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.datafixers.util.Pair;
import dev.huskcasaca.effortless.event.ClientReloadShadersEvent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class ShaderRendererMixin {

    @Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void reloadShaders(ResourceManager resourceManager, CallbackInfo ci, List<Program> programs, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaders) throws IOException {
        ClientReloadShadersEvent.REGISTER_SHADER.invoker().onRegisterShader(
                resourceManager,
                (shader, callback) -> shaders.add(Pair.of(shader, callback))
        );
    }
}