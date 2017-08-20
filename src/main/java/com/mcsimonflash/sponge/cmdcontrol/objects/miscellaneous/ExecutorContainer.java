package com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;

public class ExecutorContainer {

    private String command;
    private CommandSource source;
    private int wait;

    public ExecutorContainer(String command, CommandSource source, int wait) {
        this.command = command;
        this.source = source;
        this.wait = wait;
    }

    public void execute() {
        Sponge.getCommandManager().process(source, command);
    }

    public int getWait() {
        return wait;
    }

}
