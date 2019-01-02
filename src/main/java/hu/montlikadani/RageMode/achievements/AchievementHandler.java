package hu.montlikadani.ragemode.achievements;

import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.achievements.Achievement.AchievementReason;
import hu.montlikadani.ragemode.events.KillEvent;
import hu.montlikadani.ragemode.events.PlayerSurviveEvent;
import hu.montlikadani.ragemode.events.WinEvent;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.scores.PlayerPoints;

public class AchievementHandler implements Listener {

	private RageMode plugin;
	private Set<Achievement> achievements = new TreeSet<>();

	public AchievementHandler(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onKill(KillEvent event) {
		if (event.getKiller() == null) return;

		Player killer = event.getKiller();
		if (PlayerList.isGameRunning(event.getGame()) && PlayerList.isPlayerPlaying(killer.getName())) {
			for (Achievement achievement : achievements) {
				ConfigurationSection list = plugin.getConfiguration().getCfg().getConfigurationSection("achievements");
				for (String acm : list.getKeys(false)) {
					ConfigurationSection section = list.getConfigurationSection(acm);

					PlayerPoints pointsHolder = new PlayerPoints(killer.getUniqueId().toString());
					if (section.contains("first-kill")) {
						if (achievement.getReason() == AchievementReason.FIRST_KILL && event.isFirstKill()) {
							if (section.contains("first-kill.reward.points") && section.getInt("first-kill.reward.points") > 0)
								pointsHolder.setPoints(section.getInt("first-kill.reward.points"));

							if (section.contains("first-kill.reward.command") && !section.getStringList("first-kill.reward.command").isEmpty()) {
								for (String cmd : section.getStringList("first-kill.reward.command")) {
									cmd = cmd.replace("%player%", killer.getName());
									cmd = cmd.replace("%game%", event.getGame());
									cmd = cmd.replace("%online-ingame-players%", String.valueOf(PlayerList.getPlayersInGame(event.getGame()).length));
									Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
								}
							}
						}
					}
					if (section.contains("kill")) {
						if (achievement.getReason() == AchievementReason.KILLS && !event.isFirstKill()) {
							if (section.contains("kill.reward.points") && section.getInt("kill.reward.points") > 0)
								pointsHolder.setPoints(section.getInt("kill.reward.points"));

							if (section.contains("kill.reward.command") && !section.getStringList("kill.reward.command").isEmpty()) {
								for (String cmd : section.getStringList("kill.reward.command")) {
									cmd = cmd.replace("%player%", killer.getName());
									cmd = cmd.replace("%game%", event.getGame());
									cmd = cmd.replace("%online-ingame-players%", String.valueOf(PlayerList.getPlayersInGame(event.getGame()).length));
									Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onWin(WinEvent event) {
		if (event.getPlayer() == null) return;

		Player player = event.getPlayer();
		if (PlayerList.isGameRunning(event.getGame()) && PlayerList.isPlayerPlaying(player.getName())) {
			for (Achievement achievement : achievements) {
				ConfigurationSection list = plugin.getConfiguration().getCfg().getConfigurationSection("achievements");
				for (String acm : list.getKeys(false)) {
					ConfigurationSection section = list.getConfigurationSection(acm);

					PlayerPoints pointsHolder = new PlayerPoints(player.getUniqueId().toString());
					// TODO First win event
					/**if (section.contains("first-win")) {
						if (achievement.getReason() == AchievementReason.FIRST_WIN && event.isFirstWin()) {
							if (section.contains("first-win.reward.points") && section.getInt("first-win.reward.points") > 0)
								pointsHolder.setPoints(section.getInt("first-win.reward.points"));

							if (section.contains("first-win.reward.command") && !section.getStringList("first-win.reward.command").isEmpty()) {
								for (String cmd : section.getStringList("first-win.reward.command")) {
									cmd = cmd.replace("%player%", player.getName());
									Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
								}
							}
						}
					}*/
					if (section.contains("win")) {
						if (achievement.getReason() == AchievementReason.WIN) {
							if (section.contains("win.reward.points") && section.getInt("win.reward.points") > 0)
								pointsHolder.setPoints(section.getInt("win.reward.points"));

							if (section.contains("win.reward.command") && !section.getStringList("win.reward.command").isEmpty()) {
								for (String cmd : section.getStringList("win.reward.command")) {
									cmd = cmd.replace("%player%", player.getName());
									cmd = cmd.replace("%game%", event.getGame());
									cmd = cmd.replace("%online-ingame-players%", String.valueOf(PlayerList.getPlayersInGame(event.getGame()).length));
									Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerSurvive(PlayerSurviveEvent event) {
		if (event.getPlayer() == null) return;

		Player player = event.getPlayer();
		if (PlayerList.isGameRunning(event.getGame()) && PlayerList.isPlayerPlaying(player.getName())) {
			for (Achievement achievement : achievements) {
				ConfigurationSection list = plugin.getConfiguration().getCfg().getConfigurationSection("achievements");
				for (String acm : list.getKeys(false)) {
					ConfigurationSection section = list.getConfigurationSection(acm);

					PlayerPoints pointsHolder = new PlayerPoints(player.getUniqueId().toString());
					if (section.contains("player-survived")) {
						if (achievement.getReason() == AchievementReason.SURVIVOR) {
							if (section.contains("player-survived.reward.points") && section.getInt("player-survived.reward.points") > 0)
								pointsHolder.setPoints(section.getInt("player-survived.reward.points"));

							if (section.contains("player-survived.reward.command") && !section.getStringList("player-survived.reward.command").isEmpty()) {
								for (String cmd : section.getStringList("player-survived.reward.command")) {
									cmd = cmd.replace("%player%", player.getName());
									cmd = cmd.replace("%game%", event.getGame());
									cmd = cmd.replace("%online-ingame-players%", String.valueOf(PlayerList.getPlayersInGame(event.getGame()).length));
									Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
								}
							}
						}
					}
				}
			}
		}
	}
}
