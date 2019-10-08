package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.bukkit.entity.Entity;
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

	private List<Player> le = new ArrayList<>();

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
			gameBoard = new ScoreBoard(listPlayers, false);
			gameBoard.addToScoreBoards(game.getName(), true);
		}

		if (conf.getCV().isTabEnabled()) {
			gameTab = new TabTitles(listPlayers);
			gameTab.addToTabList(game.getName(), true);
		}

		if (conf.getCV().isTabFormatEnabled()) {
			scoreTeam = new ScoreTeam(listPlayers);
			scoreTeam.addToTeam(game.getName(), true);
		}
	}

	@Override
	public void run() {
		try { // Stop the game if something wrong or missing
			if (!game.isGameRunning(game.getName())) {
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
			if (values != null && !values.isEmpty()) {
				for (int val : values) {
					if (time == val) {
						GameUtils.broadcastToGame(game.getName(),
								RageMode.getLang().get("game.broadcast.game-end", "%time%", tFormat));
						break;
					}
				}
			}

			time--;

			for (PlayerManager pm : game.getPlayersFromList()) {
				Player player = pm.getPlayer();

				if (conf.getCfg().getBoolean("game.global.show-name-above-player-when-look")) {
					// making the game more difficult
					for (Entity e : player.getNearbyEntities(25, 10, 25)) {
						if (e instanceof Player) {
							Player p = (Player) e;

							if (!le.contains(p))
								le.add(p);

							p.setCustomNameVisible(false);

							// TODO fix issue when the player looks through the block and the name appears within the 10 radius
							if (GameUtils.getLookingAt(player, p))
								p.setCustomNameVisible(true);
						}
					}
				}
			}

			Player player = game.getPlayerInGame(game.getName());
			if (player == null || !player.isOnline())
				return;

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

				he = Utils.setPlaceholders(he, player);
				he = he.replace("%game-time%", tFormat);

				fo = Utils.setPlaceholders(fo, player);
				fo = fo.replace("%game-time%", tFormat);

				gameTab.sendTabTitle(he, fo);
			}

			if (scoreTeam != null) {
				String prefix = conf.getCV().getTabPrefix();
				String suffix = conf.getCV().getTabSuffix();

				prefix = Utils.setPlaceholders(prefix, player);
				suffix = Utils.setPlaceholders(suffix, player);

				scoreTeam.setTeam(prefix, suffix);
			}

			if (gameBoard != null) {
				String boardTitle = conf.getCV().getSbTitle();
				if (boardTitle != null && !boardTitle.equals(""))
					gameBoard.setTitle(Utils.colors(boardTitle));

				List<String> rows = conf.getCV().getSbContent();
				if (rows != null && !rows.isEmpty()) {
					int rowMax = rows.size();

					// should fix duplicated lines
					org.bukkit.scoreboard.Scoreboard sb = gameBoard.getScoreboards().get(player).getScoreboard();
					for (String entry : sb.getEntries()) {
						sb.resetScores(entry);
					}

					for (String row : rows) {
						if (row.trim().equals("")) {
							for (int i = 0; i <= rowMax; i++) {
								row = row + " ";
							}
						}

						row = Utils.setPlaceholders(row, player);
						row = row.replace("%game-time%", tFormat);

						gameBoard.setLine(row, rowMax);
						rowMax--;

						gameBoard.setScoreBoard();
					}
				}
			}

			if (time == 0) {
				if (le != null && !le.isEmpty()) {
					for (int i = 0; i < le.size(); i++) {
						if (le.get(i).isCustomNameVisible())
							le.get(i).setCustomNameVisible(true);
					}

					le.clear();
				}

				cancel();
				RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
						() -> GameUtils.stopGame(game.getName()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			cancel();
			GameUtils.stopGame(game.getName());
			return;
		}
	}
}
