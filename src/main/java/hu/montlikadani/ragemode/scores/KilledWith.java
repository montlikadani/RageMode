package hu.montlikadani.ragemode.scores;

public enum KilledWith {

	RAGEBOW("ragebow"), COMBATAXE("combataxe"), RAGEKNIFE("rageknife"), EXPLOSION("explosion"), GRENADE("grenade");

	private String name;

	KilledWith(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public KilledWith getByName(String name) {
		for (KilledWith kw : values()) {
			if (kw.getName().equalsIgnoreCase(name.trim())) {
				return kw;
			}
		}

		return null;
	}
}
