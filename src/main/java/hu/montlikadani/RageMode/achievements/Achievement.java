package hu.montlikadani.ragemode.achievements;

public class Achievement {

	public enum AchievementReason {
		FIRST_KILL, FIRST_WIN, SWORDMASTER, EXPLOSIVE, SURVIVOR, BOOM, ONEHUNDRED, WIN, KILLS;
	}

	private String name;
	private AchievementReason reason;

	public String[] names = { "First Kill", "First Win", "Swordmaster", "Explosive", "Survivor", "Boom", "100 Percent", "Win", "Kills" };

	public Achievement(String name, AchievementReason reason) {
		this.name = name;
		this.reason = reason;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AchievementReason getReason() {
		return reason;
	}

	public void setReason(AchievementReason reason) {
		this.reason = reason;
	}
}
