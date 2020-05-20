package hu.montlikadani.ragemode.events;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMPlayerKilledEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerPreRespawnEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerRespawnedEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.AntiCheatChecks;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.libs.Sounds;
import hu.montlikadani.ragemode.scores.KilledWith;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.utils.MaterialUtil;

public class GameListener implements Listener {

	private RageMode plugin;

	private final Map<UUID, UUID> explosionVictims = new HashMap<>();
	private final Map<UUID, UUID> grenadeExplosionVictims = new HashMap<>();

	public GameListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChatFormat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();

		// Cancels the spectator player chat
		if (GameUtils.isSpectatorPlaying(p)) {
			event.setCancelled(true);
			return;
		}

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(p);
		GameStatus status = game.getStatus();

		if (status == GameStatus.WAITING && !ConfigValues.isChatEnabledinLobby()
				&& !hasPerm(p, "ragemode.bypass.lobby.lockchat")) {
			event.setCancelled(true);
			sendMessage(p, RageMode.getLang().get("game.lobby.chat-is-disabled"));
			return;
		}

		if (status == GameStatus.RUNNING) {
			if (!ConfigValues.isEnableChatInGame() && !hasPerm(p, "ragemode.bypass.game.lockchat")) {
				sendMessage(p, RageMode.getLang().get("game.chat-is-disabled"));
				event.setCancelled(true);
				return;
			}

			if (ConfigValues.isChatFormatEnabled()) {
				String format = ConfigValues.getChatFormat();
				format = format.replace("%player%", p.getName());
				format = format.replace("%player-displayname%", p.getDisplayName());
				format = format.replace("%game%", game.getName());
				format = format.replace("%online-ingame-players%", Integer.toString(game.getPlayers().size()));
				format = format.replace("%message%", event.getMessage());
				format = Utils.setPlaceholders(format, p);

				event.setFormat(Utils.colors(format));
			}
		}

