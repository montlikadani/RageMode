package hu.montlikadani.ragemode.items.shop;

public enum NavigationType {

	WITHOUT,
	MAIN,
	CLOSE;

	public static NavigationType getByName(String name) {
		for (NavigationType type : values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}

		return null;
	}
}
