package hu.montlikadani.ragemode.items;

import java.util.List;

import hu.montlikadani.ragemode.RageMode;

public class ItemUtil {

	public static List<String> color(List<String> lore) {
		List<String> clore = new java.util.ArrayList<>();
		for (String s : lore) {
			clore.add(RageMode.getLang().colors(s));
		}
		return clore;
	}
}