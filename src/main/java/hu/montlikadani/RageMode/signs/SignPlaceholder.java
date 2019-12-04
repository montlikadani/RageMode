package hu.montlikadani.ragemode.signs;

import java.util.ArrayList;
import java.util.List;

import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
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

			if (line.contains("%current-players%")) {
				line = line.replace("%current-players%", GameUtils.getStatus(game) == GameStatus.RUNNING
						|| GameUtils.getStatus(game) == GameStatus.WAITING && GameUtils.getGame(game).isGameRunning()
								? Integer.toString(GameUtils.getGame(game).getPlayers().size())
								: "0");
			}

			if (line.contains("%max-players%"))
				line = line.replace("%max-players%", Integer.toString(GetGames.getMaxPlayers(game)));

			if (line.contains("%running%")) {
				if (GameUtils.getStatus(game) == GameStatus.WAITING) {
					if (GameUtils.getGame(game).getPlayers().size() == GetGames.getMaxPlayers(game))
						line = line.replace("%running%", ConfigValues.getSignGameFull());
					else
						line = line.replace("%running%", ConfigValues.getSignGameWaiting());
				}

				line = line.replace("%running%",
						GameUtils.getStatus(game) == GameStatus.RUNNING && GameUtils.getGame(game).isGameRunning()
								? ConfigValues.getSignGameRunning()
								: GameUtils.getStatus(game) == GameStatus.NOTREADY ? ConfigValues.getSignGameLocked()
										: ConfigValues.getSignGameWaiting());
			}

			line = Utils.colors(line);

			variables.add(line);
		}

		return variables;
	}
}
