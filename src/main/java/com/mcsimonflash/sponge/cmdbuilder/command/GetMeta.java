package com.mcsimonflash.sponge.cmdbuilder.command;

import com.google.inject.Inject;
import com.mcsimonflash.sponge.cmdbuilder.internal.Config;
import com.mcsimonflash.sponge.cmdbuilder.internal.Util;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.Arguments;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.command.Aliases;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.command.Command;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.command.Permission;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.ConfigurationException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

@Aliases("getmeta")
@Permission("cmdbuilder.command.getmeta.base")
public class GetMeta extends Command {

    @Inject
    private GetMeta(Settings settings) {
        super(settings.elements(Arguments.user().optional().toElement("user"), Arguments.string().toElement("name")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Optional<User> optUser = args.getOne("user");
        String name = args.<String>getOne("name").get();
        try {
            src.sendMessage(Util.toText("&c" + optUser.map(User::getName).orElse("Server") + "&6's meta at &c" + name + "&6: &c" + Config.getMeta(optUser.map(Identifiable::getUniqueId).orElse(Util.ZERO_UUID), name).getString()));
        } catch (ConfigurationException e) {
            src.sendMessage(Util.toText("&6" + optUser.map(User::getName).orElse("Server") + " has no meta set at &c" + name + "&6."));
        }
        return CommandResult.success();
    }

}