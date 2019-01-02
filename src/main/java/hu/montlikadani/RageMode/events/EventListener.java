package hu.montlikadani.ragemode.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameBroadcast;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.MapChecker;
import hu.montlikadani.ragemode.gameUtils.Titles;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.Grenade;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageBow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;

@SuppressWarnings("deprecation")
public class EventListener implements Listener {

	private RageMode plugin;
	public static HashMap<String, Boolean> waitingGames = new HashMap<>();
	private Map<UUID, UUID> explosionVictims = new HashMap<>();
	private Map<UUID, UUID> grenadeExplosionVictims = new HashMap<>();

	public EventListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		HoloHolder.showAllHolosToPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (PlayerList.isPlayerPlaying(player.getUniqueId().toString())) {
			if (PlayerList.removePlayer(player)) {
				RageMode.logConsole(Level.INFO, "Player " + player.getName() + " left the server while playing.");

				List<String> list = plugin.getConfiguration().getCfg().getStringList("game.global.run-commands-for-player-left-while-playing");
				if (list == null || list.isEmpty()) return;

				for (String cmds : list) {
					cmds = cmds.replace("%player%", player.getName());
					cmds = cmds.replace("%player-ip%", player.getAddress().getAddress().getHostAddress());
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), RageMode.getLang().colors(cmds));
				}
			}
		}
		HoloHolder.deleteHoloObjectsOfPlayer(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChatFormat(AsyncPlayerChatEvent event) {
		if (event.getPlayer() == null || !event.getPlayer().isOnline()) return;

		Player p = event.getPlayer();

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString()) && plugin.getConfiguration().getCfg().getBoolean("game.chat-format.enable")) {
			String game = PlayerList.getPlayersGame(p);

			String[] players = PlayerList.getPlayersInGame(game);
			String format = plugin.getConfiguration().getCfg().getString("game.chat-format.format");
			format = format.replace("%player%", p.getName());
			format = format.replace("%player-displayname%", p.getDisplayName());
			format = format.replace("%game%", game);
			format = format.replace("%online-ingame-players%", String.valueOf(players.length));
			format = format.replace("%points%", Integer.toString(RageScores.getPlayerPoints(p.getUniqueId().toString()).getPoints()));
			format = format.replace("%message%", event.getMessage());

			event.setFormat(RageMode.getLang().colors(format));
			/*for (String playerUUID : players) {
				Bukkit.getPlayer(UUID.fromString(playerUUID)).sendMessage(format);
			}*/
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {
		// RageArrow explosion event
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

					arrow.getWorld().createExplosion(x, y, z, 2f, false, false); // original 4f
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
						}
						i++;
					}
				}
			}
		}
	}

	@EventHandler
	public void onRageKnifeHit(EntityDamageByEntityEvent event) {
		// RageKnife hit event
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			Player killer = (Player) event.getDamager();
			Player victim = (Player) event.getEntity();
			if (PlayerList.isPlayerPlaying(killer.getUniqueId().toString())
					&& PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
				if (waitingGames.containsKey(PlayerList.getPlayersGame(killer))) {
					if (waitingGames.get(PlayerList.getPlayersGame(killer))) {
						event.setCancelled(true);
						return;
					}
				}
				ItemStack hand = Bukkit.getVersion().equals("1.8") ? killer.getItemInHand() : killer.getInventory().getItemInMainHand();
				ItemMeta meta = hand.getItemMeta();
				if (meta != null && meta.getDisplayName() != null) {
					if (meta.getDisplayName().equals(RageKnife.getRageKnifeName()))
						event.setDamage(25);
				}
			}
			if (PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
				if (!PlayerList.isGameRunning(PlayerList.getPlayersGame(victim)))
					event.setDamage(0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemSpawn(PlayerDropItemEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onHitPlayer(EntityDamageEvent event) {
		// Hit player event
		if (event.getEntity() instanceof Player) {
			if (PlayerList.isPlayerPlaying(event.getEntity().getUniqueId().toString())) {
				if (event.getCause().equals(DamageCause.PROJECTILE)) {
					Player victim = (Player) event.getEntity();
					if (PlayerList.isPlayerPlaying(victim.getUniqueId().toString())) {
						if (waitingGames.containsKey(PlayerList.getPlayersGame(victim))) {
							if (waitingGames.get(PlayerList.getPlayersGame(victim))) {
								event.setDamage(0);
								event.setCancelled(true);
								return;
							}
						}
						ItemStack hand = Bukkit.getVersion().equals("1.8") ? victim.getItemInHand() : victim.getInventory().getItemInMainHand();
						ItemMeta meta = hand.getItemMeta();
						if (meta != null && meta.getDisplayName() != null) {
							if (hand.getItemMeta().getDisplayName().equals(Grenade.getGrenadeName()))
								event.setDamage(5d);

							if (hand.getItemMeta().getDisplayName().equals(RageArrow.getRageArrowName()))
								if (event.getDamage() == 0.0d)
									event.setDamage(28.34d);
								else
									event.setDamage(27.114);
						}
					}
				}
				if (event.getCause().equals(DamageCause.FALL)) {
					Player p = (Player) event.getEntity();
					if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
						p.getInventory().clear();
						event.setDamage(100d);

						GameBroadcast.broadcastToGame(PlayerList.getPlayersGame(p), RageMode.getLang().get("game.void-fall", "%player%", p.getName()));

						playerVars(p);
					}
				}
			}
		}
	}

	//private ArrayList<String> listPlayers = new ArrayList<>();

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if (event.isCancelled()) return;

		if (event.getEntity() != null && event.getEntity() instanceof Player) {
			final Player p = (Player) event.getEntity();
			final Arrow arrow = (Arrow) event.getProjectile();

			if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
				new BukkitRunnable() {
					@Override
					public void run() {
						if (!arrow.isOnGround() && !arrow.isDead()) {
							arrow.getWorld().spawnParticle(Particle.SMOKE_NORMAL, arrow.getLocation(), 0, 0, 10, 1);
						} else {
							cancel();
						}
					}
				}.runTaskTimer(plugin, 0, 1);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		// Player autorespawn
		Player deceased;
		if (event.getEntity() != null && event.getEntity() instanceof Player)
			deceased = (Player) event.getEntity();
		else {
			deceased = null;
			return;
		}
		if (deceased != null && PlayerList.isPlayerPlaying(deceased.getUniqueId().toString())) {
			if ((deceased.getKiller() != null && PlayerList.isPlayerPlaying(deceased.getKiller().getUniqueId().toString()))
					|| deceased.getKiller() == null) {
				String game = PlayerList.getPlayersGame(deceased);

				boolean doDeathBroadcast = plugin.getConfiguration().getCfg().getBoolean("game.global.death-messages");

				if (plugin.getConfiguration().getArenasCfg().isSet("arenas." + game + ".death-messages")) {
					String gameBroadcast = plugin.getConfiguration().getArenasCfg().getString("arenas." + game + ".death-messages");
					if (gameBroadcast != null && gameBroadcast != "") {
						if (gameBroadcast.equals("true") || gameBroadcast.equals("false"))
							doDeathBroadcast = Boolean.parseBoolean(gameBroadcast);
					}
				}

				if (deceased.getLastDamage() == 0.0f) {
					if (deceased.getKiller() == null) {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "combataxe");
					} else {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.axe-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "combataxe");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.PROJECTILE)) {
					if (deceased.getKiller() == null) {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "ragebow");
					} else {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.arrow-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "ragebow");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)) {
					if (deceased.getKiller() == null) {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getName()));

						RageScores.addPointsToPlayer(deceased, deceased, "rageknife");
					} else {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.knife-kill", "%victim%", deceased.getName(),
									"%killer%", deceased.getKiller().getName()));

						RageScores.addPointsToPlayer(deceased.getKiller(), deceased, "rageknife");
					}
				} else if (deceased.getLastDamageCause().getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
					if (explosionVictims.containsKey(deceased.getUniqueId())) {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%", deceased.getName(),
									"%killer%", Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())).getName()));

						RageScores.addPointsToPlayer(Bukkit.getPlayer(explosionVictims.get(deceased.getUniqueId())),
								deceased, "explosion");
					} else if (grenadeExplosionVictims.containsKey(deceased.getUniqueId())) {
						GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.explosion-kill", "%victim%", deceased.getName(),
								"%killer%", Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())).getName()));

					RageScores.addPointsToPlayer(Bukkit.getPlayer(grenadeExplosionVictims.get(deceased.getUniqueId())),
							deceased, "explosion");
					} else {
						if (doDeathBroadcast)
							GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.broadcast.error-kill"));

						deceased.sendMessage(RageMode.getLang().get("game.unknown-killer"));
					}
				} else {
					if (doDeathBroadcast)
						GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.unknown-weapon", "%victim%", deceased.getName()));
				}
				event.setDeathMessage("");
			}

			if (plugin.getConfiguration().getCfg().getBoolean("game.keep-inventory-on-death"))
				event.setKeepInventory(true);
			else {
				event.setKeepInventory(false);
				deceased.getInventory().clear();
			}
			playerVars(deceased);
		}
	}

	private void playerVars(Player deceased) {
		GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(PlayerList.getPlayersGame(deceased));
		List<Location> spawns = gameSpawnGetter.getSpawnLocations();
		// Location[] aSpawns = (Location[]) spawns.toArray(); ----> performance optimization

		Random rand = new Random();
		int x = rand.nextInt(spawns.size() - 1); // ----> performance optimization

		deceased.setHealth(20);

		deceased.teleport(spawns.get(x)); // ----> performance optimization

		// give him a new set of items
		// deceased.getInventory().clear();
		FileConfiguration conf = plugin.getConfiguration().getCfg();
		deceased.getInventory().setItem(conf.getInt("items.rageBow.slot"), RageBow.getRageBow());
		deceased.getInventory().setItem(conf.getInt("items.rageKnife.slot"), RageKnife.getRageKnife());
		deceased.getInventory().setItem(conf.getInt("items.combatAxe.slot"), CombatAxe.getCombatAxe());
		deceased.getInventory().setItem(conf.getInt("items.rageArrow.slot"), RageArrow.getRageArrow());
		deceased.getInventory().setItem(conf.getInt("items.grenade.slot"), Grenade.getGrenade());
	}

	@EventHandler
	public void onHungerGain(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (PlayerList.isPlayerPlaying(player.getUniqueId().toString()))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSignBreak(BlockBreakEvent event) {
		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable") && event.getBlock().getState() instanceof Sign
				&& !event.isCancelled())
			SignCreator.removeSign((Sign) event.getBlock().getState());
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString()) && !p.hasPermission("ragemode.admin.cmd")) {
			if (event.getMessage() != null) {
				List<String> cmds = plugin.getConfiguration().getCfg().getStringList("game.global.commands");
				if (cmds == null || cmds.isEmpty()) return;

				String arg = event.getMessage().trim().toLowerCase();
				if (cmds.contains(arg)) {
					if (waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
						if (waitingGames.get(PlayerList.getPlayersGame(p)))
							event.setCancelled(true);
					}
				} else {
					p.sendMessage(RageMode.getLang().get("game.this-command-is-disabled-in-game"));
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void eggThrow(PlayerEggThrowEvent event) {
		if (event.getPlayer() == null || !event.getPlayer().isOnline()) return;

		Player p = event.getPlayer();
		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			event.setHatching(false); // no baby chickens

			ItemStack hand = Bukkit.getVersion().equals("1.8") ? p.getItemInHand() : p.getInventory().getItemInMainHand();
			hand.setAmount(hand.getAmount() - 1);

			final Item grenade = p.getWorld().dropItem(event.getEgg().getLocation(), new ItemStack(Material.EGG));
			grenade.setVelocity(p.getEyeLocation().getDirection());
			// move egg from land location to simulate bounce
			grenade.getLocation().add(event.getEgg().getVelocity());
			List<Entity> nears = grenade.getNearbyEntities(10, 10, 10);
			if (!Bukkit.getVersion().equals("1.8"))
				/**grenade.getWorld().playSound(grenade.getLocation(), org.bukkit.Sound.EXPLODE, 1, 1);
			else*/
				grenade.getWorld().playSound(grenade.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 1);
			grenade.setPickupDelay(41);
			new BukkitRunnable() {
				@Override
				public void run() {
					Location loc = grenade.getLocation();
					double gX = loc.getX();
					double gY = loc.getY();
					double gZ = loc.getZ();

					grenade.getWorld().createExplosion(gX, gY, gZ, 2f, false, false);
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
						}
						i++;
					}

					if (getTaskId() == 0)
						cancel();
				}
			}.runTaskLater(plugin, 40);
		}
	}

	@EventHandler
	public void onCombatAxeThrow(PlayerInteractEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
			Player thrower = event.getPlayer();
			if (waitingGames.containsKey(PlayerList.getPlayersGame(thrower))) {
				if (waitingGames.get(PlayerList.getPlayersGame(thrower)))
					return;
			}
			ItemStack hand = Bukkit.getVersion().equals("1.8") ? thrower.getItemInHand() : thrower.getInventory().getItemInMainHand();

			ItemMeta meta = hand.getItemMeta();
			if (meta != null && meta.getDisplayName() != null) {
				if (meta.getDisplayName().equals(CombatAxe.getCombatAxeName())) {
					thrower.launchProjectile(org.bukkit.entity.Snowball.class);
					if (Bukkit.getVersion().equals("1.8"))
						thrower.setItemInHand(null);
					else
						thrower.getInventory().setItemInMainHand(null);
				}
			}
		}

		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable") && event.getClickedBlock() != null
				&& event.getClickedBlock().getState() != null) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Sign sign = (Sign) event.getClickedBlock().getState();
					if (SignCreator.isJoinSign(sign)) {
						Player player = event.getPlayer();
						if (player.hasPermission("ragemode.join")) {
							String arena = SignCreator.getGameFromString(sign);
							MapChecker mapChecker = new MapChecker(arena);
							if (mapChecker.isValid()) {
								String path = "arenas." + arena + ".lobby";
								Configuration conf = plugin.getConfiguration();
								String world = conf.getArenasCfg().getString(path + ".world");
								double lobbyX = conf.getArenasCfg().getDouble(path + ".lobby.x");
								double lobbyY = conf.getArenasCfg().getDouble(path + ".lobby.y");
								double lobbyZ = conf.getArenasCfg().getDouble(path + ".lobby.z");
								double lobbyYaw = conf.getArenasCfg().getDouble(path + ".lobby.yaw");
								double lobbyPitch = conf.getArenasCfg().getDouble(path + ".lobby.pitch");

								Location lobbyLocation = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ);
								lobbyLocation.setYaw((float) lobbyYaw);
								lobbyLocation.setPitch((float) lobbyPitch);

								org.bukkit.inventory.PlayerInventory inv = player.getInventory();
								if (PlayerList.addPlayer(player, arena)) {
									PlayerList.oldLocations.addToBoth(player, player.getLocation());
									PlayerList.oldInventories.addToBoth(player, inv.getContents());
									PlayerList.oldArmor.addToBoth(player, inv.getArmorContents());
									PlayerList.oldHealth.addToBoth(player, player.getHealth());
									PlayerList.oldHunger.addToBoth(player, player.getFoodLevel());
									PlayerList.oldEffects.addToBoth(player, player.getActivePotionEffects());
									PlayerList.oldGameMode.addToBoth(player, player.getGameMode());
									PlayerList.oldDisplayName.addToBoth(player, player.getDisplayName());
									PlayerList.oldListName.addToBoth(player, player.getPlayerListName());

									inv.clear();
									player.teleport(lobbyLocation);
									player.setGameMode(GameMode.SURVIVAL);
									player.setHealth(20);
									player.setFoodLevel(20);
									inv.setHelmet(null);
									inv.setChestplate(null);
									inv.setLeggings(null);
									inv.setBoots(null);
									GameBroadcast.broadcastToGame(arena, RageMode.getLang().get("game.player-joined", "%player%", player.getName()));
									String title = conf.getCfg().getString("titles.join-game.title");
									String subtitle = conf.getCfg().getString("titles.join-game.subtitle");
									if (title != null && !title.equals("") && subtitle != null && !subtitle.equals("")) {
										title = title.replace("%game%", arena);
										subtitle = subtitle.replace("%game%", arena);
										Titles.sendTitle(player, conf.getCfg().getInt("titles.join-game.fade-in"), conf.getCfg().getInt("titles.join-game.stay"),
												conf.getCfg().getInt("titles.join-game.fade-out"), title, subtitle);
									}
									SignCreator.updateAllSigns(arena);
								} else
									RageMode.logConsole(Level.INFO, RageMode.getLang().get("game.player-could-not-join", "%player%", player.getName(), "%game%", arena));
							} else
								player.sendMessage(mapChecker.getMessage());
						} else
							player.sendMessage(RageMode.getLang().get("no-permission"));
					}
				}
			}
		}
	}

	@EventHandler
	public void onAchivement(PlayerAchievementAwardedEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()) &&
				plugin.getConfiguration().getCfg().getBoolean("game.global.disable-player-achievement-award"))
			event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryInteractEvent(InventoryClickEvent event) {
		if (PlayerList.isPlayerPlaying(event.getWhoClicked().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFly(PlayerToggleFlightEvent event) {
		if (event.isCancelled()) return;

		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	private void PlayerBedEnter(PlayerBedEnterEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onItemPickedUpEvent(PlayerPickupItemEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.getBlock() == null) return;

		Sign sign = (Sign) event.getBlock().getState();

		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable")
				&& event.getLine(0).equalsIgnoreCase("[rm]") || event.getLine(0).equalsIgnoreCase("[ragemode]")) {
			if (!event.getPlayer().hasPermission("ragemode.admin.signs")) {
				event.setCancelled(true);
				return;
			}

			for (String game : GetGames.getGameNames()) {
				if (event.getLine(1).equalsIgnoreCase(game)) {
					SignCreator.createNewSign(sign, game);
					SignCreator.updateSign(event);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (PlayerList.isPlayerPlaying(event.getPlayer().getUniqueId().toString())) {
			Player p = event.getPlayer();
			if (waitingGames != null) {
				if (waitingGames.containsKey(PlayerList.getPlayersGame(p))) {
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
