package hu.montlikadani.ragemode.achievements;

import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.ragemode.achievements.Achievement.AchievementReason;
import hu.montlikadani.ragemode.events.KillEvent;

public class AchievementHandler implements Listener {

  private Set<Achievement> achievements;

  public AchievementHandler(Set<Achievement> achievements) {
    this.achievements = achievements;
  }

  @EventHandler
  public void onKill(KillEvent event) {
    for (Achievement achievement : achievements) {
      if (achievement.getReason() == AchievementReason.FIRST_KILL) {
        // TODO Add Code responsible for the first kill achievement
      }

      if (achievement.getReason() == AchievementReason.FIRST_BLOOD) {
        // TODO Add Code responsible for the first blood achievement
      }
    }
  }
}
