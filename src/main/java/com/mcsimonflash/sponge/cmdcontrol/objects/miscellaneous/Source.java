package com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous;

import java.util.Arrays;
import java.util.Optional;

public enum Source {

    INJECT,
    PLAYER,
    SENDER,
    SERVER;

    public static Optional<Source> getSource(String name) {
        return Arrays.stream(Source.values()).filter(s -> s.name().equalsIgnoreCase(name)).findAny();
    }

    public static Source getSourceOrThrow(String name) {
        return getSource(name).orElseThrow(() -> new IllegalArgumentException("Source \"" + name + "\" does not exist."));
    }

}