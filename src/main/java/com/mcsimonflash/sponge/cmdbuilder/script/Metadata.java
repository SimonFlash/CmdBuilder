package com.mcsimonflash.sponge.cmdbuilder.script;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Metadata {

    public static final Metadata DEFAULT = builder().build();

    private boolean override;
    private long cooldown;
    private double cost;
    private ImmutableList<String> aliases;

    public Metadata(boolean override, long cooldown, double cost, ImmutableList<String> aliases) {
        this.override = override;
        this.cooldown = cooldown;
        this.cost = cost;
        this.aliases = aliases;
    }

    public boolean getOverride() {
        return override;
    }
    public long getCooldown() {
        return cooldown;
    }
    public double getCost() {
        return cost;
    }
    public ImmutableList<String> getAliases() {
        return aliases;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean override = false;
        private long cooldown = 0;
        private double cost = 0;
        private ImmutableList<String> aliases = ImmutableList.of();

        public Builder override(boolean override) {
            this.override = override;
            return this;
        }

        public Builder cooldown(long cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public Builder aliases(List<String> aliases) {
            this.aliases = ImmutableList.copyOf(aliases);
            return this;
        }

        public Metadata build() {
            return new Metadata(override, cooldown, cost, aliases);
        }

    }

}