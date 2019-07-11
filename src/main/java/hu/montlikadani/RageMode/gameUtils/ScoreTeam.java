package hu.montlikadani.ragemode.gameUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class ScoreTeam {

	public static HashMap<String, ScoreTeam> allTeams = new HashMap<>();
	private List<Player> player = new ArrayList<>();
	private Object packetPlayOutScoreboardTeam = null;

	/**
	 * Creates a new instance of Team, which manages the Team for
	 * the team prefixes/suffixes.
	 * 
	 * @param playerString List players that can be add to the list
	 */
	public ScoreTeam(List<String> playerString) {
		for (String player : playerString) {
			this.player.add(Bukkit.getPlayer(UUID.fromString(player)));
		}
	}

	/**
	 * Adds this instance to the global ScoreTeam. This
	 * can be accessed with the getScore(String gameName) method.
	 * 
	 * @param gameName the unique game-name for which the ScoreTeam element should be saved for.
	 * @return Whether the ScoreTeam was stored successfully or not.
	 */
	public boolean addToTeam(String gameName, boolean forceReplace) {
		if (!allTeams.containsKey(gameName)) {
			allTeams.put(gameName, this);
			return true;
		} else if (forceReplace) {
			allTeams.remove(gameName);
			allTeams.put(gameName, this);
			return true;
		} else
			return false;
	}

	/**
	 * Returns the stored players who added to the list.
	 * 
	 * @return List player
	 */
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(player);
	}

	/**
	 * Sends ScoreTeam to all online players that are currently playing in the game.
	 * 
	 * @param prefix String
	 * @param suffix String
	 */
	public void setTeam(String prefix, String suffix) {
		for (Player player : this.player) {
			setTeam(player, prefix, suffix);
		}
	}

	/**
	 * Sets the current player prefix/suffix.
	 * 
	 * @param player Player
	 * @param prefix String
	 * @param suffix String
	 */
	@SuppressWarnings("deprecation")
	public void setTeam(Player player, String prefix, String suffix) {
		if (Version.isCurrentLower(Version.v1_13_R1)) {
			if (prefix.length() > 16) prefix = prefix.substring(0, 16);
			if (suffix.length() > 16) suffix = suffix.substring(0, 16);
		} else if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
			if (prefix.length() > 64) prefix = prefix.substring(0, 64);
			if (suffix.length() > 64) suffix = suffix.substring(0, 64);
		}

		String teamName = player.getName();
		if (teamName.length() > 15) {
			teamName = teamName.substring(0, 15);
		}

		if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("bungee.enable")) {
			Scoreboard board = player.getScoreboard();
			Team team = getScoreboardTeam(board, player.getName());

			if (Version.isCurrentEqualOrHigher(Version.v1_9_R1)) {
				if (!team.hasEntry(player.getName()))
					team.addEntry(player.getName());
			} else {
				if (!team.hasPlayer(player))
					team.addPlayer(player);
			}

			if (prefix == null) prefix = "";
			if (suffix == null) suffix = "";

			prefix = RageMode.getLang().colors(prefix);
			suffix = RageMode.getLang().colors(suffix);

			// Prefix & suffix char limit, to prevent error
			if (Version.isCurrentLower(Version.v1_13_R1)) {
				if (prefix.length() > 15) prefix = prefix.substring(0, 16);
				if (suffix.length() > 15) suffix = suffix.substring(0, 16);
			} else {
				if (prefix.length() > 63) prefix = prefix.substring(0, 64);
				if (suffix.length() > 63) suffix = suffix.substring(0, 64);
			}

			team.setPrefix(prefix);
			team.setSuffix(suffix);
			if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
				team.setColor(Utils.fromPrefix(prefix));

			player.setScoreboard(board);
		} else {
			try {
				if (packetPlayOutScoreboardTeam == null)
					packetPlayOutScoreboardTeam = Utils.getNMSClass("PacketPlayOutScoreboardTeam").getConstructor(new Class[0])
					.newInstance(new Object[0]);

				Class<?> iChatBaseComponent = Utils.getNMSClass("IChatBaseComponent");
				Method m = iChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);

				List<String> contents = new ArrayList<>();
				if (!contents.contains(player.getName()))
					contents.add(player.getName());

				if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
					try {
						setField(packetPlayOutScoreboardTeam, "a", teamName);
						setField(packetPlayOutScoreboardTeam, "b", m.invoke(iChatBaseComponent, "{\"text\":\"" + teamName + "\"}"));
					} catch (ClassNotFoundException e) {
						setField(packetPlayOutScoreboardTeam, "a", teamName);
						setField(packetPlayOutScoreboardTeam, "b", m.invoke(iChatBaseComponent, "{\"text\":\"" + teamName + "\"}"));
					}
					setField(packetPlayOutScoreboardTeam, "c", m.invoke(iChatBaseComponent, "{\"text\":\"" + prefix + "\"}"));
					setField(packetPlayOutScoreboardTeam, "d", m.invoke(iChatBaseComponent, "{\"text\":\"" + suffix + "\"}"));
					setField(packetPlayOutScoreboardTeam, "e", "ALWAYS");

					try {
						setField(packetPlayOutScoreboardTeam, "g", contents);
						setField(packetPlayOutScoreboardTeam, "i", 0);
					} catch (Throwable e) {
						setField(packetPlayOutScoreboardTeam, "h", contents);
						setField(packetPlayOutScoreboardTeam, "j", 0);
					}
				} else {
					setField(packetPlayOutScoreboardTeam, "a", teamName);
					setField(packetPlayOutScoreboardTeam, "b", teamName);
					setField(packetPlayOutScoreboardTeam, "c", prefix);
					setField(packetPlayOutScoreboardTeam, "d", suffix);
					setField(packetPlayOutScoreboardTeam, "e", "ALWAYS");

					try {
						setField(packetPlayOutScoreboardTeam, "g", contents);
						setField(packetPlayOutScoreboardTeam, "h", 0);
					} catch (Throwable e) {
						setField(packetPlayOutScoreboardTeam, "h", contents);
						setField(packetPlayOutScoreboardTeam, "i", 0);
					}
				}

				Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
				Class<?> craftChatMessage = Class.forName("org.bukkit.craftbukkit."
						+ Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".util.CraftChatMessage");

				Field f = Utils.getNMSClass("EntityPlayer").getDeclaredField("listName");
				f.setAccessible(true);
				f.set(entityPlayer, ((Object[]) craftChatMessage.getMethod("fromString", String.class)
										.invoke(craftChatMessage, player.getName()))[0]);
				f.setAccessible(false);

				Object entityPlayerArray = Array.newInstance(entityPlayer.getClass(), 1);
				Array.set(entityPlayerArray, 0, entityPlayer);

				Class<?> enumPlayerInfoAction = null;
				if (Version.isCurrentEqual(Version.v1_8_R1)) {
					enumPlayerInfoAction = Utils.getNMSClass("EnumPlayerInfoAction");
				} else if (Version.isCurrentEqualOrHigher(Version.v1_11_R1)) {
					enumPlayerInfoAction = Utils.getNMSClass("PacketPlayOutPlayerInfo").getDeclaredClasses()[1];
				} else {
					enumPlayerInfoAction = Utils.getNMSClass("PacketPlayOutPlayerInfo").getDeclaredClasses()[2];
				}

				Object packetPlayOutPlayerInfo = Utils.getNMSClass("PacketPlayOutPlayerInfo")
						.getConstructor(enumPlayerInfoAction, entityPlayerArray.getClass())
						.newInstance(enumPlayerInfoAction.getDeclaredField("UPDATE_DISPLAY_NAME").get(enumPlayerInfoAction),
								entityPlayerArray);

				for (Player pl : this.player) {
					Utils.sendPacket(pl, packetPlayOutPlayerInfo);
					Utils.sendPacket(pl, packetPlayOutScoreboardTeam);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Removes the team from player
	 * 
	 * @param player Player
	 */
	public void removeTeam(Player player) {
		if (!RageMode.getInstance().getConfiguration().getCfg().getBoolean("bungee.enable")) {
			try {
				Class<?> iChatBaseComponent = Utils.getNMSClass("IChatBaseComponent");
				Class<?> declaredClass = iChatBaseComponent.getDeclaredClasses()[0];
				Method m = declaredClass.getMethod("a", String.class);

				if (packetPlayOutScoreboardTeam == null)
					return;

				String name = player.getName();

				if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
					setField(packetPlayOutScoreboardTeam, "a", name);
					setField(packetPlayOutScoreboardTeam, "b", m.invoke(iChatBaseComponent, "{\"text\":\"" + name + "\"}"));
					setField(packetPlayOutScoreboardTeam, "e", "ALWAYS");
					try {
						setField(packetPlayOutScoreboardTeam, "i", 1);
					} catch (Throwable e) {
						setField(packetPlayOutScoreboardTeam, "j", 1);
					}
				} else {
					setField(packetPlayOutScoreboardTeam, "a", name);
					setField(packetPlayOutScoreboardTeam, "b", name);
					setField(packetPlayOutScoreboardTeam, "e", "ALWAYS");
					try {
						setField(packetPlayOutScoreboardTeam, "h", 1);
					} catch (Throwable e) {
						setField(packetPlayOutScoreboardTeam, "i", 1);
					}
				}

				for (Player pl : this.player) {
					Utils.sendPacket(pl, packetPlayOutScoreboardTeam);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			Scoreboard board = player.getScoreboard();
			getScoreboardTeam(board, player.getName()).unregister();
			player.setScoreboard(board);
		}

		for (int i = 0; i < this.player.size(); i++) {
			if (player.equals(this.player.get(i)))
				this.player.remove(i);
		}
	}

	/**
	 * Removing the team from all online player that are currently playing in the game.
	 */
	public void removeTeam() {
		for (Player player : this.player) {
			removeTeam(player);
		}
	}

	/**
	 * Gets the scoreboard team. If not exists, then creates a new
	 * 
	 * @param board Scoreboard
	 * @param name String
	 * @return Team if exist
	 */
	private Team getScoreboardTeam(Scoreboard board, String name) {
		return board.getTeam(name) == null ? board.registerNewTeam(name) : board.getTeam(name);
	}

	private void setField(Object object, String fieldName, Object fieldValue) throws Throwable {
		Field field = object.getClass().getDeclaredField(fieldName);

		field.setAccessible(true);
		field.set(object, fieldValue);
	}
}
