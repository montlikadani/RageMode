package hu.montlikadani.ragemode.gameUtils;

public enum GameType {

	NORMAL, APOCALYPSE("apocalypse", "zombieapocalypse", "zombie");

	private String name;
	private String[] names;

	GameType() {
		this("");
	}

	GameType(String... names) {
		this.name = toString().toLowerCase();
		this.names = names;
	}

	public String getName() {
		return name;
	}

	public String[] getNames() {
		return names;
	}

	public static GameType getByName(String name) {
		for (GameType type : GameType.values()) {
			if (type.name.equalsIgnoreCase(name)) {
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
