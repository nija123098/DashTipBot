package com.github.nija123098.tipbot;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.command.Command;
import com.github.nija123098.tipbot.commands.HelpCommand;
import com.github.nija123098.tipbot.commands.TipCommand;
import com.github.nija123098.tipbot.utility.FormatHelper;
import org.reflections.Reflections;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.RequestBuffer;
import com.github.nija123098.tipbot.utility.Config;
import com.github.nija123098.tipbot.utility.WrappingException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Bot {
    public static final String OK_HAND = "👌", PONG = "🏓";
    public static final IUser MAINTAINER;
    public static final IDiscordClient DISCORD_CLIENT;
    public static final AtomicInteger COMMANDS_OCCURRING = new AtomicInteger();
    public static final AtomicBoolean SHUTTING_DOWN = new AtomicBoolean();
    private static final Map<String, Command> COMMAND_MAP = new HashMap<>();
    private static final ScheduledThreadPoolExecutor SCHEDULED_THREAD_EXECUTOR = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r, "Bot-Thread");
        thread.setDaemon(true);
        return thread;
    });

    static {
        try {
            Config.setUp();
            DISCORD_CLIENT = new ClientBuilder().withToken(Config.TOKEN).login();
            DISCORD_CLIENT.getDispatcher().registerListener(Bot.class);
            DISCORD_CLIENT.getDispatcher().waitFor(ReadyEvent.class);
            MAINTAINER = DISCORD_CLIENT.getUserByID(191677220027236352L);
            new Reflections("com.github.nija123098.tipbot.commands").getSubTypesOf(AbstractCommand.class).stream().map((clazz) -> {
                try {
                    return clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).forEach((command) -> command.getNames().forEach(name -> {
                COMMAND_MAP.put(name, command.getCommand());
                HelpCommand.HELP_MAP.put(name, command.getHelp());
                HelpCommand.FULL_HELP_MAP.put(name, command.getFullHelp());
            }));
        } catch (Exception e) {
            throw new Error("Exception during bot initialization", e);
        }
    }

    public static void initialize() {
        Discord4J.LOGGER.info("Bot initialization completed.");
    }

    @EventSubscriber
    public static void handle(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String content = FormatHelper.getCommandContent(event);
        if (content == null) return;
        synchronized (SHUTTING_DOWN) {
            if (SHUTTING_DOWN.get()) return;
            COMMANDS_OCCURRING.incrementAndGet();
        }

        if (!PermissionUtils.hasPermissions(event.getChannel(), DISCORD_CLIENT.getOurUser(), Permissions.SEND_MESSAGES, Permissions.READ_MESSAGES)) return;
        if (!PermissionUtils.hasPermissions(event.getChannel(), DISCORD_CLIENT.getOurUser(), Permissions.ADD_REACTIONS)) {
            RequestBuffer.request(() -> event.getChannel().sendMessage("I need to be able to send reactions in this channel in order to operate."));
            return;
        }

        content = FormatHelper.removeExtraSpace(content);
        String[] split = content.split(" ");
        Command command = COMMAND_MAP.get(split[0].toLowerCase());
        if (command == null) return;
        try {
            String ret = command.invoke(event.getAuthor(), Arrays.copyOfRange(split, 1, split.length), event.getChannel());
            if (!(ret == null || ret.isEmpty())) {
                if (ret.equals(OK_HAND) || ret.equals(PONG)) RequestBuffer.request(() -> event.getMessage().addReaction(ReactionEmoji.of(ret)));
                else RequestBuffer.request(() -> event.getChannel().sendMessage(ret));
            }
        } catch (Exception e) {
            issueReport(event, e instanceof WrappingException ? (Exception) e.getCause() : e);
        }
        COMMANDS_OCCURRING.decrementAndGet();
    }

    @EventSubscriber
    public static void handle(ReactionAddEvent event) {
        if (!event.getReaction().getEmoji().getName().startsWith("tip_")) return;
        SCHEDULED_THREAD_EXECUTOR.schedule(() -> {
            if (event.getReaction().getUserReacted(event.getUser())) try {
                TipCommand.handleReaction(event);
            } catch (IOException e) {
                issueReport(event, e);
            }
        }, 30, TimeUnit.SECONDS);
    }

    private static void issueReport(MessageEvent event, Exception e) {
        RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage("Something went wrong while executing your command, I am notifying my maintainer now."));
        RequestBuffer.request(() -> MAINTAINER.getOrCreatePMChannel().sendMessage("You moron, I just caught a " + e.getClass().getSimpleName() + " due to " + e.getMessage()));
        e.printStackTrace();
    }
}
