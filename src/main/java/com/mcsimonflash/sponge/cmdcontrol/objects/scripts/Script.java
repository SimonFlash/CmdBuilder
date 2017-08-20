package com.mcsimonflash.sponge.cmdcontrol.objects.scripts;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.api.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.api.ValueTypes;
import com.mcsimonflash.sponge.cmdcontrol.managers.Storage;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous.ExecutorContainer;
import com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous.Source;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Script {

    private String name;
    private LinkedList<Argument> arguments;
    private LinkedList<Executor> executors;
    private Metadata metadata;
    private CommandSpec spec;

    public Script(@Nullable String name, @Nullable LinkedList<Argument> arguments, @Nullable LinkedList<Executor> executors, @Nullable Metadata metadata) {
        Preconditions.checkNotNull(name, "Script name cannot be null.");
        Preconditions.checkArgument(!name.isEmpty(), "Script name cannot be empty.");
        this.name = name;
        Preconditions.checkNotNull(arguments, "Script arguments cannot be null.");
        List<String> names = Lists.newArrayList();
        for (int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            Preconditions.checkArgument(!names.contains(argument.getName()), "Found duplicate argument named \"%s\".", argument.getName());
            Preconditions.checkArgument(argument.getType() != ValueTypes.JOINED_STRINGS || i == arguments.size() - 1, "Found intermediate JoinedStrings argument named \"%s\"", argument.getName());
            names.add(argument.getName());
        }
        this.arguments = arguments;
        Preconditions.checkNotNull(executors, "Script executors cannot be null.");
        Preconditions.checkArgument(!executors.isEmpty(), "Script executors cannot be empty");
        this.executors = executors;
        Preconditions.checkNotNull(metadata, "Script metadata cannot be null.");
        Preconditions.checkArgument(metadata.getPlayer() || executors.stream().noneMatch(e -> e.getSource() == Source.PLAYER), "Found undeclared player source requirement.");
        this.metadata = metadata;
        spec = CommandSpec.builder()
                .executor((this::execute))
                .arguments(arguments.stream().map(Argument::getElement).toArray(CommandElement[]::new))
                .permission("cmdcontrol.scripts." + name + ".base")
                .build();
    }

    public String getName() {
        return name;
    }
    public LinkedList<Argument> getArguments() {
        return arguments;
    }
    public LinkedList<Executor> getExecutors() {
        return executors;
    }
    public Metadata getMetadata() {
        return metadata;
    }
    public CommandSpec getSpec() {
        return spec;
    }

    public CommandResult execute(CommandSource src, CommandContext args) {
        try {
            preconditions(src);
            Map<String, ValueTypeEntry> arguments = collectArgs(args);
            LinkedList<ExecutorContainer> executors = buildExecutors(src, arguments);
            postconditions(src);
            processExecutors(src, executors);
            return CommandResult.success();
        } catch (IllegalArgumentException e) {
            src.sendMessage(Util.prefix.concat(Util.toText(e.getMessage())));
            return CommandResult.empty();
        }
    }

    public void preconditions(CommandSource src) throws IllegalArgumentException {
        if (src instanceof Player) {
            for (String permission : metadata.getPermissions()) {
                Preconditions.checkArgument(src.hasPermission(permission), "You must have the permission \"%s\" to use this script.", permission);
            }
            if (metadata.getCooldown() != 0 && !src.hasPermission("cmdcontrol.scripts." + name + ".nocooldown")) {
                long time = metadata.getCooldown() - (System.currentTimeMillis() - Storage.getCooldown((Player) src, name));
                Preconditions.checkArgument(time <= 0, "You must wait \"%s\" seconds before using this script.", +time / 1000);
            }
            if (metadata.getCost() != 0 && !src.hasPermission("cmdcontrol.scripts." + name + ".nocost")) {
                UniqueAccount account = CmdControl.getEconServ().getOrCreateAccount(((Player) src).getUniqueId()).orElse(null);
                Preconditions.checkArgument(account != null, "An unexpected error occurred attempting to locate your economy account.");
                TransactionResult result = account.withdraw(CmdControl.getEconServ().getDefaultCurrency(), BigDecimal.valueOf(metadata.getCost()), Cause.source(CmdControl.getPlugin()).build());
                Preconditions.checkArgument(result.getResult() == ResultType.SUCCESS, "You cannot afford the cost of \"%s\" to use this script.", metadata.getCost());
            }
        } else {
            Preconditions.checkArgument(!metadata.getPlayer(), "Only a player may run this script.");
        }
    }

    public Map<String, ValueTypeEntry> collectArgs(CommandContext args) throws IllegalArgumentException {
        Map<String, ValueTypeEntry> arguments = Maps.newHashMap();
        this.arguments.forEach(a -> arguments.put(a.getName(), a.collectArg(args)));
        return arguments;
    }

    public LinkedList<ExecutorContainer> buildExecutors(CommandSource src, Map<String, ValueTypeEntry> arguments) throws IllegalArgumentException {
        LinkedList<ExecutorContainer> executors = Lists.newLinkedList();
        this.executors.forEach(e -> executors.add(e.buildExecutor(src, arguments)));
        return executors;
    }

    public void postconditions(CommandSource src) {
        if (src instanceof Player) {
            Storage.setCooldown((Player) src, name);
        }
    }

    public void processExecutors(CommandSource src, LinkedList<ExecutorContainer> executors) {
        for (int i = 0; i < executors.size(); i++) {
            ExecutorContainer executor = executors.get(i);
            executor.execute();
            if (executor.getWait() > 0) {
                LinkedList<ExecutorContainer> futureExecutors = Lists.newLinkedList(executors.subList(i + 1, executors.size()));
                Task.builder().name("Script \"" + name + "\" Executor for Source \"" + src.getName() + "\"")
                        .execute(task -> processExecutors(src, futureExecutors))
                        .delay(executor.getWait(), TimeUnit.MILLISECONDS)
                        .submit(CmdControl.getPlugin());
                return;
            }
        }
    }

}