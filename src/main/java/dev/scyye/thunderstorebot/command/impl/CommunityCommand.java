package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.*;
import botcommons.menu.Menu;
import botcommons.menu.types.PageMenu;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstoreapi.cache.CacheCollector;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstorebot.utils.CommandUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommandHolder(group = "community")
public class CommunityCommand {
	@Command(name = "info", help = "Get information about a community")
	public static void info(GenericCommandEvent event,
							@Param(description = "The community to get information about", autocomplete = true) String community) {
		Community community1 = Arrays.stream(Bot.bot.tsja.getCommunities()).filter(
				community2 -> community2.getIdentifier()
						.equalsIgnoreCase(community)
		).findFirst().orElse(null);

		if (community1 == null) {
			event.reply("Community not found");
			return;
		}

		event.replySuccess("""
			Name: %s
			Identifier: %s
			Link: [Click here](https://thunderstore.io/c/%s)

			Discord: %s
			Wiki: %s

			Cached Package Count: %d
			""".formatted
				(community1.getName(), community1.getIdentifier(), community1.getIdentifier(),
						community1.getDiscordUrl(), community1.getWikiUrl(), CacheCollector.getPackagesByCommunity(community1.getIdentifier()).size()))
				.finish();
	}

	@AutoCompleteHandler("community info")
	public static void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
		event.replyChoiceStrings(
				CommandUtils.getCommunityAutocomplete(
						event.getFocusedOption().getValue()
				)
		).queue();
	}

	@Command(name = "list", help = "List all communities")
	public static void list(GenericCommandEvent event) {
		event.deferReply();
		event.replyMenu("community-list").finish();
	}

	@Menu(id = "community-list")
	public static class CommunityListMenu extends PageMenu {
		@Override
		public List<EmbedBuilder> getPageData() {
			List<EmbedBuilder> embedBuilders = new ArrayList<>();

			int page = 1;
			int onPage = 0;

			EmbedBuilder currentEmbed = new EmbedBuilder()
					.setTitle("Communities")
					.setDescription("List of all communities");

			currentEmbed.setTitle("Communities - Page %d".formatted(page));
			for (Community community : Bot.bot.tsja.getCommunities()) {
				if (onPage > 5) {
					currentEmbed.setTitle("Communities - Page %d".formatted(page));
					embedBuilders.add(currentEmbed);
					currentEmbed = new EmbedBuilder()
							.setTitle("Communities")
							.setDescription("List of all communities");
					onPage=0;
					page++;
				}
				currentEmbed.addField(community.getIdentifier(),
						"https://thunderstore.io/c/%s".formatted(community.getIdentifier()), false);
				onPage++;
			}

			embedBuilders.add(currentEmbed);
			return embedBuilders;
		}
	}
}
