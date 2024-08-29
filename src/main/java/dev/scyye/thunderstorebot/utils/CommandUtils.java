package dev.scyye.thunderstorebot.utils;

import botcommons.commands.CommandInfo;
import botcommons.commands.GenericCommandEvent;
import botcommons.config.GuildConfig;
import com.google.gson.Gson;
import dev.scyye.thunderstoreapi.cache.CacheCollector;
import dev.scyye.thunderstorebot.Bot;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class CommandUtils {
	public static boolean checkExecution(GenericCommandEvent event) {
		if (!event.isGuild() && !CommandInfo.from(event).permission.equals("owner"))
			return false;
		return checkExecute(event, e ->
				!Arrays.stream(GuildConfig.fromGuildId(e.getGuild().getId()).get("disabledChannels", String[].class)).toList().contains(e.getChannel().getId())
						&& !Arrays.stream(GuildConfig.fromGuildId(e.getGuild().getId()).get("disabledUsers", String[].class)).toList().contains(e.getUser().getId()));
	}

	public static boolean checkExecute(GenericCommandEvent event, Predicate<GenericCommandEvent> predicate) {
		if (predicate.test(event))
			return true;
		event.replyError("You do not have permission to use this command. " +
				"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
		return false;
	}

	public static boolean validateCommunity(GenericCommandEvent event, String community) {
		if (community == null || community.isEmpty()) {
			event.reply("No community provided. How did you even get here?");
			return true;
		}

		community = MarkdownSanitizer.sanitize(community.toLowerCase().replace(" ", "-"));

		final String finalCommunity = community;
		boolean success = Arrays.stream(Bot.bot.tsja.getCommunities())
				.anyMatch(c -> c.getIdentifier().equals(finalCommunity));

		if (!success)
			event.reply("Invalid Community. Do `/community all` for a list of valid communities.");

		return !success;
	}


	public static List<String> getCommunityAutocomplete(String query) {
		List<String> results = new ArrayList<>();
		List<String> communities = CacheCollector.getCommunities();
		String lowerCaseQuery = query.toLowerCase();
		communities.stream()
				.filter(c -> c.toLowerCase().startsWith(lowerCaseQuery) || c.toLowerCase().contains(lowerCaseQuery))
				.distinct()
				.limit(25)
				.forEach(results::add);
		return results.stream().toList();
	}

	public static String sanitizeAndReplace(String input) {
		return (input == null || input.isEmpty()) ? null:MarkdownSanitizer.sanitize(input).replace(" ", "_");
	}
}
