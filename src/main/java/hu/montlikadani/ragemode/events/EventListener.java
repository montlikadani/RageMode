package hu.montlikadani.ragemode.events;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.signs.SignData;
import hu.montlikadani.ragemode.utils.Misc;
import hu.montlikadani.ragemode.utils.UpdateDownloader;
import hu.montlikadani.ragemode.utils.ServerVersion;

public final class EventListener implements org.bukkit.event.Listener {

	private final RageMode plugin;

	public EventListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (player.isOp()) {
			UpdateDownloader.checkFromGithub(player);
		}

		plugin.getHoloHolder().updateHologramsName(player);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		GameUtils.kickPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent ev) {
		GameUtils.kickPlayer(ev.getPlayer());
	}

	@EventHandler
	public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
		if (event.getEntityType() == org.bukkit.entity.EntityType.ARMOR_STAND) {
			plugin.getHoloHolder().deleteHologram(event.getEntity().getLocation());
		}
	}

	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		if (!ConfigValues.isSignsEnable() || !(event.getBlock().getState() instanceof Sign)) {
			return;
		}

		SignData signData = SignCreator.getSignData(event.getBlock().getLocation());
		if (signData == null) {
			return;
		}

		if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
			sendMessage(event.getPlayer(), RageMode.getLang().get("no-permission-to-interact-sign"));
			event.setCancelled(true);
			return;
		}

		SignCreator.removeSign(signData);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if ((ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_9_R1) && event.getHand() != EquipmentSlot.HAND)
				|| event.getClickedBlock() == null) {
			return;
		}

		final Player player = event.getPlayer();

		if (player.hasPermission("ragemode.admin.area")
				&& Misc.getItemInHand(player).getType() == ConfigValues.getSelectionItem()) {
			final org.bukkit.Location loc = event.getClickedBlock().getLocation();

			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				plugin.getSelection().placeLoc1(player.getUniqueId(), loc);

				sendMessage(player, RageMode.getLang().get("commands.area.selected1", "%x%", loc.getBlockX(), "%y%",
						loc.getBlockY(), "%z%", loc.getBlockZ()));
			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				plugin.getSelection().placeLoc2(player.getUniqueId(), loc);

				sendMessage(player, RageMode.getLang().get("commands.area.selected2", "%x%", loc.getBlockX(), "%y%",
						loc.getBlockY(), "%z%", loc.getBlockZ()));
			}

			event.setCancelled(true);
			return;
		}

		if (ConfigValues.isSignsEnable() && event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getState() instanceof Sign) {
			SignData data = SignCreator.getSignData(event.getClickedBlock().getLocation());

			if (data == null) {
				return;
			}

			if (!player.hasPermission("ragemode.join.sign")) {
				sendMessage(player, RageMode.getLang().get("no-permission"));
				return;
			}

			BaseGame game = GameUtils.getGame(data.getGameName());
			if (game == null) {
				sendMessage(player, RageMode.getLang().get("game.does-not-exist"));
				return;
			}

			GameUtils.joinPlayer(player, game);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		if (!ConfigValues.isSignsEnable()) {
			return;
		}

		org.bukkit.block.BlockState state = event.getBlock().getState();
		if (!(state instanceof Sign)) {
			return; // Probably never
		}

		String line0 = plugin.getComplement().getLine(event, 0).toLowerCase();

		if (!line0.contains("[rm]") && !line0.contains("[ragemode]")) {
			return;
		}

		Player player = event.getPlayer();

		if (!player.hasPermission("ragemode.admin.signs")) {
			sendMessage(player, RageMode.getLang().get("no-permission-to-interact-sign"));
			event.setCancelled(true);
			return;
		}

		String line1 = plugin.getComplement().getLine(event, 1);
		BaseGame game = GameUtils.getGame(line1);
		if (game == null) {
			sendMessage(player, RageMode.getLang().get("invalid-game", "%game%", line1));
			return;
		}

		SignCreator.createNewSign((Sign) state, game.getName()).updateSign();
	}
}
