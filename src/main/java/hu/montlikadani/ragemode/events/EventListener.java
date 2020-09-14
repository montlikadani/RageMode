package hu.montlikadani.ragemode.events;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.EquipmentSlot;

import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.UpdateDownloader;

public class EventListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (p.isOp()) {
			UpdateDownloader.checkFromGithub(p);
		}

		HoloHolder.showAllHolosToPlayer(p);
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
		if (Version.isCurrentEqualOrHigher(Version.v1_9_R1) && event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		final Player p = event.getPlayer();
		final Block b = event.getClickedBlock();
		if (b == null) {
			return;
		}

		if (hasPerm(p, "ragemode.admin.area") && NMS.getItemInHand(p).getType()
				.equals(Material.getMaterial(ConfigValues.getSelectionItem().toUpperCase()))) {
			org.bukkit.Location loc = b.getLocation();

			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				RageMode.getInstance().getSelection().placeLoc1(p, loc);
				sendMessage(p, RageMode.getLang().get("commands.area.selected1", "%x%", loc.getBlockX(), "%y%",
						loc.getBlockY(), "%z%", loc.getBlockZ()));
			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				RageMode.getInstance().getSelection().placeLoc2(p, loc);
				sendMessage(p, RageMode.getLang().get("commands.area.selected2", "%x%", loc.getBlockX(), "%y%",
						loc.getBlockY(), "%z%", loc.getBlockZ()));
			}

			event.setCancelled(true);
			return;
		}

		if (ConfigValues.isSignsEnable() && event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& b.getState() instanceof Sign) {
			if (!hasPerm(p, "ragemode.join.sign")) {
				sendMessage(p, RageMode.getLang().get("no-permission"));
				return;
			}

			if (!SignCreator.isSign(b.getLocation())) {
				return;
			}

			final String name = SignCreator.getSignData(b.getLocation()) != null
					? SignCreator.getSignData(b.getLocation()).getGame()
					: null;
			if (name == null || !GameUtils.isGameWithNameExists(name)) {
				sendMessage(p, RageMode.getLang().get("game.does-not-exist"));
				return;
			}

			GameUtils.joinPlayer(p, GameUtils.getGame(name));
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled() || !ConfigValues.isSignsEnable()) {
			return;
		}

		Block b = event.getBlock();
		Player p = event.getPlayer();
		String l0 = event.getLine(0).toLowerCase();

		if (l0.contains("[rm]") || l0.contains("[ragemode]")) {
			if (!hasPerm(p, "ragemode.admin.signs")) {
				sendMessage(p, RageMode.getLang().get("no-permission-to-interact-sign"));
				event.setCancelled(true);
				return;
			}

			String l1 = event.getLine(1);
			if (!GameUtils.isGameWithNameExists(l1)) {
				sendMessage(p, RageMode.getLang().get("invalid-game", "%game%", l1));
				return;
			}

			Game game = GameUtils.getGame(l1);
			if (game != null) {
				SignCreator.createNewSign((Sign) b.getState(), game.getName());
			}

			SignCreator.updateSign(((Sign) b.getState()).getLocation());
		}
	}
}
