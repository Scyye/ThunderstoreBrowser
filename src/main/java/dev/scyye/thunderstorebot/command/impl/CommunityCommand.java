package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.*;
import botcommons.menu.Menu;
import botcommons.menu.types.PageMenu;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstoreapi.cache.CacheCollector;
import dev.scyye.thunderstorebot.Main;
import dev.scyye.thunderstorebot.utils.CommandUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
@CommandHolder(group = "community")
public class CommunityCommand {
	@Command(name = "info", help = "Get information about a community", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void info(GenericCommandEvent event,
							@Param(description = "The community to get information about", autocomplete = true) String community) {
		Community community1 = Arrays.stream(Main.instance.tsja.getCommunities()).filter(
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

	@Command(name = "list", help = "List all communities", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void list(GenericCommandEvent event) {
		event.deferReply();
		event.replyMenu("community-list").finish();
	}

	@Menu(id = "community-list")
	public static class CommunityListMenu extends PageMenu {
		@Override
		public List<EmbedBuilder> getPageData() {
			List<EmbedBuilder> embedBuilders = new ArrayList<>();

			// Add 25 communities per page, to the description of the embed
			List<Community> communities = Arrays.stream(Main.instance.tsja.getCommunities()).toList();
			for (int i = 0; i < communities.size(); i += 25) {
				EmbedBuilder embedBuilder = new EmbedBuilder();
				StringBuilder description = new StringBuilder();
				for (int j = i; j < i + 25 && j < communities.size(); j++) {
					Community community = communities.get(j);
					description.append("**").append(community.getName()).append("** (`")
							.append(community.getIdentifier()).append("`)\n");
				}
				embedBuilder.setTitle("Communities")
						.setDescription(description.toString())
						.setFooter("Total Communities: " + communities.size());
				embedBuilders.add(embedBuilder);
			}
			return embedBuilders;
		}
	}
}
