package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.command.Command;

import java.util.HashMap;
import java.util.Map;

public class HelpCommand extends AbstractCommand {
    public static final Map<String, String> HELP_MAP = new HashMap<>();
    public static final Map<String, String> FULL_HELP_MAP = new HashMap<>();

    static {

    }

    @Override
    public String getHelp() {
        return "Gets general help or more help on a specific command";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            if (arguments.length == 0) {
                StringBuilder builder = new StringBuilder();
                HELP_MAP.forEach((name, help) -> builder.append(name).append(": ").append(help).append("\n"));
                return builder.toString();
            } else return FULL_HELP_MAP.get(arguments[0].toLowerCase());
        };
    }
}
