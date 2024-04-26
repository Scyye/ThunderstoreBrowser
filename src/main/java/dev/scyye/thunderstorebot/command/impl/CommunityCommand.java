package dev.scyye.thunderstorebot.command.impl;

import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.CommandInfo;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.botcommons.menu.Menu;
import dev.scyye.botcommons.menu.impl.PageMenu;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstorebot.cache.CacheCollector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command(name = "community", help = "Community commands", category = "COMMUNITY")
public class CommunityCommand implements ICommand {
	@Override
	public void handle(GenericCommandEvent genericCommandEvent) {
		genericCommandEvent.reply("Community commands: `/community info`, `/community list`");
	}

	@Command(name = "info", help = "Get information about a community")
	public static class CommunityInfoCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent genericCommandEvent) {
			Community community = Arrays.stream(Bot.bot.tsja.getCommunities()).filter(
					community1 -> community1.getIdentifier()
							.equalsIgnoreCase(genericCommandEvent.getArg("community", String.class))
			).findFirst().orElse(null);

			if (community == null) {
				genericCommandEvent.reply("Community not found");
				return;
			}


			genericCommandEvent.replySuccess("""
				Name: %s
				Identifier: %s
				Link: [Click here](https://thunderstore.io/c/%s)
				
				Discord: %s
				Wiki: %s
				
				Cached Package Count: %d
				""".formatted
					(community.getName(), community.getIdentifier(), community.getIdentifier(),
							community.getDiscordUrl(), community.getWikiUrl(), CacheCollector.communityPackageCache.get(community.getIdentifier()).size()));

		}

		@Override
		public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
			event.replyChoiceStrings(
					CacheCollector.getCommunityAutocomplete(
							event.getFocusedOption().getValue()
					)
			).queue();
		}

		@Override
		public CommandInfo.Option[] getArguments() {
			return new CommandInfo.Option[]{
					CommandInfo.Option.required("community", "The community to get information about", OptionType.STRING, true)
			};
		}
	}

	@Command(name = "list", help = "List all communities")
	@Menu(id = "community-list")
	public static class CommunityListCommand extends PageMenu implements ICommand {
		@Override
		public void handle(GenericCommandEvent genericCommandEvent) {
			genericCommandEvent.deferReply();
			genericCommandEvent.replyMenu("community-list");
		}

		@Override
		public List<EmbedBuilder> getPageData() {
			List<EmbedBuilder> embedBuilders = new ArrayList<>();

			int page = 1;
			int onPage = 0;

			EmbedBuilder currentEmbed = new EmbedBuilder()
					.setTitle("Communities")
					.setDescription("List of all communities");

			currentEmbed.setTitle("Communities - Page " + page);
			for (Community community : Bot.bot.tsja.getCommunities()) {
				if (onPage > 5) {
					currentEmbed.setTitle("Communities - Page " + page);
					embedBuilders.add(currentEmbed);
					currentEmbed = new EmbedBuilder()
							.setTitle("Communities")
							.setDescription("List of all communities");
					onPage=0;
					page++;
				}
				currentEmbed.addField(community.getIdentifier(),
						"https://thunderstore.io/c/"+community.getIdentifier(), false);
				onPage++;
			}

			embedBuilders.add(currentEmbed);
			return embedBuilders;
		}
	}
}
