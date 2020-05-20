package hu.montlikadani.ragemode.gameUtils;

import java.util.List;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.modules.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.modules.ScoreTeam;
import hu.montlikadani.ragemode.gameUtils.modules.TabTitles;

public class ActionMessengers {

	private Game game;
	private Player player;

	private final TabTitles gameTab;
	private final ScoreBoard gameBoard = new ScoreBoard();
	private final ScoreTeam scoreTeam;

	public ActionMessengers(Game game, Player player) {
		this.game = game;
		this.player = player;

		gameTab = new TabTitles(player);
		scoreTeam = new ScoreTeam(player);

		gameBoard.loadScoreboard(player);
	}

	public Game getGame() {
		return game;
	}

	public Player getPlayer() {
		return player;
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
		setTabList(-1);
	}

	public void setTabList(int time) {
		if (!ConfigValues.isTabEnabled()) {
			return;
		}

		List<String> tabHeader = ConfigValues.getTabHeader(), tabFooter = ConfigValues.getTabFooter();

		String header = "", footer = "";
		int s = 0;
		for (String line : tabHeader) {
			s++;

			if (s > 1) {
				header = header + "\n\u00a7r";
			}

			header = header + line;
		}

		s = 0;

		for (String line : tabFooter) {
			s++;

			if (s > 1) {
				footer = footer + "\n\u00a7r";
			}

			footer = footer + line;
		}

		header = Utils.setPlaceholders(header, player);
		footer = Utils.setPlaceholders(footer, player);

		if (time > -1) {
			header = header.replace("%game-time%", Utils.getFormattedTime(time));
			footer = footer.replace("%game-time%", Utils.getFormattedTime(time));
		}

		gameTab.sendTabTitle(player, header, footer);
	}

	public void setScoreboard() {
		setScoreboard(-1);
	}

	public void setScoreboard(int time) {
		if (!ConfigValues.isScoreboardEnabled()) {
			return;
		}

		String boardTitle = ConfigValues.getSbTitle();
		if (!boardTitle.isEmpty()) {
			gameBoard.setTitle(player, Utils.colors(boardTitle));
		}

		List<String> rows = ConfigValues.getSbContent();
		if (!rows.isEmpty()) {
			int scores = rows.size();

			if (scores < 15) {
				for (int i = (scores + 1); i <= 15; i++) {
					gameBoard.resetScores(player, i);
				}
			}

			for (String row : rows) {
				if (time > -1) {
					row = row.replace("%game-time%", Utils.getFormattedTime(time));
				}
				row = Utils.setPlaceholders(row, player);

				gameBoard.setLine(player, row, scores--);
			}
		}

		gameBoard.setScoreBoard(player);
	}

	public void setTeam() {
		if (!ConfigValues.isTabFormatEnabled()) {
			return;
		}

		String prefix = ConfigValues.getTabPrefix(), suffix = ConfigValues.getTabSuffix();

		prefix = Utils.setPlaceholders(prefix, player);
		suffix = Utils.setPlaceholders(suffix, player);

		scoreTeam.setTeam(player, prefix, suffix);
	}
}
