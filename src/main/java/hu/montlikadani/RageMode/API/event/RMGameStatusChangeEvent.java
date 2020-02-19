package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;

public class RMGameStatusChangeEvent extends GameEvent {

	private GameStatus status;

	public RMGameStatusChangeEvent(Game game, GameStatus status) {
		super(game);
		this.status = status;
	}

	public GameStatus getStatus() {
		return status;
	}
}
