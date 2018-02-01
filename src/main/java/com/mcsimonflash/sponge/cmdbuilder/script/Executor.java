package com.mcsimonflash.sponge.cmdbuilder.script;

import com.google.common.base.Preconditions;
import com.mcsimonflash.sponge.cmdbuilder.internal.Scripts;
import com.mcsimonflash.sponge.cmdbuilder.miscellaneous.ExecutorContainer;
import com.mcsimonflash.sponge.cmdbuilder.miscellaneous.Source;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.NodeUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class Executor {

    private long delay = 0;
    private String command;
    private Source source;
    private ConfigurationNode meta;

    private Executor(long delay, String command, Source source, ConfigurationNode meta) {
        this.delay = delay;
        this.command = command;
        this.source = source;
        this.meta = meta;
    }

    public ExecutorContainer buildExecutor(CommandSource src, Map<String, ValueTypeEntry> arguments) throws IllegalArgumentException {
        CommandSource modifiedSource = src;
        if (source == Source.PLAYER) {
            Preconditions.checkArgument(src instanceof Player, "This executed requested a Player source, but instead found %s.", src.getClass().getSimpleName());
        } else if (source == Source.INJECT) {
            String iden = meta.getNode("player").getString("");
            modifiedSource = Sponge.getServer().getPlayer(Scripts.insertArguments(iden, src, arguments)).orElseThrow(() -> new IllegalArgumentException("Unable to inject source from meta#player '" + iden + "'."));
        } else if (source == Source.SERVER) {
            modifiedSource = Sponge.getServer().getConsole();
        }
        return new ExecutorContainer(delay, Scripts.insertArguments(command, src, arguments), modifiedSource);
    }

    public long getDelay() {
        return delay;
    }
    public String getCommand() {
        return command;
    }
    public Source getSource() {
        return source;
    }
    public ConfigurationNode getMeta() {
        return meta;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private long delay = 0;
        private String command;
        private Source source;
        private ConfigurationNode meta = SimpleConfigurationNode.root();

        public Builder delay(long delay) {
            this.delay = delay;
            return this;
        }

        public Builder command(String command) {
            this.command = command;
            return this;
        }

        public Builder source(Source source) {
            this.source = source;
            return this;
        }

        public Builder meta(ConfigurationNode meta) {
            this.meta = NodeUtils.copy(meta);
            return this;
        }

        public Executor build() throws IllegalArgumentException {
            checkArgument(command != null, "'command' must be defined.");
            checkArgument(source != null, "'source' must be defined.");
            return new Executor(delay, command, source, meta);
        }

    }

}