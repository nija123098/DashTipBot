package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Database;
import com.github.nija123098.tipbot.Main;
import sx.blah.discord.handle.obj.IUser;
import com.github.nija123098.tipbot.utility.TransationLog;
import com.github.nija123098.tipbot.utility.Unit;

import static com.github.nija123098.tipbot.utility.DatabaseTables.BALANCES_TABLE;

public class TipCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Tip a user an amount of Dash which they may withdraw.";
    }

    @Override
    public String getFullHelp() {
        return "Tip a user an amount of Dash which they may withdraw.\n" +
                "You may specify a Dash, USD, beer, or other amount.\n" +
                "Examples:\n" +
                "    " + Main.PREFIX + "tip " + Main.MAINTAINER.mention() + " a " + Unit.COFFEE.name().toLowerCase() + "\n" +
                "    " + Main.PREFIX + "tip " + Main.MAINTAINER.mention() + " 1 " + Unit.GBP + "\n" +
                "    " + Main.PREFIX + "tip " + Main.MAINTAINER.mention() + " 1 " + Unit.$ + "\n" +
                "    " + Main.PREFIX + "tip " + Main.MAINTAINER.mention() + " .01 Dash";
    }

    @Override
    public Main.Command getCommand() {
        return ((invoker, arguments) -> {
            BalanceCommand.update(invoker);
            if (arguments.length < 2) return "Please specify a user by mentioning him or her, then a number and unit.";
            double currentWallet = Double.parseDouble(Database.getValue(BALANCES_TABLE, invoker, "0"));
            IUser recipient = Main.getUserFromMention(arguments[0]);
            if (recipient.equals(invoker)) return "You can't tip yourself.";
            if (recipient.isBot()) return "You can't tip a bot";
            String combined = arguments[1] + " ";
            if (arguments.length == 3) combined += arguments[2];
            combined = combined.toLowerCase();
            Unit unit = null;
            int index;
            String lowerName;
            for (Unit searchingUnit : Unit.values()){
                lowerName = searchingUnit.name().toLowerCase();
                index = combined.indexOf(lowerName);
                if (index == -1) continue;
                unit = searchingUnit;
                combined = combined.replace(lowerName, "").replace(" ", "");
                break;
            }
            if (unit == null) return "I was unable to find a unit to tip in.";
            double amount;
            if (combined.isEmpty()) amount = 1;
            else try {
                amount = Double.parseDouble(combined);
            } catch (NumberFormatException e){
                return "Please specify an amount to tip.";
            }
            double tipAmount = unit.getDashAmount() * amount;
            if (tipAmount > currentWallet) return "You don't have enough for that.";
            Database.setValue(BALANCES_TABLE, invoker, String.valueOf(currentWallet - tipAmount));
            Database.setValue(BALANCES_TABLE, recipient, String.valueOf(Double.valueOf(Database.getValue(BALANCES_TABLE, recipient, "0")) + tipAmount));
            TransationLog.log("tip of " + tipAmount + " from " + invoker.getStringID() + " to " + recipient.getStringID());
            return Main.OK_HAND;
        });
    }
}
