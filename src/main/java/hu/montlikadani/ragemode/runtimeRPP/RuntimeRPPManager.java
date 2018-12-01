package hu.montlikadani.ragemode.runtimeRPP;

import java.util.List;

import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;
import hu.montlikadani.ragemode.statistics.YAMLStats;
import hu.montlikadani.ragemode.toolbox.MergeSort;

public class RuntimeRPPManager {

  public static List<RetPlayerPoints> RuntimeRPPList;

  public static void getRPPListFromYAML() {
    RuntimeRPPList = YAMLStats.getAllPlayerStatistics();
    MergeSort ms = new MergeSort();
    ms.sort(RuntimeRPPList);
  }

  public static RetPlayerPoints getRPPForPlayer(String sUUID) {
    RetPlayerPoints rpp = null;
    if (RuntimeRPPList != null) {
      int i = 0;
      int imax = RuntimeRPPList.size();
      while (i < imax) {
        if (RuntimeRPPList.get(i).getPlayerUUID().equals(sUUID)) {
          rpp = RuntimeRPPList.get(i);
          rpp.setRank(i + 1);
          break;
        }
        i++;
      }
    }
    return rpp;
  }

  public synchronized static void updatePlayerEntry(PlayerPoints pp) {
    RetPlayerPoints oldRPP = getRPPForPlayer(pp.getPlayerUUID());
    if (oldRPP == null) {
      int i = RuntimeRPPList.size() - 1;
      while (RuntimeRPPList.get(i).getPoints() < pp.getPoints()) {
        i--;
      }
      RetPlayerPoints newRPP = new RetPlayerPoints(pp.getPlayerUUID());
      newRPP.setAxeDeaths(pp.getAxeDeaths());
      newRPP.setAxeKills(pp.getAxeKills());
      newRPP.setCurrentStreak(pp.getCurrentStreak());
      newRPP.setDeaths(pp.getDeaths());
      newRPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
      newRPP.setDirectArrowKills(pp.getDirectArrowKills());
      newRPP.setExplosionDeaths(pp.getExplosionDeaths());
      newRPP.setExplosionKills(pp.getExplosionKills());
      newRPP.setKills(pp.getKills());
      newRPP.setKnifeDeaths(pp.getKnifeDeaths());
      newRPP.setKnifeKills(pp.getKnifeKills());
      newRPP.setLongestStreak(pp.getLongestStreak());
      newRPP.setPoints(pp.getPoints());

      if (pp.isWinner()) {
        newRPP.setWins(1);
      } else {
        newRPP.setWins(0);
      }

      if (pp.getDeaths() != 0) {
        newRPP.setKD(((double) (pp.getKills())) / ((double) (pp.getDeaths())));
      } else {
        newRPP.setKD(1.0d);
      }

      newRPP.setGames(1);

      RuntimeRPPList.add(i + 1, newRPP);
    } else if (pp.getPoints() == oldRPP.getPoints()) {
      return;// TODO kills&deaths...
    } else if (pp.getPoints() > oldRPP.getPoints()) {
      boolean norankchange = false;
      int i = oldRPP.getRank() - 2;

      if (RuntimeRPPList.get(i).getPoints() < pp.getPoints()) {
        RuntimeRPPList.remove(i + 1);
        i--;
      } else {
        norankchange = true;
      }

      while (RuntimeRPPList.get(i).getPoints() < pp.getPoints()) {
        i--;
        if (i < 0) {
          i = 0;
        }
        break;
      }
      RetPlayerPoints newRPP = new RetPlayerPoints(pp.getPlayerUUID());

      newRPP.setAxeDeaths(pp.getAxeDeaths());
      newRPP.setAxeKills(pp.getAxeKills());
      newRPP.setCurrentStreak(pp.getCurrentStreak());
      newRPP.setDeaths(pp.getDeaths());
      newRPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
      newRPP.setDirectArrowKills(pp.getDirectArrowKills());
      newRPP.setExplosionDeaths(pp.getExplosionDeaths());
      newRPP.setExplosionKills(pp.getExplosionKills());
      newRPP.setKills(pp.getKills());
      newRPP.setKnifeDeaths(pp.getKnifeDeaths());
      newRPP.setKnifeKills(pp.getKnifeKills());
      newRPP.setLongestStreak(pp.getLongestStreak());
      newRPP.setPoints(pp.getPoints());

      if (pp.isWinner()) {
        newRPP.setWins(oldRPP.getWins() + 1);
      } else {
        newRPP.setWins(oldRPP.getWins());
      }

      if (oldRPP.getDeaths() + pp.getDeaths() != 0) {
        newRPP.setKD(((double) (pp.getKills() + oldRPP.getKills()))
            / ((double) (pp.getDeaths() + oldRPP.getDeaths())));
      } else {
        newRPP.setKD(1.0d);
      }

      newRPP.setGames(oldRPP.getGames() + 1);

      if (norankchange) {
        RuntimeRPPList.set(oldRPP.getRank() - 1, newRPP);
      } else {
        RuntimeRPPList.add(i + 1, newRPP);
      }
    } else {
      boolean norankchange = false;
      boolean last = false;
      int i = oldRPP.getRank();
      int imax = RuntimeRPPList.size();

      if (RuntimeRPPList.get(i).getPoints() > pp.getPoints()) {
        RuntimeRPPList.remove(i - 1);
      } else {
        norankchange = true;
      }

      while (RuntimeRPPList.get(i).getPoints() > pp.getPoints()) {
        i++;
        if (i >= imax) {
          i = imax;
          last = true;
          break;
        }
      }
      RetPlayerPoints newRPP = new RetPlayerPoints(pp.getPlayerUUID());

      newRPP.setAxeDeaths(pp.getAxeDeaths());
      newRPP.setAxeKills(pp.getAxeKills());
      newRPP.setCurrentStreak(pp.getCurrentStreak());
      newRPP.setDeaths(pp.getDeaths());
      newRPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
      newRPP.setDirectArrowKills(pp.getDirectArrowKills());
      newRPP.setExplosionDeaths(pp.getExplosionDeaths());
      newRPP.setExplosionKills(pp.getExplosionKills());
      newRPP.setKills(pp.getKills());
      newRPP.setKnifeDeaths(pp.getKnifeDeaths());
      newRPP.setKnifeKills(pp.getKnifeKills());
      newRPP.setLongestStreak(pp.getLongestStreak());
      newRPP.setPoints(pp.getPoints());

      if (pp.isWinner()) {
        newRPP.setWins(oldRPP.getWins() + 1);
      } else {
        newRPP.setWins(oldRPP.getWins());
      }

      if (oldRPP.getDeaths() + pp.getDeaths() != 0) {
        newRPP.setKD(((double) (pp.getKills() + oldRPP.getKills())) / ((double) (pp.getDeaths() + oldRPP.getDeaths())));
      } else {
        newRPP.setKD(1.0d);
      }

      newRPP.setGames(oldRPP.getGames() + 1);
      if (norankchange) {
        RuntimeRPPList.set(oldRPP.getRank() - 1, newRPP);
      } else if (last) {
        RuntimeRPPList.add(newRPP);
      } else
        RuntimeRPPList.add(i, newRPP);
    }
  }
}
