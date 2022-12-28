package dev.huskcasaca.effortless.render;

import dev.huskcasaca.effortless.Effortless;

public enum BlockRenderOptions {
    OUTLINES("outlines"),
//    BLOCK_TEX("block_tex"),
    DISSOLVE_SHADER("dissolve_shader");

    private final String name;

    BlockRenderOptions(String name) {
        this.name = name;
    }

    public String getNameKey() {
        return Effortless.MOD_ID + ".render.options." + name;
    }

}
