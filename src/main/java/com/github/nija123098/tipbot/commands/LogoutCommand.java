package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Main;

public class LogoutCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Shuts down the bot";
    }

    @Override
    public Main.Command getCommand() {
        return (invoker, arguments) -> {
            if (!invoker.equals(Main.MAINTAINER)) return "You do not have permission to use that command, only " + Main.MAINTAINER.mention() + " does.";
            Main.DISCORD_CLIENT.logout();
            return Main.OK_HAND;
        };
    }
}
