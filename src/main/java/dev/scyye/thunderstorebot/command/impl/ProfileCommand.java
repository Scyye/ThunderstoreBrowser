package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.Command;
import botcommons.commands.CommandHolder;
import botcommons.commands.GenericCommandEvent;
import botcommons.commands.Param;
import dev.scyye.Client;
import dev.scyye.DataObject;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstoreapi.api.Experimental;

@CommandHolder(group = "profile")
public class ProfileCommand {

	@Command(name = "modlist", help = "Get all mods in a profile")
	public static void modList(GenericCommandEvent event, @Param(description = "The profile to search") String profile) {
		Client client = new Client("https://thunderstore.io/api/experimental/");
		event.deferReply();
		client.get("legacyprofile/get/%s".formatted(profile), new DataObject());
	}
}
