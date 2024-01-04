package dev.scyye.thunderstorebot.utils;

public class Utils {
	public static boolean containsOne(Object[] arr, Object[] arr2) {
		for (var s : arr) {
			for (var s2 : arr2) {
				if (s.equals(s2)) return true;
			}
		}
		return false;
	}
}
