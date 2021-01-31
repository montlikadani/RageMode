package hu.montlikadani.ragemode.items.shop;

public class BoughtElements {

	private Object bought;

	private double cost;
	private int points;

	public BoughtElements(double cost, int points) {
		setCost(cost);
		setPoints(points);
	}

	public BoughtElements(Object bought, double cost, int points) {
		this(cost, points);

		setBought(bought);
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBought() {
		return (T) bought;
	}

	public void setBought(Object bought) {
		this.bought = bought;
	}
}
