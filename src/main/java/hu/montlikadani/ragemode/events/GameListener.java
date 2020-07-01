package hu.montlikadani.ragemode.events;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStartEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerKilledEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerPreRespawnEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerRespawnedEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.AntiCheatChecks;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.items.threads.CombatAxeThread;
import hu.montlikadani.ragemode.libs.Sounds;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.scores.KilledWith;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.utils.MaterialUtil;

public class GameListener implements Listener {

	private RageMode plugin;

	private final Map<UUID, UUID> explosionVictims = new HashMap<>();
	private final Map<UUID, UUID> grenadeExplosionVictims = new HashMap<>();
	private final Map<Location, UUID> pressureMinesOwner = new HashMap<>();

	public GameListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onGameLeave(RMGameLeaveAttemptEvent e) {
		Player player = e.getPlayer();

		if (GameUtils.USERPARTICLES.containsKey(player.getUniqueId())) {
			GameUtils.USERPARTICLES.remove(player.getUniqueId());
		}

		Location loc = null;
		for (Map.Entry<Location, UUID> entry : pressureMinesOwner.entrySet()) {
			if (entry.getValue().equals(player.getUniqueId())) {
				loc = entry.getKey();
				break;
			}
		}

		if (loc != null) {
			loc.getBlock().setType(Material.AIR);
			pressureMinesOwner.remove(loc);
		}
	}

	private int task = -1;

