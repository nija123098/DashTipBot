package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.Bot;
import com.github.nija123098.tipbot.command.Command;
import com.github.nija123098.tipbot.utility.Database;

import static com.github.nija123098.tipbot.utility.Database.*;

public class TipAnnouncementsCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            if (channel.isPrivate()) return "You can only use this in a guild.";
            setValue(ANNOUNCEMENT_CHANNEL, channel.getGuild(), arguments.length > 0 && arguments[0].equalsIgnoreCase("reset") ? "NULL" : channel.getStringID());
            return Bot.OK_HAND;
        };
    }
}
