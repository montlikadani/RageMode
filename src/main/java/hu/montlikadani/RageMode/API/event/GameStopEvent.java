package hu.montlikadani.ragemode.API.event;

import java.util.List;

public class GameStopEvent extends BaseEvent {

	private String game;
	private List<String> players;

	public GameStopEvent(String game, List<String> players) {
		this.game = game;
		this.players = players;
	}

	public String getGame() {
		return game;
	}

	public List<String> getPlayers() {
		return players;
	}
}
