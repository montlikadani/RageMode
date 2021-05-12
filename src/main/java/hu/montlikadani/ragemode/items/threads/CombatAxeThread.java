package hu.montlikadani.ragemode.items.threads;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.gameUtils.CacheableHitTarget;
import hu.montlikadani.ragemode.items.GameItems;

public class CombatAxeThread {

	private Player player;
	private Item item;

	private boolean running;

	public CombatAxeThread(Player player, Item item) {
		this.player = player;
		this.item = item;
	}

	public void start() {
		running = true;

		final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(hu.montlikadani.ragemode.RageMode.class);

		new Thread(() -> {
			while (running) {
				if (item.isDead()) {
					stop();
					return;
				}

				// To prevent async catch
				plugin.getServer().getScheduler().runTask(plugin, () -> {
					for (Entity entity : item.getNearbyEntities(0.4D, 0.5D, 0.4D)) {
						if (!running) {
							break;
						}

						if (entity instanceof Player) {
							final Player victim = (Player) entity;

							if (victim == player) {
								continue;
							}

							victim.damage(25D, player);

							CacheableHitTarget cht = new CacheableHitTarget(victim, GameItems.COMBATAXE);
							cht.add(0);
							GameListener.HIT_TARGETS.put(victim.getUniqueId(), cht);
						} else if (entity instanceof LivingEntity) {
							((LivingEntity) entity).damage(25D, player);
						}

						item.remove();
						stop();
					}
				});

				if (item.isOnGround()) {
					item.remove();
					stop();
					return;
				}

				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {
					item.remove();
					stop();
				}
			}
		}).start();
	}

	public void stop() {
		running = false;
	}
}
