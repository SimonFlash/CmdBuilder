package com.mcsimonflash.sponge.cmdbuilder.command;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.mcsimonflash.sponge.cmdbuilder.CmdBuilder;
import com.mcsimonflash.sponge.cmdbuilder.command.parser.MetaParser;
import com.mcsimonflash.sponge.cmdbuilder.internal.Config;
import com.mcsimonflash.sponge.cmdbuilder.internal.Util;
import com.mcsimonflash.sponge.cmdbuilder.type.ParserType;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.Arguments;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.command.Aliases;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.command.Command;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.command.Permission;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.ConfigurationException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.util.Tuple;

import java.util.Optional;

@Aliases("setmeta")
@Permission("cmdbuilder.command.setmeta.base")
public class SetMeta extends Command {

    @Inject
    private SetMeta(Settings settings) {
        super(settings.elements(Arguments.user().optional().toElement("user"),
                Arguments.string().toElement("name"),
                Arguments.choices(CmdBuilder.PARSER_TYPES.getAll(), ImmutableMap.of("no-choice", "Input <arg> is not the name of a parser type.")).map(Tuple::getFirst).toElement("type"),
                MetaParser.PARSER.toElement("value")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<User> optUser = args.getOne("user");
        String name = args.<String>getOne("name").get();
        ValueTypeEntry entry = args.<ParserType>getOne("type").get().getType().createEntry(args.getOne("value").get());
        try {
            Config.setMeta(optUser.map(Identifiable::getUniqueId).orElse(Util.ZERO_UUID), name, entry);
            src.sendMessage(Util.toText("&6Set &c" + optUser.map(User::getName).orElse("Server") + "&6's meta at &c" + name + " &6to &c" + entry.getString() + "&6."));
            return CommandResult.success();
        } catch (ConfigurationException e) {
            throw new CommandException(Util.toText(e.getMessage()));
        }
    }

}