	@EventHandler
	public void onGameStart(final RMGameStartEvent ev) {
		// just for optimisation (someone suggestion)
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			Game game = ev.getGame();

			for (PlayerManager pm : ev.getPlayers()) {
				Player p = pm.getPlayer();

				if (p.getLocation().getY() < 0) {
					p.teleport(GameUtils.getGameSpawn(game).getRandomSpawn());

					// Prevent damaging player when respawned
					p.setFallDistance(0);

					GameUtils.broadcastToGame(game, RageMode.getLang().get("game.void-fall", "%player%", p.getName()));
					continue;
				}

				if (game.getStatus() == GameStatus.RUNNING) {
					// prevent player moving outside of game area
					Location loc = p.getLocation();
					org.bukkit.util.Vector vector = loc.getDirection();

					if (!GameAreaManager.inArea(loc)) {
						if (vector.getX() == 0) {
							vector.setX(0.01);
						}

						if (vector.getY() == 0) {
							vector.setY(0.01);
						}

						if (vector.getZ() == 0) {
							vector.setZ(0.01);
						}

						loc = loc.subtract(vector.multiply(1));
						loc.setY(loc.getBlockY() + 1); // +1 to prevent player teleporting into block

						p.teleport(loc);
						continue;
					}
				}

				// pressure mine
				GameAreaManager.getAreaByLocation(p.getLocation()).getEntities()
						.forEach(e -> explodeMine(null, e.getLocation()));
			}
		}, 2, 2);
	}

	/**
	 * @param event
	 */
	@EventHandler
	public void onGameStop(RMGameStopEvent event) {
		for (Map.Entry<Location, UUID> entry : pressureMinesOwner.entrySet()) {
			entry.getKey().getBlock().setType(Material.AIR);
		}

		pressureMinesOwner.clear();

		if (task != -1) {
			Bukkit.getScheduler().cancelTask(task);
		}
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
		Projectile proj = event.getEntity();
		if (proj.getShooter() == null || !(proj.getShooter() instanceof Player)
				|| !GameUtils.isPlayerPlaying((Player) proj.getShooter())) {
			return;
		}

		// flash event
		if (proj instanceof Snowball && (!GameUtils.isPlayerInFreezeRoom((Player) proj.getShooter())
				|| GameUtils.getGameByPlayer((Player) proj.getShooter()).getGameType() != GameType.APOCALYPSE)) {
			final Item flash = proj.getWorld().dropItem(proj.getLocation(), new ItemStack(Material.SNOWBALL));
			if (flash == null) {
				return;
			}

			final Player shooter = (Player) proj.getShooter();
			if (shooter == null) {
				flash.remove();
				return;
			}

			flash.setPickupDelay(41);
			flash.setVelocity(shooter.getEyeLocation().getDirection());
			flash.getLocation().add(proj.getVelocity());

			new BukkitRunnable() {
				@Override
				public void run() {
					if (flash.isDead()) {
						return;
					}

					if (!GameUtils.isPlayerPlaying(shooter) || GameUtils.isPlayerInFreezeRoom(shooter)) {
						flash.remove();
						cancel();
						return;
					}

					for (Entity entity : flash.getNearbyEntities(6D, 6D, 6D)) {
						if (entity instanceof Player) {
							((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2));
						}
					}

					proj.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, proj.getLocation(), 0);
					flash.remove();
				}
			}.runTaskLater(plugin, 40);
		} else if (proj instanceof Arrow) { // RageArrow explosion event
			Arrow arrow = (Arrow) proj;
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
				if (!GameAreaManager.inArea(location)) {
					arrow.remove();
					return;
				}

				double x = location.getX(), y = location.getY(), z = location.getZ();

				List<Entity> nears = arrow.getNearbyEntities(10, 10, 10);

				arrow.getWorld().createExplosion(x, y, z, 2f, false, false);
				arrow.remove();

				if (game.getGameType() == GameType.APOCALYPSE) {
					return;
				}

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
		if (GameUtils.getGame(event.getEntity().getLocation()) != null
				&& GameUtils.getGame(event.getEntity().getLocation()).getGameType() == GameType.APOCALYPSE) {
			double finalDamage = 0d;

			if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();

				if (GameUtils.isPlayerPlaying(damager)) {
					ItemMeta meta = NMS.getItemInHand(damager).getItemMeta();
					ItemHandler knife = Items.getRageKnife();
					if (knife != null && meta != null && meta.hasDisplayName()
							&& meta.getDisplayName().equals(knife.getDisplayName())) {
						finalDamage = knife.getDamage();
					}
				}
			} else if (event.getDamager() instanceof org.bukkit.entity.Egg && Items.getGrenade() != null) {
				finalDamage = Items.getGrenade().getDamage();
			} else if (event.getDamager() instanceof Arrow && Items.getRageArrow() != null) {
				finalDamage = Items.getRageArrow().getDamage();
			}

			if (finalDamage > 0d) {
				event.setDamage(finalDamage);
			}
		} else {
			if (!(event.getEntity() instanceof Player)) {
				return;
			}

			Player victim = (Player) event.getEntity();
			if (!GameUtils.isPlayerPlaying(victim)) {
				return;
			}

			Game victimGame = GameUtils.getGameByPlayer(victim);
			// Prevent player damage in lobby
			if (victimGame.getStatus() == GameStatus.WAITING) {
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

			if (victimGame.getStatus() != GameStatus.RUNNING) {
				return;
			}

			double finalDamage = 0d;
			String tool = "";

			if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();

				if (GameUtils.isPlayerPlaying(damager)) {
					ItemMeta meta = NMS.getItemInHand(damager).getItemMeta();
					ItemHandler knife = Items.getRageKnife();
					if (knife != null && meta != null && meta.hasDisplayName()
							&& meta.getDisplayName().equals(knife.getDisplayName())) {
						finalDamage = knife.getDamage();
						tool = "knife";
					}
				}
			} else if (event.getDamager() instanceof org.bukkit.entity.Egg) {
				finalDamage = Items.getGrenade() != null ? Items.getGrenade().getDamage() : 2.20;
				tool = "grenade";
			} else if (event.getDamager() instanceof Arrow) {
				finalDamage = Items.getRageArrow() != null ? Items.getRageArrow().getDamage() : 3.35;
				tool = "arrow";
			}

			if (finalDamage > 0d) {
				event.setDamage(finalDamage);
			}

			if (!tool.isEmpty()) {
				victim.removeMetadata("killedWith", plugin);
				victim.setMetadata("killedWith", new FixedMetadataValue(plugin, tool));
			}
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

		Game deceasedGame = GameUtils.getGameByPlayer(deceased);
		if (deceasedGame.getStatus() != GameStatus.RUNNING) {
			return;
		}

		boolean killerExists = deceased.getKiller() != null;

		if ((killerExists && GameUtils.isPlayerPlaying(deceased.getKiller())) || !killerExists) {
			boolean doDeathBroadcast = ConfigValues.isDefaultDeathMessageEnabled();

			if (plugin.getConfiguration().getArenasCfg()
					.isSet("arenas." + deceasedGame.getName() + ".death-messages")) {
				String gameBroadcast = plugin.getConfiguration().getArenasCfg()
						.getString("arenas." + deceasedGame.getName() + ".death-messages", "");
				if (gameBroadcast.equals("true") || gameBroadcast.equals("false")) {
					doDeathBroadcast = Boolean.parseBoolean(gameBroadcast);
				}
			}

			if (deceasedGame.getGameType() != GameType.APOCALYPSE) {
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

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.RAGEBOW);
						break;
					case "combataxe":
						if (!killerExists) {
							message = RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceaseName,
									"%killer%", deceaseName);

							RageScores.addPointsToPlayer(deceased, deceased, KilledWith.COMBATAXE);
						} else {
							message = RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceaseName,
									"%killer%", deceased.getKiller().getName());

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, KilledWith.COMBATAXE);
						}

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.COMBATAXE);
						break;
					case "explosion":
						if (explosionVictims.containsKey(deceased.getUniqueId())) {
							message = RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%", deceaseName,
									"%killer%",
									Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())).getName());

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

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.EXPLOSION);
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

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.GRENADE);
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

						killed = new RMPlayerKilledEvent(deceasedGame, deceased, deceased.getKiller(),
								KilledWith.RAGEKNIFE);
						break;
					default:
						message = RageMode.getLang().get("game.unknown-weapon", "%victim%", deceaseName);
						break;
					}

					if (doDeathBroadcast && !message.isEmpty()) {
						GameUtils.broadcastToGame(deceasedGame, message);
					}

					deceased.removeMetadata("killedWith", plugin);
					if (killed != null) {
						Utils.callEvent(killed);
					}
				}
			}

			event.setDroppedExp(0);
			event.setDeathMessage("");
			event.setKeepInventory(true);
			event.getDrops().clear();

			GameUtils.runCommands(deceased, deceasedGame.getName(), "death");
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

	@EventHandler
	public void onMobDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			return;
		}

		Game game = GameUtils.getGame(event.getEntity().getLocation());
		if (game != null && game.isGameRunning() && game.getGameType() == GameType.APOCALYPSE) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent ev) {
		if (GameAreaManager.inArea(ev.getLocation())) {
			ev.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && GameAreaManager.inArea(e.getLocation())) {
			e.setCancelled(true);
		}
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

		IGameSpawn gsg = GameUtils.getGameSpawn(game.getName());
		if (gsg.getSpawnLocations().size() < 0) {
			return;
		}

		Location randomSpawn = gsg.getRandomSpawn();
		if (game.getGameType() == GameType.APOCALYPSE) {
			int amountEntities = 0;

			// simulate the respawn from monsters
			for (Entity nearby : randomSpawn.getWorld().getNearbyEntities(randomSpawn, 15, 15, 15)) {
				if (!(nearby instanceof Player)) {
					amountEntities++;

					// farther from the monsters to respawn the player
					if (amountEntities > 4) {
						randomSpawn.setX(randomSpawn.getX() + 8);
						randomSpawn.setZ(randomSpawn.getZ() + 8);
						break; // we don't need anymore to check for nearby entities
					}
				}
			}
		}

		e.setRespawnLocation(randomSpawn);

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
		if (GameUtils.isPlayerPlaying(ev.getPlayer())) {
			if (ev.getBlock().getType().equals(Material.TRIPWIRE)) {
				pressureMinesOwner.put(ev.getBlock().getLocation(), ev.getPlayer().getUniqueId());
				return;
			}

			ev.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer())) {
			if (explodeMine(event.getPlayer(), event.getBlock().getLocation())) {
				return;
			}

			event.setCancelled(true);
		}
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
			List<String> cmds = plugin.getConfiguration().getCfg()
					.getStringList("spectator.allowed-spectator-commands");
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

			List<String> cmds = plugin.getConfiguration().getCfg().getStringList("game.allowed-commands");
			if (!cmds.isEmpty() && !cmds.contains(arg) && !hasPerm(p, "ragemode.bypass.disabledcommands")) {
				sendMessage(p, RageMode.getLang().get("game.this-command-is-disabled-in-game"));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent ev) {
		Projectile proj = ev.getEntity();
		if (proj.getShooter() == null || !(proj.getShooter() instanceof Player)) {
			return;
		}

		Player shooter = (Player) proj.getShooter();
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

		setTrails(proj);
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

		final Game game = GameUtils.getGameByPlayer(p);
		if (game.getStatus() != GameStatus.RUNNING && game.getStatus() != GameStatus.GAMEFREEZE) {
			return;
		}

		// no baby chickens
		event.setHatching(false);

		// check if player is in waiting game (game freeze)
		if (GameUtils.isPlayerInFreezeRoom(p)) {
			return;
		}

		final Item grenade = p.getWorld().dropItem(event.getEgg().getLocation(), new ItemStack(Material.EGG));

		if (!GameAreaManager.inArea(p.getLocation())) {
			grenade.remove();
			return;
		}

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

				if (game.getGameType() == GameType.APOCALYPSE) {
					return;
				}

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

						i++;
					}
				}
			}
		}.runTaskLater(plugin, 40);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		if (GameUtils.isPlayerInFreezeRoom(p)) {
			return;
		}

		Action action = event.getAction();
		ItemStack hand = NMS.getItemInHand(p);

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

		if (action != Action.PHYSICAL) {
			// Combat axe throw
			if (hand.getType().equals(Items.getCombatAxe().getItem())
					&& GameUtils.getGameByPlayer(p).getStatus() == GameStatus.RUNNING) {
				final Item item = p.getWorld().dropItem(p.getEyeLocation(), NMS.getItemInHand(p));

				p.getInventory().remove(Items.getCombatAxe().getItem());

				double velocity = plugin.getConfiguration().getItemsCfg().getDouble("gameitems.combatAxe.velocity", 2D);
				if (velocity > 0D) {
					item.setVelocity(p.getLocation().getDirection().multiply(velocity));
				}

				final CombatAxeThread combatAxeThread = new CombatAxeThread(p, item);
				combatAxeThread.start();

				new Thread(() -> {
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
				}).start();

				return;
			}

			if (GameUtils.getGameByPlayer(p).getStatus() == GameStatus.WAITING) {
				ItemMeta meta = hand.getItemMeta();
				if (meta != null && meta.hasDisplayName()) {
					Game game = GameUtils.getGameByPlayer(p);

					if (hasPerm(p, "ragemode.admin.item.forcestart") && Items.getForceStarter() != null
							&& Items.getForceStarter().getDisplayName().equals(meta.getDisplayName())) {
						GameUtils.forceStart(game);
						sendMessage(p,
								RageMode.getLang().get("commands.forcestart.game-start", "%game%", game.getName()));
					}

					if (Items.getLeaveGameItem() != null
							&& Items.getLeaveGameItem().getDisplayName().equals(meta.getDisplayName())) {
						GameUtils.leavePlayer(p, game);
					}

					if (Items.getShopItem() != null
							&& Items.getShopItem().getDisplayName().equals(meta.getDisplayName())) {
						game.getShop().openMainPage(p);
					}
				}

				event.setCancelled(true);
			}
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
		GameStatus status = GameUtils.getGameByPlayer(p).getStatus();

		if (status == GameStatus.RUNNING || status == GameStatus.GAMEFREEZE) {
			if (action == Action.PHYSICAL) {
				if (t == Material.FARMLAND) {
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setCancelled(true);
				} else if (ConfigValues.isCancelRedstoneActivate()) {
					if (MaterialUtil.isWoodenPressurePlate(t) || t.equals(Material.STONE_PRESSURE_PLATE)
							|| t.equals(Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
							|| t.equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
						ev.setUseInteractedBlock(Event.Result.DENY);
						ev.setCancelled(true);
					}
				}
			} else if (ConfigValues.isCancelRedstoneActivate() && action == Action.RIGHT_CLICK_BLOCK) {
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

			if (ConfigValues.isCancelDoorUse() && action == Action.RIGHT_CLICK_BLOCK && MaterialUtil.isWoodenDoor(t)) {
				ev.setUseInteractedBlock(Event.Result.DENY);
				ev.setCancelled(true);
			}
		}

		// Just prevent usage of bush
		if (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && action == Action.RIGHT_CLICK_BLOCK
				&& t.equals(Material.SWEET_BERRY_BUSH) || t.equals(Material.COMPOSTER)) {
			ev.setUseInteractedBlock(Event.Result.DENY);
			ev.setCancelled(true);
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
	public void onMonsterInteract(EntityInteractEvent e) {
		if (GameAreaManager.inArea(e.getEntity().getLocation())) {
			explodeMine(null, e.getEntity().getLocation());
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent ev) {
		Player p = ev.getPlayer();

		// when the player is not in the area
		if (GameUtils.isPlayerPlaying(p) && ev.getTo() != null && !GameAreaManager.inArea(ev.getTo())) {
			ev.setCancelled(true);
		}
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

			Location from = event.getFrom(), to = event.getTo();
			if (to == null) {
				return;
			}

			double x = Math.floor(from.getX()), y = Math.floor(from.getY()), z = Math.floor(from.getZ());

			if (Math.floor(to.getX()) != x || Math.floor(to.getY()) != y || Math.floor(to.getZ()) != z) {
				x += .5;
				y += .5;
				z += .5;

				p.teleport(new Location(from.getWorld(), x, y, z, from.getYaw(), from.getPitch()));
			}
		}
	}

	// TODO: check when the pressure mine is exploded
	/*@EventHandler
	public void onExplode(BlockExplodeEvent e) {
		explodeMine(null, e.getBlock().getLocation());
	}*/

	private boolean explodeMine(Player p, Location currentLoc) {
		Block b = currentLoc.getBlock();

		if (b.getRelative(BlockFace.DOWN).getType().equals(Material.TRIPWIRE)
				|| b.getType().equals(Material.TRIPWIRE)) {
			Collection<Entity> nears = currentLoc.getWorld().getNearbyEntities(currentLoc, 10, 10, 10);
			Location explodeLoc = new Location(currentLoc.getWorld(), currentLoc.getX(), currentLoc.getY() + 1,
					currentLoc.getZ());

			explodeLoc.getWorld().createExplosion(explodeLoc, 4f, false, false);
			b.setType(Material.AIR);

			if (GameUtils.getGame(currentLoc) != null
					&& GameUtils.getGame(currentLoc).getGameType() != GameType.APOCALYPSE) {
				for (Entity entities : nears) {
					if (entities instanceof Player) {
						final Player near = (Player) entities;

						if (explosionVictims.containsKey(near.getUniqueId())) {
							explosionVictims.remove(near.getUniqueId());
						}

						if (p != null) {
							explosionVictims.put(near.getUniqueId(), p.getUniqueId());
						} else {
							nears.stream().filter(n -> !(n instanceof Player))
									.forEach(n -> explosionVictims.put(near.getUniqueId(), n.getUniqueId()));
						}

						near.removeMetadata("killedWith", plugin);
						near.setMetadata("killedWith", new FixedMetadataValue(plugin, "explosion"));
					}
				}
			}

			if (pressureMinesOwner.containsKey(currentLoc)) {
				pressureMinesOwner.remove(currentLoc);
			}

			return true;
		}

		return false;
	}

	private void setTrails(final Projectile proj) {
		final boolean isArrow = proj instanceof Arrow;
		final boolean isEgg = proj instanceof Egg;

		double velocity = plugin.getConfiguration().getItemsCfg().getDouble("gameitems.grenade.velocity", 2D);
		if (velocity > 0D && isEgg) {
			proj.setVelocity(((Player) proj.getShooter()).getLocation().getDirection().multiply(velocity));
		}

		if ((!ConfigValues.isUseArrowTrails() || !isArrow) && (!ConfigValues.isUseGrenadeTrails() || !isEgg)) {
			return;
		}

		final UUID uuid = ((Player) proj.getShooter()).getUniqueId();
		if (ConfigValues.isUseArrowTrails() && isArrow && !GameUtils.USERPARTICLES.containsKey(uuid)) {
			return;
		}

		new Thread(() -> {
			do {
				if (isEgg) {
					try {
						Thread.sleep(155L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					proj.getWorld().playEffect(proj.getLocation(), org.bukkit.Effect.SMOKE, 0);
				} else if (ConfigValues.isUseArrowTrails() && isArrow && GameUtils.USERPARTICLES.containsKey(uuid)) {
					try {
						Thread.sleep(40L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					proj.getWorld().spawnParticle(GameUtils.USERPARTICLES.get(uuid), proj.getLocation(), 0);
				}
			} while (!proj.isDead() && !proj.isOnGround());
		}).start();
	}
}
