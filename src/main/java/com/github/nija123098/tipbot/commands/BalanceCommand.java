package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Database;
import com.github.nija123098.tipbot.Main;
import sx.blah.discord.handle.obj.IUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.github.nija123098.tipbot.utility.DatabaseTables.BALANCES_TABLE;
import static com.github.nija123098.tipbot.utility.DatabaseTables.RECEIVED_TABLE;
import static com.github.nija123098.tipbot.utility.DatabaseTables.RECEIVING_ADDRESSES_TABLE;

public class BalanceCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Gets your balance.";
    }
    @Override
    public Main.Command getCommand() {
        return (invoker, arguments) -> {
            update(invoker);
            return Database.getValue(BALANCES_TABLE, invoker, "0") + " Dash";
        };
    }

    public static void update(IUser user) throws IOException {
        String receivingAddress = Database.getValue(RECEIVING_ADDRESSES_TABLE, user, null);
        if (receivingAddress == null) return;
        Process process = new ProcessBuilder("dash-cli", "getreceivedbyaddress", receivingAddress, "50").start();
        String s = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
        if (s == null || s.startsWith("e")) return;
        String previous = Database.getValue(BALANCES_TABLE, user, "0");
        Double addToBalance = Double.parseDouble(s) - Double.parseDouble(previous);
        if (addToBalance < .00001) return;
        Database.setValue(RECEIVED_TABLE, user, s);
        Database.setValue(BALANCES_TABLE, user, String.valueOf(Double.valueOf(previous) + addToBalance));
    }
}
