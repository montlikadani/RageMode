package hu.montlikadani.ragemode.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;

public class ReJoinDelay {

	private static Map<Player, Long> playerTimes = new HashMap<>();

	public static void setTime(Player p, int hour, int minute, int second) {
		Calendar cal = getCal(new Date());

		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + hour);
		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + minute);
		cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + second);

		if (p != null && !playerTimes.containsKey(p)) {
			playerTimes.put(p, cal.getTimeInMillis());
		}
	}

	public static String format(Long ticks) {
		long hours = ticks / 1000 / 60 / 60;
		ticks = ticks - (hours * 1000 * 60 * 60);

		long minutes = ticks / 1000 / 60;
		ticks = ticks - (minutes * 1000 * 60);

		long sec = ticks / 1000;
		ticks = ticks - (sec * 1000);

		String time = "";

		if (hours > 0 || (minutes > 0 || sec > 0))
			time += hours + RageMode.getLang().get("time-formats.hour") + " ";

		if (minutes > 0 || sec > 0 && minutes == 0 && (hours != 0))
			time += minutes + RageMode.getLang().get("time-formats.minute") + " ";

		if (sec > 0)
			time += sec + RageMode.getLang().get("time-formats.second");

		if (time.isEmpty())
			time += 0 + RageMode.getLang().get("time-formats.second");

		return time;
	}

	private static Calendar getCal(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public static void resetTime(Player p) {
		if (playerTimes.containsKey(p)) {
			Calendar.getInstance().setTime(new Date());
			playerTimes.remove(p);
		}
	}

	public static boolean isValid(Player pl) {
		return playerTimes.containsKey(pl) && isValid(pl, playerTimes.get(pl));
	}

	public static boolean isValid(Player pl, Long time) {
		if (playerTimes.containsKey(pl) && time > System.currentTimeMillis()) {
			return true;
		}

		return false;
	}

	public static Long getTimeByPlayer(Player p) {
		return playerTimes.containsKey(p) ? playerTimes.get(p) : null;
	}

	public static Map<Player, Long> getPlayerTimes() {
		return java.util.Collections.unmodifiableMap(playerTimes);
	}
}
