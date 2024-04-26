package dev.scyye.thunderstorebot.cache;

import dev.scyye.thunderstoreapi.api.entities.packages.PackageListing;
import dev.scyye.thunderstorebot.Bot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CacheCollector {
	public static HashMap<String, List<PackageListing>> authorPackageCache = new HashMap<>();
	public static HashMap<String, List<PackageListing>> communityPackageCache = new HashMap<>();
	public static HashMap<String, List<String>> communityAuthorCache = new HashMap<>();
	public static List<String> communityCache = new ArrayList<>();


	public static void init() {
		System.out.println("Initializing cache");
		var time = System.currentTimeMillis();
		authorPackageCache = new HashMap<>();
		communityPackageCache = new HashMap<>();
		communityAuthorCache = new HashMap<>();
		communityCache = new ArrayList<>();

		try {
			Path moo = Path.of("thunderstorebot-assets", "moo.dat");

			if (Files.notExists(moo))
				Files.createFile(moo);

			Files.write(moo, UUID.randomUUID().toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}




		var communities = Bot.bot.tsja.getCommunities();
		for (var community : communities) {
			communityCache.add(community.getIdentifier());
			PackageListing[] packages;
			try {
				packages = Bot.bot.tsja.getPackages(community.getIdentifier(), 0);
			} catch (Exception e) {
				continue;
			}
			for (var p : packages) {
				if (!authorPackageCache.containsKey(p.getOwner())) {
					authorPackageCache.put(p.getOwner(), new ArrayList<>());
				}

				if (!authorPackageCache.get(p.getOwner()).contains(p))
					authorPackageCache.get(p.getOwner()).add(p);

				if (!communityPackageCache.containsKey(community.getIdentifier())) {
					communityPackageCache.put(community.getIdentifier(), new ArrayList<>());
				}

				if (!communityPackageCache.get(community.getIdentifier()).contains(p))
					communityPackageCache.get(community.getIdentifier()).add(p);

				if (!communityAuthorCache.containsKey(community.getIdentifier())) {
					communityAuthorCache.put(community.getIdentifier(), new ArrayList<>());
				}

				if (!communityAuthorCache.get(community.getIdentifier()).contains(p.getOwner()))
					communityAuthorCache.get(community.getIdentifier()).add(p.getOwner());
			}
			System.out.println(STR."\{community.getName()} has \{packages.length} packages");
		}

		System.out.println(STR."Cache initialized in \{System.currentTimeMillis() - time}ms");
	}

	public static List<String> getCommunityAutocomplete(String query) {
		List<String> results = new ArrayList<>();
		// First add all communities that start with the query
		for (var community : communityCache) {
			if (community.startsWith(query)) {
				results.add(community);
			}
		}

		// Then add all communities that contain the query
		for (var community : communityCache) {
			if (community.contains(query) && !results.contains(community)) {
				results.add(community);
			}
		}

		return results.stream().limit(25).toList();
	}
}
