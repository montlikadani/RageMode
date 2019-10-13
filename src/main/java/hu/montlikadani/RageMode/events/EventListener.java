package hu.montlikadani.ragemode.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMPlayerKilledEvent;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameLoader;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.MapChecker;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.ForceStarter;
import hu.montlikadani.ragemode.items.LeaveGame;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.libs.Sounds;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.MaterialUtil;

public class EventListener implements Listener {

	private RageMode plugin;
	private Map<UUID, UUID> explosionVictims = new HashMap<>();
	private Map<UUID, UUID> grenadeExplosionVictims = new HashMap<>();

	public static HashMap<String, Boolean> waitingGames = new HashMap<>();

	public EventListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.getConfiguration().getCV().isCheckForUpdates() && event.getPlayer().isOp()) {
			plugin.checkVersion("player");
		}

		HoloHolder.showAllHolosToPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();

		GameUtils.kickPlayer(p, GameUtils.getGameByPlayer(p), true);
		GameUtils.kickSpectatorPlayer(p, GameUtils.getGameBySpectator(p));
		HoloHolder.deleteHoloObjectsOfPlayer(p);
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent ev) {
		Player p = ev.getPlayer();

		GameUtils.kickPlayer(p, GameUtils.getGameByPlayer(p), true);
		GameUtils.kickSpectatorPlayer(p, GameUtils.getGameBySpectator(p));
		HoloHolder.deleteHoloObjectsOfPlayer(p);
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

		String game = GameUtils.getGameByPlayer(p).getPlayersGame(p);

		if (GameUtils.getStatus(game) == GameStatus.WAITING) {
			if (!plugin.getConfiguration().getCV().isChatEnabledinLobby()
					&& !p.hasPermission("ragemode.bypass.lobby.lockchat")) {
				event.setCancelled(true);
				p.sendMessage(RageMode.getLang().get("game.lobby.chat-is-disabled"));
				return;
			}
		}

		if (GameUtils.getStatus(game) == GameStatus.RUNNING) {
			if (!plugin.getConfiguration().getCV().isEnableChatInGame()
					&& !p.hasPermission("ragemode.bypass.game.lockchat")) {
				p.sendMessage(RageMode.getLang().get("game.chat-is-disabled"));
				event.setCancelled(true);
				return;
			}

			if (plugin.getConfiguration().getCV().isChatFormatEnabled()) {
				String format = plugin.getConfiguration().getCV().getChatFormat();
				format = format.replace("%player%", p.getName());
				format = format.replace("%player-displayname%", p.getDisplayName());
				format = format.replace("%game%", game);
				format = format.replace("%online-ingame-players%",
						Integer.toString(GameUtils.getGameByName(game).getPlayers().size()));
				format = format.replace("%message%", event.getMessage());
				format = Utils.setPlaceholders(format, p);

				event.setFormat(Utils.colors(format));
			}
		}

		if (GameUtils.getStatus(game) == GameStatus.GAMEFREEZE) {
			if (!plugin.getConfiguration().getCV().isEnableChatAfterEnd()) {
				p.sendMessage(RageMode.getLang().get("game.game-freeze.chat-is-disabled"));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		// RageArrow explosion event
		if (event.getEntity() != null && event.getEntity().getShooter() != null
				&& event.getEntity().getShooter() instanceof Player
				&& GameUtils.isPlayerPlaying((Player) event.getEntity().getShooter())
				&& GameUtils.getStatus(GameUtils.getGameByPlayer((Player) event.getEntity().getShooter())
						.getPlayersGame((Player) event.getEntity().getShooter())) == GameStatus.RUNNING) {
			if (event.getEntity() instanceof Arrow) {
				Arrow arrow = (Arrow) event.getEntity();

				if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
					Player shooter = (Player) arrow.getShooter();
					if (GameUtils.isPlayerPlaying(shooter)) {
						if (waitingGames.containsKey(GameUtils.getGameByPlayer(shooter).getPlayersGame(shooter))) {
							if (waitingGames.get(GameUtils.getGameByPlayer(shooter).getPlayersGame(shooter)))
								return;
						}

						Location location = arrow.getLocation();
						double x = location.getX();
						double y = location.getY();
						double z = location.getZ();

						List<Entity> nears = arrow.getNearbyEntities(10, 10, 10);

						arrow.getWorld().createExplosion(x, y, z, 2f, false, false);
						arrow.remove();

						int i = 0;
						int imax = nears.size();
						while (i < imax) {
							if (nears.get(i) instanceof Player /*
															 * && !nears.get(i).
															 * getUniqueId().
															 * toString().equals(
															 * shooter.getUniqueId()
															 * .toString())
															 */) {
								Player near = (Player) nears.get(i);
								if (explosionVictims != null) {
									if (explosionVictims.containsKey(near.getUniqueId())) {
										explosionVictims.remove(near.getUniqueId());
										explosionVictims.put(near.getUniqueId(), shooter.getUniqueId());
									}
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

		if (GameUtils.getStatus(victim) == GameStatus.RUNNING) {
			// RageKnife hit event
			if (event.getDamager() instanceof Player) {
				Player killer = (Player) event.getDamager();

				if (GameUtils.isPlayerPlaying(killer)) {
					if (waitingGames.containsKey(GameUtils.getGameByPlayer(killer).getPlayersGame(killer))) {
						if (waitingGames.get(GameUtils.getGameByPlayer(killer).getPlayersGame(killer))) {
							event.setCancelled(true);
							return;
						}
					}

					ItemStack hand = NMS.getItemInHand(killer);
					ItemMeta meta = hand.getItemMeta();
					if (meta != null && meta.getDisplayName() != null) {
						if (meta.getDisplayName().equals(RageKnife.getName())) {
							event.setDamage(25);

							victim.removeMetadata("killedWith", plugin);
							victim.setMetadata("killedWith", new FixedMetadataValue(plugin, "knife"));
						}
					}
				}
			} else if (event.getDamager() instanceof org.bukkit.entity.Egg) {
				event.setDamage(2.20d);

				victim.removeMetadata("killedWith", plugin);
				victim.setMetadata("killedWith", new FixedMetadataValue(plugin, "grenade"));
			} else if (event.getDamager() instanceof Snowball) {
				event.setDamage(25d);

				victim.removeMetadata("killedWith", plugin);
				victim.setMetadata("killedWith", new FixedMetadataValue(plugin, "snowball"));
			} else if (event.getDamager() instanceof Arrow) {
				event.setDamage(3.35d);

				victim.removeMetadata("killedWith", plugin);
				victim.setMetadata("killedWith", new FixedMetadataValue(plugin, "arrow"));
			}
		}

		// Prevent player damage in lobby
		if (GameUtils.getStatus(victim) == GameStatus.WAITING)
			event.setCancelled(true);
	}

	@EventHandler
	public void onHitPlayer(EntityDamageEvent event) {
		Entity e = event.getEntity();
		if (!(e instanceof Player)) {
			return;
		}

		if (!GameUtils.isPlayerPlaying((Player) e)) {
			return;
		}

		Player victim = (Player) e;
		String game = GameUtils.getGameByPlayer(victim).getPlayersGame(victim);

		// Hit player event
		if (GameUtils.getStatus(victim) == GameStatus.RUNNING) {
			if (event.getCause().equals(DamageCause.FALL) && !plugin.getConfiguration().getCV().isDamagePlayerFall()) {
				event.setCancelled(true);
				return;
			}

			if (waitingGames.containsKey(game)) {
				if (waitingGames.get(game)) {
					event.setCancelled(true);
				}
			}
		} else if (GameUtils.getStatus(victim) == GameStatus.GAMEFREEZE) { // Prevent damage in game freeze
			if (waitingGames.containsKey(game)) {
				if (waitingGames.get(game))
					event.setCancelled(true);
			}
		} else if (GameUtils.getStatus(victim) == GameStatus.WAITING) {
			event.setCancelled(true); // Prevent player damage in lobby
		}
	}

	@EventHandler
	public void onBowShoot(EntityShootBowEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player p = (Player) e.getEntity();
		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		if (waitingGames.containsKey(GameUtils.getGameByPlayer(p).getPlayersGame(p))) {
			if (waitingGames.get(GameUtils.getGameByPlayer(p).getPlayersGame(p))) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() == null)
			return;

		Player deceased = event.getEntity();

		if (deceased != null && GameUtils.isPlayerPlaying(deceased)
				&& GameUtils.getStatus(deceased) == GameStatus.RUNNING) {
			String game = GameUtils.getGameByPlayer(deceased).getPlayersGame(deceased);

			if ((deceased.getKiller() != null && GameUtils.isPlayerPlaying(deceased.getKiller()))
					|| deceased.getKiller() == null) {
				boolean doDeathBroadcast = plugin.getConfiguration().getCV().isDeathMsgs();

				if (plugin.getConfiguration().getArenasCfg().isSet("arenas." + game + ".death-messages")) {
					String gameBroadcast = plugin.getConfiguration().getArenasCfg().getString("arenas." + game + ".death-messages");
					if (gameBroadcast != null && gameBroadcast != "") {
						if (gameBroadcast.equals("true") || gameBroadcast.equals("false"))
							doDeathBroadcast = Boolean.parseBoolean(gameBroadcast);
					}
				}

				String deceaseName = deceased.getName();
				List<MetadataValue> data = deceased.getMetadata("killedWith");
				RMPlayerKilledEvent killed = null;

				if (data != null && !data.isEmpty()) {
					switch (data.get(0).asString()) {
					case "arrow":
						if (deceased.getKiller() == null) {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%",
										deceaseName, "%killer%", deceaseName));

							RageScores.addPointsToPlayer(deceased, deceased, "ragebow");
						} else {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%",
										deceaseName, "%killer%", deceased.getKiller().getName()));

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "ragebow");
						}

						killed = new RMPlayerKilledEvent(GameUtils.getGameByName(game), deceased, deceased.getKiller() != null ?
								deceased.getKiller() : null, "arrow");
						break;
					case "snowball":
						if (deceased.getKiller() == null) {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.axe-kill", "%victim%",
										deceaseName, "%killer%", deceased.getName()));

							RageScores.addPointsToPlayer(deceased, deceased, "combataxe");
						} else {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.axe-kill", "%victim%",
										deceaseName, "%killer%", deceased.getKiller().getName()));

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "combataxe");
						}

						killed = new RMPlayerKilledEvent(GameUtils.getGameByName(game), deceased, deceased.getKiller() != null ?
								deceased.getKiller() : null, "snowball");
						break;
					case "explosion":
						if (explosionVictims.containsKey(deceased.getUniqueId())) {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.explosion-kill",
										"%victim%", deceaseName,
										"%killer%", Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())).getName()));

							RageScores.addPointsToPlayer(Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())),
									deceased, "explosion");
						} else if (grenadeExplosionVictims.containsKey(deceased.getUniqueId())) {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%",
										deceaseName, "%killer%", Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())).getName()));

							RageScores.addPointsToPlayer(
									Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())), deceased, "explosion");
						} else {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.error-kill"));

							deceased.sendMessage(RageMode.getLang().get("game.unknown-killer"));
						}

						killed = new RMPlayerKilledEvent(GameUtils.getGameByName(game), deceased, deceased.getKiller() != null ?
								deceased.getKiller() : null, "explosion");
						break;
					case "grenade":
						if (deceased.getKiller() == null) {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.grenade-kill", "%victim%",
										deceaseName, "%killer%", deceased.getName()));

							RageScores.addPointsToPlayer(deceased, deceased, "grenade");
						} else {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.grenade-kill", "%victim%",
										deceaseName, "%killer%", deceased.getKiller().getName()));

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "grenade");
						}

						killed = new RMPlayerKilledEvent(GameUtils.getGameByName(game), deceased, deceased.getKiller() != null ?
								deceased.getKiller() : null, "grenade");
						break;
					case "knife":
						if (deceased.getKiller() == null) {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.knife-kill",
										"%victim%", deceaseName, "%killer%", deceaseName));

							RageScores.addPointsToPlayer(deceased, deceased, "rageknife");
						} else {
							if (doDeathBroadcast)
								GameUtils.broadcastToGame(game, RageMode.getLang().get("game.broadcast.knife-kill",
										"%victim%", deceaseName, "%killer%", deceased.getKiller().getName()));

							RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "rageknife");
						}

						killed = new RMPlayerKilledEvent(GameUtils.getGameByName(game), deceased, deceased.getKiller() != null ?
								deceased.getKiller() : null, "knife");
						break;
					default:
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game,
									RageMode.getLang().get("game.unknown-weapon", "%victim%", deceaseName));
						break;
					}

					deceased.removeMetadata("killedWith", plugin);
					if (killed != null) {
						Bukkit.getPluginManager().callEvent(killed);
					}
				}

				// Remove arrows from player body
				//TODO Anyway, this possible?
				/*if (plugin.getConfiguration().getCfg().getBoolean("game.global.remove-arrows-from-player-body")) {
					for (Entity e : deceased.getNearbyEntities(1, 1, 1)) {
						if (e instanceof Arrow)
							Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> e.remove(), 1L);
					}
				}*/

				event.setDroppedExp(0);
				event.setDeathMessage("");
				event.setKeepInventory(true);
				event.getDrops().clear();

				GameUtils.runCommands(deceased, game, "death");
			}

			try {
				Class.forName("org.spigotmc.SpigotConfig");

				// respawn player instantly
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, deceased.spigot()::respawn, 1L);
			} catch (ClassNotFoundException c) {
				// Ignore
			}

			GameUtils.addGameItems(deceased, true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		// If player still is in game with respawn screen then remove player
		// 1.14 issue
		Game game = GameUtils.getGameByPlayer(p);
		if (!game.isGameRunning(game.getPlayersGame(p))) {
			game.removePlayer(p);
			return;
		}

		GameSpawnGetter gsg = GameUtils.getGameSpawnByName(game.getPlayersGame(p));
		if (gsg.getSpawnLocations().size() > 0) {
			Random rand = new Random();
			int x = rand.nextInt(gsg.getSpawnLocations().size());
			e.setRespawnLocation(gsg.getSpawnLocations().get(x));
		}

		int time = plugin.getConfiguration().getCV().getRespawnProtectTime();
		if (time > 0) {
			p.setNoDamageTicks(time * 20);
		}
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
	public void onSignBreak(BlockBreakEvent event) {
		if (event.isCancelled() || !plugin.getConfiguration().getCV().isSignsEnable()) return;

		org.bukkit.block.BlockState blockState = event.getBlock().getState();
		if (blockState instanceof Sign && SignCreator.isSign(blockState.getLocation())) {
			if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
				event.getPlayer().sendMessage(RageMode.getLang().get("no-permission-to-interact-sign"));
				event.setCancelled(true);
				return;
			}

			SignCreator.removeSign((Sign) blockState);
		}
	}

	@EventHandler
	public void disableCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();

		if (event.getMessage() == null) return;

		String arg = event.getMessage().trim().toLowerCase();
		List<String> cmds = null;

		if (plugin.getConfiguration().getCV().isSpectatorEnabled() && GameUtils.isSpectatorPlaying(p)) {
			cmds = plugin.getConfiguration().getCV().getSpectatorCmds();
			if (cmds != null && !cmds.isEmpty()) {
				if (!cmds.contains(arg) && !p.hasPermission("ragemode.bypass.spectatorcommands")) {
					p.sendMessage(RageMode.getLang().get("game.this-command-is-disabled-in-game"));
					event.setCancelled(true);
					return;
				}
			}
		}

		if (GameUtils.isPlayerPlaying(p)) {
			if (waitingGames.containsKey(GameUtils.getGameByPlayer(p).getPlayersGame(p))) {
				if (waitingGames.get(GameUtils.getGameByPlayer(p).getPlayersGame(p))) {
					p.sendMessage(RageMode.getLang().get("game.command-disabled-in-end-game"));
					event.setCancelled(true);
					return;
				}
			}

			cmds = plugin.getConfiguration().getCV().getAllowedCmds();
			if (cmds != null && !cmds.isEmpty()) {
				if (!cmds.contains(arg) && !p.hasPermission("ragemode.bypass.disabledcommands")) {
					p.sendMessage(RageMode.getLang().get("game.this-command-is-disabled-in-game"));
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void eggThrow(PlayerEggThrowEvent event) {
		final Player p = event.getPlayer();

		if (p == null || !p.isOnline()) {
			return;
		}

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		if (GameUtils.getStatus(p) != GameStatus.RUNNING && GameUtils.getStatus(p) != GameStatus.GAMEFREEZE) {
			return;
		}

		// Removes the egg from player inventory and prevent
		// other item remove when moved the slot to another
		if (p.getInventory().contains(Material.EGG)) {
			ItemStack item = p.getInventory().getItem(plugin.getConfiguration().getCfg().getInt("items.grenade.slot"));
			item.setAmount(item.getAmount() - 1);
		}

		// no baby chickens
		event.setHatching(false);

		// check if player is in waiting game (game freeze)
		if (waitingGames.containsKey(GameUtils.getGameByPlayer(p).getPlayersGame(p))) {
			if (waitingGames.get(GameUtils.getGameByPlayer(p).getPlayersGame(p)))
				return;
		}

		final Item grenade = p.getWorld().dropItem(event.getEgg().getLocation(), new ItemStack(Material.EGG));

		// Cancel egg pick up
		grenade.setPickupDelay(41);

		grenade.setVelocity(p.getEyeLocation().getDirection());
		// move egg from land location to simulate bounce
		grenade.getLocation().add(event.getEgg().getVelocity());

		final List<Entity> nears = grenade.getNearbyEntities(13, 13, 13);

		if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
			Sounds.CREEPER_HISS.playSound(grenade.getLocation(), 1, 1);
		else
			Sounds.ENTITY_CREEPER_PRIMED.playSound(grenade.getLocation(), 1, 1);

		new BukkitRunnable() {
			@Override
			public void run() {
				// Remove egg when the player is not in-game.
				if (!GameUtils.isPlayerPlaying(p)) {
					grenade.remove();
					cancel();
					return;
				}

				// Remove egg in 1 second of the game when a player dropped
				if (waitingGames.containsKey(GameUtils.getGameByPlayer(p).getPlayersGame(p))) {
					if (waitingGames.get(GameUtils.getGameByPlayer(p).getPlayersGame(p))) {
						if (grenadeExplosionVictims != null) {
							grenadeExplosionVictims.clear();
						}

						grenade.remove();
						cancel();
						return;
					}
				}

				Location loc = grenade.getLocation();
				double gX = loc.getX();
				double gY = loc.getY();
				double gZ = loc.getZ();

				grenade.getWorld().createExplosion(gX, gY, gZ, 3f, false, false);
				grenade.remove(); // get rid of egg so it can't be picked up

				int i = 0;
				int imax = nears.size();
				while (i < imax) {
					if (nears.get(i) instanceof Player) {
						Player near = (Player) nears.get(i);
						if (grenadeExplosionVictims != null) {
							if (grenadeExplosionVictims.containsKey(near.getUniqueId())) {
								grenadeExplosionVictims.remove(near.getUniqueId());
								grenadeExplosionVictims.put(near.getUniqueId(), p.getUniqueId());
							}
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

		if (GameUtils.isPlayerPlaying(p)) {
			if (GameUtils.getStatus(p) == GameStatus.RUNNING) {
				Player thrower = event.getPlayer();
				if (waitingGames.containsKey(GameUtils.getGameByPlayer(thrower).getPlayersGame(thrower))) {
					if (waitingGames.get(GameUtils.getGameByPlayer(thrower).getPlayersGame(thrower)))
						return;
				}

				ItemStack hand = NMS.getItemInHand(thrower);
				ItemMeta meta = hand.getItemMeta();
				if (meta != null && meta.getDisplayName() != null) {
					if (meta.getDisplayName().equals(CombatAxe.getName())) {
						thrower.launchProjectile(Snowball.class);
						NMS.setItemInHand(thrower, null);
					}
				}
			}

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR
					|| event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (GameUtils.getStatus(p) == GameStatus.WAITING) {
					ItemStack hand = NMS.getItemInHand(p);
					ItemMeta meta = hand.getItemMeta();
					if (meta != null && meta.getDisplayName() != null) {
						Game game = GameUtils.getGameByPlayer(p);
						String name = game.getPlayersGame(p);

						if (p.hasPermission("ragemode.admin.item.forcestart")
								&& meta.getDisplayName().equals(ForceStarter.getName())) {
							if (GameUtils.getGameByName(name).getLobbyTimer() != null) {
								GameUtils.getGameByName(name).getLobbyTimer().cancel();
								p.setLevel(0); // Set level counter back to 0
							}

							p.sendMessage(RageMode.getLang().get("commands.forcestart.game-start", "%game%", name));
							RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(
									RageMode.getInstance(), () -> new GameLoader(GameUtils.getGameByName(name)));
						}

						if (meta.getDisplayName().equals(LeaveGame.getName())) {
							game.removePlayer(p);
							game.removeSpectatorPlayer(p);
						}
					}
					event.setCancelled(true);
				}
			}
		}

		if (plugin.getConfiguration().getCV().isSignsEnable() && event.getClickedBlock() != null
				&& event.getClickedBlock().getState() != null) {
			org.bukkit.block.Block b = event.getClickedBlock();

			if (b.getState() instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK
					&& SignCreator.isSign(b.getLocation())) {
				if (!p.hasPermission("ragemode.join.sign")) {
					p.sendMessage(RageMode.getLang().get("no-permission"));
					return;
				}

				String game = SignCreator.getGameFromString();
				if (!GameUtils.isGameWithNameExists(game)) {
					p.sendMessage(RageMode.getLang().get("game.does-not-exist"));
					return;
				}

				GameUtils.joinPlayer(p, GameUtils.getGameByName(game));
			}
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (GameUtils.isPlayerPlaying((Player) e.getPlayer())) {
			InventoryType type = e.getInventory().getType();

			if (type == InventoryType.ENCHANTING
					|| type == InventoryType.CRAFTING
					|| type == InventoryType.ANVIL
					|| type == InventoryType.CHEST
					|| type == InventoryType.ENDER_CHEST
					|| type == InventoryType.BREWING
					|| type == InventoryType.FURNACE
					|| type == InventoryType.WORKBENCH
					|| type == InventoryType.DROPPER
					|| type == InventoryType.HOPPER
					|| type == InventoryType.BEACON
					|| (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && type == InventoryType.STONECUTTER
					|| type == InventoryType.BLAST_FURNACE
					|| type == InventoryType.MERCHANT)
					|| (Version.isCurrentEqualOrHigher(Version.v1_11_R1) && type == InventoryType.SHULKER_BOX))
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractRedstone(PlayerInteractEvent ev) {
		Player p = ev.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		if (ev.getClickedBlock() != null) {
			Material t = ev.getClickedBlock().getType();

			if (ev.getAction() == Action.PHYSICAL) {
				if (t == Material.FARMLAND) {
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setCancelled(true);
				} else if (RageMode.getInstance().getConfiguration().getCV().isCancelRedstoneActivate()
						&& GameUtils.getStatus(p) == GameStatus.RUNNING
						|| GameUtils.getStatus(p) == GameStatus.GAMEFREEZE) {
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
			} else if (RageMode.getInstance().getConfiguration().getCV().isCancelRedstoneActivate()
					&& ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (GameUtils.getStatus(p) == GameStatus.RUNNING || GameUtils.getStatus(p) == GameStatus.GAMEFREEZE) {
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
					} else {
						if (t.equals(Material.COMPARATOR) || t.equals(Material.REPEATER)) {
							ev.setUseInteractedBlock(Event.Result.DENY);
							ev.setCancelled(true);
						}
					}

					if (t.equals(Material.LEVER) || t.equals(Material.DAYLIGHT_DETECTOR)) {
						ev.setUseInteractedBlock(Event.Result.DENY);
						ev.setCancelled(true);
					}
				}
			}

			if (RageMode.getInstance().getConfiguration().getCV().isCancelDoorUse()
					&& ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (GameUtils.getStatus(p) == GameStatus.RUNNING || GameUtils.getStatus(p) == GameStatus.GAMEFREEZE) {
					if (MaterialUtil.isWoodenDoor(t)) {
						ev.setUseInteractedBlock(Event.Result.DENY);
						ev.setCancelled(true);
					}
				}
			}

			// Just prevent usage of bush
			if (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (t.equals(Material.SWEET_BERRY_BUSH) || t.equals(Material.COMPOSTER)) {
					ev.setUseInteractedBlock(Event.Result.DENY);
					ev.setCancelled(true);
				}
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
		if (event.getWhoClicked() instanceof Player && GameUtils.isPlayerPlaying((Player) event.getWhoClicked()))
			event.setCancelled(true);
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
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled() || event.getPlayer() == null || event.getBlock() == null) return;

		if (plugin.getConfiguration().getCV().isSignsEnable()
				&& event.getLine(0).contains("[rm]") || event.getLine(0).contains("[ragemode]")) {
			if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
				event.getPlayer().sendMessage(RageMode.getLang().get("no-permission-to-interact-sign"));
				event.setCancelled(true);
				return;
			}

			String l1 = event.getLine(1);
			if (!GameUtils.isGameWithNameExists(l1)) {
				event.getPlayer().sendMessage(RageMode.getLang().get("invalid-game", "%game%", l1));
				return;
			}

			for (String game : GetGames.getGameNames()) {
				if (game.equalsIgnoreCase(l1))
					SignCreator.createNewSign((Sign) event.getBlock().getState(), game);

				SignCreator.updateSign(((Sign) event.getBlock().getState()).getLocation());
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		if (!GameUtils.isPlayerPlaying(p)) {
			return;
		}

		String game = GameUtils.getGameByPlayer(p).getPlayersGame(p);

		if (GameUtils.getStatus(p) == GameStatus.RUNNING) {
			if (waitingGames != null && waitingGames.containsKey(game)) {
				if (waitingGames.get(game)) {
					Location from = event.getFrom();
					Location to = event.getTo();
					double x = Math.floor(from.getX());
					double z = Math.floor(from.getZ());
					if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
						x += .5;
						z += .5;
						p.teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
					}
				}
			}
		}

		if (p.getLocation().getY() < 0) {
			GameUtils.getGameSpawnByName(game).randomSpawn(p);

			// Prevent damaging player when respawned
			p.setFallDistance(0);

			GameUtils.broadcastToGame(game, RageMode.getLang().get("game.void-fall", "%player%", p.getName()));
		}
	}

	@EventHandler
	public void onWorldChangedEvent(PlayerTeleportEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()) && !MapChecker.isGameWorld(
				GameUtils.getGameByPlayer(event.getPlayer()).getPlayersGame(event.getPlayer()),
				event.getTo().getWorld())) {
			if (!event.getPlayer().hasMetadata("Leaving"))
				event.getPlayer().performCommand("rm leave");
			else
				event.getPlayer().removeMetadata("Leaving", RageMode.getInstance());
		}
	}
}
