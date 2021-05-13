package hu.montlikadani.ragemode.signs;

import java.util.ArrayList;
import java.util.List;

import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
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

	protected List<String> parsePlaceholder(Game game) {
		List<String> variables = new ArrayList<>();

		if (lines == null) {
			return variables;
		}

		for (int i = 0; i < 4; i++) {
			if (i >= lines.size()) {
				break;
			}

			String line = lines.get(i);
			line = line.replace("%game%", game.getName());

			if (line.contains("%current-players%")) {
				line = line.replace("%current-players%",
						(game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.WAITING)
								? Integer.toString(game.getPlayers().size())
								: "0");
			}

			if (line.contains("%max-players%"))
				line = line.replace("%max-players%", Integer.toString(game.maxPlayers));

			if (line.contains("%running%")) {
				switch (game.getStatus()) {
				case WAITING:
					line = line.replace("%running%",
							(game.getPlayers().size() >= game.maxPlayers) ? ConfigValues.getSignGameFull()
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
			}

			variables.add(line = Utils.colors(line));
		}

		return variables;
	}
}
