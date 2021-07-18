package hu.montlikadani.ragemode.signs;

import java.util.ArrayList;
import java.util.List;

import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.utils.Utils;

public class SignPlaceholder {

	private List<String> lines;

	public SignPlaceholder(List<String> lines) {
		this.lines = lines;
	}

	public List<String> getLines() {
		return lines;
	}

	protected List<String> parsePlaceholder(BaseGame game) {
		List<String> variables = new ArrayList<>();

		if (lines == null) {
			return variables;
		}

		int size = lines.size();
		if (size == 0) {
			return variables;
		}

		int gamePlayersSize = game.getPlayers().size();

		for (int i = 0; i < 4; i++) {
			if (i >= size) {
				break;
			}

			String line = lines.get(i);
			line = line.replace("%game%", game.getName());

			if (line.indexOf("%current-players%") >= 0) {
				line = line.replace("%current-players%",
						(game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.WAITING)
								? Integer.toString(gamePlayersSize)
								: "0");
			}

			if (line.indexOf("%max-players%") >= 0)
				line = line.replace("%max-players%", Integer.toString(game.maxPlayers));

			switch (game.getStatus()) {
			case WAITING:
				line = line.replace("%running%", (gamePlayersSize >= game.maxPlayers) ? ConfigValues.getSignGameFull()
						: ConfigValues.getSignGameWaiting());
				break;
			case RUNNING:
				if (game.isRunning()) {
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

			variables.add(line = Utils.colors(line));
		}

		return variables;
	}
}
