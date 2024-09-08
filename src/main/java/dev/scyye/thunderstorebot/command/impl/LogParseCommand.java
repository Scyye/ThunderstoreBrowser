package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.Command;
import botcommons.commands.CommandHolder;
import botcommons.commands.GenericCommandEvent;
import botcommons.commands.Param;
import botcommons.menu.Menu;
import botcommons.menu.types.PageMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@CommandHolder(group = "logparse")
public class LogParseCommand {
	@Command(name = "parseinfo", help = "Parses a log file")
	public static void parseInfo(GenericCommandEvent event,
								 @Param(description = "The log file to parse") Message.Attachment attachment) {
		FileInfo fileInfo = get(event, attachment);

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle(STR."\{fileInfo.gameName} - BepInEx v\{fileInfo.bepInVersion}")
				.setAuthor(STR."Unity \{fileInfo.unityVersion}")
				.addField("Plugin Count", String.valueOf(fileInfo.plugins.size()), true)
				.addField("Error Count", STR."\{fileInfo.errors.size()} (\{fileInfo.uniqueErrors.size()} unique errors)", true);

		for (var e : fileInfo.uniqueErrors) {
			embedBuilder.addField("Error", e, false);
		}
		embedBuilder.setDescription("Use /logparse pluginlist to see all plugins");

		event.replyEmbed(embedBuilder).finish();
	}

	static FileInfo get(GenericCommandEvent event, Message.Attachment attachment) {
		// Get all files from the message
		if (attachment==null || !attachment.getFileName().toLowerCase().contains("output")) {
			event.reply("Please attach a valid LogOutput file.").finish();
			return null;
		}

		return getFileInfo(attachment.getProxy().downloadToFile(new File(STR."thunderstorebot-assets\\logs\\\{UUID.randomUUID()}.log")).join());
	}

	@Command(name = "pluginlist", help = "List all plugins")
	public static void pluginList(GenericCommandEvent event,
								  @Param(description = "The attachment to parse", type = OptionType.ATTACHMENT) Message.Attachment attachment) {
		FileInfo fileInfo = get(event, attachment);
		event.replyMenu("plugin-list", fileInfo).finish();
	}

	@Menu(id = "plugin-list")
	public static class PluginList extends PageMenu {
		FileInfo info;
		public PluginList() {
		}
		public PluginList(FileInfo fileInfo) {
			this.info = fileInfo;
		}

		@Override
		public List<EmbedBuilder> getPageData() {
			List<EmbedBuilder> embeds = new ArrayList<>();
			for (int i = 0; i < info.plugins.size(); i+=10) {
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setTitle("Plugins");
				embedBuilder.setDescription(String.join("\n", info.plugins.subList(i, Math.min(i+10, info.plugins.size()))));
				embeds.add(embedBuilder);
			}
			return embeds;
		}
	}

	public static FileInfo getFileInfo(File file) {
		FileInfo result = new FileInfo();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}


		result.bepInVersion = lines.get(0).split(" ")[5];
		result.gameName = lines.get(0).split("-")[1].split(" ")[1];

		result.unityVersion = lines.get(1).split(" ")[10];
		int i = lines.indexOf("[Message:   BepInEx] Chainloader started");

		result.plugins = lines.subList(i - 1, lines.size() - 1)
				.stream()
				.filter(s -> s.startsWith("[Info   :   BepInEx] Loading"))
				.map(s -> String.join(" ", Arrays.copyOfRange(s.split(" "), 8, s.split(" ").length)))
				.toList();

		result.errors = lines.stream().filter(s -> s.startsWith("[Error"))
				.map(s -> String.join(" ", Arrays.copyOfRange(s.split(" "), 5, s.split(" ").length)))
				.toList();

		for (String error : result.errors) {
			if (result.uniqueErrors == null)
				result.uniqueErrors = new ArrayList<>();

			if (!result.uniqueErrors.contains(error))
				result.uniqueErrors.add(error);

		}

		return result;
	}

	public static class FileInfo {
		String bepInVersion = "Unknown";
		String gameName = "Unknown";
		String unityVersion = "Unknown";
		List<String> plugins = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		List<String> uniqueErrors = new ArrayList<>();
	}
}
