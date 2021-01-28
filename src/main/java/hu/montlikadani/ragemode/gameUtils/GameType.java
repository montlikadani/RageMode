package hu.montlikadani.ragemode.gameUtils;

/**
 * Represents the type of the gane
 */
public enum GameType {

	/**
	 * Normal game, player vs player
	 */
	NORMAL,

	/**
	 * Zombie apocalypse game, zombies vs players
	 */
	APOCALYPSE("apocalypse", "zombieapocalypse", "zombie");

	private String[] names = new String[0];

	GameType() {
	}

	GameType(String... names) {
		this.names = names;
	}

	/**
	 * Gets the array sub names of this type.
	 * 
	 * @return array of string
	 */
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
