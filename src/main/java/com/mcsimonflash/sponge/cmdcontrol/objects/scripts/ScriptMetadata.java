package com.mcsimonflash.sponge.cmdcontrol.objects.scripts;

import com.google.common.collect.Lists;

import java.util.List;

public class ScriptMetadata {

    public int Cooldown = 0;
    public double Cost = 0.0;
    public boolean RequirePlayer = false;
    public boolean Override = false;
    public String Description = "";
    public List<String> Aliases = Lists.newArrayList();
    public List<String> Permissions = Lists.newArrayList();
}
