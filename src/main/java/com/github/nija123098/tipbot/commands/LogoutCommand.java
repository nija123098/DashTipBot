package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.command.Command;
import com.github.nija123098.tipbot.Bot;
import sx.blah.discord.util.RequestBuffer;

public class LogoutCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Shuts down the bot";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            if (!invoker.equals(Bot.MAINTAINER))
                return "You do not have permission to use that command, only " + Bot.MAINTAINER.mention() + " does.";
            synchronized (Bot.SHUTTING_DOWN) {
                Bot.SHUTTING_DOWN.set(true);
            }
            RequestBuffer.request(Bot.DISCORD_CLIENT::dnd);
            while (true) {
                if (Bot.COMMANDS_OCCURRING.get() != 0) break;
                Thread.sleep(500);
            }
            Bot.DISCORD_CLIENT.logout();
            return null;
        };
    }
}
