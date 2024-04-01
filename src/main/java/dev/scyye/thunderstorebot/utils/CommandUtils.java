package dev.scyye.thunderstorebot.utils;

import com.google.gson.Gson;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.config.GuildConfig;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstorebot.Bot;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.util.Arrays;
import java.util.function.Predicate;

public class CommandUtils {
	public static boolean checkExecution(GenericCommandEvent event) {
		Gson gson = new Gson();
		return checkExecute(event, e ->
				!Arrays.stream(gson.fromJson(GuildConfig.fromGuildId(e.getGuild().getId()).get("disabledChannels", String.class), String[].class)).toList().contains(e.getChannel().getId())
						&& !Arrays.stream(gson.fromJson(GuildConfig.fromGuildId(e.getGuild().getId()).get("disabledUsers", String.class), String[].class)).toList().contains(e.getUser().getId()));
	}

	public static boolean checkExecute(GenericCommandEvent event, Predicate<GenericCommandEvent> predicate) {
		if (predicate.test(event))
			return true;
		event.reply("You do not have permission to use this command. " +
				"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
		return false;
	}

	public static boolean validateCommunity(GenericCommandEvent event, String community) {
		if (community == null || community.isEmpty())
			event.reply("No community provided. How did you even get here?");

		community = MarkdownSanitizer.sanitize(community.toLowerCase().replace(" ", "-"));

		final String finalCommunity = community;
		boolean success = Arrays.stream(Bot.bot.tsja.getCommunities())
				.anyMatch(c -> c.getIdentifier().equals(finalCommunity));

		/*Arrays.stream(Bot.bot.tsja.getCommunities()).map(Community::getIdentifier).forEach(
				System.out::println
		);*/

		//System.out.println();System.out.println();System.out.println();System.out.println();

		//System.out.println(finalCommunity);



		if (!success)
			event.reply("Invalid Community. Do `/community all` for a list of valid communities.");

		return success;
	}

	public static String sanitizeAndReplace(String input) {
		return (input == null || input.isEmpty()) ? null:MarkdownSanitizer.sanitize(input).replace(" ", "_");
	}
}
