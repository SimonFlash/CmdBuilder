package com.mcsimonflash.sponge.cmdcontrol.objects.exceptions;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class ScriptConfigurationException extends Exception {

    private CommentedConfigurationNode errNode;

    public ScriptConfigurationException(CommentedConfigurationNode errNode, String error) {
        super(error);
        this.errNode = errNode;
    }

    public CommentedConfigurationNode getErrNode() {
        return errNode;
    }
}
