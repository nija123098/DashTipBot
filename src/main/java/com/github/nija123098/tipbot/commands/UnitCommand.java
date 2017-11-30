package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Command;
import com.github.nija123098.tipbot.utility.Unit;

import java.util.stream.Stream;

public class UnitCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Returns the units in which you may gift dash in and may see your current balance value in.";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            StringBuilder builder = new StringBuilder("The units in which you may gift dash in and may see your current balance value in.\n```\n");
            Stream.of(Unit.values()).forEach(unit -> builder.append(unit.name()).append("\n"));
            return builder.append("```").toString();
        };
    }
}
