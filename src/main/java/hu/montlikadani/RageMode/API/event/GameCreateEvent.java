package hu.montlikadani.ragemode.API.event;

public class GameCreateEvent extends BaseEvent {

	private String game;
	private int maxPlayers;

	public GameCreateEvent(String game, int maxPlayers) {
		this.game = game;
		this.maxPlayers = maxPlayers;
	}

	public String getGame() {
		return game;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}
}
