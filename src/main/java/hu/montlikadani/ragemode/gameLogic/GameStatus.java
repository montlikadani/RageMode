package hu.montlikadani.ragemode.gameLogic;

/**
 * Represents the status of the game
 */
public enum GameStatus {
	/**
	 * When the game is running
	 */
	RUNNING,
	/**
	 * When the players who joined to the waiting game (lobby)
	 */
	WAITING,
	/**
	 * At the game end when calculating the winner player.
	 */
	WINNER_CALCULATING,
	/**
	 * When the game has end
	 */
	GAMEFREEZE,
	/**
	 * When the game has stopped
	 */
	STOPPED,
	/**
	 * When the game is ready to start
	 */
	READY,
	/**
	 * When the game is not ready to start
	 */
	NOTREADY;
}