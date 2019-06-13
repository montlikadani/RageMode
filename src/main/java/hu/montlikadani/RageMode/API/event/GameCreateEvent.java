package hu.montlikadani.ragemode.API.event;

public class GameCreateEvent extends BaseEvent {

	private String game;

	public GameCreateEvent(String game) {
		this.game = game;
	}

	public String getGame() {
		return game;
	}
}
