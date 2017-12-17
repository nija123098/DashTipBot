package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.Bot;
import com.github.nija123098.tipbot.command.Command;
import com.github.nija123098.tipbot.utility.Database;
import com.github.nija123098.tipbot.utility.Unit;

import static com.github.nija123098.tipbot.utility.Database.*;

public class SetCurrencyCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Sets the currency to print value for.";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            Unit unit = Unit.getUnitForName(arguments[0]);
            if (unit == null) return "That is not a recognized currency unit, please try the ISO code.";
            setValue(PREFERRED_CURRENCY, invoker, unit.name());
            return Bot.OK_HAND;
        };
    }
}
