package hu.montlikadani.ragemode.API.event;

import java.util.List;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class RMGameStopEvent extends GameEvent {

	private List<PlayerManager> players;

	public RMGameStopEvent(Game game, List<PlayerManager> players) {
		super(game);
		this.players = players;
	}

	public List<PlayerManager> getPlayers() {
		return players;
	}
}
