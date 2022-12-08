package dev.huskcasaca.effortless.config;

import com.google.gson.annotations.Expose;

public class BuildConfig extends Config {

    public static int MIN_MAX_REACH_DISTANCE = 0;
    public static int MAX_MAX_REACH_DISTANCE = 64;

    public static int MIN_MAX_BLOCK_PLACE_PER_AXIS = 0;
    public static int MAX_MAX_BLOCK_PLACE_PER_AXIS = 512;

    public static int MIN_MAX_BLOCK_PLACE_AT_ONCE = 0;
    public static int MAX_MAX_BLOCK_PLACE_AT_ONCE = 100_000;

    public static int MIN_UNDO_STACK_SIZE = 0;
    public static int MAX_UNDO_STACK_SIZE = 100;


    @Expose
    private int maxReachDistance = 16;
    @Expose
    private int maxBlockPlacePerAxis = 128;
    @Expose
    private int maxBlockPlaceAtOnce = 10_000;
    @Expose
    private boolean canBreakFar = true;
    @Expose
    private boolean enableUndo = false;
    @Expose
    private int undoStackSize = 20;


    public int getMaxReachDistance() {
        return maxReachDistance;
    }

    public void setMaxReachDistance(int maxReachDistance) {
        this.maxReachDistance = maxReachDistance;
    }

    /**
     * @return true if anything was changed
     */

    public int getMaxBlockPlacePerAxis() {
        return maxBlockPlacePerAxis;
    }

    public void setMaxBlockPlacePerAxis(int maxBlockPlacePerAxis) {
        this.maxBlockPlacePerAxis = maxBlockPlacePerAxis;
    }

    public int getMaxBlockPlaceAtOnce() {
        return maxBlockPlaceAtOnce;
    }

    public void setMaxBlockPlaceAtOnce(int maxBlockPlaceAtOnce) {
        this.maxBlockPlaceAtOnce = maxBlockPlaceAtOnce;
    }

    public boolean isCanBreakFar() {
        return canBreakFar;
    }

    public void setCanBreakFar(boolean canBreakFar) {
        this.canBreakFar = canBreakFar;
    }


    public boolean isEnableUndo() {
        return enableUndo;
    }

    public void setEnableUndo(boolean enableUndo) {
        this.enableUndo = enableUndo;
    }


    public int getUndoStackSize() {
        return undoStackSize;
    }

    public void setUndoStackSize(int undoStackSize) {
        this.undoStackSize = undoStackSize;
    }

    @Override
    public boolean isValid() {
        return maxReachDistance >= MIN_MAX_REACH_DISTANCE && maxReachDistance <= MAX_MAX_REACH_DISTANCE
                && maxBlockPlacePerAxis >= MIN_MAX_BLOCK_PLACE_PER_AXIS && maxBlockPlacePerAxis <= MAX_MAX_BLOCK_PLACE_PER_AXIS
                && maxBlockPlaceAtOnce >= MIN_MAX_BLOCK_PLACE_AT_ONCE && maxBlockPlaceAtOnce <= MAX_MAX_BLOCK_PLACE_AT_ONCE
                && maxBlockPlaceAtOnce % 1000 == 0
                && undoStackSize >= MIN_UNDO_STACK_SIZE && undoStackSize <= MAX_UNDO_STACK_SIZE;
    }

    @Override
    public void validate() {
        maxReachDistance = Math.max(MIN_MAX_REACH_DISTANCE, Math.min(maxReachDistance, MAX_MAX_REACH_DISTANCE));
        maxBlockPlacePerAxis = Math.max(MIN_MAX_BLOCK_PLACE_PER_AXIS, Math.min(maxBlockPlacePerAxis, MAX_MAX_BLOCK_PLACE_PER_AXIS));
        maxBlockPlaceAtOnce = Math.max(MIN_MAX_BLOCK_PLACE_AT_ONCE, Math.min(maxBlockPlaceAtOnce, MAX_MAX_BLOCK_PLACE_AT_ONCE));
        maxBlockPlaceAtOnce = Math.toIntExact(Math.round(maxBlockPlaceAtOnce / 1000.0)) * 1000;
        undoStackSize = Math.max(MIN_UNDO_STACK_SIZE, Math.min(undoStackSize, MAX_UNDO_STACK_SIZE));
    }

}
