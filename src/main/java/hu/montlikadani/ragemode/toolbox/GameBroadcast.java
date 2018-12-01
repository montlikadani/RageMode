package hu.montlikadani.ragemode.toolbox;

import hu.montlikadani.ragemode.gameLogic.PlayerList;
import org.bukkit.Bukkit;

import java.util.UUID;

public class GameBroadcast {

    public static void broadcastToGame(String game, String message) {
        String[] playersInGame = PlayerList.getPlayersInGame(game);
        int i = 0;
        int imax = playersInGame.length;

        while(i < imax) {
            if(playersInGame[i] != null)
                Bukkit.getPlayer(UUID.fromString(playersInGame[i])).sendMessage(message);
            i++;
        }
    }
}
