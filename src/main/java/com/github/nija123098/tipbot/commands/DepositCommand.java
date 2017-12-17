package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.command.Command;
import com.github.nija123098.tipbot.utility.Database;
import com.github.nija123098.tipbot.utility.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.github.nija123098.tipbot.utility.Database.RECEIVING_ADDRESSES;

public class DepositCommand extends AbstractCommand {
    private static final int CONFIRMATIONS = Integer.parseInt(Config.REQUIRED_CONFIRMATIONS);

    @Override
    public String getHelp() {
        return "Deposits Dash into the bot's wallet to enable tipping.";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            String address = Database.getValue(RECEIVING_ADDRESSES, invoker, null);
            if (address == null) {
                Process process = new ProcessBuilder("dash-cli", "getnewaddress").start();
                address = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
                Database.setValue(RECEIVING_ADDRESSES, invoker, address);
            }
            return "Funds deposited to the following address will be attributed to you.\n" + address;
        };
    }
}
