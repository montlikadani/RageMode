package hu.montlikadani.ragemode.signs;

import java.util.ArrayList;
import java.util.List;

import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class SignPlaceholder {

	private List<String> lines;

	public SignPlaceholder(List<String> lines) {
		this.lines = lines;
	}

	public List<String> getLines() {
		return lines;
	}

	protected List<String> parsePlaceholder(String gameName) {
		List<String> variables = new ArrayList<>();

		if (lines == null) {
			return variables;
		}

		while (lines.size() < 4) {
			lines.add(""); // Sign lines should be 4
		}

		Game game = GameUtils.getGame(gameName);
		GameStatus status = game.getStatus();

		for (int i = 0; i < 5; i++) {
			String line = lines.get(i);

			if (line.contains("%game%"))
				line = line.replace("%game%", gameName);

			if (line.contains("%current-players%")) {
				line = line.replace("%current-players%",
						(status == GameStatus.RUNNING || status == GameStatus.WAITING)
								? Integer.toString(game.getPlayers().size())
								: "0");
			}

			if (line.contains("%max-players%"))
				line = line.replace("%max-players%", Integer.toString(game.maxPlayers));

			if (line.contains("%running%")) {
				switch (status) {
				case WAITING:
					line = line.replace("%running%",
							(game.getPlayers().size() == game.maxPlayers) ? ConfigValues.getSignGameFull()
									: ConfigValues.getSignGameWaiting());
					break;
				case RUNNING:
					if (game.isGameRunning()) {
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
