package hu.montlikadani.ragemode.items.threads;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.gameUtils.CacheableHitTarget;
import hu.montlikadani.ragemode.items.GameItems;

public class CombatAxeThread {

	private final Item item;

	private boolean running;

	public CombatAxeThread(Item item) {
		this.item = item;
	}

	public void start(Player player) {
		running = true;

		new Thread(() -> {
			while (running) {
				if (item.isDead()) {
					stop();
					return;
				}

				hu.montlikadani.ragemode.utils.SchedulerUtil.submitSync(() -> {
					for (Entity entity : hu.montlikadani.ragemode.utils.Utils.getNearbyEntities(item, 0.4D)) {
						if (!running) {
							break;
						}

						if (entity instanceof Player) {
							final Player victim = (Player) entity;

							if (victim != player) {
								victim.damage(25D, player);

								GameListener.HIT_TARGETS.put(victim.getUniqueId(),
										new CacheableHitTarget(GameItems.COMBATAXE, victim, 0));
							}
						} else if (entity instanceof LivingEntity) {
							((LivingEntity) entity).damage(25D, player);
						}

						item.remove();
						stop();
						break;
					}

					return 1;
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
