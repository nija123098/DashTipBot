package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.command.Command;
import com.github.nija123098.tipbot.utility.Database;
import com.github.nija123098.tipbot.utility.TransactionLog;
import com.github.nija123098.tipbot.utility.Unit;
import sx.blah.discord.handle.obj.IUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.github.nija123098.tipbot.utility.Database.*;
import static com.github.nija123098.tipbot.utility.Database.BALANCES;
import static com.github.nija123098.tipbot.utility.Database.RECEIVED;
import static com.github.nija123098.tipbot.utility.Database.RECEIVING_ADDRESSES;

public class BalanceCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Gets your balance.";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            update(invoker);
            double amount = Double.parseDouble(getValue(BALANCES, invoker, "0"));
            Unit displayUnit = Unit.getUnitForName(getValue(PREFERRED_CURRENCY, invoker, "USD"));
            return Unit.displayAmount(amount, 4) + " Dash which is worth " + displayUnit.display(amount / displayUnit.getDashAmount());
        };
    }

    static void update(IUser user) throws IOException {
        String receivingAddress = getValue(RECEIVING_ADDRESSES, user, null);
        if (receivingAddress == null) return;
        Process process = new ProcessBuilder("dash-cli", "getreceivedbyaddress", receivingAddress, "6").start();
        String s = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
        if (s == null || s.startsWith("e")) return;
        String previous = getValue(BALANCES, user, "0");
        Double addToBalance = Double.parseDouble(s) - Double.parseDouble(previous);
        if (addToBalance < .0000001D) return;
        TransactionLog.log("adding " + addToBalance + " to balance for user " + user.getStringID());
        setValue(RECEIVED, user, s);
        setValue(BALANCES, user, String.valueOf(Double.valueOf(previous) + addToBalance));
    }
}
