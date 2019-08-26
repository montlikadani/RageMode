package hu.montlikadani.ragemode.utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

public class MaterialUtil {

	private static Set<Material> WALL_SIGNS = new HashSet<>();
	private static Set<Material> WOODEN_PRESSURE_PLATES = new HashSet<>();
	private static Set<Material> TRAPWOODEN_DOORS = new HashSet<>();
	private static Set<Material> BUTTONS = new HashSet<>();
	private static Set<Material> WOODEN_DOORS = new HashSet<>();

	static {
		WALL_SIGNS.clear();
		WOODEN_PRESSURE_PLATES.clear();
		TRAPWOODEN_DOORS.clear();
		BUTTONS.clear();
		WOODEN_DOORS.clear();

		WALL_SIGNS.add(Material.getMaterial("WALL_SIGN"));
		WOODEN_PRESSURE_PLATES.add(Material.getMaterial("WOODEN_PRESSURE_PLATE"));
		TRAPWOODEN_DOORS.add(Material.getMaterial("TRAP_DOOR"));
		BUTTONS.add(Material.getMaterial("WOODEN_BUTTON"));
		WOODEN_DOORS.add(Material.getMaterial("WOODEN_DOOR"));

		BUTTONS.add(Material.getMaterial("STONE_BUTTON"));
		TRAPWOODEN_DOORS.add(Material.getMaterial("IRON_TRAPDOOR"));
		WOODEN_DOORS.add(Material.getMaterial("ACACIA_DOOR"));
		WOODEN_DOORS.add(Material.getMaterial("BIRCH_DOOR"));
		WOODEN_DOORS.add(Material.getMaterial("DARK_OAK_DOOR"));
		WOODEN_DOORS.add(Material.getMaterial("JUNGLE_DOOR"));
		WOODEN_DOORS.add(Material.getMaterial("SPRUCE_DOOR"));

		WALL_SIGNS.add(Material.getMaterial("ACACIA_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("BIRCH_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("DARK_OAK_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("JUNGLE_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("OAK_WALL_SIGN"));
		WALL_SIGNS.add(Material.getMaterial("SPRUCE_WALL_SIGN"));
		WOODEN_PRESSURE_PLATES.add(Material.getMaterial("OAK_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(Material.getMaterial("ACACIA_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(Material.getMaterial("BIRCH_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(Material.getMaterial("DARK_OAK_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(Material.getMaterial("JUNGLE_PRESSURE_PLATE"));
		WOODEN_PRESSURE_PLATES.add(Material.getMaterial("SPRUCE_PRESSURE_PLATE"));
		TRAPWOODEN_DOORS.add(Material.getMaterial("OAK_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(Material.getMaterial("ACACIA_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(Material.getMaterial("BIRCH_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(Material.getMaterial("DARK_OAK_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(Material.getMaterial("JUNGLE_TRAPDOOR"));
		TRAPWOODEN_DOORS.add(Material.getMaterial("SPRUCE_TRAPDOOR"));
		BUTTONS.add(Material.getMaterial("OAK_BUTTON"));
		BUTTONS.add(Material.getMaterial("ACACIA_BUTTON"));
		BUTTONS.add(Material.getMaterial("BIRCH_BUTTON"));
		BUTTONS.add(Material.getMaterial("DARK_OAK_BUTTON"));
		BUTTONS.add(Material.getMaterial("JUNGLE_BUTTON"));
		BUTTONS.add(Material.getMaterial("SPRUCE_BUTTON"));
		WOODEN_DOORS.add(Material.getMaterial("OAK_DOOR"));
	}

	public static boolean isWallSign(Material mat) {
		return WALL_SIGNS.contains(mat);
	}

	public static boolean isWoodenPressurePlate(Material mat) {
		return WOODEN_PRESSURE_PLATES.contains(mat);
	}

	public static boolean isTrapdoor(Material mat) {
		return TRAPWOODEN_DOORS.contains(mat);
	}

	public static boolean isButton(Material mat) {
		return BUTTONS.contains(mat);
	}

	public static boolean isWoodenDoor(Material mat) {
		return WOODEN_DOORS.contains(mat);
	}
}
