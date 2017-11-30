package com.github.nija123098.tipbot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

@FunctionalInterface
public interface Command {
    String invoke(IUser invoker, String[] arguments, IChannel channel) throws Exception;
}
