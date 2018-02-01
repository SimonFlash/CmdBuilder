package com.mcsimonflash.sponge.cmdbuilder.mixin;

import com.google.common.collect.ImmutableList;
import com.mcsimonflash.sponge.cmdbuilder.internal.Scripts;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.dispatcher.SimpleDispatcher;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.command.SpongeCommandManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static org.spongepowered.api.command.CommandMessageFormatting.error;
import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

@Mixin(value = SpongeCommandManager.class, remap = false)
public class MixinSpongeCommandManager {

    @Shadow
    @Final
    private SimpleDispatcher dispatcher;

    /**
     * @author Simon_Flash, 1/14/2018
     */
    @Overwrite()
    public List<String> getSuggestions(CommandSource src, String arguments, @Nullable Location<World> targetPosition, boolean usingBlock) {
        List<String> suggestions = Scripts.complete(src, arguments, targetPosition);
        if (suggestions instanceof ImmutableList) {
            return suggestions;
        }
        try {
            List<String> spongeSuggestions = dispatcher.getSuggestions(src, arguments, targetPosition);
            Sponge.getCauseStackManager().pushCause(src);
            String[] split = arguments.split(" ", 2);
            TabCompleteEvent.Command event = SpongeEventFactory.createTabCompleteEventCommand(Sponge.getCauseStackManager().getCurrentCause(), ImmutableList.copyOf(spongeSuggestions), spongeSuggestions, split.length > 1 ? split[1] : "", split[0], arguments, Optional.ofNullable(targetPosition), usingBlock);
            if (!Sponge.getGame().getEventManager().post(event)) {
                suggestions.addAll(event.getTabCompletions());
            }
            Sponge.getCauseStackManager().popCause();
        } catch (CommandException e) {
            src.sendMessage(error(t("Error getting suggestions: %s", e.getText())));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error occurred while tab completing '%s'", arguments), e);
        }
        return ImmutableList.copyOf(suggestions);
    }

}