package hu.montlikadani.ragemode.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class Language {

	private RageMode plugin;
	private String lang;

	private File localeFolder = null;
	private File langFile = null;

	public Language(RageMode plugin) {
		this.plugin = plugin;
	}

	public void loadLanguage(String lang) {
		if (lang == null || lang.isEmpty()) {
			lang = "en";
		}

		this.lang = lang;

		if (localeFolder == null) {
			localeFolder = new File(plugin.getFolder(), "locale");
		}

		if (!localeFolder.exists()) {
			localeFolder.mkdirs();
		}

		langFile = new File(localeFolder, "locale_" + lang + ".yml");

		if (!langFile.exists()) {
			plugin.saveResource("locale/locale_" + lang + ".yml", false);
		}

		if (lang.equalsIgnoreCase("en")) {
			FileConfiguration lf = YamlConfiguration.loadConfiguration(langFile);
			loadMessages(langFile, new FileConfig(lf));
		} else {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(langFile), java.nio.charset.StandardCharsets.UTF_8));
				FileConfiguration lf = YamlConfiguration.loadConfiguration(reader);
				lf.load(langFile);
				reader.close();
			} catch (InvalidConfigurationException e) {
				Debug.logConsole("Bad/invalid string found in your language file: " + e.getLocalizedMessage());
			} catch (IOException e2) {
				Debug.throwMsg();
				e2.printStackTrace();
			}
		}
	}

	private void loadMessages(File f, FileConfig l) {
		l.getFC().options().copyDefaults(true);

		l.getL("in-game-only", "&cThis command can only be in-game.");
		l.getL("not-a-player", "&cThis player not a player.");
		l.getL("wrong-command", "&cThis is not the command you are looking for!");
		l.getL("no-permission", "&cYou don't have permission for that!");
		l.getL("no-permission-to-interact-sign", "&cYou don't have permission to interact signs!");
		l.getL("missing-arguments", "&cMissing arguments! Usage:&e %usage%");
		l.getL("missing-dependencies", "&e%depend%&c must be installed to use this!");
		l.getL("not-a-number", "&e%number%&c is not a number.");
		l.getL("not-a-boolean", "&e%value%&c is not a boolean.");
		l.getL("invalid-game", "&e%game%&4 is not a valid RageMode Map.");
		l.getL("player-non-existent", "&cThat player doesn't even exist.");
		l.getL("not-played-yet", "&cThat player&7 %player%&c hasn't played on this server yet.");
		l.getL("bad-ragemode-name", "&cThe game name may contains special characters, ragemode commands, or the the name greater than 20.");

		l.getL("commands.listgames.listing-games", "&6Listing all available ragemode games... There are&a %games%&6 available.");
		l.getL("commands.listgames.no-games-available", "&cThere are currently no RageMode maps on this server.");
		l.getL("commands.listgames.game-running", "%number%.) %game%&6&o running");
		l.getL("commands.listgames.game-stopped", "%number%.) %game%&7 idle");
		l.getL("commands.reload.success", "&aRageMode was reloaded successfully!");
		l.getL("commands.forcestart.game-start", "&aStarting the&e %game%&a game...");
		l.getL("commands.forcestart.not-enough-players", "&cNot enough player to run this game!");
		l.getL("commands.forcestart.game-not-exist", "&cThis game not exists. Please add correctly name.");
		l.getL("commands.forcestart.player-not-in-game", "&cYou are not in-game lobby to start!");
		l.getL("commands.holostats.no-holo-found", "&cThere is no hologram saved.");
		l.getL("commands.stats.player-not-null", "&cThe player couldn't be null!");
		l.getL("commands.stats.player-not-found", "&cThis player not found.");
		l.getL("commands.stats.reseted", "&2Your stats has been reseted!");
		l.getL("commands.stats.target-stats-reseted", "&7%player%&2 stats has been reseted!");
		l.getL("commands.stats.player-currently-in-game", "&cThis player is currently playing! Please wait while the game end.");
		l.getL("commands.kick.game-not-null", "&cThe game name can not be null!");
		l.getL("commands.kick.player-not-found", "&cThe player name can not be null!");
		l.getL("commands.kick.player-kicked", "&2The player&e %player%&2 successfully kicked from&e %game%&2 game!");
		l.getL("commands.kick.player-currently-not-playing", "&cThis player currently not playing.");
		l.getL("commands.signupdate.usage", "&cUsage:&7 '/rm signupdate <gameName>'&c.");
		l.getL("commands.togglegame.game-is-running", "&cThis game is currently running, please stop it firstly.");
		l.getL("commands.togglegame.successfully-toggled", "&2This game&7 %game%&2 successfully toggled %status%&2.");
		l.getL("commands.togglegame.status.ready", "&aON");
		l.getL("commands.togglegame.status.not-ready", "&cOFF");
		l.getL("commands.join.game-locked", "&cThis game is locked, so you can not join.");
		l.getL("commands.join.empty-inventory.armor", "&cYou must empty your armor inventory to join the game.");
		l.getL("commands.join.empty-inventory.contents", "&cYou must empty your inventory contents to join the game.");
		l.getL("commands.join.rejoin-delay", "&cYou can't rejoin to the game. Wait&e %delay%&c.");
		l.getL("commands.points.player-not-found", "&cThe player name can not be null!");
		l.getL("commands.points.amount-not-less", "&cThe number must be greater than 0.");
		l.getL("commands.points.changed", "&2The player points has been changed:&e %amount%&2, new:&e %new%");
		l.getL("commands.points.player-is-in-game", "&cThis player&7 %player%&c is in game, so the points can not be modifiable while playing.");
		l.getL("commands.removespawn.not-valid-spawn-id", "&cGame spawn with id&e %id%&c is not a valid spawn.");
		l.getL("commands.removespawn.remove-success", "&cSpawn&e %number%&c for the game&3 %game%&c was removed successfully!");
		l.getL("commands.removespawn.no-more-spawn", "&cNo more spawn to delete, because you removed all spawns...");
		l.getL("commands.givesaveditems.not-enabled", "&cThis option is not enabled in the configuration file.");
		l.getL("commands.givesaveditems.player-not-found-in-data-file", "&cThis player&7 %player%&c not found in the data file.");
		l.getL("commands.givesaveditems.no-player-saved-inventory", "&cThere are no player found in the data file that have been saved their inventory.");
		l.getL("commands.givesaveditems.player-is-in-game", "&cThis player&7 %player%&c is in game.");
		l.getL("commands.latestart.player-not-in-lobby", "&cYou are not in the lobby to increase the time.");
		l.getL("commands.latestart.time-can-not-less", "&cTime should not be less than 1.");
		l.getL("commands.latestart.lobby-timer-increased", "&aThe lobby time is increased by&e %newtime%&a seconds.");
		l.getL("commands.listplayers.game-not-running", "&cThis game is currently not running.");
		l.getL("commands.listplayers.player-currently-not-playing", "&cYou are not currently playing.");
		l.getL("commands.maxplayers.changed", "&2The maxplayers for&e %game%&2 has been changed to&e %value%&2.");
		l.getL("commands.minplayers.changed", "&2The minplayers for&e %game%&2 has been changed to&e %value%&2.");

		List<String> holoList = Arrays.asList("&9Score:&a %points%", "&eWins:&a %wins%", "&3Games:&a %games%",
				"&5KD:&a %kd%", "&4Kills:&a %kills%", "&7Deaths:&a %deaths%");
		l.getL("hologram-list", holoList);

		l.getL("setup.not-set-yet", "&cThis game was not set yet! Set it with&e %usage%");
		l.getL("setup.lobby.set-success", "&2The lobby for the game&3 %game%&2 was set successfully!");
		l.getL("setup.lobby.not-set", "&cThe lobby was not set yet for&3 %game%&c. Set it with&e /rm setlobby <gameName>&c command.");
		l.getL("setup.lobby.coords-not-set", "&cThe lobby coordinates were not set properly. Ask an Admin to check the config.yml.");
		l.getL("setup.lobby.not-set-properly", "&4The lobby was not set properly. Ask an Admin to check the config.yml.");
		l.getL("setup.lobby.worldname-not-set", "&cThe world key can't be empty! Ask an Admin to check the config.yml.");
		l.getL("setup.addgame.success-added", "&2The game &3%game%&2 was added successfully!");
		l.getL("setup.addgame.already-exists", "&cThis &e%game%&c game already exists.");
		l.getL("setup.addgame.special-chars", "&cThe name of the ragemode map contains special characters.");
		l.getL("setup.addgame.name-greater", "&cThe name of the ragemode map is too long&7 (< 20).");
		l.getL("setup.addgame.bad-name", "&cThe name of the ragemode map is contains the command name.");
		l.getL("setup.at-least-two", "&cThe value must be at least two.");
		l.getL("setup.spawn-set-success", "&2Spawn&e %number%&2 for the game&3 %game%&2 was set successfully!");
		l.getL("setup.success", "&2Success!");
		l.getL("setup.removed-non-existent-game", "&cDon't remove non-existent games!");
		l.getL("setup.success-removed", "&cThe game&e %game%&c was removed successfully.");
		l.getL("setup.set-game-time-success", "&aGame time successfully set for&e %game%&a game with&e %time% minutes&a.");

		List<String> statList = Arrays.asList("&e--------&2 %player%&e --------", "", "&6Knife kills/deaths:&a %knife-kills%&7/&6%knife-deaths%",
				"&6Explosion kills/deaths:&a %explosion-kills%&7/&6%explosion-deaths%", "&6Axe kills/deaths:&a %axe-kills%&7/&6%axe-deaths%",
				"&6Direct arrow kills/deaths:&a %direct-arrow-kills%&7/%direct-arrow-deaths%", "", "&cKills:&2 %kills%",
				"&cDeaths:&2 %deaths%", "&cKd:&2 %kd%", "&cGames:&2 %games%", "&cWins:&2 %wins%", "&cPoints:&2 %points%");
		l.getL("statistic-list", statList);

		l.getL("game.lobby.start-message", "&9This round will start in&e %time%&9 seconds.");
		l.getL("game.lobby.chat-is-disabled", "&cThe chat currently is disabled in lobby!");
		l.getL("game.spawns-not-set-properly", "&cOne or more spawns are not set properly!");
		l.getL("game.too-few-spawns", "&4The number of spawns must be greater than or equal the maxplayers value!");
		l.getL("game.no-spawns-configured", "&cIn&e %game%&c are no spawns configured!");
		l.getL("game.player-could-not-join", "&e%player%&4 couldn't join the RageMode game&e %game%.");
		l.getL("game.game-stopped-for-reload", "&cThe game has stopped because we reloading the plugin and need to stop the game. Sorry!");
		l.getL("game.broadcast.axe-kill", "&a%victim%&3 was killed by&a %killer%&3 with a&6 CombatAxe&3.");
		l.getL("game.broadcast.arrow-kill", "&a%victim%&3 was killed by a&6 direct arrow hit&3 from&a %killer%&3.");
		l.getL("game.broadcast.knife-kill", "&a%victim%&3 was killed by&a %killer%&3 with a&6 RageKnife&3.");
		l.getL("game.broadcast.explosion-kill", "&a%victim%&3 was&6 blown up&3 by&a %killer%&3.");
		l.getL("game.broadcast.grenade-kill", "&a%victim%&3 was killed by&a %killer%&3 with a&7 Grenade&3. LOL");
		l.getL("game.broadcast.error-kill", "&cWhoops, that shouldn't happen normally...");
		l.getL("game.broadcast.game-end", "&9The game ends in&e %time%&9.");
		l.getL("game.unknown-killer", "&cDo you know who killed you? Because we don't know it...");
		l.getL("game.unknown-weapon", "&a%victim%&3 was killed by something unexpected.");
		l.getL("game.void-fall", "&c%player%&2 has void fall from the game.");
		l.getL("game.not-set-up", "&4The game is not set up correctly. Please contact an Admin.");
		l.getL("game.stopped", "&3%game%&2 has been stopped.");
		l.getL("game.running", "&4This game is running at the moment. Please wait until it is over.");
		l.getL("game.not-running", "&cThis game isn't running.");
		l.getL("game.chat-is-disabled", "&cThe chat currently is disabled!");
		l.getL("game.game-freeze.chat-is-disabled", "&cThe chat currently is disabled!");
		l.getL("game.name-or-maxplayers-not-set", "&4The worldname or the maxplayers are not set. Please contact an Admin for further information.");
		l.getL("game.maxplayers-not-set", "&cThe maxplayers value for&e %game%&c is not set properly.");
		l.getL("game.worldname-not-set", "&4The world key can't be empty! Ask an Admin to check the config.yml.");
		l.getL("game.does-not-exist", "&cThe game you wish to join wasn't found.");
		//l.getL("game.no-enough-points", "&cThere are not enough points to pay for your suicide.");
		l.getL("game.command-disabled-in-end-game", "&cAll commands are disabled at the end of the game. Endure!!");
		l.getL("game.this-command-is-disabled-in-game", "&cThis command is currently disabled.");
		l.getL("game.full", "&cThis Game is already full.");
		l.getL("game.player-joined", "&2%player%&9 joined the game.");
		l.getL("game.you-joined-the-game", "&aYou joined to&3 %game%&a game.");
		l.getL("game.player-not-switch-spectate", "&cYou are playing in-game, so you can not switch to spectator mode.");
		l.getL("game.player-kicked-for-vip", "&cYou were kicked out of the Game to make room for a VIP players.");
		l.getL("game.player-already-in-game", "&cYou are already in a game. You can leave it by typing&e %usage%");
		l.getL("game.player-not-ingame", "&cThe fact that you are not in a game caused a Problem while trying to remove you from that game.");
		l.getL("game.player-left", "&cYou left from the current game.");
		l.getL("game.no-won", "&cNo one won this game.");
		l.getL("game.message.arrow-kill", "&3You killed&6&l %victim%&3 with a direct arrow hit.&6&l %points%");
		l.getL("game.message.axe-kill", "&3You killed&6&l %victim%&3 with your CombatAxe.&6&l %points%");
		l.getL("game.message.knife-kill", "&3You killed&6&l %victim%&3 with your RageKnife.&6&l %points%");
		l.getL("game.message.explosion-kill", "&3You killed&6&l %victim%&3 by causing heavy explosions with your RageBow.&6&l %points%");
		l.getL("game.message.grenade-kill", "&3You killed&6&l %victim%&3 with your Grenade.&6&l %points%");
		l.getL("game.message.arrow-death", "&3You were killed by&6&l %killer%&3 with a direct arrow hit.&4&l %points%");
		l.getL("game.message.axe-death", "&3You were killed by&6&l %killer%&3 with a CombatAxe.&4&l %points%");
		l.getL("game.message.knife-death", "&3You were killed by&6&l %killer%&3 with a RageKnife.&4&l %points%");
		l.getL("game.message.explosion-death", "&3You were killed by&6&l %killer%&3 by an explosion.&4&l %points%");
		l.getL("game.message.grenade-death", "&3You were killed by&6&l %killer%&3 with a Grenade.&4&l %points%&7, HOW?");
		l.getL("game.message.current-points", "&3You now have&6&l %points%&3 points.");
		l.getL("game.message.points-loss-for-suicide", "&cYou lost&6 %amount%&c points. Your current points/deaths number:&6 %current%/%deaths%");
		l.getL("game.message.streak", "&6%number%&2 KILLSTREAK&6 %points%");
		l.getL("game.message.suicide", "&3You killed yourself you silly idiot.");
		l.getL("game.message.player-won", "&2%player%&d won the &6%game%&d game.");
		l.getL("game.message.you-won", "&dYou won the&6 %game%&d game!");

		l.getL("time-formats.second", "s");
		l.getL("time-formats.minute", "m");
		l.getL("time-formats.hour", "h");

		Configuration.saveFile(l.getFC(), f);
	}

	public String get(String key, Object... variables) {
		FileConfiguration yc = YamlConfiguration.loadConfiguration(getLangFile());
		String msg = "";
		String missing = "BADF " + key;

		if (key == null || key.isEmpty())
			return msg;

		if (!yc.contains(key) || !yc.isString(key)) {
			msg = missing;
			Debug.sendMessage("[RageMode]&c Can't read language file for:&7 " + key);
			return msg;
		}

		if (yc.getString(key, "").isEmpty())
			return msg;

		msg = Utils.colors(yc.getString(key));

		if (variables.length > 0) {
			for (int i = 0; i < variables.length; i++) {
				if (variables.length >= i + 2)
					msg = msg.replace(String.valueOf(variables[i]), String.valueOf(variables[i + 1]));
				i++;
			}
		}
		return msg;
	}

	public List<String> getList(String key, Object... variables) {
		FileConfiguration yc = YamlConfiguration.loadConfiguration(getLangFile());
		String missing = "BADF " + key + " ";

		if (key == null || key.isEmpty())
			return Collections.emptyList();

		List<String> ls = null;
		if (yc.contains(key) && yc.isList(key))
			ls = Utils.colorList(yc.getStringList(key));
		else {
			ls = Arrays.asList(missing);
			Debug.sendMessage("[RageMode]&c Can't read language file for:&7 " + key);
			return ls;
		}

		if (variables.length > 0) {
			for (int i = 0; i < ls.size(); i++) {
				String msg = ls.get(i);

				for (int y = 0; y < variables.length; y += 2) {
					msg = msg.replace(String.valueOf(variables[y]), String.valueOf(variables[y + 1]));
				}

				msg = filterNewLine(msg);
				ls.set(i, Utils.colors(msg));
			}
		}

		return ls;
	}

	private String filterNewLine(String msg) {
		Pattern patern = Pattern.compile("([ ]?[\\/][n][$|\\s])");
		Matcher match = patern.matcher(msg);
		while (match.find()) {
			msg = msg.replace(match.group(0), "\n");
		}
		return msg;
	}

	public File getLangFile() {
		return langFile;
	}

	public String getCurrentLang() {
		return lang;
	}
}