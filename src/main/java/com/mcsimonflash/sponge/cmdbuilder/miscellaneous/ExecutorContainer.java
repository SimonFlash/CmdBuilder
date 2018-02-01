package com.mcsimonflash.sponge.cmdbuilder.miscellaneous;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;

public class ExecutorContainer {

    private final long delay;
    private final String command;
    private final CommandSource source;

    public ExecutorContainer(long delay, String command, CommandSource source) {
        this.delay = delay;
        this.command = command;
        this.source = source;
    }

    public CommandResult execute() {
        return Sponge.getCommandManager().process(source, command);
    }

    public long getDelay() {
        return delay;
    }
    public String getCommand() {
        return command;
    }
    public CommandSource getSource() {
        return source;
    }

}
