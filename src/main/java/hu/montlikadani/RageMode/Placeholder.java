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
			return String.valueOf(pP.getKills());

		if (var.equals("axe_kills"))
			return String.valueOf(pP.getAxeKills());

		if (var.equals("direct_arrow_kills"))
			return String.valueOf(pP.getDirectArrowKills());

		if (var.equals("explosion_kills"))
			return String.valueOf(pP.getExplosionKills());

		if (var.equals("knife_kills"))
			return String.valueOf(pP.getKnifeKills());

		// Player death stats
		if (var.equals("deaths"))
			return String.valueOf(pP.getDeaths());

		if (var.equals("axe_deaths"))
			return String.valueOf(pP.getAxeDeaths());

		if (var.equals("direct_arrow_deaths"))
			return String.valueOf(pP.getDirectArrowDeaths());

		if (var.equals("explosion_deaths"))
			return String.valueOf(pP.getExplosionDeaths());

		if (var.equals("knife_deaths"))
			return String.valueOf(pP.getKnifeDeaths());

		// Other stats
		if (var.equals("current_streak"))
			return String.valueOf(pP.getCurrentStreak());

		if (var.equals("longest_streak"))
			return String.valueOf(pP.getLongestStreak());

		if (var.equals("points"))
			return String.valueOf(pP.getPoints());

		if (var.equals("games"))
			return String.valueOf(rpp.getGames());

		if (var.equals("wins"))
			return String.valueOf(rpp.getWins());

		if (var.equals("kd"))
			return String.valueOf(rpp.getKD());

		if (var.equals("rank"))
			return String.valueOf(rpp.getRank());

		return null;
	}
}
