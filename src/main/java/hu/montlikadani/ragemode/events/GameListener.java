package hu.montlikadani.ragemode.events;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.ClickType;
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
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerKilledEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerPreRespawnEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerRespawnedEvent;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.CacheableHitTarget;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.items.GameItems;
import hu.montlikadani.ragemode.items.handler.PressureMine;
import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.items.shop.LobbyShop;
import hu.montlikadani.ragemode.items.threads.CombatAxeThread;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.managers.RewardManager;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.utils.MaterialUtil;
import hu.montlikadani.ragemode.utils.Misc;
import hu.montlikadani.ragemode.utils.SchedulerUtil;
import hu.montlikadani.ragemode.utils.Utils;
import hu.montlikadani.ragemode.utils.ServerVersion;

@SuppressWarnings("deprecation")
public final class GameListener implements org.bukkit.event.Listener {

	private final RageMode plugin;
	private final LobbyShop lobbyShop = new LobbyShop();

	/**
	 * The set of pressure mines
	 */
	public static final Set<PressureMine> PRESSUREMINES = new HashSet<>();

	/**
	 * The field for storing hit targets
	 */
	public static final Map<UUID, CacheableHitTarget> HIT_TARGETS = new HashMap<>();

	public GameListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onGameLeave(RMGameLeaveAttemptEvent e) {
		LobbyShop.USER_PARTICLES.remove(e.getPlayerManager().getUniqueId());

		PRESSUREMINES.removeIf(mine -> {
			if (mine.getOwner().equals(e.getPlayerManager().getUniqueId())) {
				mine.removeMines();
				return true;
			}

			return false;
		});
	}

