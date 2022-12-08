package dev.huskcasaca.effortless.config;

public abstract class Config {

    /**
     * @return true if anything was changed
     */
    public boolean isValid() {
        return true;
    }

    public abstract void validate();

}
