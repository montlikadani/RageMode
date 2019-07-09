package hu.montlikadani.ragemode.signs;

import java.util.ArrayList;
import java.util.List;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class SignPlaceholder {

	private List<String> lines;

	public SignPlaceholder(List<String> lines) {
		this.lines = lines;
	}

	public List<String> getLines() {
		return lines;
	}

	public List<String> parsePlaceholder(String game) {
		List<String> variables = new ArrayList<>();

		for (String line : lines) {
			if (line.contains("%game%"))
				line = line.replace("%game%", game);

			if (line.contains("%current-players%"))
				line = line.replace("%current-players%", Integer.toString(PlayerList.getPlayersInGame(game).length));

			if (line.contains("%max-players%"))
				line = line.replace("%max-players%", Integer.toString(GetGames.getMaxPlayers(game)));

			if (line.contains("%running%")) {
				if (GameUtils.getStatus() == GameStatus.WAITING) {
					if (PlayerList.getPlayersInGame(game).length == GetGames.getMaxPlayers(game))
						line = line.replace("%running%", RageMode.getInstance().getConfiguration().getCfg().getString("signs.game.full"));
					else
						line = line.replace("%running%", RageMode.getInstance().getConfiguration().getCfg().getString("signs.game.waiting"));
				}
				line = line.replace("%running%", PlayerList.isGameRunning(game)
						? RageMode.getInstance().getConfiguration().getCfg().getString("signs.game.running")
						: GameUtils.getStatus() == GameStatus.NOTREADY ? RageMode.getInstance().getConfiguration()
								.getCfg().getString("signs.game.locked")
								: RageMode.getInstance().getConfiguration().getCfg().getString("signs.game.waiting"));
			}

			line = RageMode.getLang().colors(line);

			variables.add(line);
		}

		return variables;
	}
}
