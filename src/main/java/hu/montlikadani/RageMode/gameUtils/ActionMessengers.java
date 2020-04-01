package hu.montlikadani.ragemode.gameUtils;

import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class ActionMessengers {

	private Game game;
	private List<PlayerManager> players;

	private final TabTitles gameTab = new TabTitles();
	private final ScoreBoard gameBoard = new ScoreBoard();
	private final ScoreTeam scoreTeam = new ScoreTeam();

	public ActionMessengers(Game game, List<PlayerManager> players) {
		this.game = game;
		this.players = players;

		scoreTeam.addToList(game.getName());
		gameTab.addToList(game.getName());
		gameBoard.addToList(game.getName());

		gameBoard.loadScoreboard(players);
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
		setTabList(-1);
	}

	public void setTabList(int time) {
		if (game == null) {
			return;
		}

		if (players == null) {
			return;
		}

		if (!ConfigValues.isTabEnabled()) {
			return;
		}

		List<String> tabHeader = ConfigValues.getTabHeader();
		List<String> tabFooter = ConfigValues.getTabFooter();

		for (Iterator<PlayerManager> it = players.iterator(); it.hasNext();) {
			Player pl = it.next().getPlayer();
			if (!GameUtils.isPlayerPlaying(pl)) {
				it.remove();
				continue;
			}

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

			if (time > -1) {
				he = he.replace("%game-time%", Utils.getFormattedTime(time));
				fo = fo.replace("%game-time%", Utils.getFormattedTime(time));
			}

			gameTab.sendTabTitle(pl, he, fo);
		}
	}

	public void setScoreboard() {
		setScoreboard(-1);
	}

	public void setScoreboard(int time) {
		if (game == null) {
			return;
		}

		if (players == null) {
			return;
		}

		if (!ConfigValues.isScoreboardEnabled()) {
			return;
		}

		for (Iterator<PlayerManager> it = players.iterator(); it.hasNext();) {
			Player pl = it.next().getPlayer();
			if (!GameUtils.isPlayerPlaying(pl)) {
				gameBoard.remove(pl);
				continue;
			}

			String boardTitle = ConfigValues.getSbTitle();
			if (!boardTitle.isEmpty()) {
				gameBoard.setTitle(pl, Utils.colors(boardTitle));
			}

			List<String> rows = ConfigValues.getSbContent();
			if (!rows.isEmpty()) {
				int scores = rows.size();

				if (scores < 15) {
					for (int i = (scores + 1); i <= 15; i++) {
						gameBoard.resetScores(pl, i);
					}
				}

				for (String row : rows) {
					if (time > -1) {
						row = row.replace("%game-time%", Utils.getFormattedTime(time));
					}
					row = Utils.setPlaceholders(row, pl);

					gameBoard.setLine(pl, row, scores--);
				}
			}

			gameBoard.setScoreBoard(pl);
		}
	}

	public void setTeam() {
		if (game == null) {
			return;
		}

		if (players == null) {
			return;
		}

		if (!ConfigValues.isTabFormatEnabled()) {
			return;
		}

		for (Iterator<PlayerManager> it = players.iterator(); it.hasNext();) {
			Player pl = it.next().getPlayer();
			if (!GameUtils.isPlayerPlaying(pl)) {
				it.remove();
				continue;
			}

			String prefix = ConfigValues.getTabPrefix();
			String suffix = ConfigValues.getTabSuffix();

			prefix = Utils.setPlaceholders(prefix, pl);
			suffix = Utils.setPlaceholders(suffix, pl);

			scoreTeam.setTeam(pl, prefix, suffix);
		}
	}
}
