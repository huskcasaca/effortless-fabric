package dev.huskcasaca.effortless.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.function.Consumer;

public class ClientReloadShadersEvent {

    public static final Event<ShaderRegister> REGISTER_SHADER = EventFactory.createArrayBacked(ShaderRegister.class, callbacks -> (manager, sink) -> {
        for (ShaderRegister callback : callbacks) {
            callback.onRegisterShader(manager, sink);
        }
    });

    @FunctionalInterface
    public interface ShaderRegister {
        void onRegisterShader(ResourceManager manager, ShadersSink sink) throws IOException;

        @FunctionalInterface
        interface ShadersSink {
            void registerShader(ShaderInstance shader, Consumer<ShaderInstance> consumer);
        }
    }


}
