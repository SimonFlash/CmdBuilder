package com.mcsimonflash.sponge.cmdcontrol.objects.enums;

import java.util.Arrays;

public enum CmdSource {
    INJECT,
    PLAYER,
    SENDER,
    SERVER;

    public static CmdSource getType(String type) {
        return Arrays.stream(CmdSource.values()).filter(v -> v.name().equalsIgnoreCase(type)).findAny().orElse(null);
    }
}