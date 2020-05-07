package hu.montlikadani.ragemode.scores;

import java.util.UUID;

public class PlayerPoints implements Comparable<PlayerPoints>, Cloneable {

	private UUID uuid;
	@Deprecated private String playerUUID;

	private int kills = 0;
	private int axeKills = 0;
	private int directArrowKills = 0;
	private int explosionKills = 0;
	private int knifeKills = 0;
	private int deaths = 0;
	private int axeDeaths = 0;
	private int directArrowDeaths = 0;
	private int explosionDeaths = 0;
	private int knifeDeaths = 0;
	private int currentStreak = 0;
	private int longestStreak = 0;
	private int wins = 0;
	private int games = 0;

	private double kd = 0d;

	private Integer points = Integer.valueOf(0);

	private boolean isWinner = false;

	public PlayerPoints(UUID uuid) {
		this.uuid = uuid;
	}

	@Deprecated
	public PlayerPoints(String playerUUID) {
		this.playerUUID = playerUUID;
	}

	@Deprecated
	public String getPlayerUUID() {
		return playerUUID;
	}

	public UUID getUUID() {
		return uuid;
	}

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public int getAxeKills() {
		return axeKills;
	}

	public void setAxeKills(int axeKills) {
		this.axeKills = axeKills;
	}

	public int getDirectArrowKills() {
		return directArrowKills;
	}

	public void setDirectArrowKills(int directArrowKills) {
		this.directArrowKills = directArrowKills;
	}

	public int getExplosionKills() {
		return explosionKills;
	}

	public void setExplosionKills(int explosionKills) {
		this.explosionKills = explosionKills;
	}

	public int getKnifeKills() {
		return knifeKills;
	}

	public void setKnifeKills(int knifeKills) {
		this.knifeKills = knifeKills;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public int getAxeDeaths() {
		return axeDeaths;
	}

	public void setAxeDeaths(int axeDeaths) {
		this.axeDeaths = axeDeaths;
	}

	public int getDirectArrowDeaths() {
		return directArrowDeaths;
	}

	public void setDirectArrowDeaths(int directArrowDeaths) {
		this.directArrowDeaths = directArrowDeaths;
	}

	public int getExplosionDeaths() {
		return explosionDeaths;
	}

	public void setExplosionDeaths(int explosionDeaths) {
		this.explosionDeaths = explosionDeaths;
	}

	public int getKnifeDeaths() {
		return knifeDeaths;
	}

	public void setKnifeDeaths(int knifeDeaths) {
		this.knifeDeaths = knifeDeaths;
	}

	public int getCurrentStreak() {
		return currentStreak;
	}

	public void setCurrentStreak(int currentStreak) {
		this.currentStreak = currentStreak;
	}

	public int getLongestStreak() {
		return longestStreak;
	}

	public void setLongestStreak(int longestStreak) {
		this.longestStreak = longestStreak;
	}

	public Integer getPoints() {
		return points;
	}

	public boolean hasPoints(int points) {
		return this.points >= points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public void addPoints(Integer points) {
		this.points += points;
	}

	public void takePoints(Integer points) {
		this.points -= points;
	}

	public boolean isWinner() {
		return isWinner;
	}

	public void setWinner(boolean isWinner) {
		this.isWinner = isWinner;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getWins() {
		return wins;
	}

	public void setGames(int games) {
		this.games = games;
	}

	public int getGames() {
		return games;
	}

	public void setKD(double kd) {
		this.kd = kd;
	}

	public double getKD() {
		return kd;
	}

	@Override
	public int compareTo(PlayerPoints anotherPlayerPoints) {
		return anotherPlayerPoints.getPoints().compareTo(points);
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return null;
	}
}
