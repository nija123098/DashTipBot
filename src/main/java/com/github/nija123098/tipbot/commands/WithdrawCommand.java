package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Command;
import com.github.nija123098.tipbot.Database;
import com.github.nija123098.tipbot.utility.TransactionLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.github.nija123098.tipbot.Database.BALANCES_TABLE;

public class WithdrawCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return null;
    }
    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            if (arguments.length == 0) return "Please specify an amount to withdraw and the address to send it to.";
            BalanceCommand.update(invoker);
            double currentWallet = Double.parseDouble(Database.getValue(BALANCES_TABLE, invoker, "0"));
            double withdrawAmount;
            boolean firstAmount = true;
            try {
                withdrawAmount = Double.parseDouble(arguments[0]);
            } catch (NumberFormatException ignored){
                try {
                    withdrawAmount = Double.parseDouble(arguments[1]);
                    firstAmount = false;
                } catch (NumberFormatException ignoredAgain){
                    return "Please specify an amount to withdraw and the address to send it to.";
                }
            }
            if (currentWallet < withdrawAmount) return "Your balance is not that high.";
            TransactionLog.log("withdrawing " + withdrawAmount + " for user " + invoker.getStringID());
            return sendMoney(arguments[firstAmount ? 1 : 0], withdrawAmount, invoker.getStringID());
        };
    }
    private static String sendMoney(String address, Double amount, String userID) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("dash-cli", "sendtoaddress", "\"" + address + "\"", "Standard Withdraw", userID, String.valueOf(amount), "true").start();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        process.waitFor();
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = inputReader.readLine()) != null) builder.append(line).append("\n");
        builder.append("\n\n");
        while ((line = errorReader.readLine()) != null) builder.append(line).append("\n");
        return builder.toString();
    }
}
