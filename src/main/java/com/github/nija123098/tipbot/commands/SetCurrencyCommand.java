package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Command;
import com.github.nija123098.tipbot.Database;
import com.github.nija123098.tipbot.Main;
import com.github.nija123098.tipbot.utility.Unit;

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
            Database.setValue(Database.PREFERRED_CURRENCY, invoker, unit.name());
            return Main.OK_HAND;
        };
    }
}
