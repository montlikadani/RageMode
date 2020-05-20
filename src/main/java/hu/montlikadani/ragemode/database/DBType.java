package hu.montlikadani.ragemode.database;

public enum DBType {

	MYSQL, SQLITE, YAML;

	public static boolean isDBWithNameExists(String name) {
		for (DBType type : values()) {
			if (type.name().equals(name.trim().toUpperCase())) {
				return true;
			}
		}

		return false;
	}
}
