package com.mcsimonflash.sponge.cmdbuilder.miscellaneous;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.cmdbuilder.script.Script;

import javax.annotation.Nullable;
import java.util.Map;

public class Mapping {

    public static final Mapping ROOT = new Mapping("root", null);

    public final String Name;
    @Nullable public Script Script;
    @Nullable public Mapping Parent;
    public final Map<String, Mapping> Children = Maps.newHashMap();

    public Mapping(String name, @Nullable Mapping parent) {
        Preconditions.checkArgument(!name.isEmpty(), "'name' must be defined.");
        Name = name;
        Parent = parent;
        if (Parent != null) {
            Parent.Children.put(Name, this);
        }
    }

    public static class Result {

        private final Mapping mapping;
        private final String[] split;
        private final int start;

        public Result(Mapping mapping, String[] split, int start) {
            this.mapping = mapping;
            this.split = split;
            this.start = start;
        }

        public Mapping getMapping() {
            return mapping;
        }
        public String[] getSplit() {
            return split;
        }
        public int getStart() {
            return start;
        }

    }

}