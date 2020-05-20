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

		if (lines == null) {
			return variables;
		}

		GameStatus status = GameUtils.getGame(game).getStatus();

		for (String line : lines) {
			if (line.contains("%game%"))
				line = line.replace("%game%", game);

			if (line.contains("%current-players%")) {
				line = line.replace("%current-players%",
						status == GameStatus.RUNNING || status == GameStatus.WAITING
								? Integer.toString(GameUtils.getGame(game).getPlayers().size())
								: "0");
			}

			if (line.contains("%max-players%"))
				line = line.replace("%max-players%", Integer.toString(GetGames.getMaxPlayers(game)));

			if (line.contains("%running%")) {
				switch (status) {
				case WAITING:
					if (GameUtils.getGame(game).getPlayers().size() == GetGames.getMaxPlayers(game))
						line = line.replace("%running%", ConfigValues.getSignGameFull());
					else
						line = line.replace("%running%", ConfigValues.getSignGameWaiting());
					break;
				case RUNNING:
					if (GameUtils.getGame(game).isGameRunning()) {
						line = line.replace("%running%", ConfigValues.getSignGameRunning());
					}
					break;
				case NOTREADY:
					line = line.replace("%running%", ConfigValues.getSignGameLocked());
					break;
				case READY:
				case STOPPED:
					line = line.replace("%running%", ConfigValues.getSignGameWaiting());
					break;
				default:
					break;
				}
			}

			line = Utils.colors(line);

			variables.add(line);
		}

		return variables;
	}
}
