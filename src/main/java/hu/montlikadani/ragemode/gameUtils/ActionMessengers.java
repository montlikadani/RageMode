package hu.montlikadani.ragemode.gameUtils;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.modules.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.modules.TitleSender;
import hu.montlikadani.ragemode.utils.Utils;

public class ActionMessengers {

	private final UUID playerUUID;
	private final ScoreBoard gameBoard;

	private final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	public ActionMessengers(UUID playerUUID) {
		this.playerUUID = playerUUID;

		gameBoard = new ScoreBoard(getPlayer());
	}

	@Nullable
	public Player getPlayer() {
		return rm.getServer().getPlayer(playerUUID);
	}

	@NotNull
	public UUID getUniqueId() {
		return playerUUID;
	}

	@NotNull
	public ScoreBoard getScoreboard() {
		return gameBoard;
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

		String header = "", footer = "";
		int s = 0;

		for (String line : ConfigValues.getTabListHeader()) {
			s++;

			if (s > 1) {
				header += "\n\u00a7r";
			}

			header += line;
		}

		s = 0;

		for (String line : ConfigValues.getTabListFooter()) {
			s++;

			if (s > 1) {
				footer += "\n\u00a7r";
			}

			footer += line;
		}

		header = Utils.setPlaceholders(header, player);
		footer = Utils.setPlaceholders(footer, player);

		String formattedTime = time < 0 ? "0" : Utils.getFormattedTime(time);

		header = header.replace("%game-time%", formattedTime);
		footer = footer.replace("%game-time%", formattedTime);

		TitleSender.sendTabTitle(player, header, footer);
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

		String formattedTime = time < 0 ? "0" : Utils.getFormattedTime(time);

		String boardTitle = ConfigValues.getSbTitle();
		if (!boardTitle.isEmpty()) {
			boardTitle = boardTitle.replace("%game-time%", formattedTime);
			boardTitle = Utils.setPlaceholders(boardTitle, player);
			gameBoard.setTitle(player, boardTitle);
		}

		if (!ConfigValues.getScoreboardContent().isEmpty()) {
			int scores = ConfigValues.getScoreboardContent().size();

			if (scores < 15) {
				for (int i = (scores + 1); i <= 15; i++) {
					gameBoard.resetScores(player, i);
				}
			}

			for (String row : ConfigValues.getScoreboardContent()) {
				row = row.replace("%game-time%", formattedTime);
				row = Utils.setPlaceholders(row, player);
				gameBoard.setLine(player, row, scores--);
			}
		}

		gameBoard.setScoreBoard(player);
	}

	public void setPlayerName() {
		if (ConfigValues.getTabPlayerListName().isEmpty()) {
			return;
		}

		Player player = getPlayer();

		if (player != null) {
			String name = Utils.setPlaceholders(ConfigValues.getTabPlayerListName(), player);
			name = name.replace("%player_name%", player.getName());

			rm.getComplement().setPlayerListName(player, name);
		}
	}
}
