package hu.montlikadani.ragemode.API.event;

import java.util.List;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class RMGameStopEvent extends BaseEvent {

	private Game game;
	private List<PlayerManager> players;

	public RMGameStopEvent(Game game, List<PlayerManager> players) {
		this.game = game;
		this.players = players;
	}

	public Game getGame() {
		return game;
	}

	public List<PlayerManager> getPlayers() {
		return players;
	}
}
