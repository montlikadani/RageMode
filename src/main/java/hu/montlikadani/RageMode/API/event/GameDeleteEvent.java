package hu.montlikadani.ragemode.API.event;

public class GameDeleteEvent extends BaseEvent {

	private String game;

	public GameDeleteEvent(String game) {
		this.game = game;
	}

	public String getGame() {
		return game;
	}
}
