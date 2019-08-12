package hu.montlikadani.ragemode.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
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
import hu.montlikadani.ragemode.gameLogic.GameLoader;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
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
import hu.montlikadani.ragemode.signs.SignData;

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
		HoloHolder.showAllHolosToPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		GameUtils.kickPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent ev) {
		GameUtils.kickPlayer(ev.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChatFormat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();

		// Cancels the spectator player chat
		if (PlayerList.specPlayer.containsKey(p.getUniqueId()))
			event.setCancelled(true);

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (GameUtils.getStatus() == GameStatus.WAITING) {
				if (!plugin.getConfiguration().getCfg().getBoolean("game.global.lobby.enable-chat-in-lobby")
						&& !p.hasPermission("ragemode.bypass.lobby.lockchat")) {
					event.setCancelled(true);
					p.sendMessage(RageMode.getLang().get("game.lobby.chat-is-disabled"));
					return;
				}
			}

			if (GameUtils.getStatus() == GameStatus.RUNNING) {
				if (!plugin.getConfiguration().getCfg().getBoolean("game.global.enable-chat-in-game")
						&& !p.hasPermission("ragemode.bypass.game.lockchat")) {
					p.sendMessage(RageMode.getLang().get("game.chat-is-disabled"));
					event.setCancelled(true);
					return;
				}

				if (plugin.getConfiguration().getCfg().getBoolean("game.global.chat-format.enable")) {
					String game = PlayerList.getPlayersGame(p);
					String format = plugin.getConfiguration().getCfg().getString("game.global.chat-format.format");
					format = format.replace("%player%", p.getName());
					format = format.replace("%player-displayname%", p.getDisplayName());
					format = format.replace("%game%", game);
					format = format.replace("%online-ingame-players%", Integer.toString(PlayerList.getPlayers().size()));
					format = format.replace("%message%", event.getMessage());
					format = Utils.setPlaceholders(format, p);

					event.setFormat(RageMode.getLang().colors(format));
				}
			}

			if (GameUtils.getStatus() == GameStatus.GAMEFREEZE) {
				if (!plugin.getConfiguration().getCfg().getBoolean("game.global.enable-chat-after-end")) {
					p.sendMessage(RageMode.getLang().get("game.game-freeze.chat-is-disabled"));
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		// RageArrow explosion event
		if (GameUtils.getStatus() == GameStatus.RUNNING) {
			if (event.getEntity() instanceof Arrow) {
				Arrow arrow = (Arrow) event.getEntity();

				if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
					Player shooter = (Player) arrow.getShooter();
					if (PlayerList.isPlayerPlaying(shooter.getUniqueId().toString())) {
						if (waitingGames.containsKey(PlayerList.getPlayersGame(shooter))) {
							if (waitingGames.get(PlayerList.getPlayersGame(shooter)))
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
		if (event.getEntity() instanceof Player) {
			Player victim = (Player) event.getEntity();

			if (GameUtils.getStatus() == GameStatus.RUNNING) {
				// RageKnife hit event
				if (event.getDamager() instanceof Player) {
					Player killer = (Player) event.getDamager();

					if (PlayerList.isPlayerPlaying(killer.getUniqueId().toString())
							&& PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
						if (waitingGames.containsKey(PlayerList.getPlayersGame(killer))) {
							if (waitingGames.get(PlayerList.getPlayersGame(killer))) {
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
			if (GameUtils.getStatus() == GameStatus.WAITING && PlayerList.isPlayerPlaying(victim.getUniqueId().toString()))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHitPlayer(EntityDamageEvent event) {
		Entity e = event.getEntity();
		// Hit player event
		if (GameUtils.getStatus() == GameStatus.RUNNING) {
			if (e instanceof Player && PlayerList.isPlayerPlaying(e.getUniqueId().toString())) {
				if (event.getCause().equals(DamageCause.FALL) && !plugin.getConfiguration().getCfg()
						.getBoolean("game.global.damage-player-fall")) {
					event.setCancelled(true);
					return;
				}

				Player victim = (Player) e;
				if (waitingGames.containsKey(PlayerList.getPlayersGame(victim))) {
					if (waitingGames.get(PlayerList.getPlayersGame(victim))) {
						event.setCancelled(true);
					}
				}
			}
		} else if (GameUtils.getStatus() == GameStatus.WAITING && e instanceof Player
				&& PlayerList.isPlayerPlaying(e.getUniqueId().toString()))
			event.setCancelled(true); // Prevent player damage in lobby
		else if (GameUtils.getStatus() == GameStatus.GAMEFREEZE) { // Prevent damage in game freeze
			if (e instanceof Player && PlayerList.isPlayerPlaying(e.getUniqueId().toString())) {
				if (waitingGames.containsKey(PlayerList.getPlayersGame((Player) e))) {
					if (waitingGames.get(PlayerList.getPlayersGame((Player) e)))
						event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBowShoot(EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player && PlayerList.isPlayerPlaying(e.getEntity().getUniqueId().toString())) {
			if (waitingGames.containsKey(PlayerList.getPlayersGame((Player) e.getEntity()))) {
				if (waitingGames.get(PlayerList.getPlayersGame((Player) e.getEntity())))
					e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() == null)
			return;

		Player deceased = event.getEntity();

		if (deceased != null && PlayerList.isPlayerPlaying(deceased.getUniqueId().toString()) &&
				GameUtils.getStatus() == GameStatus.RUNNING) {
			String game = PlayerList.getPlayersGame(deceased);

			if ((deceased.getKiller() != null && PlayerList.isPlayerPlaying(deceased.getKiller().getUniqueId().toString()))
					|| deceased.getKiller() == null) {
				boolean doDeathBroadcast = plugin.getConfiguration().getCfg().getBoolean("game.global.death-messages");

				if (plugin.getConfiguration().getArenasCfg().isSet("arenas." + game + ".death-messages")) {
					String gameBroadcast = plugin.getConfiguration().getArenasCfg().getString("arenas." + game + ".death-messages");
					if (gameBroadcast != null && gameBroadcast != "") {
						if (gameBroadcast.equals("true") || gameBroadcast.equals("false"))
							doDeathBroadcast = Boolean.parseBoolean(gameBroadcast);
					}
				}

				String deceaseName = deceased.getName();
				List<MetadataValue> data = deceased.getMetadata("killedWith");

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
						break;

					default:
						if (doDeathBroadcast)
							GameUtils.broadcastToGame(game,
									RageMode.getLang().get("game.unknown-weapon", "%victim%", deceaseName));
						break;
					}

					deceased.removeMetadata("killedWith", plugin);
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

			GameUtils.addGameItems(deceased, true);
		}
	}

	//TODO fix 1.14 respawn bug: does not respawn suddenly the player
	@EventHandler(priority = EventPriority.HIGH)
	public void onRespawn(PlayerRespawnEvent e) {
		if (PlayerList.isPlayerPlaying(e.getPlayer().getUniqueId().toString())) {
			// If player still is in game with respawn screen then remove player
			// 1.14 issue
			if (!PlayerList.isGameRunning(PlayerList.getPlayersGame(e.getPlayer()))) {
				PlayerList.removePlayer(e.getPlayer());
				return;
			}

			GameSpawnGetter gsg = GameUtils.getGameSpawnByName(PlayerList.getPlayersGame(e.getPlayer()));
			Random rand = new Random();
			if (gsg.getSpawnLocations().size() > 0) {
				int x = rand.nextInt(gsg.getSpawnLocations().size());
				e.setRespawnLocation(gsg.getSpawnLocations().get(x));
			}
		}
	}

	@EventHandler
	public void onHungerGain(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && PlayerList.isPlayerPlaying(event.getEntity().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev) {
		if (PlayerList.isPlayerPlaying(ev.getPlayer().getUniqueId().toString()))
			ev.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDrop(PlayerDropItemEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		if (event.isCancelled() || !plugin.getConfiguration().getCfg().getBoolean("signs.enable")) return;

		org.bukkit.block.BlockState blockState = event.getBlock().getState();
		if (blockState instanceof Sign) {
			for (SignData data : SignCreator.getSignData()) {
				if (blockState.getLocation().equals(data.getLocation())) {
					if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
						event.getPlayer().sendMessage(RageMode.getLang().get("no-permission-to-interact-sign"));
						event.setCancelled(true);
						return;
					}

					SignCreator.removeSign((Sign) blockState);
				}
			}
		}
	}

	@EventHandler
	public void onDisabledCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();

		if (event.getMessage() == null) return;

		String arg = event.getMessage().trim().toLowerCase();
		List<String> cmds = null;

		if (plugin.getConfiguration().getCfg().getBoolean("spectator.enable") && PlayerList.specPlayer.containsKey(p.getUniqueId())) {
			cmds = plugin.getConfiguration().getCfg().getStringList("spectator.allowed-spectator-commands");
			if (cmds != null && !cmds.isEmpty()) {
				if (!cmds.contains(arg) && !p.hasPermission("ragemode.bypass.spectatorcommands")) {
					p.sendMessage(RageMode.getLang().get("game.this-command-is-disabled-in-game"));
					event.setCancelled(true);
					return;
				}
			}
		}

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
				if (waitingGames.get(PlayerList.getPlayersGame(p))) {
					p.sendMessage(RageMode.getLang().get("game.command-disabled-in-end-game"));
					event.setCancelled(true);
					return;
				}
			}

			cmds = plugin.getConfiguration().getCfg().getStringList("game.global.allowed-commands");
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
		if (event.getPlayer() == null || !event.getPlayer().isOnline()) return;

		final Player p = event.getPlayer();
		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (GameUtils.getStatus() == GameStatus.RUNNING || GameUtils.getStatus() == GameStatus.GAMEFREEZE) {
				// Removes the egg from player inventory and prevent
				// other item remove when moved the slot to another
				if (p.getInventory().contains(Material.EGG)) {
					ItemStack item = p.getInventory().getItem(plugin.getConfiguration().getCfg().getInt("items.grenade.slot"));
					item.setAmount(item.getAmount() - 1);
				}

				// no baby chickens
				event.setHatching(false);

				// check if player is in waiting game (game freeze)
				if (waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
					if (waitingGames.get(PlayerList.getPlayersGame(p)))
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
						// Remove egg in 1 second of the game when a player dropped
						if (waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
							if (waitingGames.get(PlayerList.getPlayersGame(p))) {
								if (grenadeExplosionVictims != null)
									grenadeExplosionVictims.clear();
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
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (GameUtils.getStatus() == GameStatus.RUNNING) {
				// Should fix inventoryOpen when the top of the chest opened
				/*if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
						event.getClickedBlock() != null && !event.getClickedBlock().getType().equals(Material.AIR) ||
						event.getClickedBlock().getType().equals(Material.CHEST) ||
						event.getClickedBlock().getType().equals(Material.ENDER_CHEST)) {
					event.setCancelled(true);
				}*/

				Player thrower = event.getPlayer();
				if (waitingGames.containsKey(PlayerList.getPlayersGame(thrower))) {
					if (waitingGames.get(PlayerList.getPlayersGame(thrower)))
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

			// Don't use left click block action to prevent block randomly hide
			//
			// Left/Right click air bug under 1.13: when the player joins the game with a left/right click
			// through the sign and the slot is on one of the lobby items, the item is executed
			// SO THESE ARE BUGGY!!
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
				if (GameUtils.getStatus() == GameStatus.WAITING) {
					ItemStack hand = NMS.getItemInHand(p);
					ItemMeta meta = hand.getItemMeta();
					if (meta != null && meta.getDisplayName() != null) {
						if (p.hasPermission("ragemode.admin.item.forcestart")
								&& meta.getDisplayName().equals(ForceStarter.getName())) {
							String game = PlayerList.getPlayersGame(p);

							PlayerList.getLobbyTimer().cancel();
							// Set level counter back to 0
							p.setLevel(0);

							p.sendMessage(RageMode.getLang().get("commands.forcestart.game-start", "%game%", game));
							new GameLoader(game);
						}

						if (meta.getDisplayName().equals(LeaveGame.getName())) {
							PlayerList.removePlayer(p);
							PlayerList.removeSpectatorPlayer(p);
						}
					}
					event.setCancelled(true);
				} else if (GameUtils.getStatus() != GameStatus.WAITING && GameUtils.getStatus() != GameStatus.RUNNING)
					event.setCancelled(true); // Fix AssertionError: TRAP server crash
			}
		}

		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable") && event.getClickedBlock() != null
				&& event.getClickedBlock().getState() != null) {
			org.bukkit.block.Block b = event.getClickedBlock();

			if (b.getState() instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (SignCreator.isJoinSign(b.getLocation())) {
					if (p.hasPermission("ragemode.join.sign")) {
						String game = SignCreator.getGameFromString();
						GameUtils.joinPlayer(p, game);
					} else
						p.sendMessage(RageMode.getLang().get("no-permission"));
				}
			}
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (PlayerList.isPlayerPlaying(e.getPlayer().getUniqueId().toString())) {
			InventoryType type = e.getInventory().getType();

			if (type == InventoryType.ENCHANTING
					|| type == InventoryType.CRAFTING
					|| type == InventoryType.ANVIL
					|| type == InventoryType.BREWING
					|| type == InventoryType.FURNACE
					|| type == InventoryType.WORKBENCH
					|| type == InventoryType.DROPPER
					|| type == InventoryType.HOPPER
					|| type == InventoryType.BEACON
					|| type == InventoryType.STONECUTTER
					|| type == InventoryType.BLAST_FURNACE
					|| type == InventoryType.SHULKER_BOX
					|| type == InventoryType.MERCHANT)
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractRedstone(PlayerInteractEvent ev) {
		if (PlayerList.isPlayerPlaying(ev.getPlayer().getUniqueId().toString())) {
			if (ev.getClickedBlock() != null) {
				Material t = ev.getClickedBlock().getType();

				if (ev.getAction() == Action.PHYSICAL) {
					if (t == Material.FARMLAND) {
						ev.setUseInteractedBlock(Event.Result.DENY);
						ev.setCancelled(true);
					} else if (plugin.getConfiguration().getCfg().getBoolean("game.global.cancel-redstone-activating-blocks")
							&& GameUtils.getStatus() == GameStatus.RUNNING || GameUtils.getStatus() == GameStatus.GAMEFREEZE) {
						if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
							if (Tag.WOODEN_PRESSURE_PLATES.isTagged(t)) {
								ev.setUseInteractedBlock(Event.Result.DENY);
								ev.setCancelled(true);
							}
						}

						if (Version.isCurrentEqualOrLower(Version.v1_12_R1)) {
							if (t.equals(Material.OAK_PRESSURE_PLATE) || t.equals(Material.STONE_PRESSURE_PLATE)) {
								ev.setUseInteractedBlock(Event.Result.DENY);
								ev.setCancelled(true);
							}
						}

						if (t.equals(Material.STONE_PRESSURE_PLATE) || t.equals(Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
								|| t.equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
							ev.setUseInteractedBlock(Event.Result.DENY);
							ev.setCancelled(true);
						}
					}
				} else if (plugin.getConfiguration().getCfg().getBoolean("game.global.cancel-redstone-activating-blocks")
						&& ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (GameUtils.getStatus() == GameStatus.RUNNING || GameUtils.getStatus() == GameStatus.GAMEFREEZE) {
						if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
							if (Tag.TRAPDOORS.isTagged(t) || Tag.BUTTONS.isTagged(t) || Tag.WOODEN_BUTTONS.isTagged(t)
									|| Tag.WOODEN_TRAPDOORS.isTagged(t)) {
								ev.setUseInteractedBlock(Event.Result.DENY);
								ev.setCancelled(true);
							}
						}

						if (Version.isCurrentEqualOrLower(Version.v1_12_R1)) {
							if (t.equals(Material.valueOf("WOOD_BUTTON")) || t.equals(Material.valueOf("TRAP_DOOR"))
									|| t.equals(Material.valueOf("STONE_BUTTON"))
									|| t.equals(Material.valueOf("REDSTONE_COMPARATOR"))
									|| t.equals(Material.valueOf("REDSTONE_COMPARATOR_ON"))
									|| t.equals(Material.valueOf("REDSTONE_COMPARATOR_OFF"))) {
								ev.setUseInteractedBlock(Event.Result.DENY);
								ev.setCancelled(true);
							}
						}

						if (t.equals(Material.LEVER) || t.equals(Material.COMPARATOR) || t.equals(Material.REPEATER)
								|| t.equals(Material.DAYLIGHT_DETECTOR)) {
							ev.setUseInteractedBlock(Event.Result.DENY);
							ev.setCancelled(true);
						}
					}
				}

				if (plugin.getConfiguration().getCfg().getBoolean("game.global.cancel-door-use") && ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (GameUtils.getStatus() == GameStatus.RUNNING || GameUtils.getStatus() == GameStatus.GAMEFREEZE) {
						if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
							if (Tag.DOORS.isTagged(t)) {
								ev.setUseInteractedBlock(Event.Result.DENY);
								ev.setCancelled(true);
							}
						}

						if (Version.isCurrentEqualOrLower(Version.v1_12_R1)) {
							if (t.equals(Material.valueOf("WOODEN_DOOR"))) {
								ev.setUseInteractedBlock(Event.Result.DENY);
								ev.setCancelled(true);
							}
						}
					}
				}

				// Just prevent usage of bush
				if (Version.isCurrentEqualOrHigher(Version.v1_14_R1) && ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (t.equals(Material.SWEET_BERRY_BUSH)) {
						ev.setUseInteractedBlock(Event.Result.DENY);
						ev.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (e.getEntered() instanceof Player && PlayerList.isPlayerPlaying(e.getEntered().getUniqueId().toString()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player && PlayerList.isPlayerPlaying(event.getWhoClicked().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onFly(PlayerToggleFlightEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void playerBedEnter(PlayerBedEnterEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled() || event.getPlayer() == null || event.getBlock() == null) return;

		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable")
				&& event.getLine(0).contains("[rm]") || event.getLine(0).contains("[ragemode]")) {
			if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(RageMode.getLang().get("no-permission-to-interact-sign"));
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

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (GameUtils.getStatus() == GameStatus.RUNNING) {
				if (waitingGames != null && waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
					if (waitingGames.get(PlayerList.getPlayersGame(p))) {
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
				GameUtils.getGameSpawnByName(PlayerList.getPlayersGame(p)).randomSpawn(p);

				// Prevent damaging player when respawned
				p.setFallDistance(0);

				GameUtils.broadcastToGame(PlayerList.getPlayersGame(p), RageMode.getLang().get("game.void-fall", "%player%", p.getName()));
			}
		}
	}

	@EventHandler
	public void onWorldChangedEvent(PlayerTeleportEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
			if (!MapChecker.isGameWorld(PlayerList.getPlayersGame(event.getPlayer()), event.getTo().getWorld())) {
				if (!event.getPlayer().hasMetadata("Leaving"))
					event.getPlayer().performCommand("rm leave");
				else
					event.getPlayer().removeMetadata("Leaving", RageMode.getInstance());
			}
		}
	}
}
