package com.mcsimonflash.sponge.cmdcontrol.objects.scripts;

import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import com.mcsimonflash.sponge.cmdcontrol.objects.enums.CmdSource;
import com.mcsimonflash.sponge.cmdcontrol.objects.exceptions.ScriptExecutionException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Map;

public class ScriptExecutor {

    String command;
    CmdSource source;
    String sourceMeta;
    int wait;

    public ScriptExecutor(String command, CmdSource source, String sourceMeta, int wait) {
        this.command = command;
        this.source = source;
        this.sourceMeta = sourceMeta;
        this.wait = wait;
    }

    public CommandSource getModifiedSrc(CommandSource src, Map<String, Object> arguments) throws ScriptExecutionException {
        switch (source) {
            case INJECT:
                String name = Util.getParsedValue(src, sourceMeta, arguments);
                Player player = Sponge.getServer().getPlayer(name).orElse(null);
                if (player != null) {
                    return player;
                }
                throw new ScriptExecutionException("No source found for name. Name:[" + name + "]");
            case SERVER:
                return Sponge.getServer().getConsole();
            case SENDER:
                return src;
            case PLAYER:
                if (src instanceof Player) {
                    return src;
                }
                throw new ScriptExecutionException("Source is not instanceof Player. | Source:[" + src.getClass().getSimpleName() + "]");
            default:
                throw new ScriptExecutionException("Unknown source constant. | Source:[" + source.name() + "]");
        }
    }
}