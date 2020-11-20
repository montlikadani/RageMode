package hu.montlikadani.ragemode.gameUtils;

public enum GameType {

	NORMAL, APOCALYPSE("apocalypse", "zombieapocalypse", "zombie");

	private String[] names;

	GameType() {
		this("");
	}

	GameType(String... names) {
		this.names = names;
	}

	public String[] getNames() {
		return names;
	}

	public static GameType getByName(String name) {
		for (GameType type : GameType.values()) {
			if (type.toString().equalsIgnoreCase(name)) {
				return type;
			}

			for (String n : type.names) {
				if (n.equalsIgnoreCase(name)) {
					return type;
				}
			}
		}

		return null;
	}
}
