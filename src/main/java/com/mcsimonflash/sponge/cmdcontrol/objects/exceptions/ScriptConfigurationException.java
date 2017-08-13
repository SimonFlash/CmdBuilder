package com.mcsimonflash.sponge.cmdcontrol.objects.exceptions;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class ScriptConfigurationException extends Exception {

    private CommentedConfigurationNode node;

    public ScriptConfigurationException(CommentedConfigurationNode node, String error) {
        super(error);
        this.node = node;
    }

    public CommentedConfigurationNode getNode() {
        return node;
    }
}
