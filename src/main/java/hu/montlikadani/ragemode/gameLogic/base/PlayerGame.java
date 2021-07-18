package hu.montlikadani.ragemode.gameLogic.base;

import java.util.Optional;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.SpectatorJoinToGameEvent;
import hu.montlikadani.ragemode.API.event.SpectatorLeaveGameEvent;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.utils.Utils;

public class PlayerGame extends BaseGame {

	public PlayerGame(@org.jetbrains.annotations.NotNull String name) {
		super(name, GameType.NORMAL);
	}

	@Override
	public final boolean addPlayer(Player player, boolean spectator) {
		if (spectator) {
			PlayerManager pm = new PlayerManager(player.getUniqueId(), gameName);
			players.add(pm);

			if (!ConfigValues.isBungee()) {
				pm.storePlayerTools(true);
			}

			player.getInventory().clear();
			player.updateInventory();

			Utils.callEvent(new SpectatorJoinToGameEvent(this, player));
			return true;
		}

		if (running) {
			player.sendMessage(RageMode.getLang().get("game.running"));
			return false;
		}

		if (isPlayerInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		acList.add(new ActionMessengers(player.getUniqueId()));

		PlayerManager pm = new PlayerManager(player.getUniqueId(), gameName);
		savePlayerData(pm);

		if (players.size() < maxPlayers) {
			players.add(pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", gameName));

			if (players.size() == minPlayers) {
				gameLobby.getLobbyTimer().beginScheduling(this);
			}

			return true;
		}

		// Gets a random player who is in game and kicks from the game to join the VIP
		// player.
		if (ConfigValues.isKickRandomPlayerIfJoinsVip() && player.hasPermission("ragemode.vip") && hasRoomForVIP()) {
			boolean isVIP = false;
			int b = -1;
			Player playerToKick;

			do {
				int kickposition = maxPlayers < 2 ? 0
						: java.util.concurrent.ThreadLocalRandom.current().nextInt(maxPlayers - 1);

				playerToKick = com.google.common.collect.Iterables.get(players, kickposition).getPlayer();
				isVIP = playerToKick != null && playerToKick.hasPermission("ragemode.vip");

				b++;
			} while (isVIP && b < players.size());

			if (playerToKick == null) {
				return false;
			}

			playerToKick.getInventory().clear();

			getPlayerManager(playerToKick).ifPresent(pmToKick -> {
				pmToKick.giveBackTools();
				players.remove(pmToKick);
			});

			playerToKick.updateInventory();
			playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

			if (players.add(pm)) {
				player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", gameName));

				if (players.size() == minPlayers) {
					gameLobby.getLobbyTimer().beginScheduling(this);
				}
			}

			return true;
		}

		player.sendMessage(RageMode.getLang().get("game.full"));
		return false;
	}

	@Override
	public final boolean removePlayer(final Player player, boolean switchToSpec) {
		Optional<PlayerManager> opt = getPlayerManager(player);

		if (!opt.isPresent()) {
			if (player != null) {
				player.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			}

			return false;
		}

		return removePlayer(opt.get(), switchToSpec);
	}

	@Override
	public final boolean removePlayer(PlayerManager pm, boolean switchToSpec) {
		Player player = pm.getPlayer();

		if (player != null) {
			player.getInventory().clear();
		}

		if (pm.isSpectator()) {
			pm.giveBackTools();

			if (player != null) {
				player.updateInventory();
				Utils.callEvent(new SpectatorLeaveGameEvent(this, player));
			}

			return players.remove(pm);
		}

		if (player != null) {
			acList.remove(removePlayerSynced(player));
		}

		if (switchToSpec) {
			pm.setSpectator(true);
		} else {
			pm.giveBackTools();
			players.remove(pm);
		}

		if (player != null) {
			player.updateInventory();

			// Send left message when switched to spec or not
			player.sendMessage(RageMode.getLang().get("game.player-left"));
		}

		return true;
	}
}
