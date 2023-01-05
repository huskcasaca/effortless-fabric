package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import dev.huskcasaca.effortless.buildmode.oneclick.Single;
import dev.huskcasaca.effortless.buildmode.oneclick.Disable;
import dev.huskcasaca.effortless.buildmode.threeclick.*;
import dev.huskcasaca.effortless.buildmode.twoclick.*;
import com.mojang.math.Vector4f;

public enum BuildMode {
    DISABLE("disable", new Disable(), Category.BASIC),
    SINGLE("single", new Single(), Category.BASIC /*, BuildOption.BUILD_SPEED*/),

    LINE("line", new Line(), Category.SQUARE /*, OptionEnum.THICKNESS*/),
    WALL("wall", new Wall(), Category.SQUARE, Option.PLANE_FILLING),
    FLOOR("floor", new Floor(), Category.SQUARE, Option.PLANE_FILLING),
    CUBE("cube", new Cube(), Category.SQUARE, Option.CUBE_FILLING),

    DIAGONAL_LINE("diagonal_line", new DiagonalLine(), Category.DIAGONAL /*, BuildOption.THICKNESS*/),
    DIAGONAL_WALL("diagonal_wall", new DiagonalWall(), Category.DIAGONAL /*, BuildOption.FILL*/),
    SLOPE_FLOOR("slope_floor", new SlopeFloor(), Category.DIAGONAL, Option.RAISED_EDGE),

    CIRCLE("circle", new Circle(), Category.CIRCULAR, Option.CIRCLE_START, Option.PLANE_FILLING, Option.ORIENTATION),
    CYLINDER("cylinder", new Cylinder(), Category.CIRCULAR, Option.CIRCLE_START, Option.PLANE_FILLING, Option.ORIENTATION),
    SPHERE("sphere", new Sphere(), Category.CIRCULAR, Option.CIRCLE_START, Option.PLANE_FILLING, Option.ORIENTATION);

//    PYRAMID("pyramid", new Pyramid(), Category.ROOF);
//    CONE("cone", new Cone(), BuildCategory.ROOF);
//    DOME("dome", new Dome(), BuildCategory.ROOF);

    private final BuildableProvider provider;
    private final Category category;
    private final Option[] options;
    private final String name;

    BuildMode(String name, Buildable instance, Category category, Option... options) {
        this.name = name;
        this.provider = actions -> instance;
        this.category = category;
        this.options = options;
    }

    BuildMode(String name, BuildableProvider provider, Category category, Option... options) {
        this.name = name;
        this.provider = provider;
        this.category = category;
        this.options = options;
    }

    public String getNameKey() {
        return Effortless.MOD_ID + ".mode." + getName();
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

//            case PYRAMID -> "pyramid";
        };
    }

    public String getDescriptionKey() {
        return Effortless.MOD_ID + ".description." + getName();
    }

    public Buildable getInstance() {
        return provider.getBuildable(BuildActionHandler.getOptions());
    }

    public Category getCategory() {
        return category;
    }

    public Option[] getOptions() {
        return options;
    }

    public String getName() {
        return name;
    }

    public enum Option {
        BUILD_SPEED("build_speed",
                BuildAction.SPEED_NORMAL,
                BuildAction.SPEED_FAST),
        LINE_THICKNESS("line_thickness",
                BuildAction.THICKNESS_1,
                BuildAction.THICKNESS_3,
                BuildAction.THICKNESS_5),
        PLANE_FILLING("plane_filling",
                BuildAction.PLANE_FULL,
                BuildAction.PLANE_HOLLOW),
        CUBE_FILLING("cube_filling",
                BuildAction.CUBE_FULL,
                BuildAction.CUBE_HOLLOW,
                BuildAction.CUBE_SKELETON),
        RAISED_EDGE("raised_edge",
                BuildAction.RAISE_SHORT_EDGE,
                BuildAction.RAISE_LONG_EDGE),
        CIRCLE_START("circle_start",
                BuildAction.CIRCLE_START_CORNER,
                BuildAction.CIRCLE_START_CENTER),
        ORIENTATION("orientation",
                BuildAction.FACE_HORIZONTAL,
                BuildAction.FACE_VERTICAL);

        private final String name;
        private final BuildAction[] actions;

        Option(String name, BuildAction... actions) {
            this.name = name;
            this.actions = actions;
        }

        public String getNameKey() {
            return String.join(".", Effortless.MOD_ID, "option", name);
        }

        public BuildAction[] getActions() {
            return actions;
        }
    }

    public enum Category {
        BASIC(new Vector4f(0f, .5f, 1f, .5f)),
        SQUARE(new Vector4f(1f, .54f, .24f, .5f)),
        DIAGONAL(new Vector4f(0.56f, 0.28f, 0.87f, .5f)),
        CIRCULAR(new Vector4f(0.29f, 0.76f, 0.3f, .5f)),
        ROOF(new Vector4f(0.83f, 0.87f, 0.23f, .5f));

        // TODO: 21/12/22 use Color
        private final Vector4f color;

        Category(Vector4f color) {
            this.color = color;
        }

        public Vector4f getColor() {
            return color;
        }
    }

    public interface BuildableProvider {
        Buildable getBuildable(BuildAction[] actions);
    }

}
