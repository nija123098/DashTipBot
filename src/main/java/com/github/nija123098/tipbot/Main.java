package com.github.nija123098.tipbot;

import org.reflections.Reflections;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;
import com.github.nija123098.tipbot.utility.Config;
import com.github.nija123098.tipbot.utility.InputException;
import com.github.nija123098.tipbot.utility.WrappingException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static final String OK_HAND = "👌", PONG = "🏓";
    public static final String PREFIX = "~";
    private static final String BOT_MENTION;
    public static final IUser MAINTAINER;
    public static final IDiscordClient DISCORD_CLIENT;
    private static final Map<String, Command> COMMAND_MAP = new HashMap<>();
    public static final Map<String, String> HELP_MAP = new HashMap<>();
    public static final Map<String, String> FULL_HELP_MAP = new HashMap<>();
    static {
        crashStuffIfNecessary(Config::setUp);
        DISCORD_CLIENT = crashStuffIfNecessary(() -> new ClientBuilder().withToken(Config.TOKEN)).login();
        DISCORD_CLIENT.getDispatcher().registerListener(Main.class);
        crashStuffIfNecessary(() -> DISCORD_CLIENT.getDispatcher().waitFor(ReadyEvent.class));
        MAINTAINER = DISCORD_CLIENT.getUserByID(191677220027236352L);
        BOT_MENTION = DISCORD_CLIENT.getOurUser().mention();
        new Reflections("com.github.nija123098.tipbot.commands").getSubTypesOf(AbstractCommand.class).stream().map((clazz) -> {
            try{return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).forEach((command) -> {
            String name = command.getName();
            COMMAND_MAP.put(name, command.getCommand());
            HELP_MAP.put(name, command.getHelp());
            FULL_HELP_MAP.put(name, command.getFullHelp());
        });
    }
    public static void main(String[] args) {

    }
    @FunctionalInterface
    public interface Command {
        String invoke(IUser invoker, String[] arguments) throws Exception;
    }
    @EventSubscriber
    public static void handle(MessageReceivedEvent event){
        if (event.getAuthor().isBot()) return;
        String content = event.getMessage().getContent();
        if (content.startsWith(PREFIX)) content = content.substring(PREFIX.length());
        else if (content.startsWith(BOT_MENTION)) content = content.substring(BOT_MENTION.length());
        else return;
        int length, newLength;
        do {
            length = content.length();
            content = content.replace("  ", " ");
            newLength = content.length();
        } while (length != newLength);
        if (content.startsWith(" ")) content = content.substring(1);
        String[] split = content.split(" ");
        Command command = COMMAND_MAP.get(split[0].toLowerCase());
        if (command == null) return;
        try {
            String ret = command.invoke(event.getAuthor(), Arrays.copyOfRange(split, 1, split.length));
            if (ret == null || ret.isEmpty()) return;
            if (ret.equals(OK_HAND) || ret.equals(PONG)) RequestBuffer.request(() -> event.getMessage().addReaction(ReactionEmoji.of(ret)));
            else RequestBuffer.request(() -> event.getChannel().sendMessage(ret));
        } catch (Exception e){
            if (e instanceof InputException) event.getChannel().sendMessage(e.getMessage());
            else {
                if (e instanceof WrappingException) e = (Exception) e.getCause();
                Exception finalE = e;
                RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage("Something went wrong while executing your command, I am notifying my maintainer now."));
                RequestBuffer.request(() -> MAINTAINER.getOrCreatePMChannel().sendMessage("You moron, I just caught a " + finalE.getClass().getSimpleName() + " due to " + finalE.getMessage()));
            }
        }
    }

    public static IUser getUserFromMention(String mention){
        if (!(mention.startsWith("<@") && mention.endsWith(">"))) throw new InputException("Please mention the user.");
        mention = mention.replace("!", "");
        try {
            return DISCORD_CLIENT.getUserByID(Long.parseLong(mention.substring(2, mention.length() - 1)));
        } catch (NumberFormatException e){
            throw new InputException("Please mention the user.");
        }
    }

    private static <E> E crashStuffIfNecessary(CrashFunction<E> function){
        try {
            return function.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private interface CrashFunction<E> {
        E run() throws Exception;
    }
}
