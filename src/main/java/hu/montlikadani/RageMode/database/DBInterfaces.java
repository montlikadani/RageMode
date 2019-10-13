package hu.montlikadani.ragemode.database;

public interface DBInterfaces {

	public boolean hasConnection();

	public boolean isTable(String name);

	public boolean createTable(String query);

	public boolean truncate(String table);

	public boolean isCollumn(String table, String collumn);

	public boolean drop(String table);
}
