package hu.montlikadani.ragemode.gameLogic;

import java.util.List;
import java.util.TimerTask;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.ScoreTeam;
import hu.montlikadani.ragemode.gameUtils.TabTitles;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class GameTimer extends TimerTask {

	private TabTitles gameTab = null;
	private ScoreBoard gameBoard = null;
	private ScoreTeam scoreTeam = null;

	private Game game;
	private int time;

	public GameTimer(Game game, int time) {
		this.game = game;
		this.time = time;
	}

	public Game getGame() {
		return game;
	}

	public int getGameTime() {
		return time;
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

	public void loadModules() {
		List<PlayerManager> listPlayers = game.getPlayersFromList();
		Configuration conf = RageMode.getInstance().getConfiguration();

		if (conf.getCV().isScoreboardEnabled()) {
			gameBoard = new ScoreBoard(listPlayers);
			gameBoard.addToList(game.getName());
		}

		if (conf.getCV().isTabEnabled()) {
			gameTab = new TabTitles(listPlayers);
			gameTab.addToList(game.getName());
		}

		if (conf.getCV().isTabFormatEnabled()) {
			scoreTeam = new ScoreTeam(listPlayers);
			scoreTeam.addToList(game.getName());
		}
	}

	@Override
	public void run() {
		// Cancel the task if the instance is null, due to server restart
		if (RageMode.getInstance() == null) {
			cancel();
			return;
		}

		try { // Stop the game if something wrong or missing
			if (!game.isGameRunning()) {
				GameUtils.setStatus(game.getName(), null);
				cancel();
				return;
			}

			if (game.getPlayers().size() < 2) {
				GameUtils.stopGame(game.getName());
				cancel();
				return;
			}

			String tFormat = Utils.getFormattedTime(time);
			Configuration conf = RageMode.getInstance().getConfiguration();

			// Broadcast time message should be in this place, before counting
			List<Integer> values = conf.getCV().getGameEndBcs();
			if (values != null) {
				for (int val : values) {
					if (time == val) {
						GameUtils.broadcastToGame(game.getName(),
								RageMode.getLang().get("game.broadcast.game-end", "%time%", tFormat));
						break;
					}
				}
			}

			if (gameTab != null) {
				List<String> tabHeader = conf.getCV().getTabHeader();
				List<String> tabFooter = conf.getCV().getTabFooter();

				String he = "";
				String fo = "";
				int s = 0;
				for (String line : tabHeader) {
					s++;
					if (s > 1)
						he = he + "\n\u00a7r";

					he = he + line;
				}
				s = 0;
				for (String line : tabFooter) {
					s++;
					if (s > 1)
						fo = fo + "\n\u00a7r";

					fo = fo + line;
				}

				for (Player pl : gameTab.getPlayers()) {
					he = Utils.setPlaceholders(he, pl);
					fo = Utils.setPlaceholders(fo, pl);
				}

				he = he.replace("%game-time%", tFormat);
				fo = fo.replace("%game-time%", tFormat);

				gameTab.sendTabTitle(he, fo);
			}

			if (scoreTeam != null) {
				String prefix = conf.getCV().getTabPrefix();
				String suffix = conf.getCV().getTabSuffix();

				for (Player pl : scoreTeam.getPlayers()) {
					prefix = Utils.setPlaceholders(prefix, pl);
					suffix = Utils.setPlaceholders(suffix, pl);
				}

				scoreTeam.setTeam(prefix, suffix);
			}

			if (gameBoard != null) {
				String boardTitle = conf.getCV().getSbTitle();
				List<String> rows = conf.getCV().getSbContent();
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
							row = row.replace("%game-time%", tFormat);

							gameBoard.setLine(row, rowMax);
							rowMax--;

							gameBoard.setScoreBoard(pl);
						}
					}
				}
			}

			if (time == 0) {
				cancel();
				RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
						() -> GameUtils.stopGame(game.getName()));
			}

			time--;
		} catch (Exception e) {
			e.printStackTrace();
			cancel();
			GameUtils.stopGame(game.getName());
			return;
		}
	}
}
