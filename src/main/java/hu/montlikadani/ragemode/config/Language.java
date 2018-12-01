package hu.montlikadani.ragemode.config;

import hu.montlikadani.ragemode.RageMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Language {

    private RageMode plugin;
    private String lang;

    public Language(RageMode plugin, String lang) {
        this.plugin = plugin;
        this.lang = lang;
    }

    public void loadLanguage() {
        try {
            File localeFolder = new File(plugin.getFolder(), "locale");
            if(!localeFolder.exists())
                localeFolder.mkdirs();

            File langFile = null;
            YamlConfiguration lf = new YamlConfiguration();
            if(lang == null || lang.equals("")) {
                langFile = new File(localeFolder + File.separator + "locale_en.yml");
                if(!langFile.exists())
                    plugin.saveResource("locale/locale_en.yml", false);
                else
                    lf = YamlConfiguration.loadConfiguration(langFile);
            } else {
                langFile = new File(localeFolder + File.separator + "locale_" + lang + ".yml");
                if(!langFile.exists())
                    plugin.saveResource("locale/locale_" + lang + ".yml", false);
                else
                    lf = YamlConfiguration.loadConfiguration(langFile);
            }
            loadMessages(langFile, lf);
        } catch(Exception e) {
            e.printStackTrace();
            plugin.throwMsg();
        }
    }

    private void loadMessages(File f, YamlConfiguration l) {
        l.options().copyDefaults(true);

        l.get("in-game-only", "&cThis command can only be in-game.");
        l.get("not-a-player", "&cThis player not a player.");
        l.get("wrong-command", "&cThis is not the command you are looking for!");
        l.get("no-permission", "&cYou don't have permission for that!");
        l.get("missing-arguments", "&cMissing arguments! Usage:&e %usage%");
        l.get("missing-dependencies", "&e%depend%&c must be installed to use this!");
        l.get("not-a-number", "&e%number%&c is not a number.");
        l.get("invalid-game", "&e%game%&4 is not a valid RageMode Map.");
        l.get("player-non-existent", "&cThat player doesn't even exist.");
        l.get("not-played-yet", "&cThat player hasn't played on this server yet.");

        l.get("commands.listgames.listing-games", "&6Listing all available ragemode games...");
        l.get("commands.listgames.no-games-available", "&cThere are currently no RageMode maps on this server.");
        l.get("commands.listgames.game-running", "%number%.) %game%&6&o running");
        l.get("commands.listgames.game-stopped", "%number%.) %game%&7 idle");
        l.get("commands.reload.success", "&aRageMode was reloaded successfully!");
        l.get("commands.forcestart.game-start", "&aStarting the&e %game%&a game...");
        l.get("commands.forcestart.not-enough-players", "&cNot enough player to run this game!");
        l.get("commands.holostats.no-holo-found", "&cThere is no hologram saved.");
        l.get("commands.stats.player-not-null", "&cThe player couldn't be null!");
        l.get("commands.stats.reseted", "&2Your stats has been reseted!");
        l.get("commands.stats.target-stats-reseted", "&7%player%&2 stats has been reseted!");
        l.get("commands.kick.game-not-null", "&cThe game name can not be null!");
        l.get("commands.kick.player-not-null", "&cThe player name can not be null!");
        l.get("commands.player-kicked", "&2The player&e %player%&2 successfully kicked from&e %game%&2 game!");
        l.get("commands.player-not-play-currently", "&cThis player currently not playing.");

        String[] holoList = new String[]{"&6Rank:&a %rank%", "&9Score:&a %points%", "&eWins:&a %wins%", "&3Games:&a %games%",
                "&5KD:&a %kd%", "&4Kills:&a %kills%", "&7Deaths:&a %deaths%"};
        l.get("hologram-list", Arrays.asList(holoList));

        l.get("setup.not-set-yet", "&cThis game was not set yet! Set it with&e %usage%");
        l.get("setup.lobby-set-success", "&2The lobby for the game&3 %game%&2 was set successfully!");
        l.get("setup.success-added", "&2The game &3%game%&2 was added successfully!");
        l.get("setup.already-exists", "&cThis &e%game%&c game already exists.");
        l.get("setup.at-least-two", "&cThe maxplayers value must be at least two.");
        l.get("setup.spawn-set-success", "&2Spawn&e %number%&2 for the game&3 %game%&2 was set successfully!");
        l.get("setup.success", "&2Success!");
        l.get("setup.removed-non-existent-game", "&cDon't remove non-existent games!");
        l.get("setup.success-removed", "&cThe game&e %game%&c was removed successfully.");

        String[] statList = new String[]{"&6Knife kills/deaths:&a %knife-kills%&7/&6%knife-deaths%",
                "&6Explosion kills/deaths:&a %explosion-kills%&7/&6%explosion-deaths%", "&6Axe kills/deaths:&a %axe-kills%&7/&6%axe-deaths%",
                "&6Direct arrow kills/deaths:&a %direct-arrow-kills%&7/%direct-arrow-deaths%", "", "&cKills:&2 %kills%",
                "&cDeaths:&2 %deaths%", "&cKd:&2 %kd%", "&cGames:&2 %games%", "&cWins:&2 %games%", "&cPoints:&2 %points%", "&cRank:&2 %rank%"};
        l.get("statistic-list", Arrays.asList(statList));

        l.get("game.lobby-not-set", "&cThe lobby was not set yet for&3 %game%&c. Set it with&e /rm addlobby <gameName>&c command.");
        l.get("game.lobby-coors-not-set", "&cThe lobby coordinates were not set properly. Ask an Admin to check the config.yml.");
        l.get("game.worldname-not-set", "&cThe world key can't be empty! Ask an Admin to check the config.yml.");
        l.get("game.spawns-not-set-properly", "&cOne or more spawns are not set properly!");
        l.get("game.no-spawns-configured", "&cIn&e %game%&c are no spawns configured!");
        l.get("game.too-few-spawns", "&4The number of spawns must be greater than or equal the maxplayers value!");
        l.get("game.player-could-not-join", "&e%player%&4 couldn't join the RageMode game&e %game%.");
        l.get("game.broadcast-axe-kill", "&a%victim%&3 was killed by&a %killer%&3 with a&6 CombatAxe&3.");
        l.get("game.broadcast-arrow-kill", "&a%victim%&3 was killed by a&6 direct arrow hit&3 from&a %killer%&3.");
        l.get("game.broadcast-knife-kill", "&a%victim%&3 was killed by&a %killer%&3 with a&6 RageKnife&3.");
        l.get("game.broadcast-explosion-kill", "&a%victim%&3 was&6 blown up&3 by&a %killer%&3.");
        l.get("game.broadcast-error-kill", "&cWhoops, that shouldn't happen normally...");
        l.get("game.unknown-killer", "&cDo you know who killed you? Because we don't know it...");
        l.get("game.unknown-weapon", "&a%victim%&3 was killed by something unexpected.");
        l.get("game.not-set-up", "&4The game is not set up correctly. Please contact an Admin.");
        l.get("game.lobby-not-set-properly", "&4The lobby was not set properly. Ask an Admin to check the config.yml.");
        l.get("game.lobby-message", "&9This round will start in&e %time%&9 seconds.");
        l.get("game.stopped", "&3%game%&2 has been stopped.");
        l.get("game.running", "&4This game is running at the moment. Please wait until it is over.");
        l.get("game.not-running", "&cThis game isn't running.");
        l.get("game.name-or-maxplayers-not-set", "&4The worldname or the maxplayers are not set. Please contact an Admin for further information.");
        l.get("game.maxplayers-not-set", "&cThe maxplayers value for&e %game%&c is not set properly.");
        l.get("game.worldname-not-set", "&4The world key can't be empty! Ask an Admin to check the config.yml.");
        l.get("game.does-not-exist", "&cThe game you wish to join wasn't found.");
        l.get("game.this-command-is-disabled-in-game", "&cThis command is currently disabled.");
        l.get("game.full", "&cThis Game is already full.");
        l.get("game.player-joined", "&2%player%&9 joined the game.");
        l.get("game.you-joined-the-game", "&aYou joined to&3 %game%&a game.");
        l.get("game.player-kicked-for-vip", "&cYou were kicked out of the Game to make room for a VIP players.");
        l.get("game.player-already-in-game", "&cYou are already in a game. You can leave it by typing&e %usage%");
        l.get("game.player-not-ingame", "&cThe fact that you are not in a game caused a Problem while trying to remove you from that game.");
        l.get("game.player-left", "&cYou left your current Game.");
        l.get("game.message.arrow-kill", "&3You killed&6&l %victim%&3 with a direct arrow hit.&6&l %points%");
        l.get("game.message.axe-kill", "&3You killed&6&l %victim%&3 with your CombatAxe.&6&l %points%");
        l.get("game.message.knife-kill", "&3You killed&6&l %victim%&3 with your RageKnife.&6&l %points%");
        l.get("game.message.explosion-kill", "&3You killed&6&l %victim%&3 by causing heavy explosions with your RageBow.&6&l %points%");
        l.get("game.message.arrow-death", "&3You were killed by&6&l %killer%&3 with a direct arrow hit.&4&l %points%");
        l.get("game.message.axe-death", "&3You were killed by&6&l %killer%&3 with a CombatAxe.&4&l %points%");
        l.get("game.message.knife-death", "&3You were killed by&6&l %killer%&3 with a RageKnife.&4&l %points%");
        l.get("game.message.explosion-death", "&3You were killed by&6&l %killer%&3 by an explosion.&4&l %points%");
        l.get("game.message.current-points", "&3You now have&6&l %points%&3 points.");
        l.get("game.message.streak", "&6%number%&2 KILLSTREAK&6 %points%");
        l.get("game.message.suicide", "&3You killed yourself you silly idiot.");
        l.get("game.message.player-won", "&2%player%&d won the &6%game%&d game.");
        l.get("game.message.you-won", "&dYou won the&6 %game%&d game!");

        try {
            l.load(f);
        } catch(IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            plugin.throwMsg();
        }
    }

    public String get(String key) {
        return get(key, "");
    }

    public String get(String key, Object... variables) {
        YamlConfiguration lf = getCurrentLangConf();

        String missing = "MLF " + key;
        String msg = "";
        try {
            if(lf == null || !lf.contains(key))
                msg = getDefaultLangConf().isString(key) ? colors(getDefaultLangConf().getString(key)) : missing;
            else
                msg = lf.isString(key) ? colors(lf.getString(key)) : missing;
        } catch(Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(colors("&c[RageMode] Can't read language file for: " + key));
            throw e;
        }
        if(variables.length > 0) {
            for(int i = 0; i < variables.length; i++) {
                if(variables.length >= i + 2)
                    msg = msg.replace(String.valueOf(variables[i]), String.valueOf(variables[i + 1]));
                i++;
            }
        }
        return msg;
    }

    public List<String> getList(String key, Object... variables) {
        YamlConfiguration defL = getDefaultLangConf();

        String missing = "MLF " + key + " ";

        List<String> ls;
        if(getCurrentLangConf().isList(key))
            ls = colorsArray(getCurrentLangConf().getStringList(key), true);
        else
            ls = !defL.getStringList(key).isEmpty() && defL.getStringList(key) != null ? colorsArray(defL.getStringList(key), true)
                    : Collections.singletonList(missing);

        if(variables != null && variables.length > 0) {
            for(int i = 0; i < ls.size(); i++) {
                String msg = ls.get(i);
                for(int y = 0; y < variables.length; y += 2) {
                    msg = msg.replace(String.valueOf(variables[y]), String.valueOf(variables[y + 1]));
                }
                msg = filterNewLine(msg);
                ls.set(i, colors(msg));
            }
        }
        return ls;
    }

    public String filterNewLine(String msg) {
        Pattern patern = Pattern.compile("([ ]?[\\/][n][$|\\s])");
        Matcher match = patern.matcher(msg);
        while(match.find()) {
            msg = msg.replace(match.group(0), "\n");
        }
        return msg;
    }

    public List<String> colorsArray(List<String> text, Boolean colorize) {
        List<String> temp = new java.util.ArrayList<>();
        for(String part : text) {
            if(colorize)
                part = colors(part);
            temp.add(colors(part));
        }
        return temp;
    }

    public String colors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public File getCurrentLangFile() {
        File localeFolder = new File(plugin.getFolder(), "locale");
        return new File(localeFolder + File.separator + "locale_" + lang + ".yml");
    }

    public File getDefaultLangFile() {
        File localeFolder = new File(plugin.getFolder(), "locale");
        return new File(localeFolder + File.separator + "locale_en.yml");
    }

    public YamlConfiguration getCurrentLangConf() {
        return YamlConfiguration.loadConfiguration(getCurrentLangFile());
    }

    public YamlConfiguration getDefaultLangConf() {
        return YamlConfiguration.loadConfiguration(getDefaultLangFile());
    }
}