package com.mcsimonflash.sponge.cmdcontrol.objects.scripts;

import com.mcsimonflash.sponge.cmdcontrol.objects.enums.ArgType;
import org.spongepowered.api.command.args.CommandElement;

public class ScriptArgument {

    String name;
    ArgType type;
    String typeMeta;

    public ScriptArgument(String name, ArgType type, String modifier) {
        this.name = name;
        this.type = type;
        this.typeMeta = modifier;
    }

    public CommandElement getCmdElem() {
        return null;
    }
}
