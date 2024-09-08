package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.*;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

@SuppressWarnings("unused")
@CommandHolder
public class AddInviteCommand {

	@Command(name = "addinvite", help = "Add an invite to the bot", permission = "owner", category = "OWNER")
	public static void addInvite(GenericCommandEvent event,
								 @Param(description = "The guild to get an invite to", autocomplete = true) String guild) {
		try {
			String guildId = guild.split(" ")[0];

			event.getJDA().getGuildById(guildId).getTextChannels()
					.getFirst().createInvite().
					setMaxUses(1).queue(invite ->
							event.replySuccess("Invite created: " + invite.getUrl()));
		} catch (Exception e) {
			event.replyError("I don't know that guild!\nAre they in any guilds with me?");
		}
	}

	@AutoCompleteHandler("addinvite")
	public static void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
		event.replyChoiceStrings(
				event.getJDA().getGuilds().stream().map(guild -> "%s (%s)".formatted(guild.getId(),guild.getName())).toList()
		).queue();
	}
}
