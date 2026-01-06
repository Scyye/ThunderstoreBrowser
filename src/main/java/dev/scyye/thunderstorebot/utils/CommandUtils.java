package dev.scyye.thunderstorebot.utils;

import botcommons.commands.CommandInfo;
import botcommons.commands.GenericCommandEvent;
import dev.scyye.thunderstoreapi.cache.CacheCollector;
import dev.scyye.thunderstorebot.Main;
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
				!event.getConfig().get("disabledChannels", String.class).contains(e.getChannel().getId())
						&& !event.getConfig().get("disabledUsers", String.class).contains(e.getUser().getId()));
	}

	public static boolean checkExecute(GenericCommandEvent event, Predicate<GenericCommandEvent> predicate) {
		if (1+1==2)
			return true;
		if (predicate.test(event))
			return true;
		event.replyError("You do not have permission to use this command. \nIf you believe this is an error, please contact this server's staff, or <@553652308295155723>").finish();
		return false;
	}

	public static boolean validateCommunity(GenericCommandEvent event, String community) {
		if (community == null || community.isEmpty()) {
			event.replyError("No community provided. How did you even get here?").finish();
			return true;
		}

		community = MarkdownSanitizer.sanitize(community.toLowerCase().replace(" ", "-"));

		final String finalCommunity = community;
		boolean success = Arrays.stream(Main.instance.tsja.getCommunities())
				.anyMatch(c -> c.getIdentifier().equals(finalCommunity));

		if (!success)
			event.replyError("Invalid Community. Do `/community all` for a list of valid communities.").finish();

		return !success;
	}


	public static List<String> getCommunityAutocomplete(String query) {
		List<String> results = new ArrayList<>();
		List<String> communities = CacheCollector.getCommunities();
		String lowerCaseQuery = query.toLowerCase();
		communities.stream()
				.filter(c -> c.toLowerCase().startsWith(lowerCaseQuery) || Utils.loseEquals(c, lowerCaseQuery))
				.distinct()
				.limit(25)
				.forEach(results::add);
		return results.stream().toList();
	}

	public static String sanitizeAndReplace(String input) {
		return (input == null || input.isEmpty()) ? null:MarkdownSanitizer.sanitize(input).replace(" ", "_");
	}
}
