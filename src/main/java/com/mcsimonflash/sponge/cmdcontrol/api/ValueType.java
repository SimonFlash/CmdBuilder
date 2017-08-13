package com.mcsimonflash.sponge.cmdcontrol.api;

import com.mcsimonflash.sponge.cmdcontrol.objects.exceptions.ScriptConfigurationException;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

public abstract class ValueType {

    private String name;
    public final String getName() {
        return name;
    }

    public ValueType(String name) {
        this.name = name;
    }

    public abstract CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException;

    public Object getParam(Object object, String param) {
        return getString(object) + "#" + param;
    }

    public String getString(Object object) {
        return object.toString();
    }

}