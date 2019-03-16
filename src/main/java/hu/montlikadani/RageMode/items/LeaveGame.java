package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;

public class LeaveGame {

	public static ItemStack getLeaveGameItem() {
		ItemStack grenade = new ItemStack(Material.valueOf(RageMode.getInstance().getConfiguration().getCfg().getString("items.leavegameitem.item")));
		ItemMeta meta = grenade.getItemMeta();
		meta.setDisplayName(getLeaveGameItemName());

		List<String> lore = RageMode.getInstance().getConfiguration().getCfg().getStringList("items.leavegameitem.lore");
		if (lore != null && !lore.isEmpty())
			meta.setLore(ItemUtil.color(lore));

		grenade.setItemMeta(meta);
		return grenade;
	}

	public static String getLeaveGameItemName() {
		String iname = RageMode.getInstance().getConfiguration().getCfg().getString("items.leavegameitem.name");
		return iname != null && !iname.equals("") ? RageMode.getLang().colors(iname) : org.bukkit.ChatColor.RED + "Exit";
	}
}