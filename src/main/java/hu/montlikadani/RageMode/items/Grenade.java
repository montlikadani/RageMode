package hu.montlikadani.ragemode.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;

public class Grenade {

	public static ItemStack getGrenade() {
		ItemStack grenade = new ItemStack(Material.EGG);
		ItemMeta meta = grenade.getItemMeta();
		meta.setDisplayName(getGrenadeName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.grenade.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(color(lore));

		grenade.setItemMeta(meta);
		return grenade;
	}

	private static List<String> color(List<String> lore) {
		List<String> clore = new ArrayList<>();
		for (String s : lore) {
			clore.add(RageMode.getLang().colors(s));
		}
		return clore;
	}

	public static String getGrenadeName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.grenade.name");
		return iname != null && !iname.equals("") ? RageMode.getLang().colors(iname) : org.bukkit.ChatColor.DARK_GRAY + "Grenade";
	}
}
