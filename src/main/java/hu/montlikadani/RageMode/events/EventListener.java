package hu.montlikadani.ragemode.events;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.HashMap;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.signs.SignData;

public class EventListener implements Listener {

	private RageMode plugin;

	@Deprecated
	public static HashMap<String, Boolean> waitingGames = new HashMap<>();

	public EventListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (ConfigValues.isCheckForUpdates() && event.getPlayer().isOp()) {
			plugin.checkVersion("player");
		}

		HoloHolder.showAllHolosToPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();

		GameUtils.kickPlayer(p, GameUtils.getGameByPlayer(p));
		GameUtils.kickSpectator(p, GameUtils.getGameBySpectator(p));
		HoloHolder.deleteHoloObjectsOfPlayer(p);
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent ev) {
		Player p = ev.getPlayer();

		GameUtils.kickPlayer(p, GameUtils.getGameByPlayer(p));
		GameUtils.kickSpectator(p, GameUtils.getGameBySpectator(p));
		HoloHolder.deleteHoloObjectsOfPlayer(p);
	}

	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		if (event.isCancelled() || !ConfigValues.isSignsEnable()) {
			return;
		}

		org.bukkit.block.BlockState blockState = event.getBlock().getState();
		if (blockState instanceof Sign && SignCreator.isSign(blockState.getLocation())) {
			if (!hasPerm(event.getPlayer(), "ragemode.admin.signs")) {
				sendMessage(event.getPlayer(), RageMode.getLang().get("no-permission-to-interact-sign"));
				event.setCancelled(true);
				return;
			}

			SignCreator.removeSign((Sign) blockState);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		org.bukkit.block.Block b = event.getClickedBlock();
		if (ConfigValues.isSignsEnable() && b != null && b.getState() != null && b.getState() instanceof Sign
				&& event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (!hasPerm(p, "ragemode.join.sign")) {
				sendMessage(p, RageMode.getLang().get("no-permission"));
				return;
			}

			if (!SignCreator.isSign(b.getLocation())) {
				return;
			}

			for (SignData data : SignCreator.getSignData()) {
				if (data.getLocation().equals(b.getLocation())) {
					String name = data.getGame();
					if (name == null || !GameUtils.isGameWithNameExists(name)) {
						sendMessage(p, RageMode.getLang().get("game.does-not-exist"));
						break;
					}

					GameUtils.joinPlayer(p, GameUtils.getGame(name));
					break;
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled() || event.getPlayer() == null || event.getBlock() == null) {
			return;
		}

		if (!ConfigValues.isSignsEnable()) {
			return;
		}

		if (event.getLine(0).contains("[rm]")
				|| event.getLine(0).contains("[ragemode]")) {
			if (!hasPerm(event.getPlayer(), "ragemode.admin.signs")) {
				sendMessage(event.getPlayer(), RageMode.getLang().get("no-permission-to-interact-sign"));
				event.setCancelled(true);
				return;
			}

			String l1 = event.getLine(1);
			if (!GameUtils.isGameWithNameExists(l1)) {
				sendMessage(event.getPlayer(), RageMode.getLang().get("invalid-game", "%game%", l1));
				return;
			}

			for (Game game : plugin.getGames()) {
				if (game.getName().equalsIgnoreCase(l1)) {
					SignCreator.createNewSign((Sign) event.getBlock().getState(), game.getName());
					break;
				}
			}

			SignCreator.updateSign(((Sign) event.getBlock().getState()).getLocation());
		}
	}
}
