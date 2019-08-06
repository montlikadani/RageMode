package hu.montlikadani.ragemode.API.event;

import java.util.List;

public class GameStartEvent extends BaseEvent {

	private String game;
	private List<String> playerUUIDs;

	public GameStartEvent(String game, List<String> playerUUIDs) {
		this.game = game;
		this.playerUUIDs = playerUUIDs;
	}

	public String getGame() {
		return game;
	}

	public List<String> getPlayers() {
		return playerUUIDs;
	}
}
