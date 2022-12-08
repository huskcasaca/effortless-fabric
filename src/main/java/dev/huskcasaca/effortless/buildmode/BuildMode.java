package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmode.oneclick.Single;
import dev.huskcasaca.effortless.buildmode.oneclick.Disable;
import dev.huskcasaca.effortless.buildmode.oneclick.Vanilla;
import dev.huskcasaca.effortless.buildmode.threeclick.*;
import dev.huskcasaca.effortless.buildmode.twoclick.Circle;
import dev.huskcasaca.effortless.buildmode.twoclick.Floor;
import dev.huskcasaca.effortless.buildmode.twoclick.Line;
import dev.huskcasaca.effortless.buildmode.twoclick.Wall;

public enum BuildMode {
    DISABLE("disable", new Disable(), BuildCategory.BASIC),
    //    VANILLA("vanilla", new Vanilla(), BuildCategory.BASIC),
    SINGLE("single", new Single(), BuildCategory.BASIC /*, BuildOption.BUILD_SPEED*/),

    LINE("line", new Line(), BuildCategory.BASIC /*, OptionEnum.THICKNESS*/),
    WALL("wall", new Wall(), BuildCategory.BASIC, BuildOption.FILL),
    FLOOR("floor", new Floor(), BuildCategory.BASIC, BuildOption.FILL),
    CUBE("cube", new Cube(), BuildCategory.BASIC, BuildOption.CUBE_FILL),
    DIAGONAL_LINE("diagonal_line", new DiagonalLine(), BuildCategory.DIAGONAL /*, BuildOption.THICKNESS*/),
    DIAGONAL_WALL("diagonal_wall", new DiagonalWall(), BuildCategory.DIAGONAL /*, BuildOption.FILL*/),
    SLOPE_FLOOR("slope_floor", new SlopeFloor(), BuildCategory.DIAGONAL, BuildOption.RAISED_EDGE),
    CIRCLE("circle", new Circle(), BuildCategory.CIRCULAR, BuildOption.CIRCLE_START, BuildOption.FILL),
    CYLINDER("cylinder", new Cylinder(), BuildCategory.CIRCULAR, BuildOption.CIRCLE_START, BuildOption.FILL),
    SPHERE("sphere", new Sphere(), BuildCategory.CIRCULAR, BuildOption.CIRCLE_START, BuildOption.FILL);
//    PYRAMID("pyramid", new Pyramid(), BuildCategory.ROOF),
//    CONE("cone", new Cone(), BuildCategory.ROOF),
//    DOME("dome", new Dome(), BuildCategory.ROOF);

    public final Buildable instance;
    public final BuildCategory category;
    public final BuildOption[] options;
    private final String name;

    BuildMode(String name, Buildable instance, BuildCategory category, BuildOption... options) {
        this.name = name;
        this.instance = instance;
        this.category = category;
        this.options = options;
    }

    public String getNameKey() {
        return Effortless.MOD_ID + ".mode." + name;
    }

    public String getCommandName() {
        return switch (this) {
            case DISABLE -> "disabled";
            case SINGLE -> "single";
            case LINE -> "line";
            case WALL -> "wall";
            case FLOOR -> "floor";
            case CUBE -> "cube";
            case DIAGONAL_LINE -> "diagonalLine";
            case DIAGONAL_WALL -> "diagonalWall";
            case SLOPE_FLOOR -> "slopeFloor";
            case CIRCLE -> "circle";
            case CYLINDER -> "cylinder";
            case SPHERE -> "sphere";
        };
    }

    public String getDescriptionKey() {
        return Effortless.MOD_ID + ".description." + name;
    }
}
