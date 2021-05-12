package hu.montlikadani.ragemode.utils.exception;

public final class GameRunningException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GameRunningException() {
	}

	public GameRunningException(String message) {
		super(message);
	}
}
