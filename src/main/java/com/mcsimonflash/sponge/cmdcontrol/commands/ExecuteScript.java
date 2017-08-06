package com.mcsimonflash.sponge.cmdcontrol.commands;

import com.mcsimonflash.sponge.cmdcontrol.managers.Storage;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Script;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class ExecuteScript implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String scriptName = args.<String>getOne("script-name").get();
        String arguments = args.<String>getOne("arguments").orElse("");

        Script script = Storage.scriptDirectory.get(scriptName.toLowerCase());
        if (script != null) {
            script.process(src, arguments);
            return CommandResult.success();
        } else {
            src.sendMessage(Util.msgPrefix.concat(Util.toText("Unable to locate script. | Script:[" + scriptName + "]")));
            return CommandResult.empty();
        }
    }
}
