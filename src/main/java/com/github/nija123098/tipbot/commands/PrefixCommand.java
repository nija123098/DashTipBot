package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.Bot;
import com.github.nija123098.tipbot.command.Command;
import com.github.nija123098.tipbot.utility.Database;
import sx.blah.discord.handle.obj.Permissions;

import static com.github.nija123098.tipbot.utility.Database.*;

public class PrefixCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Gets and sets the prefix.";
    }

    @Override
    public Command getCommand() {
        return (invoker, arguments, channel) -> {
            if (channel.isPrivate()) return "Prefixes in private channels are not necessary.";
            if (arguments.length != 0) {
                if (!(invoker.equals(Bot.MAINTAINER) || invoker.getPermissionsForGuild(channel.getGuild()).contains(Permissions.ADMINISTRATOR))) return "You must be an administrator to change the prefix";
                setValue(PREFIXES, channel.getGuild(), arguments[0]);
            }
            return "The prefix on this server is `" + getValue(PREFIXES, channel.getGuild(), "~") + "`";
        };
    }
}
