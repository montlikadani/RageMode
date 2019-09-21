package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class ForceStarter {

	public static ItemStack getItem() {
		ItemStack grenade = new ItemStack(Material.valueOf(RageMode.getInstance().getConfiguration().getCfg().getString("items.force-start.item")));
		ItemMeta meta = grenade.getItemMeta();
		meta.setDisplayName(getName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.force-start.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(Utils.colorList(lore));

		grenade.setItemMeta(meta);
		return grenade;
	}

	public static String getName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.force-start.name", "");
		return !iname.equals("") ? RageMode.getLang().colors(iname) : org.bukkit.ChatColor.DARK_GREEN + "Force the game start";
	}
}
