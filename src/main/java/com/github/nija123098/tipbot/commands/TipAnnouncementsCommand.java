package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Command;
import com.github.nija123098.tipbot.Database;
import com.github.nija123098.tipbot.Main;

public class TipAnnouncementsCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            Database.setValue(Database.ANNOUNCEMENT_CHANNEL, channel.getGuild(), arguments.length > 0 && arguments[0].equalsIgnoreCase("reset") ? "NULL" : channel.getStringID());
            return Main.OK_HAND;
        };
    }
}
