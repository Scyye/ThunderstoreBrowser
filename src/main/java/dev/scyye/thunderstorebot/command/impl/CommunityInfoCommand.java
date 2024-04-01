package dev.scyye.thunderstorebot.command.impl;

import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.CommandInfo;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstorebot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Arrays;

@Command(name = "community-info", help = "Get information about a community")
public class CommunityInfoCommand implements ICommand {
	@Override
	public void handle(GenericCommandEvent genericCommandEvent) {
		Community community = Arrays.stream(Bot.bot.tsja.getCommunities()).filter(
				community1 -> community1.getIdentifier().equalsIgnoreCase(genericCommandEvent.getArg(0, String.class))
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
				""".formatted
				(community.getName(), community.getIdentifier(), community.getIdentifier(), community.getDiscordUrl(), community.getWikiUrl()));
	}

	@Override
	public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
		event.replyChoiceStrings(Arrays.stream(Bot.bot.tsja.getCommunities()).map(Community::getIdentifier).filter(
				s -> s.contains(event.getFocusedOption().getValue())
		).limit(25).toList()).queue();
	}

	@Override
	public CommandInfo.Option[] getArguments() {
		return new CommandInfo.Option[]{
				CommandInfo.Option.required("community", "The community to get information about", OptionType.STRING, true)
		};
	}
}
