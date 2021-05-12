package hu.montlikadani.ragemode.items;

import org.jetbrains.annotations.NotNull;

public enum GameItems {

	RAGEBOW("arrow-kill", 3), COMBATAXE("axe-kill", 0), RAGEKNIFE("knife-kill", 4), EXPLOSION("explosion-kill"),
	GRENADE("explosion", "grenade-kill", 1), RAGEARROW("explosion", "arrow-kill", 2), PRESSUREMINE("explosion", "", 6),
	UNKNOWN("unknown-weapon");

	private String metaName, killTextPath = "";
	private ItemHandler item;

	GameItems(String killTextPath) {
		this(killTextPath, -1);
	}

	GameItems(String killTextPath, int itemIndex) {
		this("", killTextPath, itemIndex);
	}

	GameItems(String metaName, String killTextPath, int itemIndex) {
		this.metaName = metaName;
		this.killTextPath = killTextPath;

		item = Items.getGameItem(itemIndex);
	}

	@NotNull
	public String getMetaName() {
		return metaName;
	}

	@NotNull
	public String getKillTextPath() {
		return killTextPath;
	}

	@NotNull
	public ItemHandler getItem() {
		return item;
	}

	@NotNull
	public static GameItems getByName(String name) {
		for (GameItems kw : values()) {
			if (kw.toString().equalsIgnoreCase(name)) {
				return kw;
			}
		}

		return UNKNOWN;
	}
}
