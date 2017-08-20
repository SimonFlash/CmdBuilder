package com.mcsimonflash.sponge.cmdcontrol.objects.scripts;

import com.google.common.base.Preconditions;

import java.util.List;

public class Metadata {

    private boolean override;
    private boolean player;
    private int cooldown;
    private double cost;
    private List<String> aliases;
    private List<String> permissions;

    public Metadata(boolean override, boolean player, int cooldown, double cost, List<String> aliases, List<String> permissions) {
        Preconditions.checkNotNull(override, "Metadata override cannot be null.");
        this.override = override;
        Preconditions.checkNotNull(player, "Metadata player cannot be null.");
        this.player = player;
        Preconditions.checkNotNull(cooldown, "Metadata cooldown cannot be null.");
        Preconditions.checkArgument(cooldown >= 0, "Metadata cooldown cannot be negative.");
        this.cooldown = cooldown;
        Preconditions.checkNotNull(cost, "Metadata cost cannot be null.");
        Preconditions.checkArgument(cost >= 0, "Metadata cost cannot be negative.");
        this.cost = cost;
        Preconditions.checkNotNull(aliases, "Metadata aliases cannot be null.");
        this.aliases = aliases;
        Preconditions.checkNotNull(permissions, "Metadata permissions cannot be null.");
        this.permissions = permissions;
    }

    public boolean getOverride() {
        return override;
    }
    public boolean getPlayer() {
        return player;
    }
    public int getCooldown() {
        return cooldown;
    }
    public double getCost() {
        return cost;
    }
    public List<String> getAliases() {
        return aliases;
    }
    public List<String> getPermissions() {
        return permissions;
    }

}