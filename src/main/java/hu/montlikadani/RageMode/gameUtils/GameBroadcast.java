package hu.montlikadani.ragemode.gameUtils;

import java.util.UUID;

import org.bukkit.Bukkit;

import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class GameBroadcast {

	public static void broadcastToGame(String game, String message) {
		String[] playersInGame = PlayerList.getPlayersInGame(game);
		int i = 0;
		int imax = playersInGame.length;

		while (i < imax) {
			if (playersInGame[i] != null)
				Bukkit.getPlayer(UUID.fromString(playersInGame[i])).sendMessage(message);
			i++;
		}
	}
}
