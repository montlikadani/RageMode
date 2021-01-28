package hu.montlikadani.ragemode;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.API.RageModeAPI;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class Placeholder extends PlaceholderExpansion {

	Placeholder() {
	}

	public static final String IDENTIFIER = "ragemode";
	public static final String PREFIX = IDENTIFIER + '_';

	public enum RMPlaceholders {
		KILLS,
		AXE_KILLS,
		DIRECT_ARROW_KILLS,
		EXPLOSION_KILLS,
		KNIFE_KILLS,
		ZOMBIE_KILLS,
		DEATHS,
		AXE_DEATHS,
		DIRECT_ARROW_DEATHS,
		EXPLOSION_DEATHS,
		KNIFE_DEATHS,
		CURRENT_STREAK,
		LONGEST_STREAK,
		POINTS,
		GAMES,
		WINS,
		KD,

		PLAYER_LIVES,

		STATE("game"),
		PLAYERS("game"),
		SPECTATOR_PLAYERS("game"),
		MAXPLAYERS("game"),
		ZOMBIES_ALIVE("game"),
		TYPE("game"),
		;

		private String requirements = "";
		private String gameName = "";

		private RMPlaceholders() {
		}

		private RMPlaceholders(String requirements) {
			this.requirements = requirements;
		}

		public boolean isComplexed() {
			return !requirements.isEmpty();
		}

		public String getRequirements() {
			return requirements;
		}

		public String getFullName() {
			return PREFIX + name();
		}

		public String getGameName() {
			return gameName;
		}

		public void setGameName(String gameName) {
			this.gameName = gameName;
		}

		public static RMPlaceholders getByIdentifier(final String id) {
			String original = id;
			String gameName = id;
			if (gameName.contains("_")) {
				for (int i = gameName.length() - 1; i > 0; i--) {
					if (gameName.charAt(i) == '_') {
						gameName = gameName.substring(i + 1);
						original = original.substring(0, i);
						break;
					}
				}
			}

			boolean gameDefined = GameUtils.isGameExist(gameName);

			for (RMPlaceholders holder : values()) {
				if (id.equalsIgnoreCase(holder.toString())) {
					return holder;
				}
			}

			for (RMPlaceholders holder : values()) {
				if (id.equalsIgnoreCase(holder.getFullName())) {
					return holder;
				}
			}

			for (RMPlaceholders holder : values()) {
				if (StringUtils.startsWithIgnoreCase(original, holder.toString()) && holder.isComplexed()) {
					if (gameDefined) {
						holder.setGameName(gameName);
					}

					return holder;
				}
			}

			return null;
		}
	}

	@Override
	public String getAuthor() {
		return RageModeAPI.getPlugin().getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String onPlaceholderRequest(Player p, String v) {
		if (p == null)
			return "";

		RMPlaceholders placeholder = RMPlaceholders.getByIdentifier(v);
		if (placeholder == null) {
			return "";
		}

		PlayerPoints rpp = RuntimePPManager.getPPForPlayer(p.getUniqueId());
		if (rpp != null) {
			switch (placeholder) {
			case KILLS:
				return Integer.toString(rpp.getKills());
			case AXE_KILLS:
				return Integer.toString(rpp.getAxeKills());
			case DIRECT_ARROW_KILLS:
				return Integer.toString(rpp.getDirectArrowKills());
			case EXPLOSION_KILLS:
				return Integer.toString(rpp.getExplosionKills());
			case KNIFE_KILLS:
				return Integer.toString(rpp.getKnifeKills());
			case ZOMBIE_KILLS:
				return Integer.toString(rpp.getZombieKills());
			case DEATHS:
				return Integer.toString(rpp.getDeaths());
			case AXE_DEATHS:
				return Integer.toString(rpp.getAxeDeaths());
			case DIRECT_ARROW_DEATHS:
				return Integer.toString(rpp.getDirectArrowDeaths());
			case EXPLOSION_DEATHS:
				return Integer.toString(rpp.getExplosionDeaths());
			case KNIFE_DEATHS:
				return Integer.toString(rpp.getKnifeDeaths());
			case CURRENT_STREAK:
				return Integer.toString(rpp.getCurrentStreak());
			case LONGEST_STREAK:
				return Integer.toString(rpp.getLongestStreak());
			case POINTS:
				return Integer.toString(rpp.getPoints());
			case GAMES:
				return Integer.toString(rpp.getGames());
			case WINS:
				return Integer.toString(rpp.getWins());
			case KD:
				return Double.toString(rpp.getKD());
			default:
				break;
			}
		}

		if (placeholder == RMPlaceholders.PLAYER_LIVES && GameUtils.isPlayerPlaying(p)) {
			PlayerManager pm = GameUtils.getGameByPlayer(p).getPlayerManager(p).orElse(null);
			if (pm != null) {
				return Integer.toString(pm.getPlayerLives());
			}
		}

		String gameName = placeholder.getGameName();
		if (gameName.isEmpty()) {
			return "";
		}

		switch (placeholder) {
		case STATE:
			return GameUtils.getGame(gameName).getStatus().toString().toLowerCase();
		case PLAYERS:
			return Integer.toString(GameUtils.getGame(gameName).getPlayers().size());
		case SPECTATOR_PLAYERS:
			return Integer.toString(GameUtils.getGame(gameName).getSpectatorPlayers().size());
		case MAXPLAYERS:
			return Integer.toString(GameUtils.getGame(gameName).maxPlayers);
		case ZOMBIES_ALIVE:
			hu.montlikadani.ragemode.area.GameArea area = GameAreaManager.getAreaByGame(GameUtils.getGame(gameName));
			if (area != null) {
				return Integer.toString(area.getEntities(org.bukkit.entity.EntityType.ZOMBIE).size());
			}

			break;
		case TYPE:
			return GameUtils.getGame(gameName).getGameType().toString().toLowerCase();
		default:
			return "";
		}

		return "";
	}
}
