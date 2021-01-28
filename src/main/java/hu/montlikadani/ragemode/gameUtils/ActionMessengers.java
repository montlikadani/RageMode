package hu.montlikadani.ragemode.gameUtils;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.modules.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.modules.ScoreTeam;
import hu.montlikadani.ragemode.gameUtils.modules.TabTitles;

public class ActionMessengers {

	private Game game;
	private UUID playerUUID;

	private final ScoreBoard gameBoard;
	private final ScoreTeam scoreTeam;

	public ActionMessengers(Game game, UUID playerUUID) {
		this.game = game;
		this.playerUUID = playerUUID;

		scoreTeam = new ScoreTeam(playerUUID);
		gameBoard = new ScoreBoard(getPlayer());
	}

	public Game getGame() {
		return game;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(playerUUID);
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

		Player player = getPlayer();
		if (player == null) {
			return;
		}

		List<String> tabHeader = RageMode.getInstance().getConfig().getStringList("game.tablist.list.header"),
				tabFooter = RageMode.getInstance().getConfig().getStringList("game.tablist.list.footer");

		String header = "", footer = "";
		int s = 0;
		for (String line : tabHeader) {
			s++;

			if (s > 1) {
				header += "\n\u00a7r";
			}

			header += line;
		}

		s = 0;

		for (String line : tabFooter) {
			s++;

			if (s > 1) {
				footer += "\n\u00a7r";
			}

			footer += line;
		}

		header = Utils.setPlaceholders(header, player);
		footer = Utils.setPlaceholders(footer, player);

		header = header.replace("%game-time%", time < -1 ? "0" : Utils.getFormattedTime(time));
		footer = footer.replace("%game-time%", time < -1 ? "0" : Utils.getFormattedTime(time));

		TabTitles.sendTabTitle(player, header, footer);
	}

	public void setScoreboard() {
		setScoreboard(-1);
	}

	public void setScoreboard(int time) {
		if (!ConfigValues.isScoreboardEnabled()) {
			return;
		}

		Player player = getPlayer();
		if (player == null) {
			return;
		}

		String boardTitle = ConfigValues.getSbTitle();
		if (!boardTitle.isEmpty()) {
			boardTitle = boardTitle.replace("%game-time%", time < -1 ? "0" : Utils.getFormattedTime(time));
			boardTitle = Utils.setPlaceholders(boardTitle, player);
			gameBoard.setTitle(player, boardTitle);
		}

		List<String> rows = RageMode.getInstance().getConfig().getStringList("game.scoreboard.content");
		if (!rows.isEmpty()) {
			int scores = rows.size();
			if (scores < 15) {
				for (int i = (scores + 1); i <= 15; i++) {
					gameBoard.resetScores(player, i);
				}
			}

			for (String row : rows) {
				row = row.replace("%game-time%", time < -1 ? "0" : Utils.getFormattedTime(time));
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

		Player player = getPlayer();
		if (player == null) {
			return;
		}

		String prefix = Utils.setPlaceholders(ConfigValues.getTabPrefix(), player);
		String suffix = Utils.setPlaceholders(ConfigValues.getTabSuffix(), player);

		scoreTeam.setTeam(prefix, suffix);
	}
}
