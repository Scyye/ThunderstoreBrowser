package dev.scyye.thunderstorebot.command.impl;

import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.botcommons.menu.Menu;
import dev.scyye.botcommons.menu.impl.PageMenu;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstorebot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

@Command(name = "community-list", help = "List all communities")
@Menu(id = "community-list")
public class CommunityListCommand extends PageMenu implements ICommand {
	@Override
	public void handle(GenericCommandEvent genericCommandEvent) {
		genericCommandEvent.replyMenu("community-list");
	}

	@Override
	public List<EmbedBuilder> getPageData() {
		List<EmbedBuilder> embedBuilders = new ArrayList<>();
		final EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("Communities")
				.setDescription("List of all communities");

		final int pageSize = 10;
		int i = 0;

		EmbedBuilder currentEmbed = new EmbedBuilder(embedBuilder);
		for (Community community : Bot.bot.tsja.getCommunities()) {
			if (i >= pageSize) {
				embedBuilders.add(currentEmbed);
				currentEmbed = new EmbedBuilder(embedBuilder);
				i = 0;
			}
			currentEmbed.addField(community.getIdentifier(), "https://thunderstore.io/c/"+community.getIdentifier(), false);
			i++;
		}

		embedBuilders.add(currentEmbed);
		return embedBuilders;
	}
}
