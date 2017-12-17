package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.command.AbstractCommand;
import com.github.nija123098.tipbot.Bot;
import com.github.nija123098.tipbot.command.Command;
import com.github.nija123098.tipbot.utility.Database;
import com.github.nija123098.tipbot.utility.FormatHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import com.github.nija123098.tipbot.utility.TransactionLog;
import com.github.nija123098.tipbot.utility.Unit;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

import java.io.IOException;
import java.util.*;

import static com.github.nija123098.tipbot.utility.Database.ANNOUNCEMENT_CHANNEL;
import static com.github.nija123098.tipbot.utility.Database.BALANCES;

public class TipCommand extends AbstractCommand {
    @Override
    public List<String> getNames() {
        return Arrays.asList("tip", "give", "gift", "donate", "grant");
    }

    @Override
    public String getHelp() {
        return "Tip a user an amount of Dash which they may withdraw.";
    }

    @Override
    public String getFullHelp() {
        return "Tip a user an amount of Dash which they may withdraw.\n" +
                "You may specify a Dash, USD, beer, or other amount.\n" +
                "Examples:\n" +
                "    " + Bot.DISCORD_CLIENT.getOurUser().mention() + "tip " + Bot.MAINTAINER.mention() + " a " + Unit.COFFEE.name().toLowerCase() + "\n" +
                "    " + Bot.DISCORD_CLIENT.getOurUser().mention() + "tip " + Bot.MAINTAINER.mention() + " 2 " + Unit.GBP + "\n" +
                "    " + Bot.DISCORD_CLIENT.getOurUser().mention() + "tip " + Bot.MAINTAINER.mention() + " 1 " + Unit.USD + "\n" +
                "    " + Bot.DISCORD_CLIENT.getOurUser().mention() + "tip " + Bot.MAINTAINER.mention() + " .01 Dash";
    }

    @Override
    public Command getCommand() {
        return ((invoker, arguments, channel) -> {
            BalanceCommand.update(invoker);
            List<IUser> recipients = new ArrayList<>();
            IUser user;
            Double amount = null;
            Unit unit = null;
            for (String argument : arguments) {
                user = FormatHelper.getUserFromMention(argument);
                if (user != null) recipients.add(user);
                if (unit == null) unit = Unit.getUnitForName(argument);
                else return "Too many units are specified.";
                if (amount == null) amount = FormatHelper.softParseDouble(argument);
                else return "Too many amounts specified";
            }
            if (amount == null) amount = 1D;
            if (unit == null) {
                if (amount < Unit.USD.getDashAmount() * 5) unit = Unit.DASH;
                else return "Please specify a unit to tip in.";
            }
            if (recipients.isEmpty()) return "Please specify a user by mentioning him or her, then a number and unit.";
            for (IUser recipient : recipients) {
                if (recipient.equals(invoker)) return "You can't tip yourself.";
                if (recipient.isBot()) return "You can't tip a bot";
            }
            String ret = completeTransaction(invoker, channel, amount, unit, recipients);
            return ret == null ? Bot.OK_HAND : ret;
        });
    }

    private static final Map<Long, Double> TIP_AMOUNT = new HashMap<>();
    private static final Map<Long, IMessage> TALLY_MESSAGE = new HashMap<>();

    public static void handleReaction(ReactionAddEvent event) throws IOException {
        String[] name = event.getReaction().getEmoji().getName().split("_");
        double amount;
        try {
            amount = Double.parseDouble(name[1]);
        } catch (NumberFormatException ignored) {
            return;
        }
        Unit unit = Unit.getUnitForName(name[2]);
        if (unit == null) return;
        if (completeTransaction(event.getAuthor(), event.getChannel(), amount, unit, Collections.singleton(event.getMessage().getAuthor())) != null) {
            TIP_AMOUNT.compute(event.getMessageID(), (aLong, aDouble) -> (aDouble == null ? 0 : aDouble) + amount * unit.getDashAmount());
            if (event.getMessage().getReactions().stream().map(IReaction::getCount).reduce((integer, integer2) -> integer + integer2).orElse(0) > 3) {
                RequestBuffer.request(() -> {
                    TALLY_MESSAGE.computeIfAbsent(event.getMessageID(), aLong -> event.getChannel().sendMessage("Tallying tips for " + event.getAuthor().mention() + "'s message"));
                    RequestBuffer.request(() -> TALLY_MESSAGE.get(event.getMessageID()).edit("A total of " + TIP_AMOUNT.get(event.getMessageID()) + " Dash has been tipped to " + event.getAuthor().mention()));
                });
            }
            TIP_AMOUNT.compute(event.getMessageID(), (aLong, aDouble) -> (aDouble == null ? 0 : aDouble) + amount * unit.getDashAmount());
        }
    }

    private static String completeTransaction(IUser invoker, IChannel channel, double amount, Unit unit, Collection<IUser> recipients) throws IOException {
        double currentWallet = Double.parseDouble(Database.getValue(BALANCES, invoker, "0"));
        double tipAmount = unit.getDashAmount() * amount;
        if (tipAmount * recipients.size() > currentWallet) return "You don't have enough for that.";
        Database.setValue(BALANCES, invoker, String.valueOf(currentWallet - tipAmount * recipients.size()));
        for (IUser recipient : recipients) {
            Database.setValue(BALANCES, recipient, String.valueOf(Double.valueOf(Database.getValue(BALANCES, recipient, "0")) + tipAmount));
            TransactionLog.log("tip of " + tipAmount + " from " + invoker.getStringID() + " to " + recipient.getStringID());
            tipAnnounce(invoker, recipient, channel, amount, unit);
        }
        return null;
    }

    private static void tipAnnounce(IUser invoker, IUser recipient, IChannel channel, double amount, Unit unit) {
        if (channel.isPrivate()) return;
        String channelID = Database.getValue(ANNOUNCEMENT_CHANNEL, channel.getGuild(), "NULL");
        if (channelID.equals("NULL")) return;
        IChannel dest = channel.getGuild().getChannelByID(Long.parseLong(channelID));
        if (dest != null) dest.sendMessage(invoker.mention() + " tipped " + recipient.mention() + " " + unit.display(amount));
    }
}
