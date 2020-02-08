package hu.montlikadani.ragemode.database;

public interface DBMethods {

	boolean isTable(String name);

	boolean createTable(String query);

	boolean truncate(String table);

	boolean isCollumn(String table, String collumn);

	boolean drop(String table);
}
