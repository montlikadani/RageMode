package hu.montlikadani.ragemode.gameUtils;

import java.util.List;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class ActionMessengers {

	private Game game;
	private List<PlayerManager> players;

	private TabTitles gameTab = null;
	private ScoreBoard gameBoard = null;
	private ScoreTeam scoreTeam = null;

	public ActionMessengers(Game game, List<PlayerManager> players) {
		this.game = game;
		this.players = players;
	}

	public Game getGame() {
		return game;
	}

	public List<PlayerManager> getPlayers() {
		return java.util.Collections.unmodifiableList(players);
	}

	public TabTitles getTabTitles() {
		return gameTab;
	}

	public ScoreBoard getScoreboard() {
		return gameBoard;
	}

	public ScoreTeam getScoreTeam() {
		return scoreTeam;
	}

	public void setTabList() {
		if (!ConfigValues.isTabEnabled()) {
			return;
		}

		if (players == null) {
			return;
		}

		if (gameTab == null) {
			gameTab = new TabTitles();
			gameTab.addToList(game.getName());
			return;
		}

		List<String> tabHeader = ConfigValues.getTabHeader();
		List<String> tabFooter = ConfigValues.getTabFooter();

		for (PlayerManager pm : players) {
			Player pl = pm.getPlayer();
			String he = "";
			int s = 0;

			for (String line : tabHeader) {
				s++;
				if (s > 1) {
					he = he +  "\n\u00a7r";
				}

				he = he + line;
			}

			String fo = "";
			s = 0;

			for (String line : tabFooter) {
				s++;
				if (s > 1) {
					fo = fo + "\n\u00a7r";
				}

				fo = fo + line;
			}

			he = Utils.setPlaceholders(he, pl);
			fo = Utils.setPlaceholders(fo, pl);

			he = he.replace("%game-time%", "");
			fo = fo.replace("%game-time%", "");

			gameTab.sendTabTitle(pl, he, fo);
		}
	}

	public void setScoreboard() {
		if (!ConfigValues.isScoreboardEnabled()) {
			return;
		}

		if (players == null) {
			return;
		}

		if (gameBoard == null) {
			gameBoard = new ScoreBoard(players);
			gameBoard.addToList(game.getName());
			return;
		}

		String boardTitle = ConfigValues.getSbTitle();
		List<String> rows = ConfigValues.getSbContent();
		for (Player pl : gameBoard.getPlayers()) {
			if (!boardTitle.isEmpty()) {
				gameBoard.setTitle(Utils.colors(boardTitle));
			}

			// should fix duplicated lines
			org.bukkit.scoreboard.Scoreboard sb = gameBoard.getScoreboards().get(pl).getScoreboard();
			for (String entry : sb.getEntries()) {
				sb.resetScores(entry);
			}

			if (rows != null && !rows.isEmpty()) {
				int rowMax = rows.size();

				for (String row : rows) {
					if (row.trim().isEmpty()) {
						for (int i = 0; i <= rowMax; i++) {
							row = row + " ";
						}
					}

					row = Utils.setPlaceholders(row, pl);
					row = row.replace("%game-time%", "");

					gameBoard.setLine(row, rowMax);
					rowMax--;

					gameBoard.setScoreBoard(pl);
				}
			}
		}
	}

	public void setTeam() {
		if (!ConfigValues.isTabFormatEnabled()) {
			return;
		}

		if (players == null) {
			return;
		}

		if (scoreTeam == null) {
			scoreTeam = new ScoreTeam();
			scoreTeam.addToList(game.getName());
			return;
		}

		String prefix = ConfigValues.getTabPrefix();
		String suffix = ConfigValues.getTabSuffix();

		for (PlayerManager pm : players) {
			Player pl = pm.getPlayer();

			prefix = Utils.setPlaceholders(prefix, pl);
			suffix = Utils.setPlaceholders(suffix, pl);

			scoreTeam.setTeam(pl, prefix, suffix);
		}
	}
}