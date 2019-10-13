package hu.montlikadani.ragemode.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

public class ReJoinDelay {

	private static Map<Player, Map<String, Date>> playerTimes = new HashMap<>();

	public static void setTime(Player p, String game, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + hour);
		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + minute);
		cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + second);

		if (p != null && !playerTimes.containsKey(p)) {
			Map<String, Date> times = playerTimes.get(p);
			if (times == null) {
				times = new HashMap<String, Date>();
			}

			if (game != null) {
				times.put(game, cal.getTime());
			}

			playerTimes.put(p, times);
		}
	}

	public static String serialize(Date date) {
		Calendar c = getCal(date);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		return (hour + ", " + minute + ", " + second);
	}

	public static Date deserialize(String date) {
		String[] s = date.split(", ");
		int hour = 0;
		int minute = 0;
		int second = 0;

		if (s.length == 1) {
			hour = Integer.valueOf(s[0]);
		}
		if (s.length == 2) {
			minute = Integer.valueOf(s[1]);
		}
		if (s.length == 3) {
			second = Integer.valueOf(s[2]);
		}

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + hour);
		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + minute);
		cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + second);
		return cal.getTime();
	}

	public static String format(Date date) {
		Calendar c = getCal(date);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);

		c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + hour);
		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + minute);
		c.set(Calendar.SECOND, c.get(Calendar.SECOND) + second);

		// TODO: calculate the remaining time
		/*long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long millis = System.currentTimeMillis() - c.getTime().getTime();
		millis = millis % daysInMilli;

		long rsHour = TimeUnit.MILLISECONDS.toHours(millis) / hoursInMilli;
		millis = millis % hoursInMilli;

		long rsMinute = TimeUnit.MILLISECONDS.toMinutes(millis) / minutesInMilli;
		millis = millis % minutesInMilli;

		long rsSecond = TimeUnit.MILLISECONDS.toSeconds(millis) / secondsInMilli;*/

		return ((hour > 0 ? hour + "h " : "") + (minute > 0 ? minute + "min " : "")
				+ (second > 0 ? second + "sec" : ""));
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

	public static boolean isRunning(Player pl) {
		if (playerTimes.containsKey(pl)) {
			Date date = new Date();
			if (date.before(getTimeByPlayer(pl))) {
				return true;
			}
		}

		return false;
	}

	public static Date getTimeByPlayer(Player p) {
		if (playerTimes.containsKey(p)) {
			for (Entry<String, Date> m : playerTimes.get(p).entrySet()) {
				return m.getValue();
			}
		}
		return null;
	}

	public static Map<Player, Map<String, Date>> getPlayerTimes() {
		return java.util.Collections.unmodifiableMap(playerTimes);
	}
}
