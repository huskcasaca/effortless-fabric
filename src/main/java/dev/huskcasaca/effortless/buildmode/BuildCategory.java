package dev.huskcasaca.effortless.buildmode;

import com.mojang.math.Vector4f;

public enum BuildCategory {
    BASIC(new Vector4f(0f, .5f, 1f, .8f)),
    DIAGONAL(new Vector4f(0.56f, 0.28f, 0.87f, .8f)),
    CIRCULAR(new Vector4f(0.29f, 0.76f, 0.3f, 1f)),
    ROOF(new Vector4f(0.83f, 0.87f, 0.23f, .8f));

    public final Vector4f color;

    BuildCategory(Vector4f color) {
        this.color = color;
    }
}
