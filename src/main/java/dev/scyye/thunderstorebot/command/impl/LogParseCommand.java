package dev.scyye.thunderstorebot.command.impl;

import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.CommandInfo;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.botcommons.menu.Menu;
import dev.scyye.botcommons.menu.impl.PageMenu;
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

@Command(name = "logparse", help = "Parses a log file")
public class LogParseCommand implements ICommand {
	@Override
	public void handle(GenericCommandEvent event) {
		Message.Attachment attachment = event.getArg("attachment", Message.Attachment.class);
		// Get all files from the message
		if (attachment==null || !attachment.getFileName().toLowerCase().contains("output")) {
			event.reply("Please attach a valid LogOutput file.");
			return;
		}

		File file = attachment.getProxy().downloadToFile(new File(STR."logs\\\{UUID.randomUUID()}.log")).join();

		FileInfo fileInfo = getFileInfo(file);

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle(STR."\{fileInfo.gameName} - BepInEx v\{fileInfo.bepInVersion}")
				.setAuthor(STR."Unity \{fileInfo.unityVersion}")
				.addField("Plugin Count", String.valueOf(fileInfo.plugins.size()), true)
				.addField("Error Count", STR."\{fileInfo.errors.size()} (\{fileInfo.uniqueErrors.size()} unique errors)", true);

		for (var e : fileInfo.uniqueErrors) {
			embedBuilder.addField("Error", e, false);
		}
		embedBuilder.setDescription("Use /pluginlist to see all plugins");

		event.replyEmbed(embedBuilder);
	}

	@Override
	public CommandInfo.Option[] getArguments() {
		return new CommandInfo.Option[]{
			CommandInfo.Option.required("attachment", "The attachment to parse", OptionType.ATTACHMENT, false)
		};
	}

	@Command(name="pluginlist", help = "List all plugins")
	@Menu(id = "plugin-list")
	public static class PluginList extends PageMenu implements ICommand {
		FileInfo info;
		public PluginList() {

		}
		public PluginList(FileInfo fileInfo) {
			this.info = fileInfo;
		}

		@Override
		public void handle(GenericCommandEvent event) {
			event.deferReply();
			Message.Attachment attachment = event.getArg("attachment", Message.Attachment.class);
			if (attachment==null || !attachment.getFileName().toLowerCase().contains("output")) {
				event.reply("Please attach a valid LogOutput file.");
				return;
			}

			File file = attachment.getProxy().downloadToFile(new File(STR."logs\\\{UUID.randomUUID()}.log")).join();

			FileInfo fileInfo = getFileInfo(file);

			event.replyMenu("plugin-list", fileInfo);
		}

		@Override
		public CommandInfo.Option[] getArguments() {
			return new CommandInfo.Option[]{
				CommandInfo.Option.required("attachment", "The attachment to parse", OptionType.ATTACHMENT, false)
			};
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
		String bepInVersion;
		String gameName;
		String unityVersion;
		List<String> plugins;
		List<String> errors;
		List<String> uniqueErrors;
	}
}
