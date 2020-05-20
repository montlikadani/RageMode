package hu.montlikadani.ragemode.gameLogic;

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