package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Command;

public class TermsCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Displays the terms of using the bot.";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> "In no event shall the bot be responsible in the event of lost, stolen or misdirected funds.";
    }
}
