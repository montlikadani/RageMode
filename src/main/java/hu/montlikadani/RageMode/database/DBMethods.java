package hu.montlikadani.ragemode.database;

public interface DBMethods {

	public boolean isTable(String name);

	public boolean createTable(String query);

	public boolean truncate(String table);

	public boolean isCollumn(String table, String collumn);

	public boolean drop(String table);
}
