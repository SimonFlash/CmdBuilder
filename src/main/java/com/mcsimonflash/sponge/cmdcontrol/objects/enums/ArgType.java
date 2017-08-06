package com.mcsimonflash.sponge.cmdcontrol.objects.enums;

import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import com.mcsimonflash.sponge.cmdcontrol.objects.exceptions.ScriptExecutionException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.regex.Matcher;

public enum ArgType {
    BOOLEAN {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            try {
                return Boolean.parseBoolean(value);
            } catch (IllegalArgumentException ignored) {
                throw new ScriptExecutionException("Expected argument to be of type Boolean. | Argument:[" + value + "]");
            }
        }
    },
    CHOICES {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            if (Arrays.asList(Util.separatorPattern.split(params.toLowerCase())).contains(value.toLowerCase())) {
                return value;
            }
            throw new ScriptExecutionException("Expected argument to be within set choices. | Argument:[" + value + "] + Choices:[" + params + "]");
        }
    },
    DOUBLE {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            try {
                Double num = Double.parseDouble(value);
                if (!params.isEmpty()) {
                    Matcher matcher = Util.doubleRangePattern.matcher(params);
                    if (matcher.group(1) != null) {
                        double lower = Double.parseDouble(matcher.group(1));
                        if (matcher.group(2) != null ? lower > num : lower >= num) {
                            throw new ScriptExecutionException("Less than lower bound. | Lower:[" + lower + "] Argument:[" + num + "]");
                        }
                    }
                    if (matcher.group(4) != null) {
                        double upper = Double.parseDouble(matcher.group(4));
                        if (matcher.group(3) != null ? num > upper : num >= upper) {
                            throw new ScriptExecutionException("Greater than upper bound. | Upper:[" + upper + "] Argument:[" + num + "]");
                        }
                    }
                }
                return num;
            } catch (NumberFormatException ignored) {
                throw new ScriptExecutionException("Expected argument to be of type Double. | Argument:[" + value + "]");
            }
        }
    },
    INJECT {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            return params;
        }
    },
    INTEGER {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            try {
                int num = Integer.parseInt(value);
                if (!params.isEmpty()) {
                    Matcher matcher = Util.integerRangePattern.matcher(params);
                    if (matcher.matches()) {
                        if (matcher.group(1) != null) {
                            int lower = Integer.parseInt(matcher.group(1));
                            if (matcher.group(2) != null ? lower > num : lower >= num) {
                                throw new ScriptExecutionException("Less than lower bound. | Lower:[" + lower + "] Argument:[" + num + "]");
                            }
                        }
                        if (matcher.group(4) != null) {
                            int upper = Integer.parseInt(matcher.group(4));
                            if (matcher.group(3) != null ? num > upper : num >= upper) {
                                throw new ScriptExecutionException("Greater than upper bound. | Upper:[" + upper + "] Argument:[" + num + "]");
                            }
                        }
                    }
                }
                return num;
            } catch (NumberFormatException ignored) {
                throw new ScriptExecutionException("Expected argument to be of type Integer. | Argument:[" + value + "]");
            }
        }
    },
    ITEM {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            ItemType item = Sponge.getRegistry().getType(ItemType.class, value).orElse(null);
            if (item != null) {
                return item;
            }
            throw new ScriptExecutionException("Expected argument to be of type Item. | Argument:[" + value + "]");
        }

        @Override
        public String getString(Object object) throws ScriptExecutionException {
            return object instanceof ItemType ? ((ItemType) object).getId() : object.toString();
        }
    },
    JOINED_STRINGS,
    PLAYER {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            Player player = Sponge.getServer().getPlayer(value).orElse(null);
            if (player != null) {
                return player;
            }
            throw new ScriptExecutionException("Expected argument to be of type Player. | Argument:[" + value + "]");
        }

        @Override
        public String getString(Object object) throws ScriptExecutionException {
            return object instanceof Player ? ((Player) object).getName() : object.toString();
        }
    },
    STRING,
    TRISTATE {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            try {
                return Tristate.valueOf(value);
            } catch (IllegalArgumentException ignored) {
                throw new ScriptExecutionException("Expected argument to be of type Tristate. | Argument:[" + value + "]");
            }
        }

        @Override
        public String getString(Object object) throws ScriptExecutionException {
            return object instanceof Tristate ? ((Tristate) object).name().toLowerCase() : object.toString();
        }
    },
    USER {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            User user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(value).orElse(null);
            if (user != null) {
                return user;
            }
            throw new ScriptExecutionException("Expected argument to be of type User. | Argument:[" + value + "]");
        }

        @Override
        public String getString(Object object) throws ScriptExecutionException {
            return object instanceof User ? ((User) object).getName() : object.toString();
        }
    },
    UUID {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            try {
                if (!value.contains("-") && value.matches("[0-9a-f]{32}")) {
                    value = value.substring(0, 8) + "-" + value.substring(8, 12) + "-" + value.substring(12, 16) + "-" + value.substring(16, 20) + "-" + value.substring(20);
                }
                return java.util.UUID.fromString(value);
            } catch (IllegalArgumentException e) {
                throw new ScriptExecutionException("Expected argument to be of type UUID. | Argument:[" + value + "]");
            }
        }

        @Override
        public String getString(Object object) throws ScriptExecutionException {
            return object instanceof World ? ((World) object).getName() : object.toString();
        }
    },
    WORLD {
        @Override
        public Object getParsedType(String value, String params) throws ScriptExecutionException {
            World world = Sponge.getServer().getWorld(value).orElse(null);
            if (world != null) {
                return world;
            }
            throw new ScriptExecutionException("Expected argument to be of type World. | Argument:[" + value + "]");
        }

        @Override
        public String getString(Object object) throws ScriptExecutionException {
            return object instanceof World ? ((World) object).getName() : object.toString();
        }
    };

    public Object getParsedType(String value, String params) throws ScriptExecutionException {
        return value;
    }

    public String getString(Object object) throws ScriptExecutionException {
        return object.toString();
    }

    public static ArgType getType(String type) {
        return Arrays.stream(ArgType.values()).filter(v -> v.name().replace("_", "").equalsIgnoreCase(type)).findAny().orElse(null);
    }
}