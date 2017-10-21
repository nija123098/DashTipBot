package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Main;

public class PingCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Pong.";
    }

    @Override
    public Main.Command getCommand() {
        return (invoker, arguments) -> Main.PONG;
    }
}
