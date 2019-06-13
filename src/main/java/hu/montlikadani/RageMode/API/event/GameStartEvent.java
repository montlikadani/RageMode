package hu.montlikadani.ragemode.API.event;

public class GameStartEvent extends BaseEvent {

	private String game;
	private String[] players;

	public GameStartEvent(String game, String[] players) {
		this.game = game;
		this.players = players;
	}

	public String getGame() {
		return game;
	}

	public String[] getPlayers() {
		return players;
	}
}
