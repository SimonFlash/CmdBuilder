package com.mcsimonflash.sponge.cmdcontrol.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Script;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;

import java.util.Map;
import java.util.stream.Collectors;

public class Scripts {

    public static Map<String, Script> directory = Maps.newHashMap();

    public static void register() {
        CommandSpec ExecuteScript = CommandSpec.builder()
                .children(directory.values().stream().collect(Collectors.toMap(s -> Lists.newArrayList(s.getName()), Script::getSpec)))
                .permission("cmdcontrol.executescript.base")
                .build();
        Sponge.getCommandManager().register(CmdControl.getPlugin(), ExecuteScript, "ExecuteScript", "Script");
    }

}