	@EventHandler
	public void onGameStop(RMGameStopEvent event) {
		PRESSUREMINES.forEach(PressureMine::removeMines);
		PRESSUREMINES.clear();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChatFormat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		PlayerManager pm = GameUtils.getPlayerManager(player);
		if (pm == null) {
			return;
		}

		// Cancels the spectator player chat
		if (pm.isSpectator()) {
			event.setCancelled(true);
			return;
		}

		BaseGame game = pm.getPlayerGame();
		if (game == null) {
			return;
		}

		if (game.getStatus() == GameStatus.WAITING && !ConfigValues.isChatEnabledinLobby()
				&& !player.hasPermission("ragemode.bypass.lobby.lockchat")) {
			event.setCancelled(true);
			sendMessage(player, RageMode.getLang().get("game.lobby.chat-is-disabled"));
			return;
		}

		if (game.getStatus() == GameStatus.RUNNING) {
			if (!ConfigValues.isEnableChatInGame() && !player.hasPermission("ragemode.bypass.game.lockchat")) {
				sendMessage(player, RageMode.getLang().get("game.chat-is-disabled"));
				event.setCancelled(true);
				return;
			}

			if (ConfigValues.isChatFormatEnabled()) {
				String format = ConfigValues.getChatFormat();

				format = format.replace("%player%", player.getName());
				format = format.replace("%player-displayname%", plugin.getComplement().getDisplayName(player));
				format = format.replace("%game%", game.getName());
				format = format.replace("%online-ingame-players%", Integer.toString(game.getPlayers().size()));
				format = format.replace("%message%", event.getMessage());
				format = Utils.setPlaceholders(format, player);

				event.setFormat(Utils.colors(format));
			}
		}

		if (!ConfigValues.isEnableChatAfterEnd() && game.getStatus() == GameStatus.GAMEFREEZE) {
			sendMessage(player, RageMode.getLang().get("game.game-freeze.chat-is-disabled"));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile proj = event.getEntity();

		if (!(proj.getShooter() instanceof Player)) {
			return;
		}

		Player shooter = (Player) proj.getShooter();
		BaseGame game = GameUtils.getGameByPlayer(shooter);
		if (game == null) {
			return;
		}

		// Trigger flash using snowball
		if (proj instanceof Snowball
				&& (!GameUtils.isGameInFreezeRoom(game) || game.getGameType() != GameType.APOCALYPSE)) {
			final Item flash = proj.getWorld().dropItem(proj.getLocation(),
					new ItemStack(
							ServerVersion.isCurrentLower(ServerVersion.v1_9_R1) ? Material.getMaterial("SNOW_BALL")
									: Material.SNOWBALL));

			flash.setPickupDelay(41);
			flash.setVelocity(shooter.getEyeLocation().getDirection());
			flash.getLocation().add(proj.getVelocity());

			plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
				if (flash.isDead()) {
					return;
				}

				if (!GameUtils.isPlayerPlaying(shooter) || GameUtils.isGameInFreezeRoom(game)) {
					flash.remove();
					return;
				}

				for (Entity entity : Utils.getNearbyEntities(flash, 6)) {
					if (entity instanceof Player) {
						((Player) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
								org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 2));
					}
				}

				if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_9_R1)) {
					proj.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, proj.getLocation(), 0);
				}

				flash.remove();
			}, 40);
		} else if (proj instanceof Arrow) { // RageArrow explosion event
			if (GameUtils.isGameInFreezeRoom(game)) {
				proj.remove();
				return;
			}

			if (game.getStatus() == GameStatus.RUNNING) {
				Location location = proj.getLocation();

				if (!GameAreaManager.inArea(location)) {
					proj.remove();
					return;
				}

				proj.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 2f, false, false);
				proj.remove();

				HIT_TARGETS.put(shooter.getUniqueId(), new CacheableHitTarget(GameItems.RAGEARROW, proj, 10));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHit(EntityDamageByEntityEvent event) {
		BaseGame game = GameUtils.getGame(event.getEntity().getLocation());
		BaseGame damagerGame = null;

		boolean damagerIsPlayer = event.getDamager() instanceof Player;

		if (damagerIsPlayer && (damagerGame = GameUtils.getGameByPlayer((Player) event.getDamager())) != null) {
			game = damagerGame;
		}

		if (game == null) {
			return;
		}

		if (game.getGameType() == GameType.APOCALYPSE) {
			if (damagerIsPlayer && game.getStatus() == GameStatus.WAITING) {
				event.setCancelled(true); // Prevent other entities getting damaged in lobby
				return;
			}

			if (game.getStatus() != GameStatus.RUNNING) {
				return;
			}

			// Players do not hurt players, because friendship
			if (damagerIsPlayer && event.getEntity() instanceof Player) {
				event.setCancelled(true);
				return;
			}

			double finalDamage = 0d;

			if (damagerIsPlayer) {
				if (damagerGame == null) {
					return;
				}

				ItemHandler knife = Items.getGameItem(4);

				if (knife != null && Misc.getItemInHand((Player) event.getDamager()).getType() == knife.getItem()) {
					finalDamage = knife.getDamage();
				}
			} else if (event.getDamager() instanceof Egg) {
				ItemHandler grenade = Items.getGameItem(1);

				if (grenade != null) {
					finalDamage = grenade.getDamage();
				}
			} else if (event.getDamager() instanceof Arrow) {
				ItemHandler rageArrow = Items.getGameItem(2);

				if (rageArrow != null) {
					finalDamage = rageArrow.getDamage();
				}
			}

			if (finalDamage > 0d) {
				event.setDamage(finalDamage);
			}
		} else {
			if (!(event.getEntity() instanceof Player)) {
				if (damagerIsPlayer && game.getStatus() == GameStatus.WAITING) {
					event.setCancelled(true); // Prevent other entities getting damaged in lobby
				}

				return;
			}

			Player victim = (Player) event.getEntity();

			BaseGame victimGame = GameUtils.getGameByPlayer(victim);
			if (victimGame == null) {
				return;
			}

			// Prevent player damage in lobby
			if (victimGame.getStatus() == GameStatus.WAITING) {
				event.setCancelled(true);
				return;
			}

			if (damagerIsPlayer) {
				if (damagerGame == null) {
					return; // Preventing non-playing players damaging in game players
				}

				if (GameUtils.isGameInFreezeRoom(damagerGame)) {
					event.setCancelled(true);
					return;
				}
			}

			if (victimGame.getStatus() != GameStatus.RUNNING) {
				return;
			}

			double finalDamage = 0d;
			GameItems tool = GameItems.UNKNOWN;

			if (damagerIsPlayer) {
				ItemHandler knife = Items.getGameItem(4);

				if (knife != null && Misc.getItemInHand((Player) event.getDamager()).getType() == knife.getItem()) {
					finalDamage = knife.getDamage();
					tool = GameItems.RAGEKNIFE;
				}
			} else if (event.getDamager() instanceof Egg) {
				ItemHandler grenade = Items.getGameItem(1);

				finalDamage = grenade != null ? grenade.getDamage() : 2.20;
				tool = GameItems.GRENADE;
			} else if (event.getDamager() instanceof Arrow) {
				ItemHandler rageArrow = Items.getGameItem(2);

				finalDamage = rageArrow != null ? rageArrow.getDamage() : 3.35;
				tool = GameItems.RAGEARROW;
			}

			if (finalDamage <= 0d) {
				return;
			}

			event.setDamage(finalDamage);

			if (tool != GameItems.UNKNOWN) {
				HIT_TARGETS.put(event.getDamager().getUniqueId(), new CacheableHitTarget(tool, victim, 0));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onHitPlayer(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		BaseGame game = GameUtils.getGameByPlayer((Player) event.getEntity());
		if (game == null) {
			return;
		}

		if ((game.getStatus() == GameStatus.RUNNING && event.getCause() == DamageCause.FALL
				&& !ConfigValues.isDamagePlayerFall())
				|| (GameUtils.isGameInFreezeRoom(game) || game.getStatus() == GameStatus.WAITING)) {
			event.setCancelled(true); // Prevent fall damage, damage in lobby or game freeze
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBowShoot(EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player && GameUtils.isPlayerInFreezeRoom((Player) e.getEntity())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player deceased = event.getEntity();

		BaseGame deceasedGame = GameUtils.getGameByPlayer(deceased);
		if (deceasedGame == null || deceasedGame.getStatus() != GameStatus.RUNNING) {
			return;
		}

		boolean killerExists = deceased.getKiller() != null;

		if (!killerExists || GameUtils.isPlayerPlaying(deceased.getKiller())) {
			if (deceasedGame.getGameType() != GameType.APOCALYPSE) {
				CacheableHitTarget cht = HIT_TARGETS
						.remove((killerExists ? deceased.getKiller() : deceased).getUniqueId());

				if (cht != null) {
					boolean canSeeDeathMessages = killerExists && !PlayerManager.DEATH_MESSAGES_TOGGLE
							.getOrDefault(deceased.getKiller().getUniqueId(), false);

					switch (cht.getTool()) {
					case RAGEARROW:
					case RAGEBOW:
					case COMBATAXE:
					case GRENADE:
					case RAGEKNIFE:
						if (canSeeDeathMessages) {
							GameUtils.broadcastToGame(deceasedGame,
									RageMode.getLang().get("game.broadcast." + cht.getTool().getKillTextPath(),
											"%victim%", deceased.getName(), "%killer%", deceased.getName()));
						}

						RageScores.addPointsToPlayer(killerExists ? deceased.getKiller() : deceased, deceased,
								cht.getTool());
						break;
					case EXPLOSION:
						boolean hasNearPlayer = false;

						for (UUID near : cht.getNearTargets()) {
							if (!deceased.getUniqueId().equals(near)) {
								continue;
							}

							Player nearPlayer = plugin.getServer().getPlayer(near);

							if (nearPlayer != null) {
								if (canSeeDeathMessages) {
									GameUtils.broadcastToGame(deceasedGame,
											RageMode.getLang().get("game.broadcast." + cht.getTool().getKillTextPath(),
													"%victim%", deceased.getName(), "%killer%", nearPlayer.getName()));
								}

								RageScores.addPointsToPlayer(nearPlayer, deceased, cht.getTool());
								hasNearPlayer = true;
								break;
							}
						}

						if (!hasNearPlayer) {
							GameUtils.broadcastToGame(deceasedGame,
									RageMode.getLang().get("game.broadcast.error-kill"));

							sendMessage(deceased, RageMode.getLang().get("game.unknown-killer"));
						}

						break;
					default:
						GameUtils.broadcastToGame(deceasedGame,
								RageMode.getLang().get("game.unknown-weapon", "%victim%", deceased.getName()));
						break;
					}

					if (cht.getTool() != GameItems.UNKNOWN) {
						Utils.callEvent(
								new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(), cht.getTool()));
					}
				}
			} else {
				deceasedGame.getPlayerManager(deceased).filter(PlayerManager::canRespawn)
						.ifPresent(pl -> pl.setPlayerLives(pl.getPlayerLives() + 1));
			}

			event.setDroppedExp(0);
			plugin.getComplement().setDeathMessage(event, "");
			event.setKeepInventory(true);
			event.getDrops().clear();

			GameUtils.runCommands(deceased, deceasedGame, RewardManager.InGameCommand.CommandType.DEATH);

			if (killerExists) {
				plugin.getRewardManager().giveBonuses(deceased.getKiller());
			}
		}

		if (plugin.isSpigot()) {
			plugin.getServer().getScheduler().runTaskLater(plugin, deceased.spigot()::respawn, 1L);
		}

		GameUtils.addGameItems(deceased, true);
	}

	@EventHandler
	public void onMobDeath(EntityDeathEvent event) {
		org.bukkit.entity.LivingEntity entity = event.getEntity();

		if (!(entity instanceof org.bukkit.entity.Zombie)) {
			return;
		}

		BaseGame game = GameUtils.getGame(entity.getLocation());
		if (game == null || game.getGameType() != GameType.APOCALYPSE || game.getStatus() != GameStatus.RUNNING) {
			return;
		}

		event.setDroppedExp(0);
		event.getDrops().clear();

		Player killer = entity.getKiller();
		UUID entityUId = entity.getUniqueId();

		for (Map.Entry<UUID, CacheableHitTarget> map : HIT_TARGETS.entrySet()) {
			if (killer != null || map.getValue().getNearTargets().remove(entityUId)) {
				if (killer == null) {
					killer = plugin.getServer().getPlayer(map.getKey());
				}

				if (killer != null) {
					RageScores.addPointsToPlayer(killer, entity);
					Utils.callEvent(new RMPlayerKilledEvent(game, entity, killer));
					HIT_TARGETS.remove(map.getKey());
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent ev) {
		if (GameAreaManager.inArea(ev.getLocation())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntitySpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && GameAreaManager.inArea(e.getLocation())) {
			e.setCancelled(true);
		}
	}

	// The event priority should be HIGHEST to prevent overwriting
	// spawns over plugins
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e) {
		final Player player = e.getPlayer();

		BaseGame game = GameUtils.getGameByPlayer(player);
		if (game == null) {
			return;
		}

		// If player still is in game with respawn screen then remove player
		if (!game.isRunning()) {
			game.removePlayer(player);
			return;
		}

		RMPlayerPreRespawnEvent preRespawn = new RMPlayerPreRespawnEvent(game, player);
		Utils.callEvent(preRespawn);
		if (preRespawn.isCancelled()) {
			return;
		}

		IGameSpawn gsg = game.getSpawn(GameSpawn.class);
		if (gsg == null || !gsg.haveAnySpawn()) {
			return;
		}

		// Checks if the player can respawn again in apocalypse
		if (game.getGameType() == GameType.APOCALYPSE) {
			java.util.Optional<PlayerManager> opt = game.getPlayerManager(player).filter(pm -> !pm.canRespawn());

			if (opt.isPresent()) {
				sendMessage(player,
						RageMode.getLang().get("game.killed-by-zombies", "%amount%", ConfigValues.getPlayerLives()));

				// Switching to spectate
				if (ConfigValues.isSpectatorEnabled() && game.removePlayer(player, true)) {
					e.setRespawnLocation(gsg.getRandomSpawn());

					ItemHandler leaveItem = Items.getLobbyItem(1);
					if (leaveItem != null && leaveItem.getAmount() > 0) {
						player.getInventory().setItem(leaveItem.getSlot(), leaveItem.get());
					}

					// Expected behaviour that the players can't go through blocks, because it has
					// added a leave item to theirs inventory
					player.setGameMode(org.bukkit.GameMode.SPECTATOR);
				} else {
					game.removePlayer(player);
				}

				return;
			}
		}

		// TODO Add simulation respawn from zombies

		e.setRespawnLocation(gsg.getRandomSpawn());

		int time = ConfigValues.getRespawnProtectTime();
		if (time > 0) {
			player.setNoDamageTicks(time * 20);
		}

		Utils.callEvent(new RMPlayerRespawnedEvent(game, player, gsg));
	}

	@EventHandler(ignoreCancelled = true)
	public void onHungerGain(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && GameUtils.isPlayerPlaying((Player) event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		if (!ev.canBuild()) {
			return;
		}

		Player player = ev.getPlayer();

		BaseGame game = GameUtils.getGameByPlayer(player);
		if (game == null) {
			return;
		}

		if (ev.getBlock().getType() != Material.TRIPWIRE || GameUtils.isGameInFreezeRoom(game)
				|| !GameAreaManager.inArea(ev.getBlock().getLocation())) {
			ev.setCancelled(true);
			return;
		}

		PressureMine pressureMine = PRESSUREMINES.stream().filter(mine -> player.getUniqueId().equals(mine.getOwner()))
				.findFirst().orElse(new PressureMine(player.getUniqueId()));

		if (pressureMine.addMine(ev.getBlock())) {
			PRESSUREMINES.add(pressureMine);
			return;
		}

		ev.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()) && !explodeMine(event.getBlock().getLocation())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPortalTeleport(PlayerPortalEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityPortalTeleport(EntityPortalEvent event) {
		if (GameUtils.getGame(event.getEntity().getLocation()) != null)
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void disableCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		PlayerManager pm = GameUtils.getPlayerManager(player);

		if (pm == null) {
			return;
		}

		if (ConfigValues.isSpectatorEnabled() && pm.isSpectator()) {
			if (((ConfigValues.isAllowAllCommandsForSpecOperators() && !player.isOp())
					|| !ConfigValues.isAllowAllCommandsForSpecOperators())
					&& !ConfigValues.getAllowedSpectatorCommands().isEmpty()
					&& !ConfigValues.isCommandAllowed(true, event.getMessage())
					&& !player.hasPermission("ragemode.bypass.spectatorcommands")) {
				sendMessage(player, RageMode.getLang().get("game.command-disabled-in-game"));
				event.setCancelled(true);
			}

			return;
		}

		BaseGame game = pm.getPlayerGame();
		if (game == null) {
			return;
		}

		if (ConfigValues.isCommandsDisabledInEndGame() && GameUtils.isGameInFreezeRoom(game)) {
			sendMessage(player, RageMode.getLang().get("game.command-disabled-in-end-game"));
			event.setCancelled(true);
			return;
		}

		if (ConfigValues.isAllowAllCommandsForOperators() && player.isOp()) {
			return;
		}

		if (!ConfigValues.getAllowedInGameCommands().isEmpty()
				&& !ConfigValues.isCommandAllowed(false, event.getMessage())
				&& !player.hasPermission("ragemode.bypass.disabledcommands")) {
			sendMessage(player, RageMode.getLang().get("game.command-disabled-in-game"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent ev) {
		Projectile proj = ev.getEntity();

		if (!(proj.getShooter() instanceof Player)) {
			return;
		}

		BaseGame game = GameUtils.getGameByPlayer((Player) proj.getShooter());
		if (game == null) {
			return;
		}

		if (GameUtils.isGameInFreezeRoom(game)) {
			ev.setCancelled(true);
			return;
		}

		setTrails(proj);
	}

	@EventHandler
	public void eggThrow(PlayerEggThrowEvent event) {
		final Player player = event.getPlayer();

		if (!player.isOnline()) {
			return;
		}

		final BaseGame game = GameUtils.getGameByPlayer(player);

		if (game == null || (game.getStatus() != GameStatus.RUNNING && game.getStatus() != GameStatus.GAMEFREEZE)) {
			return;
		}

		// no baby chickens
		event.setHatching(false);

		// check if player is in waiting game (game freeze) or not inside the area
		if (GameUtils.isGameInFreezeRoom(game) || !GameAreaManager.inArea(player.getLocation())) {
			return;
		}

		final Item grenade = player.getWorld().dropItem(event.getEgg().getLocation(), new ItemStack(Material.EGG));

		// Cancel egg pick up
		grenade.setPickupDelay(41);

		// make custom name
		ItemHandler grenadeItem = Items.getGameItem(1);
		if (grenadeItem != null && !grenadeItem.getCustomName().isEmpty()) {
			grenade.setCustomName(Utils.colors(grenadeItem.getCustomName()));
			grenade.setCustomNameVisible(true);
		}

		grenade.setVelocity(player.getEyeLocation().getDirection());
		// move egg from land location to simulate bounce
		grenade.getLocation().add(event.getEgg().getVelocity());

		final Location loc = grenade.getLocation();

		loc.getWorld().playSound(loc,
				ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_8_R3) ? Sound.valueOf("CREEPER_HISS")
						: Sound.valueOf("ENTITY_CREEPER_PRIMED"),
				1, 1);

		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			// Remove egg when the player is not in-game
			if (!player.isOnline()) {
				grenade.remove();
				return;
			}

			final BaseGame playerGame = GameUtils.getGameByPlayer(player);

			// Remove egg when the player game is in freeze room.
			if (playerGame == null || GameUtils.isGameInFreezeRoom(playerGame)) {
				grenade.remove();
				return;
			}

			grenade.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 3f, false, false);
			grenade.remove(); // get rid of egg so it can't be picked up

			HIT_TARGETS.put(player.getUniqueId(), new CacheableHitTarget(GameItems.GRENADE, grenade, 13));
		}, 40);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final PlayerManager pm = GameUtils.getPlayerManager(player);

		if (pm == null) {
			return;
		}

		final BaseGame playerGame = pm.getPlayerGame();
		if (playerGame == null || GameUtils.isGameInFreezeRoom(playerGame)) {
			return;
		}

		Action action = event.getAction();
		ItemStack hand = Misc.getItemInHand(player);

		/**
		 * Cancels the rage knife usage, for example with collecting beehives
		 * 
		 * For this we don't need to check all game items, because there is no
		 * possibility to change the item types from config.
		 */
		if (action == Action.RIGHT_CLICK_BLOCK) {
			ItemHandler rageKnife = Items.getGameItem(4);

			if (rageKnife != null && hand.getType() == rageKnife.getItem()) {
				event.setCancelled(true);
			}
		}

		if (action == Action.PHYSICAL) {
			return;
		}

		ItemHandler combatAxe = Items.getGameItem(0);

		// Combat axe throw
		if (combatAxe != null && hand.getType() == combatAxe.getItem()
				&& playerGame.getStatus() == GameStatus.RUNNING) {
			final Item item = player.getWorld().dropItem(player.getEyeLocation(), Misc.getItemInHand(player));

			player.getInventory().remove(combatAxe.getItem());

			if (combatAxe.getVelocity() > 0D) {
				item.setVelocity(player.getLocation().getDirection().multiply(combatAxe.getVelocity()));
			}

			final CombatAxeThread combatAxeThread = new CombatAxeThread(item);
			combatAxeThread.start(player);

			CompletableFuture.supplyAsync(() -> {
				while (!item.isDead()) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (item.isOnGround()) {
						item.remove();
						combatAxeThread.stop();
						break; // To make sure we're broke the thread
					}
				}

				return true;
			});

			return;
		}

		if (playerGame.getStatus() == GameStatus.WAITING) {
			ItemMeta meta = hand.getItemMeta();

			if (meta != null && meta.hasDisplayName()) {
				ItemHandler forceStarter = Items.getLobbyItem(0);

				if (forceStarter != null && player.hasPermission("ragemode.admin.item.forcestart")
						&& forceStarter.getItem() == hand.getType()) {
					if (GameUtils.forceStart(playerGame)) {
						sendMessage(player, RageMode.getLang().get("commands.forcestart.game-start", "%game%",
								playerGame.getName()));
					} else {
						sendMessage(player, RageMode.getLang().get("not-enough-players"));
					}
				}

				ItemHandler leaveItem = Items.getLobbyItem(1);
				if (leaveItem != null && leaveItem.getItem() == hand.getType()) {
					GameUtils.leavePlayer(pm, playerGame);
				}

				ItemHandler shopItem = Items.getLobbyItem(2);
				if (shopItem != null && shopItem.getItem() == hand.getType()) {
					lobbyShop.openMainPage(player);
				}

				ItemHandler hideItem = Items.getLobbyItem(3);
				if (hideItem != null) {
					ItemHandler.Extra extraItem = hideItem.getExtras().isEmpty() ? null : hideItem.getExtras().get(0);

					if (hideItem.getItem() == hand.getType() || (extraItem != null
							&& extraItem.getExtraName().equals(plugin.getComplement().getDisplayName(meta)))) {
						if (PlayerManager.DEATH_MESSAGES_TOGGLE.remove(pm.getUniqueId()) != null) {
							plugin.getComplement().setDisplayName(meta, hideItem.getDisplayName());
							plugin.getComplement().setLore(meta, hideItem.getLore());
						} else {
							PlayerManager.DEATH_MESSAGES_TOGGLE.put(pm.getUniqueId(), true);

							if (extraItem != null) {
								plugin.getComplement().setDisplayName(meta, extraItem.getExtraName());
								plugin.getComplement().setLore(meta, extraItem.getExtraLore());
							}
						}

						hand.setItemMeta(meta);
					}
				}
			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return; // Probably never
		}

		BaseGame playerGame = GameUtils.getGameByPlayer((Player) e.getPlayer());

		if (playerGame != null && playerGame.getStatus() == GameStatus.RUNNING
				&& e.getInventory().getType() != InventoryType.PLAYER) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		if (GameUtils.isPlayerPlaying(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	// To prevent removing paintings
	@EventHandler(ignoreCancelled = true)
	public void onHanging(HangingBreakByEntityEvent ev) {
		if (ev.getRemover() instanceof Player && GameUtils.isPlayerPlaying((Player) ev.getRemover())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractRedstone(PlayerInteractEvent ev) {
		if (ev.getClickedBlock() == null) {
			return;
		}

		BaseGame playerGame = GameUtils.getGameByPlayer(ev.getPlayer());
		if (playerGame == null) {
			return;
		}

		if (playerGame.getStatus() != GameStatus.RUNNING && playerGame.getStatus() != GameStatus.GAMEFREEZE
				&& playerGame.getStatus() != GameStatus.WAITING) {
			return;
		}

		Material t = ev.getClickedBlock().getType();

		if (ev.getAction() == Action.PHYSICAL) {
			if (t == Material.FARMLAND) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			} else if (ConfigValues.isCancelRedstoneActivate() && MaterialUtil.isPressurePlate(t)) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			}
		} else if (ConfigValues.isCancelRedstoneActivate() && ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (MaterialUtil.isTrapdoor(t) || MaterialUtil.isButton(t) || MaterialUtil.isComparator(t)
					|| (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_12_R1) && t == Material.REPEATER)
					|| t == Material.LEVER || t == Material.DAYLIGHT_DETECTOR) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			}
		}

		if (ConfigValues.isCancelDoorUse() && ev.getAction() == Action.RIGHT_CLICK_BLOCK && MaterialUtil.isDoor(t)) {
			ev.setUseInteractedBlock(Event.Result.DENY);
			ev.setCancelled(true);
		}

		// Just prevent usage of bush
		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_14_R1) && (ev.getAction() == Action.RIGHT_CLICK_BLOCK
				&& (t == Material.SWEET_BERRY_BUSH || t == Material.COMPOSTER))) {
			ev.setUseInteractedBlock(Event.Result.DENY);
			ev.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (e.getEntered() instanceof Player && GameUtils.isPlayerPlaying((Player) e.getEntered()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return; // Probably never
		}

		PlayerManager pm = GameUtils.getPlayerManager((Player) event.getWhoClicked());
		if (pm == null) {
			return;
		}

		org.bukkit.inventory.Inventory clicked = event.getClickedInventory();

		if (clicked != null && !(clicked.getHolder() instanceof IShop)) {
			if (clicked.getType() == InventoryType.PLAYER && (event.getClick() == ClickType.LEFT
					|| event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_LEFT
					|| event.getClick() == ClickType.MIDDLE || event.getClick() == ClickType.SHIFT_RIGHT)) {
				ItemHandler leaveItem = Items.getLobbyItem(1);

				if (leaveItem != null && leaveItem.getAmount() > 0
						&& leaveItem.get().isSimilar(event.getCurrentItem())) {
					event.setCancelled(true); // Cancel before clearing player's inventory
					GameUtils.leavePlayer(pm, null);
					return;
				}
			}

			// Cancel all inventory type interaction/click
			event.setCancelled(true);
			return;
		}

		BaseGame playerGame = pm.getPlayerGame();

		if (playerGame != null && playerGame.getStatus() == GameStatus.WAITING
				&& !(event.getInventory().getHolder() instanceof IShop)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onFly(PlayerToggleFlightEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerBedEnter(PlayerBedEnterEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent e) {
		GameAreaManager.getAreaByLocation(e.getBlock().getLocation()).filter(area -> area.getGame().isRunning())
				.ifPresent(area -> e.setCancelled(true));
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent ev) {
		BaseGame playerGame = GameUtils.getGameByPlayer(ev.getPlayer());

		if (playerGame == null || playerGame.getStatus() != GameStatus.RUNNING) {
			return;
		}

		// The player teleported outside of area
		if (!GameAreaManager.inArea(ev.getTo())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!ConfigValues.isFreezePlayers()) {
			return;
		}

		BaseGame game = GameUtils.getGameByPlayer(event.getPlayer());
		if (game == null || !GameUtils.isGameInFreezeRoom(game)) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();

		double x = Math.floor(from.getX()), y = Math.floor(from.getY()), z = Math.floor(from.getZ());

		if (Math.floor(to.getX()) != x || Math.floor(to.getY()) != y || Math.floor(to.getZ()) != z) {
			x += .5;
			y += .5;
			z += .5;

			Utils.teleport(event.getPlayer(), new Location(from.getWorld(), x, y, z, from.getYaw(), from.getPitch()));
		}
	}

	@EventHandler
	public void onExplode(ExplosionPrimeEvent e) {
		explodeMine(e.getEntity().getLocation());
	}

	public static boolean explodeMine(Location currentLoc) {
		final org.bukkit.World world = currentLoc.getWorld();
		if (world == null) {
			return false;
		}

		final Block block = world.getBlockAt(currentLoc);

		if (block.getType() != Material.TRIPWIRE
				&& block.getRelative(org.bukkit.block.BlockFace.DOWN).getType() != Material.TRIPWIRE) {
			return false;
		}

		if (GameUtils.getGame(currentLoc) == null) {
			return false;
		}

		SchedulerUtil.submitSync(() -> {
			java.util.Collection<Entity> nears = Utils.getNearbyEntities(world, currentLoc, 10);

			world.createExplosion(currentLoc.getX(), currentLoc.getY() + 1, currentLoc.getZ(), 4f, false, false);
			block.setType(Material.AIR);

			for (Entity near : nears) {
				for (PressureMine mines : PRESSUREMINES) {
					if (mines.getMines().contains(currentLoc)) {
						CacheableHitTarget hitTarget = HIT_TARGETS.get(mines.getOwner());

						if (hitTarget != null) {
							hitTarget.getNearTargets().add(near.getUniqueId());
						} else {
							HIT_TARGETS.put(mines.getOwner(), new CacheableHitTarget(GameItems.PRESSUREMINE, near, 0));
						}

						break;
					}
				}
			}

			for (PressureMine mines : PRESSUREMINES) {
				mines.removeMine(currentLoc);
			}

			return 1;
		});

		return true;
	}

	private void setTrails(final Projectile proj) {
		final boolean isArrow = proj instanceof Arrow;
		final boolean isEgg = proj instanceof Egg;

		final Player shooter = (Player) proj.getShooter();

		ItemHandler grenade = Items.getGameItem(1);
		if (isEgg && grenade != null && grenade.getVelocity() > 0D) {
			proj.setVelocity(shooter.getLocation().getDirection().multiply(grenade.getVelocity()));
		}

		if ((!ConfigValues.isUseArrowTrails() || !isArrow) && (!ConfigValues.isUseGrenadeTrails() || !isEgg)) {
			return;
		}

		final Enum<?> particle = LobbyShop.USER_PARTICLES.get(shooter.getUniqueId());

		if (ConfigValues.isUseArrowTrails() && isArrow && particle == null) {
			return;
		}

		CompletableFuture.supplyAsync(() -> {
			do {
				if (isEgg) {
					try {
						Thread.sleep(155L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					proj.getWorld().playEffect(proj.getLocation(), Effect.SMOKE, 0);
				} else if (ConfigValues.isUseArrowTrails() && isArrow && particle != null) {
					try {
						Thread.sleep(40L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (ServerVersion.isCurrentLower(ServerVersion.v1_9_R1)) {
						proj.getWorld().playEffect(proj.getLocation(), (Effect) particle, 0);
					} else {
						proj.getWorld().spawnParticle((Particle) particle, proj.getLocation(), 0);
					}
				} else {
					return false;
				}
			} while (!proj.isDead() && !proj.isOnGround());

			return true;
		});
	}
}
