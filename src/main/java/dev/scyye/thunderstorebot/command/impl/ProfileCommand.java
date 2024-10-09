package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.Command;
import botcommons.commands.CommandHolder;
import botcommons.commands.GenericCommandEvent;
import botcommons.commands.Param;
import dev.scyye.Client;
import dev.scyye.DataObject;
import jakarta.xml.bind.DatatypeConverter;

import java.io.*;
import java.nio.file.Path;

@SuppressWarnings("unused")
@CommandHolder(group = "profile")
public class ProfileCommand {

	@Command(name = "modlist", help = "Get all mods in a profile")
	public static void modList(GenericCommandEvent event, @Param(description = "The profile to search") String profile) throws FileNotFoundException {
		Client client = new Client("https://thunderstore.io/api/experimental/");
		event.deferReply();
		String data = client.getString("legacyprofile/get/%s".formatted(profile), new DataObject());
		String sanitized = data.substring("#r2modman\n".length(), data.length()-1);

		byte[] decoded = DatatypeConverter.parseBase64Binary(sanitized);
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(
					Path.of("thunderstorebot-assets", "profiles", "%s.zip".formatted(profile)).toFile()));
			os.write(decoded);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
