package com.mcsimonflash.sponge.cmdcontrol.objects.scripts;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.managers.Storage;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import com.mcsimonflash.sponge.cmdcontrol.objects.enums.ArgType;
import com.mcsimonflash.sponge.cmdcontrol.objects.exceptions.ScriptExecutionException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Script {

    String name;
    LinkedList<ScriptArgument> arguments;
    LinkedList<ScriptExecutor> executors;
    ScriptMetadata metadata;

    public Script(String name, LinkedList<ScriptArgument> arguments, LinkedList<ScriptExecutor> executors, ScriptMetadata metadata) {
        this.name = name;
        this.arguments = arguments;
        this.executors = executors;
        this.metadata = metadata;
    }

    public void registerAliases() {
        for (String alias : metadata.Aliases) {
            if (!Storage.aliasRegistry.containsKey(alias)) {
                if (metadata.Override || !Sponge.getCommandManager().containsAlias(alias)) {
                    Storage.aliasRegistry.put(alias, name);
                } else {
                    CmdControl.getPlugin().getLogger().warn("Attempt to register non-overriden alias. | Alias:[" + alias + "] Script:[" + name + "]");
                }
            } else {
                CmdControl.getPlugin().getLogger().warn("Alias was registered to another script. | Alias:[" + alias + "] Script:[" + name + "]");
            }
        }
    }

    public void process(CommandSource src, String rawArguments) {
        boolean paid = false;
        try {
            paid = processConditions(src);
            Map<String, Object> arguments = createArguments(rawArguments);
            LinkedList<ScriptExecutor> executors = createExecutors(src, arguments);
            LinkedList<CommandSource> sources = createSources(src, arguments, executors);
            if (metadata.Cooldown != 0 && src instanceof Player) {
                if (!Storage.setCooldown((Player) src, name)) {
                    throw new ScriptExecutionException("An unexpected error occurred attempting to store cooldown.");
                }
            }
            processExecutors(src, executors, sources);
        } catch (ScriptExecutionException e) {
            src.sendMessage(Util.msgPrefix.concat(Util.toText(e.getMessage())));
            if (paid) {
                UniqueAccount account = CmdControl.getEconServ().getOrCreateAccount(((Player) src).getUniqueId()).orElse(null);
                if (account != null) {
                    TransactionResult result = account.deposit(CmdControl.getEconServ().getDefaultCurrency(), BigDecimal.valueOf(metadata.Cost), Cause.source(CmdControl.getPlugin()).build());
                    if (result.getResult() != ResultType.SUCCESS) {
                        CmdControl.getPlugin().getLogger().error("Unable to refund cost. | Cost:[" + metadata.Cost + "] Player:[" + src.getName() + "]");
                    }
                } else {
                    CmdControl.getPlugin().getLogger().error("Unable to locate economy account to refund cost. | Cost:[" + metadata.Cost + "] Player:[" + src.getName() + "]");
                }
            }
        }
    }

    public boolean processConditions(CommandSource src) throws ScriptExecutionException {
        if (src instanceof Player) {
            if (!src.hasPermission("cmdcontrol.scripts." + name + ".base")) {
                throw new ScriptExecutionException("No permission to run this script.");
            }
            for (String permission : metadata.Permissions) {
                if (!src.hasPermission(permission)) {
                    throw new ScriptExecutionException("Missing required permission. | Permission:[" + permission + "]");
                }
            }
            if (metadata.Cooldown != 0 && !src.hasPermission("cmdcontrol.scripts." + name + ".nocooldown")) {
                long time = metadata.Cooldown - (System.currentTimeMillis() - Storage.getCooldown((User) src, name));
                if (time > 0) {
                    throw new ScriptExecutionException("Cooldown has not passed. | TimeRemaining:[" + time/1000 + "s]");
                }
            }
            if (metadata.Cost != 0 && !src.hasPermission("cmdcontrol.scripts." + name + ".nocost")) {
                Optional<UniqueAccount> accOpt = CmdControl.getEconServ().getOrCreateAccount(((Player) src).getUniqueId());
                if (accOpt.isPresent()) {
                    TransactionResult result = accOpt.get().withdraw(CmdControl.getEconServ().getDefaultCurrency(), BigDecimal.valueOf(metadata.Cost), Cause.source(CmdControl.getPlugin()).build());
                    if (result.getResult() != ResultType.SUCCESS) {
                        throw new ScriptExecutionException("Not enough funds. | Cost:[" + metadata.Cost + "]");
                    }
                    return true;
                } else {
                    throw new ScriptExecutionException("An unexpected error occurred attempting to locate economy account.");
                }
            }
        } else if (metadata.RequirePlayer) {
            throw new ScriptExecutionException("Only a player may run this script.");
        }
        return false;
    }

    public Map<String, Object> createArguments(String rawInput) throws ScriptExecutionException {
        String[] rawArgs = rawInput.split(" ");
        LinkedList<ScriptArgument> requiredArgs = Lists.newLinkedList(arguments.stream().filter(a -> !a.type.equals(ArgType.INJECT)).collect(Collectors.toList()));
        if (rawArgs.length < requiredArgs.size() || (rawArgs.length > requiredArgs.size() && !requiredArgs.getLast().type.equals(ArgType.JOINED_STRINGS))) {
            throw new ScriptExecutionException("Incorrect number of arguments. Required:[" + requiredArgs.size() + "]");
        }
        Map<String, Object> parsedArgs = Maps.newHashMap();
        int c = 0;
        for (ScriptArgument arg : arguments) {
            String value = arg.type.equals(ArgType.INJECT) ? "" : (arg.type.equals(ArgType.JOINED_STRINGS) ? String.join(arg.typeMeta, Arrays.copyOfRange(rawArgs, c++, rawArgs.length)) : rawArgs[c++]);
            CmdControl.getPlugin().getLogger().warn("Arg # " + c + ": " + value);
            parsedArgs.put(arg.name, arg.type.getString(arg.type.getParsedType(value, arg.typeMeta)));
        }
        return parsedArgs;
    }

    public LinkedList<ScriptExecutor> createExecutors(CommandSource src, Map<String, Object> arguments) throws ScriptExecutionException {
        LinkedList<ScriptExecutor> modifiedExecutors = Lists.newLinkedList();
        for (ScriptExecutor executor : executors) {
            modifiedExecutors.add(new ScriptExecutor(Util.getParsedValue(src, executor.command, arguments), executor.source, executor.sourceMeta, executor.wait));
        }
        return modifiedExecutors;
    }

    public LinkedList<CommandSource> createSources(CommandSource src, Map<String, Object> arguments, LinkedList<ScriptExecutor> executors) throws ScriptExecutionException {
        LinkedList<CommandSource> sources = Lists.newLinkedList();
        for (ScriptExecutor executor : executors) {
            sources.add(executor.getModifiedSrc(src, arguments));
        }
        return sources;
    }

    public void processExecutors(CommandSource src, LinkedList<ScriptExecutor> executors, LinkedList<CommandSource> sources) {
        for (int i = 0; i < executors.size(); i++) {
            ScriptExecutor executor = executors.get(i);
            if (!executor.command.isEmpty()) {
                Sponge.getCommandManager().process(sources.get(i), executor.command);
            }
            if (executor.wait != 0) {
                LinkedList<ScriptExecutor> futureExecutors = Lists.newLinkedList(executors.subList(i + 1, executors.size()));
                LinkedList<CommandSource> futureSources = Lists.newLinkedList(sources.subList(i + 1, sources.size()));
                Task.builder().name("Script Executor | Script:[" + name + "] Source:[" + src.getName() + "]")
                        .execute(task -> processExecutors(src, futureExecutors, futureSources))
                        .delay(executor.wait, TimeUnit.MILLISECONDS)
                        .submit(CmdControl.getPlugin());
                return;
            }
        }
    }
}