		if (!ConfigValues.isEnableChatAfterEnd() && status == GameStatus.GAMEFREEZE) {
			sendMessage(p, RageMode.getLang().get("game.game-freeze.chat-is-disabled"));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		// RageArrow explosion event
		if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player
				&& GameUtils.isPlayerPlaying((Player) event.getEntity().getShooter())
				&& event.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getEntity();
			Player shooter = (Player) arrow.getShooter();
			if (shooter == null) {
				return;
			}

			Game game = GameUtils.getGameByPlayer(shooter);
			GameStatus status = game.getStatus();

			if (status == GameStatus.GAMEFREEZE && GameUtils.isGameInFreezeRoom(game)) {
				arrow.remove();
				return;
			}

			if (status == GameStatus.RUNNING) {
				Location location = arrow.getLocation();
				double x = location.getX(),
						y = location.getY(),
						z = location.getZ();

				List<Entity> nears = arrow.getNearbyEntities(10, 10, 10);

				arrow.getWorld().createExplosion(x, y, z, 2f, false, false);
				arrow.remove();

				int i = 0;
				int imax = nears.size();
				while (i < imax) {
					if (nears.get(i) instanceof Player) {
						Player near = (Player) nears.get(i);
						if (explosionVictims.containsKey(near.getUniqueId())) {
							explosionVictims.remove(near.getUniqueId());
						}
						explosionVictims.put(near.getUniqueId(), shooter.getUniqueId());

						near.removeMetadata("killedWith", plugin);
						near.setMetadata("killedWith", new FixedMetadataValue(plugin, "explosion"));
					}

					i++;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHit(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player victim = (Player) event.getEntity();
		if (!GameUtils.isPlayerPlaying(victim)) {
			return;
		}

		// Prevent player damage in lobby
		if (GameUtils.getGameByPlayer(victim).getStatus() == GameStatus.WAITING) {
			event.setCancelled(true);
		}

		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			if (!GameUtils.isPlayerPlaying(damager)) {
				return; // Preventing non-playing players damaging to playing players
			}

			if (GameUtils.isPlayerInFreezeRoom(damager)) {
				event.setCancelled(true);
				return;
			}
		}

		victim.removeMetadata("killedWith", plugin);

		if (GameUtils.getGameByPlayer(victim).getStatus() != GameStatus.RUNNING) {
			return;
		}

		double finalDamage = 0d;
		String tool = "";

		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();

			if (GameUtils.isPlayerPlaying(damager)) {
				ItemStack hand = NMS.getItemInHand(damager);
				ItemMeta meta = hand.getItemMeta();
				ItemHandler knife = Items.getRageKnife();
				if (knife != null && meta != null && meta.hasDisplayName()
						&& meta.getDisplayName().equals(knife.getDisplayName())) {
					finalDamage = knife.getDamage();
					tool = "knife";
				}
			}
		} else if (event.getDamager() instanceof org.bukkit.entity.Egg) {
			finalDamage = 2.20d;
			tool = "grenade";
		} else if (event.getDamager() instanceof Snowball) {
			finalDamage = 25d;
			tool = "snowball";
		} else if (event.getDamager() instanceof Arrow) {
			finalDamage = 3.35d;
			tool = "arrow";
		}

		if (finalDamage > 0d) {
			event.setDamage(finalDamage);
		}

		if (!tool.isEmpty()) {
			victim.setMetadata("killedWith", new FixedMetadataValue(plugin, tool));
		}
	}

	@EventHandler
	public void onHitPlayer(EntityDamageEvent event) {
		Entity e = event.getEntity();
		if (!(e instanceof Player)) {
			return;
		}

		Player victim = (Player) e;
		if (!GameUtils.isPlayerPlaying(victim)) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(victim);

		// Hit player event
		if (GameUtils.getGameByPlayer(victim).getStatus() == GameStatus.RUNNING) {
			if (event.getCause().equals(DamageCause.FALL) && !ConfigValues.isDamagePlayerFall()) {
				event.setCancelled(true);
				return;
			}

			if (GameUtils.isGameInFreezeRoom(game)) {
				event.setCancelled(true);
			}
		} else if (GameUtils.getGameByPlayer(victim).getStatus() == GameStatus.GAMEFREEZE) {
			// Prevent damage in game freeze
			if (GameUtils.isGameInFreezeRoom(game)) {
				event.setCancelled(true);
			}
		} else if (GameUtils.getGameByPlayer(victim).getStatus() == GameStatus.WAITING) {
			event.setCancelled(true); // Prevent player damage in lobby
		}
	}

	@EventHandler
	public void onBowShoot(EntityShootBowEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player p = (Player) e.getEntity();
		if (GameUtils.isPlayerInFreezeRoom(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player deceased = event.getEntity();
		if (deceased == null) {
			return;
		}

		if (!GameUtils.isPlayerPlaying(deceased)) {
			return;
		}

		if (GameUtils.getGameByPlayer(deceased).getStatus() != GameStatus.RUNNING) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(deceased);
		boolean killerExists = deceased.getKiller() != null;

		if ((killerExists && GameUtils.isPlayerPlaying(deceased.getKiller())) || !killerExists) {
			boolean doDeathBroadcast = ConfigValues.isDefaultDeathMessageEnabled();

			if (plugin.getConfiguration().getArenasCfg().isSet("arenas." + game.getName() + ".death-messages")) {
				String gameBroadcast = plugin.getConfiguration().getArenasCfg()
						.getString("arenas." + game.getName() + ".death-messages", "");
				if (!gameBroadcast.isEmpty() && gameBroadcast.equals("true") || gameBroadcast.equals("false")) {
					doDeathBroadcast = Boolean.parseBoolean(gameBroadcast);
				}
			}

			String message = "";
			String deceaseName = deceased.getName();
			List<MetadataValue> data = deceased.getMetadata("killedWith");
			RMPlayerKilledEvent killed = null;

			if (!data.isEmpty()) {
				switch (data.get(0).asString()) {
				case "arrow":
					if (!killerExists) {
						message = RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceaseName,
								"%killer%", deceaseName);

						RageScores.addPointsToPlayer(deceased, deceased, KilledWith.RAGEBOW);
					} else {
						message = RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceaseName,
								"%killer%", deceased.getKiller().getName());

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.RAGEBOW);
					}

					killed = new RMPlayerKilledEvent(game, deceased, deceased.getKiller(), KilledWith.RAGEBOW);
					break;
				case "snowball":
					if (!killerExists) {
						message = RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceaseName, "%killer%",
								deceaseName);

						RageScores.addPointsToPlayer(deceased, deceased, KilledWith.COMBATAXE);
					} else {
						message = RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceaseName, "%killer%",
								deceased.getKiller().getName());

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.COMBATAXE);
					}

					killed = new RMPlayerKilledEvent(game, deceased, deceased.getKiller(), KilledWith.COMBATAXE);
					break;
				case "explosion":
					if (explosionVictims.containsKey(deceased.getUniqueId())) {
						message = RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%", deceaseName,
								"%killer%", Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())).getName());

						RageScores.addPointsToPlayer(Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())),
								deceased, KilledWith.EXPLOSION);
					} else if (grenadeExplosionVictims.containsKey(deceased.getUniqueId())) {
						message = RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%", deceaseName,
								"%killer%",
								Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())).getName());

						RageScores.addPointsToPlayer(
								Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())), deceased,
								KilledWith.EXPLOSION);
					} else {
						message = RageMode.getLang().get("game.broadcast.error-kill");

						sendMessage(deceased, RageMode.getLang().get("game.unknown-killer"));
					}

					killed = new RMPlayerKilledEvent(game, deceased, deceased.getKiller(), KilledWith.EXPLOSION);
					break;
				case "grenade":
					if (!killerExists) {
						message = RageMode.getLang().get("game.broadcast.grenade-kill", "%victim%", deceaseName,
								"%killer%", deceaseName);

						RageScores.addPointsToPlayer(deceased, deceased, KilledWith.GRENADE);
					} else {
						message = RageMode.getLang().get("game.broadcast.grenade-kill", "%victim%", deceaseName,
								"%killer%", deceased.getKiller().getName());

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.GRENADE);
					}

					killed = new RMPlayerKilledEvent(game, deceased, deceased.getKiller(), KilledWith.GRENADE);
					break;
				case "knife":
					if (!killerExists) {
						message = RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceaseName,
								"%killer%", deceaseName);

						RageScores.addPointsToPlayer(deceased, deceased, KilledWith.RAGEKNIFE);
					} else {
						message = RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceaseName,
								"%killer%", deceased.getKiller().getName());

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.RAGEKNIFE);
					}

					killed = new RMPlayerKilledEvent(game, deceased, deceased.getKiller(), KilledWith.RAGEKNIFE);
					break;
				default:
					message = RageMode.getLang().get("game.unknown-weapon", "%victim%", deceaseName);
					break;
				}

				if (doDeathBroadcast && !message.isEmpty()) {
					GameUtils.broadcastToGame(game, message);
				}

				deceased.removeMetadata("killedWith", plugin);
				if (killed != null) {
					Utils.callEvent(killed);
				}
			}

			event.setDroppedExp(0);
			event.setDeathMessage("");
			event.setKeepInventory(true);
			event.getDrops().clear();

			GameUtils.runCommands(deceased, game.getName(), "death");
			if (killerExists) {
				hu.montlikadani.ragemode.gameLogic.Bonus.addKillBonus(deceased.getKiller());
			}
		}

		// respawn player instantly
		if (RageMode.isSpigot()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, deceased.spigot()::respawn, 1L);
		}

		GameUtils.addGameItems(deceased, true);
	}

	// The event priority should be HIGHEST to prevent overwriting
	// spawns over plugins
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(p);

		// If player still is in game with respawn screen then remove player
		// 1.14 issue
		if (!game.isGameRunning()) {
			game.removePlayer(p);
			return;
		}

		RMPlayerPreRespawnEvent preRespawn = new RMPlayerPreRespawnEvent(game, p);
		Utils.callEvent(preRespawn);
		if (preRespawn.isCancelled()) {
			return;
		}

		GameSpawn gsg = GameUtils.getGameSpawn(game.getName());
		if (gsg.getSpawnLocations().size() < 0) {
			return;
		}

		e.setRespawnLocation(gsg.getRandomSpawn());

		int time = ConfigValues.getRespawnProtectTime();
		if (time > 0) {
			p.setNoDamageTicks(time * 20);
		}

		Utils.callEvent(new RMPlayerRespawnedEvent(game, p, gsg));
	}

	@EventHandler
	public void onHungerGain(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && GameUtils.isPlayerPlaying((Player) event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		if (GameUtils.isPlayerPlaying(ev.getPlayer()))
			ev.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDrop(PlayerDropItemEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void disableCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String arg = event.getMessage().trim().toLowerCase();

		if (ConfigValues.isSpectatorEnabled() && GameUtils.isSpectatorPlaying(p)) {
			List<String> cmds = ConfigValues.getSpectatorCmds();
			if (!cmds.isEmpty() && !cmds.contains(arg) && !hasPerm(p, "ragemode.bypass.spectatorcommands")) {
				sendMessage(p, RageMode.getLang().get("game.this-command-is-disabled-in-game"));
				event.setCancelled(true);
				return;
			}
		}

		if (GameUtils.isPlayerPlaying(p)) {
			if (ConfigValues.isCommandsDisabledInEndGame() && GameUtils.isPlayerInFreezeRoom(p)) {
				sendMessage(p, RageMode.getLang().get("game.command-disabled-in-end-game"));
				event.setCancelled(true);
				return;
			}

			List<String> cmds = ConfigValues.getAllowedCmds();
			if (!cmds.isEmpty() && !cmds.contains(arg) && !hasPerm(p, "ragemode.bypass.disabledcommands")) {
				sendMessage(p, RageMode.getLang().get("game.this-command-is-disabled-in-game"));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent ev) {
		if (!ConfigValues.isUseGrenadeTrails()) {
			return;
		}

		Projectile proj = ev.getEntity();
		if (!(proj.getShooter() instanceof Player)) {
			return;
		}

		Player shooter = (Player) proj.getShooter();
		if (shooter == null) {
			return;
		}

		if (!GameUtils.isPlayerPlaying(shooter)) {
			return;
		}

		if (GameUtils.isPlayerInFreezeRoom(shooter)) {
			ev.setCancelled(true);
			return;
		}

		if (proj instanceof Arrow && ConfigValues.isPreventFastBowEvent() && AntiCheatChecks.checkBowShoot(shooter)) {
			ev.setCancelled(true);
			return;
		}

		if (proj instanceof Egg) {
			setTrails(ev.getEntity());
		}
	}

	@EventHandler
	public void eggThrow(PlayerEggThrowEvent event) {
		final Player p = event.getPlayer();
		if (!p.isOnline()) {
			return;
		}

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		if (GameUtils.getGameByPlayer(p).getStatus() != GameStatus.RUNNING
				&& GameUtils.getGameByPlayer(p).getStatus() != GameStatus.GAMEFREEZE) {
			return;
		}

		// Removes the egg from player inventory and prevent
		// other item remove when moved the slot to another
		/*if (p.getInventory().contains(Material.EGG)) {
			ItemStack item = p.getInventory().getItem(plugin.getConfiguration().getCfg().getInt("items.grenade.slot"));
			if (item != null) {
				item.setAmount(item.getAmount() - 1);
			}
		}*/

		// no baby chickens
		event.setHatching(false);

		// check if player is in waiting game (game freeze)
		if (GameUtils.isPlayerInFreezeRoom(p)) {
			return;
		}

		final Item grenade = p.getWorld().dropItem(event.getEgg().getLocation(), new ItemStack(Material.EGG));

		// Cancel egg pick up
		grenade.setPickupDelay(41);

		// make custom name
		if (Items.getGrenade() != null && !Items.getGrenade().getCustomName().isEmpty()) {
			grenade.setCustomName(Items.getGrenade().getCustomName());
			grenade.setCustomNameVisible(true);
		}

		grenade.setVelocity(p.getEyeLocation().getDirection());
		// move egg from land location to simulate bounce
		grenade.getLocation().add(event.getEgg().getVelocity());

		final List<Entity> nears = grenade.getNearbyEntities(13, 13, 13);

		final Location loc = grenade.getLocation();

		Sounds.playSound(loc,
				Version.isCurrentEqualOrLower(Version.v1_8_R3) ? Sounds.CREEPER_HISS : Sounds.ENTITY_CREEPER_PRIMED, 1,
				1);

		new BukkitRunnable() {
			@Override
			public void run() {
				// Remove egg when the player is not in-game or is in freeze room.
				if (!GameUtils.isPlayerPlaying(p) || GameUtils.isPlayerInFreezeRoom(p)) {
					grenade.remove();
					cancel();
					return;
				}

				double gX = loc.getX(),
						gY = loc.getY(),
						gZ = loc.getZ();

				grenade.getWorld().createExplosion(gX, gY, gZ, 3f, false, false);
				grenade.remove(); // get rid of egg so it can't be picked up

				int i = 0;
				int imax = nears.size();
				while (i < imax) {
					if (nears.get(i) instanceof Player) {
						Player near = (Player) nears.get(i);
						if (grenadeExplosionVictims.containsKey(near.getUniqueId())) {
							grenadeExplosionVictims.remove(near.getUniqueId());
						}
						grenadeExplosionVictims.put(near.getUniqueId(), p.getUniqueId());

						near.removeMetadata("killedWith", plugin);
						near.setMetadata("killedWith", new FixedMetadataValue(plugin, "explosion"));
					}

					i++;
				}
			}
		}.runTaskLater(plugin, 40);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		if (GameUtils.isPlayerInFreezeRoom(p)) {
			return;
		}

		if (GameUtils.getGameByPlayer(p).getStatus() == GameStatus.RUNNING) {
			Player thrower = event.getPlayer();
			ItemStack hand = NMS.getItemInHand(thrower);
			ItemMeta meta = hand.getItemMeta();
			if (meta != null && meta.hasDisplayName() && Items.getCombatAxe() != null
					&& meta.getDisplayName().equals(Items.getCombatAxe().getDisplayName())) {
				thrower.launchProjectile(Snowball.class);
				NMS.setItemInHand(thrower, null);
			}
		}

		ItemStack hand = NMS.getItemInHand(p);
		Action action = event.getAction();

		/**
		 * Cancels the rage knife usage, for example with collecting beehives
		 * 
		 * For this we don't need to check all game items, because there is no
		 * possibility to change the item types from config.
		 */
		if (action == Action.RIGHT_CLICK_BLOCK && Items.getRageKnife() != null
				&& hand.getType().equals(Items.getRageKnife().getItem())) {
			event.setCancelled(true);
		}

		if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR
				|| action == Action.LEFT_CLICK_BLOCK)
				&& GameUtils.getGameByPlayer(p).getStatus() == GameStatus.WAITING) {
			ItemMeta meta = hand.getItemMeta();
			if (meta != null && meta.hasDisplayName()) {
				Game game = GameUtils.getGameByPlayer(p);

				if (hasPerm(p, "ragemode.admin.item.forcestart") && Items.getForceStarter() != null
						&& Items.getForceStarter().getDisplayName().equals(meta.getDisplayName())) {
					GameUtils.forceStart(game);
					sendMessage(p, RageMode.getLang().get("commands.forcestart.game-start", "%game%", game.getName()));
				}

				if (Items.getLeaveGameItem() != null
						&& Items.getLeaveGameItem().getDisplayName().equals(meta.getDisplayName())) {
					GameUtils.leavePlayer(p, game);
				}

				if (Items.getShopItem() != null && Items.getShopItem().getDisplayName().equals(meta.getDisplayName())) {
					game.getShop().openMainPage(p);
				}
			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		Player p = (Player) e.getPlayer();

		if (GameUtils.isPlayerPlaying(p) && GameUtils.getGameByPlayer(p).getStatus() == GameStatus.RUNNING
				&& e.getInventory().getType() != InventoryType.PLAYER) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		if (GameUtils.isPlayerPlaying(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	// To prevent removing paintings
	@EventHandler
	public void onHanging(HangingBreakByEntityEvent ev) {
		if (ev.getRemover() != null && ev.getRemover() instanceof Player
				&& GameUtils.isPlayerPlaying((Player) ev.getRemover())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractRedstone(PlayerInteractEvent ev) {
		if (ev.getClickedBlock() == null) {
			return;
		}

		Player p = ev.getPlayer();
		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		Action action = ev.getAction();
		Material t = ev.getClickedBlock().getType();

		if (action == Action.PHYSICAL) {
			if (t == Material.FARMLAND) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			} else if (ConfigValues.isCancelRedstoneActivate()
					&& GameUtils.getGameByPlayer(p).getStatus() == GameStatus.RUNNING
					|| GameUtils.getGameByPlayer(p).getStatus() == GameStatus.GAMEFREEZE) {
				if (MaterialUtil.isWoodenPressurePlate(t)) {
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setCancelled(true);
				}

				if (t.equals(Material.STONE_PRESSURE_PLATE) || t.equals(Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
						|| t.equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setCancelled(true);
				}
			}
		} else if (ConfigValues.isCancelRedstoneActivate() && action == Action.RIGHT_CLICK_BLOCK) {
			if (GameUtils.getGameByPlayer(p).getStatus() == GameStatus.RUNNING
					|| GameUtils.getGameByPlayer(p).getStatus() == GameStatus.GAMEFREEZE) {
				if (MaterialUtil.isTrapdoor(t) || MaterialUtil.isButton(t)) {
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setCancelled(true);
				}

				if (Version.isCurrentEqualOrLower(Version.v1_12_R1)) {
					if (t.equals(Material.valueOf("REDSTONE_COMPARATOR"))
							|| t.equals(Material.valueOf("REDSTONE_COMPARATOR_ON"))
							|| t.equals(Material.valueOf("REDSTONE_COMPARATOR_OFF"))) {
						ev.setUseInteractedBlock(Event.Result.DENY);
						ev.setCancelled(true);
					}
				} else if (t.equals(Material.COMPARATOR) || t.equals(Material.REPEATER)) {
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setCancelled(true);
				}

				if (t.equals(Material.LEVER) || t.equals(Material.DAYLIGHT_DETECTOR)) {
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setCancelled(true);
				}
			}
		}

		if (ConfigValues.isCancelDoorUse() && action == Action.RIGHT_CLICK_BLOCK
				&& GameUtils.getGameByPlayer(p).getStatus() == GameStatus.RUNNING
				|| GameUtils.getGameByPlayer(p).getStatus() == GameStatus.GAMEFREEZE) {
			if (MaterialUtil.isWoodenDoor(t)) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			}
		}

		// Just prevent usage of bush
		if (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && action == Action.RIGHT_CLICK_BLOCK) {
			if (t.equals(Material.SWEET_BERRY_BUSH) || t.equals(Material.COMPOSTER)) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (e.getEntered() instanceof Player && GameUtils.isPlayerPlaying((Player) e.getEntered()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		Player p = (Player) event.getWhoClicked();

		if (GameUtils.isPlayerPlaying(p) && GameUtils.getGameByPlayer(p).getStatus() == GameStatus.WAITING
				&& !(event.getInventory().getHolder() instanceof IShop)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onFly(PlayerToggleFlightEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void playerBedEnter(PlayerBedEnterEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		Game game = GameUtils.getGameByPlayer(p);

		if (game.getStatus() == GameStatus.GAMEFREEZE && GameUtils.isGameInFreezeRoom(game)) {
			if (!ConfigValues.isFreezePlayers()) {
				return;
			}

			Location from = event.getFrom(),
					to = event.getTo();

			double x = Math.floor(from.getX()),
					y = Math.floor(from.getY()),
					z = Math.floor(from.getZ());

			if (Math.floor(to.getX()) != x || Math.floor(to.getY()) != y || Math.floor(to.getZ()) != z) {
				x += .5;
				y += .5;
				z += .5;

				p.teleport(new Location(from.getWorld(), x, y, z, from.getYaw(), from.getPitch()));
			}
		} else if (p.getLocation().getY() < 0) {
			p.teleport(GameUtils.getGameSpawn(game).getRandomSpawn());

			// Prevent damaging player when respawned
			p.setFallDistance(0);

			GameUtils.broadcastToGame(game, RageMode.getLang().get("game.void-fall", "%player%", p.getName()));
		}
	}

	private void setTrails(Projectile proj) {
		Thread thread = new Thread(() -> {
			do {
				try {
					Thread.sleep(155L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				proj.getWorld().playEffect(proj.getLocation(), org.bukkit.Effect.SMOKE, 0);
			} while (!proj.isDead() && !proj.isOnGround());
		});

		thread.start();
	}
}
