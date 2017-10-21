package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Database;
import com.github.nija123098.tipbot.Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.github.nija123098.tipbot.utility.DatabaseTables.RECEIVING_ADDRESSES_TABLE;

public class DepositCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Deposits Dash into the bot's wallet to enable tipping.";
    }

    @Override
    public Main.Command getCommand() {
        return (invoker, arguments) -> {
            String address = Database.getValue(RECEIVING_ADDRESSES_TABLE, invoker, null);
            if (address == null){
                Process process = new ProcessBuilder("client-cli", "getnewaddress").start();
                address = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
                Database.setValue(RECEIVING_ADDRESSES_TABLE, invoker, address);
            }
            return "Funds deposited to " + address + " will be attributed to you.";
        };
    }
}
