package hu.montlikadani.ragemode;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class Placeholder extends PlaceholderExpansion {

	@Override
	public String getAuthor() {
		return "montlikadani";
	}

	@Override
	public String getIdentifier() {
		return "ragemode";
	}

	public String getPlugin() {
		return null;
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	public String onPlaceholderRequest(Player p, String var) {
		if (p == null)
			return null;

		PlayerPoints pP = new PlayerPoints(p.getUniqueId().toString());
		RetPlayerPoints rpp = new RetPlayerPoints(p.getUniqueId().toString());
		// Player kill stats
		if (var.equals("kills"))
			return Integer.toString(pP.getKills());

		if (var.equals("axe_kills"))
			return Integer.toString(pP.getAxeKills());

		if (var.equals("direct_arrow_kills"))
			return Integer.toString(pP.getDirectArrowKills());

		if (var.equals("explosion_kills"))
			return Integer.toString(pP.getExplosionKills());

		if (var.equals("knife_kills"))
			return Integer.toString(pP.getKnifeKills());

		// Player death stats
		if (var.equals("deaths"))
			return Integer.toString(pP.getDeaths());

		if (var.equals("axe_deaths"))
			return Integer.toString(pP.getAxeDeaths());

		if (var.equals("direct_arrow_deaths"))
			return Integer.toString(pP.getDirectArrowDeaths());

		if (var.equals("explosion_deaths"))
			return Integer.toString(pP.getExplosionDeaths());

		if (var.equals("knife_deaths"))
			return Integer.toString(pP.getKnifeDeaths());

		// Other stats
		if (var.equals("current_streak"))
			return Integer.toString(pP.getCurrentStreak());

		if (var.equals("longest_streak"))
			return Integer.toString(pP.getLongestStreak());

		if (var.equals("points"))
			return Integer.toString(pP.getPoints());

		if (var.equals("games"))
			return Integer.toString(rpp.getGames());

		if (var.equals("wins"))
			return Integer.toString(rpp.getWins());

		if (var.equals("kd"))
			return String.valueOf(rpp.getKD());

		if (var.equals("rank"))
			return Integer.toString(rpp.getRank());

		return null;
	}
}
