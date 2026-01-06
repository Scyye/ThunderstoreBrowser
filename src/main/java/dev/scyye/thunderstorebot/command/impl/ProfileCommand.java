package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.Command;
import botcommons.commands.CommandHolder;
import botcommons.commands.GenericCommandEvent;
import botcommons.commands.Param;
import botcommons.menu.Menu;
import botcommons.menu.types.PageMenu;
import dev.scyye.Client;
import dev.scyye.DataObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("unused")
@CommandHolder(group = "profile")
public class ProfileCommand {

	@Menu(id="modlist-menu")
	public static class ModlistMenu extends PageMenu {

		public ModInfo[] result;
		public String search;
		public String community;

		public ModlistMenu() {

		}

		public ModlistMenu(ModInfo[] result, String search, String community) {
			this.result = result;
			this.search = search;
			this.community = community;
		}

		@Override
		public List<EmbedBuilder> getPageData() {
			return buildPages(result, search, community);
		}

		private LinkedList<EmbedBuilder> buildPages(ModInfo[] result, String search, String community) {
			LinkedList<EmbedBuilder> pages = new LinkedList<>();

			String footer = "Community: %s".formatted(community);
			if (!"null".equals(search))
				footer += "\nSearch: %s".formatted(search);

			int totalPages = Math.max(1, (result.length + 4) / 5);
			EmbedBuilder currentPage =
					new EmbedBuilder()
							.setTitle("Mods Page 1/%d".formatted(totalPages))
							.setFooter(footer)
							.setColor(0x00ff00);

			int onPage = 0;
			int page = 1;
			for (ModInfo p : result) {
				if (onPage == 5) {
					onPage = 0;
					pages.add(currentPage);
					page = page + 1;
					currentPage = new EmbedBuilder()
							.setTitle("Mods Page %d/%d".formatted(page, totalPages))
							.setFooter(footer)
							.setColor(0x00ff00);
				}
				String packageName = MarkdownUtil.underline(p.deprecated ? "~~%s~~".formatted(p.name) : p.name);
				String ownerLink = MarkdownUtil.maskedLink(p.owner, "https://thunderstore.io/c/%s/p/%s/".formatted(community, p.name));
				String downloadLink = MarkdownUtil.maskedLink("here", "<%s>".formatted(p.downloadUrl));

				currentPage.addField(packageName, """
    Page: %s
    Created By: %s
    Download: %s
    """.formatted(p.packageUrl, ownerLink, downloadLink), false);

				onPage++;
			}
			pages.add(currentPage);

			return pages;
		}

		public static class ModInfo {
			public final String name;
			public final String owner;
			public final boolean deprecated;
			public final String packageUrl;
			public final String downloadUrl;

			public ModInfo(String name, String owner, boolean deprecated, String packageUrl, String downloadUrl) {
				this.name = name;
				this.owner = owner;
				this.deprecated = deprecated;
				this.packageUrl = packageUrl;
				this.downloadUrl = downloadUrl;
			}
		}
	}

	@Command(name = "modlist", help = "Get all mods in a profile", userContext = {InteractionContextType.PRIVATE_CHANNEL, InteractionContextType.GUILD, InteractionContextType.BOT_DM})
	public static void modList(GenericCommandEvent event, @Param(description = "The profile to search") String profile) {
		Client client = new Client("https://thunderstore.io/api/experimental/");
		event.deferReply();
		// extract base64 payload after the "#r2modman\n" header and decode safely
		String data = client.getString("legacyprofile/get/%s".formatted(profile), new DataObject());
		final String prefix = "#r2modman\n";
		if (!data.startsWith(prefix)) {
			event.reply("Unexpected profile format.").finish();
			return;
		}

		String base64Part = data.substring(prefix.length()).trim();
// strip surrounding quotes if present
		if (base64Part.startsWith("\"") && base64Part.endsWith("\"") && base64Part.length() >= 2) {
			base64Part = base64Part.substring(1, base64Part.length() - 1).trim();
		}

		byte[] decoded;
		try {
			// use the MIME decoder to tolerate line breaks in the payload
			decoded = Base64.getMimeDecoder().decode(base64Part);
		} catch (IllegalArgumentException ex) {
			event.reply("Failed to decode profile data (invalid Base64).").finish();
			return;
		}



		Path zipPath = Path.of("thunderstorebot-assets", "profiles", "%s.zip".formatted(profile));
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(zipPath.toFile()))) {
			os.write(decoded);
		} catch (IOException e) {
			e.printStackTrace();
			event.reply("Failed to write profile zip.").finish();
			return;
		}


		// read export.r2x from the zip and parse simple YAML to extract mod names
		String r2xContent = null;
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().endsWith("export.r2x")) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int r;
					while ((r = zis.read(buf)) != -1) {
						baos.write(buf, 0, r);
					}
					r2xContent = baos.toString(StandardCharsets.UTF_8);
					break;
				}
				zis.closeEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (r2xContent == null) {
			event.reply("Could not find export.r2x inside the profile zip.").finish();
			return;
		}

		List<String> modNames = new ArrayList<>();
		String[] lines = r2xContent.split("\\R");
		boolean inMods = false;
		for (String line : lines) {
			String t = line.trim();
			if (t.startsWith("mods:")) {
				inMods = true;
				continue;
			}
			if (!inMods) continue;
			if (t.startsWith("- name:")) {
				String[] parts = t.split(":", 2);
				if (parts.length == 2) {
					String name = parts[1].trim();
					modNames.add(name);
				}
			}
		}

		// build ModInfo array
		ProfileCommand.ModlistMenu.ModInfo[] modInfos = new ProfileCommand.ModlistMenu.ModInfo[modNames.size()];
		for (int i = 0; i < modNames.size(); i++) {
			String name = modNames.get(i);
			String owner = name.contains("-") ? name.split("-", 2)[0] : "unknown";
			String community = "unknown";
			String packageUrl = "https://thunderstore.io/c/%s/p/%s/".formatted(community, name);
			String downloadUrl = "https://thunderstore.io/c/%s/p/%s/".formatted(owner, name);
			modInfos[i] = new ProfileCommand.ModlistMenu.ModInfo(name, owner, false, packageUrl, downloadUrl);
		}

		event.replyMenu("modlist-menu", new ModlistMenu(modInfos, "null", "unknown")).finish();
	}
}
