package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.command.Command;

public class TermsCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Displays the terms of using the bot.";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> "In no event shall the bot or any maintainer be responsible in the event of lost, stolen, misdirected, or any exchange of funds.";
    }
}
