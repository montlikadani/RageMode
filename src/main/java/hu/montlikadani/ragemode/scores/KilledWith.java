package hu.montlikadani.ragemode.scores;

import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.items.Items;

public enum KilledWith {

	RAGEBOW("arrow-kill", 3), COMBATAXE("axe-kill", 0), RAGEKNIFE("knife-kill", 4), EXPLOSION("explosion-kill"),
	GRENADE("explosion", "grenade-kill", 1), RAGEARROW("explosion", "arrow-kill", 2), PRESSUREMINE("explosion", "", 6),
	UNKNOWN("unknown-weapon");

	private String metaName, killTextPath = "";
	private ItemStack item;

	KilledWith() {
	}

	KilledWith(String killTextPath) {
		this(killTextPath, -1);
	}

	KilledWith(String killTextPath, int itemIndex) {
		this("", killTextPath, itemIndex);
	}

	KilledWith(String metaName, String killTextPath, int itemIndex) {
		this.metaName = metaName;
		this.killTextPath = killTextPath;
		this.item = Items.getGameItem(itemIndex) == null ? new ItemStack(org.bukkit.Material.STONE)
				: Items.getGameItem(itemIndex).get();
	}

	public String getMetaName() {
		return metaName;
	}

	public String getKillTextPath() {
		return killTextPath;
	}

	public ItemStack asItemStack() {
		return item;
	}

	public static KilledWith getByName(String name) {
		for (KilledWith kw : values()) {
			if (kw.toString().equalsIgnoreCase(name)) {
				return kw;
			}
		}

		return UNKNOWN;
	}
}
