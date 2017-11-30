package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Command;
import com.github.nija123098.tipbot.Main;
import sx.blah.discord.util.RequestBuffer;

public class LogoutCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Shuts down the bot";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            if (!invoker.equals(Main.MAINTAINER)) return "You do not have permission to use that command, only " + Main.MAINTAINER.mention() + " does.";
            synchronized (Main.SHUTTING_DOWN){
                Main.SHUTTING_DOWN.set(true);
            }
            RequestBuffer.request(Main.DISCORD_CLIENT::dnd);
            while (true) if (Main.COMMANDS_OCCURRING.get() != 0) break;
            Main.DISCORD_CLIENT.logout();
            return Main.OK_HAND;
        };
    }
}
