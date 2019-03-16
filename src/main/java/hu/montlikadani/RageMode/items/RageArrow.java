package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;

public class RageArrow {

	public static ItemStack getRageArrow() {
		ItemStack arrow = new ItemStack(Material.ARROW);
		ItemMeta meta = arrow.getItemMeta();
		meta.setDisplayName(getRageArrowName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.rageArrow.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(ItemUtil.color(lore));

		arrow.setItemMeta(meta);
		return arrow;
	}

	public static String getRageArrowName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.rageArrow.name");
		return iname != null && !iname.equals("") ? RageMode.getLang().colors(iname) : org.bukkit.ChatColor.GOLD + "RageArrow";
	}
}
