package hu.montlikadani.ragemode.scores;

public enum KilledWith {

	RAGEBOW, COMBATAXE, RAGEKNIFE, EXPLOSION, GRENADE;

	private String name;

	KilledWith() {
		this.name = toString().toLowerCase();
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
