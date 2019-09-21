package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class RageKnife {

	public static ItemStack getItem() {
		ItemStack knife = new ItemStack(Material.SHEARS);
		ItemMeta meta = knife.getItemMeta();
		meta.setDisplayName(getName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.rageKnife.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(Utils.colorList(lore));

		knife.setItemMeta(meta);
		return knife;
	}

	public static String getName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.rageKnife.name", "");
		return !iname.equals("") ? RageMode.getLang().colors(iname) : org.bukkit.ChatColor.GOLD + "RageKnife";
	}
}
