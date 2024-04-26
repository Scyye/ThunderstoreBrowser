package dev.scyye.thunderstorebot.command.impl;

import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.CommandInfo;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.botcommons.menu.Menu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Command(name = "addinvite", help = "Add an invite to the bot", permission = "admin", category = "OWNER")
public class AddInviteCommand implements ICommand {
	@Override
	public void handle(GenericCommandEvent event) {
		try {
			String guild = event.getArg("guild", String.class);
			String guildId = guild.split(" ")[0];
			event.getJDA().getGuildById(guildId).retrieveInvites().queue(invites -> {
				event.getJDA().getGuildById(guildId).getTextChannels().get(0).createInvite().
						setMaxUses(1).queue(invite -> {
							event.replySuccess("Invite created: " + invite.getUrl());
						});
			});
		} catch (Exception e) {
			event.replyError("I don't know that guild!\nAre they in any guilds with me?");
		}
	}

	@Override
	public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
		event.replyChoiceStrings(
				event.getJDA().getGuilds().stream().map(guild -> STR."\{guild.getId()} (\{guild.getName()})").toList()
		).queue();
	}

	@Override
	public CommandInfo.Option[] getArguments() {
		return new CommandInfo.Option[]{
				CommandInfo.Option.required("guild", "The guild to get an invite to", OptionType.STRING, true)
		};
	}
}
