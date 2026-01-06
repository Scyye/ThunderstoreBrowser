package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.Command;
import botcommons.commands.CommandHolder;
import botcommons.commands.GenericCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;

@CommandHolder
public class ContextCommands {
	@Command(name = "List Mods", help = "Get all mods in a profile", userContext = {InteractionContextType.GUILD}, type = net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE)
	public static void modListContext(GenericCommandEvent event) {
		String message = event.getMessageContextEvent().getTarget().getContentRaw();
		ProfileCommand.modList(event, message);
	}
}
