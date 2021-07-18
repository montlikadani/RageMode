package hu.montlikadani.ragemode.utils;

import org.bukkit.Material;

import com.google.common.collect.ImmutableSet;

public final class MaterialUtil {

	private static final ImmutableSet<Material> WALL_SIGNS = ImmutableSet.<Material>builder()
			.add(getMat("WALL_SIGN"), getMat("ACACIA_WALL_SIGN"), getMat("BIRCH_WALL_SIGN"),
					getMat("DARK_OAK_WALL_SIGN"), getMat("JUNGLE_WALL_SIGN"), getMat("OAK_WALL_SIGN"),
					getMat("SPRUCE_WALL_SIGN"), getMat("WARPED_WALL_SIGN"), getMat("CRIMSON_WALL_SIGN"))
			.build();

	private static final ImmutableSet<Material> PRESSURE_PLATES = ImmutableSet.<Material>builder()
			.add(getMat("WOODEN_PRESSURE_PLATE"), getMat("OAK_PRESSURE_PLATE"), getMat("ACACIA_PRESSURE_PLATE"),
					getMat("BIRCH_PRESSURE_PLATE"), getMat("DARK_OAK_PRESSURE_PLATE"), getMat("JUNGLE_PRESSURE_PLATE"),
					getMat("SPRUCE_PRESSURE_PLATE"), getMat("WARPED_PRESSURE_PLATE"),
					getMat("POLISHED_BLACKSTONE_PRESSURE_PLATE"), getMat("CRIMSON_PRESSURE_PLATE"),
					getMat("STONE_PRESSURE_PLATE"), getMat("HEAVY_WEIGHTED_PRESSURE_PLATE"),
					getMat("LIGHT_WEIGHTED_PRESSURE_PLATE"))
			.build();

	private static final ImmutableSet<Material> TRAPDOORS = ImmutableSet.<Material>builder()
			.add(getMat("TRAP_DOOR"), getMat("IRON_TRAPDOOR"), getMat("OAK_TRAPDOOR"), getMat("ACACIA_TRAPDOOR"),
					getMat("BIRCH_TRAPDOOR"), getMat("DARK_OAK_TRAPDOOR"), getMat("JUNGLE_TRAPDOOR"),
					getMat("SPRUCE_TRAPDOOR"), getMat("WARPED_TRAPDOOR"), getMat("CRIMSON_TRAPDOOR"))
			.build();

	private static final ImmutableSet<Material> BUTTONS = ImmutableSet.<Material>builder()
			.add(getMat("WOODEN_BUTTON"), getMat("STONE_BUTTON"), getMat("OAK_BUTTON"), getMat("ACACIA_BUTTON"),
					getMat("BIRCH_BUTTON"), getMat("DARK_OAK_BUTTON"), getMat("JUNGLE_BUTTON"), getMat("SPRUCE_BUTTON"),
					getMat("WARPED_BUTTON"), getMat("POLISHED_BLACKSTONE_BUTTON"), getMat("CRIMSON_BUTTON"))
			.build();

	private static final ImmutableSet<Material> DOORS = ImmutableSet.<Material>builder()
			.add(getMat("WOODEN_DOOR"), getMat("ACACIA_DOOR"), getMat("BIRCH_DOOR"), getMat("DARK_OAK_DOOR"),
					getMat("JUNGLE_DOOR"), getMat("SPRUCE_DOOR"), getMat("WOOD_DOOR"), getMat("OAK_DOOR"),
					getMat("WARPED_DOOR"), getMat("CRIMSON_DOOR"))
			.build();

	private static final ImmutableSet<Material> COMPARATORS = ImmutableSet.<Material>builder()
			.add(getMat("REDSTONE_COMPARATOR"), getMat("REDSTONE_COMPARATOR_ON"), getMat("REDSTONE_COMPARATOR_OFF"),
					getMat("COMPARATOR"))
			.build();

	private MaterialUtil() {
	}

	public static boolean isWallSign(Material mat) {
		return WALL_SIGNS.contains(mat);
	}

	public static boolean isPressurePlate(Material mat) {
		return PRESSURE_PLATES.contains(mat);
	}

	public static boolean isTrapdoor(Material mat) {
		return TRAPDOORS.contains(mat);
	}

	public static boolean isButton(Material mat) {
		return BUTTONS.contains(mat);
	}

	public static boolean isDoor(Material mat) {
		return DOORS.contains(mat);
	}

	public static boolean isComparator(Material mat) {
		return COMPARATORS.contains(mat);
	}

	private static Material getMat(String name) {
		Material mat = Material.getMaterial(name);
		return mat == null ? Material.AIR : mat;
	}
}
