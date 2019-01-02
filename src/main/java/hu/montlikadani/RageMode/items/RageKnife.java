package hu.montlikadani.ragemode.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;

public class RageKnife {

	public static ItemStack getRageKnife() {
		ItemStack knife = new ItemStack(Material.SHEARS);
		ItemMeta meta = knife.getItemMeta();
		meta.setDisplayName(getRageKnifeName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.rageKnife.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(color(lore));

		knife.setItemMeta(meta);
		return knife;
	}

	private static List<String> color(List<String> lore) {
		List<String> clore = new ArrayList<>();
		for (String s : lore) {
			clore.add(RageMode.getLang().colors(s));
		}
		return clore;
	}

	public static String getRageKnifeName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.rageKnife.name");
		return iname != null && !iname.equals("") ? RageMode.getLang().colors(iname) : org.bukkit.ChatColor.GOLD + "RageKnife";
	}
}
