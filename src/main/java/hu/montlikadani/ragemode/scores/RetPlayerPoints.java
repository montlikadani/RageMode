package hu.montlikadani.ragemode.scores;

public class RetPlayerPoints extends PlayerPoints {

  private int wins = 0;
  private int games = 0;
  private double kd = 0d;
  private int rank = 0;

  public RetPlayerPoints(String playerUUID) {
    super(playerUUID);
  }

  public int getWins() {
    return wins;
  }

  public void setWins(int wins) {
    this.wins = wins;
  }

  public int getGames() {
    return games;
  }

  public void setGames(int games) {
    this.games = games;
  }

  public double getKD() {
    return kd;
  }

  public void setKD(double kd) {
    this.kd = kd;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }
}
