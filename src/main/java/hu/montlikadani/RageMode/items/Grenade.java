package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class Grenade {

	public static ItemStack getItem() {
		ItemStack grenade = new ItemStack(Material.EGG);
		ItemMeta meta = grenade.getItemMeta();
		meta.setDisplayName(getName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.grenade.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(Utils.colorList(lore));

		grenade.setItemMeta(meta);
		return grenade;
	}

	public static String getName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.grenade.name");
		return iname != null && !iname.equals("") ? RageMode.getLang().colors(iname) : org.bukkit.ChatColor.DARK_GRAY + "Grenade";
	}
}