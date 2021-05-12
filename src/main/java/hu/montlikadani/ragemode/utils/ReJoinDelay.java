package hu.montlikadani.ragemode.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;

public final class ReJoinDelay {

	private static final Map<OfflinePlayer, Long> PLAYERTIMES = new HashMap<>();

	public static void setTime(OfflinePlayer p, Long ticks) {
		ticks -= System.currentTimeMillis();

		int hours = (int) (ticks / 1000 / 60 / 60);
		ticks -= (hours * 1000 * 60 * 60);

		int minutes = (int) (ticks / 1000 / 60);
		ticks -= (minutes * 1000 * 60);

		int sec = (int) (ticks / 1000);
		ticks -= (sec * 1000);

		setTime(p, hours, minutes, sec);
	}

	public static void setTime(OfflinePlayer p, int hour, int minute, int second) {
		if (p == null || PLAYERTIMES.containsKey(p)) {
			return;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());

		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + hour);
		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + minute);
		cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + second);

		PLAYERTIMES.put(p, cal.getTimeInMillis());
	}

	public static String format(long ticks) {
		long hours = ticks / 1000 / 60 / 60;
		ticks -= (hours * 1000 * 60 * 60);

		long minutes = ticks / 1000 / 60;
		ticks -= (minutes * 1000 * 60);

		long sec = ticks / 1000;
		ticks -= (sec * 1000);

		String time = "";

		if ((minutes > 0 || sec > 0) && hours > 0) {
			time += hours + RageMode.getLang().get("time-formats.hour") + " ";
		}

		if (minutes > 0 || sec > 0 && minutes == 0 && (hours != 0)) {
			time += minutes + RageMode.getLang().get("time-formats.minute") + " ";
		}

		if (sec > 0) {
			time += sec + RageMode.getLang().get("time-formats.second");
		}

		if (time.isEmpty()) {
			time += 0 + RageMode.getLang().get("time-formats.second");
		}

		return time;
	}

	public static void resetTime(Player p) {
		PLAYERTIMES.remove(p);
	}

	public static boolean checkRejoinDelay(Player player, String j) {
		if (!ConfigValues.isRejoinDelayEnabled() || player.hasPermission("ragemode.bypass.rejoindelay")) {
			return true;
		}

		if (isValid(player)) {
			if (j == null || j.trim().isEmpty()) {
				j = "join";
			}

			Misc.sendMessage(player, RageMode.getLang().get("commands." + j + ".rejoin-delay", "%delay%",
					format(getTimeByPlayer(player) - System.currentTimeMillis())));
			return false;
		}

		int hour = ConfigValues.getRejoinDelayHour(), minute = ConfigValues.getRejoinDelayMinute(),
				second = ConfigValues.getRejoinDelaySecond();

		if (hour == 0 && minute == 0 && second == 0) {
			Debug.logConsole("The rejoin times can't be 0 hour, 0 minute and 0 second.");
			return true;
		}

		resetTime(player);
		setTime(player, hour, minute, second);
		return true;
	}

	public static boolean isValid(Player pl) {
		return getTimeByPlayer(pl) > System.currentTimeMillis();
	}

	public static boolean isValid(Player pl, Long time) {
		return time != null && time.longValue() > System.currentTimeMillis() && PLAYERTIMES.containsKey(pl);
	}

	public static long getTimeByPlayer(Player p) {
		return PLAYERTIMES.getOrDefault(p, 0L).longValue();
	}

	public static ImmutableMap<OfflinePlayer, Long> getPlayerTimes() {
		return ImmutableMap.copyOf(PLAYERTIMES);
	}
}
