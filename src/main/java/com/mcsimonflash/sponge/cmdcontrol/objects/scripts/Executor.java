package com.mcsimonflash.sponge.cmdcontrol.objects.scripts;

import com.google.common.base.Preconditions;
import com.mcsimonflash.sponge.cmdcontrol.api.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous.ExecutorContainer;
import com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous.Source;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;

import javax.annotation.Nullable;
import java.util.Map;

public class Executor {

    private String command;
    private Source source;
    private CommentedConfigurationNode meta;
    private int wait;

    public Executor(@Nullable String command, @Nullable Source source, @Nullable CommentedConfigurationNode meta, int wait) {
        Preconditions.checkNotNull(command, "Executor command cannot be null.");
        Preconditions.checkArgument(!command.isEmpty(), "Executor command cannot be empty.");
        this.command = command;
        Preconditions.checkNotNull(source, "Executor source cannot be null.");
        this.source = source;
        Preconditions.checkNotNull(meta, "Executor meta cannot be null.");
        this.meta = meta;
        Preconditions.checkNotNull(wait, "Executor wait cannot be null.");
        Preconditions.checkArgument(wait >= 0, "Executor wait cannot be negative.");
        this.wait = wait;
    }

    public String getCommand() {
        return command;
    }
    public Source getSource() {
        return source;
    }
    public CommentedConfigurationNode getMeta() {
        return meta;
    }

    public ExecutorContainer buildExecutor(CommandSource src, Map<String, ValueTypeEntry> arguments) throws IllegalArgumentException {
        CommandSource modifiedSource = src;
        if (source == Source.INJECT) {
            String iden = meta.getNode("player").getString("");
            modifiedSource = Sponge.getServer().getPlayer(Util.insertArguments(iden, src, arguments)).orElseThrow(() -> new IllegalArgumentException("Unable to inject source from meta \"" + iden + "\"."));
        } else if (source == Source.SERVER) {
            modifiedSource = Sponge.getServer().getConsole();
        }
        String modifiedCommand = Util.insertArguments(command, src, arguments);
        return new ExecutorContainer(modifiedCommand, modifiedSource, wait);
    }

}