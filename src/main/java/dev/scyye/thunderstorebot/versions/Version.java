package dev.scyye.thunderstorebot.versions;

import java.util.*;

public class Version {
	public static List<Version> versions = new ArrayList<>();

	public String releaseDate;
	public String version;
	public String changelog;
	public boolean beta;

	public Version(String releaseDate, String version, String changelog, boolean beta) {
		this.releaseDate = releaseDate;
		this.version = version;
		this.changelog = changelog;
		this.beta = beta;
		versions.add(this);
	}
}
