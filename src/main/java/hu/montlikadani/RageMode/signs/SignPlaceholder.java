package hu.montlikadani.ragemode.signs;

import java.util.ArrayList;
import java.util.List;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.Game;
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

	protected List<String> parsePlaceholder(String game) {
		List<String> variables = new ArrayList<>();

		for (String line : lines) {
			if (line.contains("%game%"))
				line = line.replace("%game%", game);

			if (line.contains("%current-players%"))
				line = line.replace("%current-players%", Integer.toString(Game.getPlayers().size()));

			if (line.contains("%max-players%"))
				line = line.replace("%max-players%", Integer.toString(GetGames.getMaxPlayers(game)));

			if (line.contains("%running%")) {
				if (GameUtils.getStatus() == GameStatus.WAITING) {
					if (Game.getPlayers().size() == GetGames.getMaxPlayers(game))
						line = line.replace("%running%",
								RageMode.getInstance().getConfiguration().getCV().getSignGameFull());
					else
						line = line.replace("%running%",
								RageMode.getInstance().getConfiguration().getCV().getSignGameWaiting());
				}

				line = line.replace("%running%",
						Game.isGameRunning(game)
								? RageMode.getInstance().getConfiguration().getCV().getSignGameRunning()
								: GameUtils.getStatus() == GameStatus.NOTREADY
										? RageMode.getInstance().getConfiguration().getCV().getSignGameLocked()
										: RageMode.getInstance().getConfiguration().getCV().getSignGameWaiting());
			}

			line = RageMode.getLang().colors(line);

			variables.add(line);
		}

		return variables;
	}
}
