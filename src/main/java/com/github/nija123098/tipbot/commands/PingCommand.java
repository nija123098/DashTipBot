package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.Bot;
import com.github.nija123098.tipbot.command.Command;

public class PingCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Pong.";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> Bot.PONG;
    }
}
