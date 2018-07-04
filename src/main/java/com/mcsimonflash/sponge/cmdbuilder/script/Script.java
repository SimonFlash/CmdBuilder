package com.mcsimonflash.sponge.cmdbuilder.script;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.cmdbuilder.CmdBuilder;
import com.mcsimonflash.sponge.cmdbuilder.internal.Config;
import com.mcsimonflash.sponge.cmdbuilder.internal.Util;
import com.mcsimonflash.sponge.cmdbuilder.miscellaneous.ExecutorContainer;
import com.mcsimonflash.sponge.cmdbuilder.miscellaneous.Source;
import com.mcsimonflash.sponge.cmdbuilder.type.ParserTypes;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public class Script {

    private final String name;
    private final ImmutableList<Argument> arguments;
    private final ImmutableList<Executor> executors;
    private final Metadata metadata;
    private final CommandSpec spec;

    public Script(String name, ImmutableList<Argument> arguments, ImmutableList<Executor> executors, Metadata metadata) {
        this.name = name;
        this.arguments = arguments;
        this.executors = executors;
        this.metadata = metadata;
        this.spec = CommandSpec.builder()
                .executor(this::execute)
                .arguments(arguments.stream().map(Argument::getElement).toArray(CommandElement[]::new))
                .permission("cmdbuilder.scripts." + name + ".base")
                .build();
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        try {
            if (src instanceof User) {
                if (metadata.getCooldown() > 0 && !src.hasPermission("cmdbuilder.scripts." + name + ".nocooldown")) {
                    long time = metadata.getCooldown() - (System.currentTimeMillis() - Config.getCooldown(((User) src).getUniqueId(), this));
                    checkArgument(time <= 0, "You must wait '%s' seconds before using this script.", +time / 1000);
                }
                if (metadata.getCost() > 0 && !src.hasPermission("cmdbuilder.scripts." + name + ".nocost")) {
                    EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElseThrow(() -> new IllegalArgumentException("An economy plugin is required to use this script."));
                    UniqueAccount account = service.getOrCreateAccount(((User) src).getUniqueId()).orElseThrow(() -> new IllegalArgumentException("An unexpected error occurred attempting to locate your economy account."));
                    TransactionResult result = account.withdraw(service.getDefaultCurrency(), BigDecimal.valueOf(metadata.getCost()), Sponge.getCauseStackManager().getCurrentCause());
                    checkArgument(result.getResult() == ResultType.SUCCESS, "You cannot afford the cost of '%s' to use this script.", metadata.getCost());
                }
            } else {
                checkArgument(executors.stream().noneMatch(e -> e.getSource() == Source.PLAYER), "Only a player may run this script.");
            }
            Map<String, ValueTypeEntry> arguments = this.arguments.stream().collect(Collectors.toMap(Argument::getName, a -> a.collectArg(args)));
            List<ExecutorContainer> executors = this.executors.stream().map(e -> e.buildExecutor(src, arguments)).collect(Collectors.toList());
            if (src instanceof User && metadata.getCooldown() > 0) {
                if (!Config.setCooldown(((User) src).getUniqueId(), this, System.currentTimeMillis())) {
                    throw new IllegalArgumentException("Unable to save cooldown - please contact an administrator");
                }
            }
            for (ExecutorContainer executor : executors) {
                if (executor.getDelay() > 0) {
                    Task.builder().name("Script '" + name + "' Executor for Source '" + src.getName() + "'")
                            .execute(executor::execute)
                            .delay(executor.getDelay(), TimeUnit.MILLISECONDS)
                            .submit(CmdBuilder.get().getContainer());
                } else {
                    executor.execute();
                }
            }
            return CommandResult.success();
        } catch (IllegalArgumentException e) {
            src.sendMessage(CmdBuilder.get().getPrefix().concat(Util.toText(e.getMessage())));
            return CommandResult.empty();
        }
    }

    public String getName() {
        return name;
    }
    public ImmutableList<Argument> getArguments() {
        return arguments;
    }
    public ImmutableList<Executor> getExecutors() {
        return executors;
    }
    public Metadata getMetadata() {
        return metadata;
    }
    public CommandSpec getSpec() {
        return spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private ImmutableList<Argument> arguments = ImmutableList.of();
        private ImmutableList<Executor> executors = ImmutableList.of();
        private Metadata metadata = Metadata.DEFAULT;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder arguments(List<Argument> arguments) {
            this.arguments = ImmutableList.copyOf(arguments);
            return this;
        }

        public Builder executors(List<Executor> executors) {
            this.executors = ImmutableList.copyOf(executors);
            return this;
        }

        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Script build() throws IllegalArgumentException {
            checkArgument(name != null && !name.isEmpty(), "'name' must be defined");
            List<String> names = Lists.newArrayList();
            for (int i = 0; i < arguments.size(); i++) {
                Argument argument = arguments.get(i);
                checkArgument(!names.contains(argument.getName()), "Found duplicate argument named '%s'.", argument.getName());
                checkArgument(argument.getParser() != ParserTypes.JOINED_STRINGS || i == arguments.size() - 1, "Found intermediate JoinedStrings argument named '%s'", argument.getName());
                names.add(argument.getName());
            }
            return new Script(name, arguments, executors, metadata);
        }

    }

}