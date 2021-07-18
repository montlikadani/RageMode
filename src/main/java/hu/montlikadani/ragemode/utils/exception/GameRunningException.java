package hu.montlikadani.ragemode.utils.exception;

/**
 * {@code GameRunningException} is the class that can't be extended, can be
 * thrown when a game is running currently.
 * <p>
 * This class is extended to {@link RuntimeException}
 */
public final class GameRunningException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GameRunningException() {
	}

	public GameRunningException(String message) {
		super(message);
	}
}
