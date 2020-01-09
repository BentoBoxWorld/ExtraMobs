package world.bentobox.extramobs.listeners;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.extramobs.ExtraMobsAddon;


/**
 * This listener checks each TreeGrowth listener and spawns bee hive on it, if all
 * requirements are met.
 */
public class TreesGrowthListener implements Listener
{
	/**
	 * Constructor TreesGrowthListener creates a new TreesGrowthListener instance.
	 *
	 * @param addon of type ExtraMobsAddon
	 */
	public TreesGrowthListener(ExtraMobsAddon addon)
	{
		this.addon = addon;
		this.beeHiveRandom = new Random();
	}


	/**
	 * On tree growth check if player can receive a new bee hive
	 * @param event StructureGrowEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTreeGrowth(StructureGrowEvent event)
	{
		Optional<GameModeAddon> optionalAddon =
			this.addon.getPlugin().getIWM().getAddon(event.getWorld());

		if (!optionalAddon.isPresent() ||
			this.addon.getSettings().getDisabledGameModes().contains(
				optionalAddon.get().getDescription().getName()))
		{
			// GameMode addon is not in enable list.
			return;
		}

		if (!event.getWorld().getEnvironment().equals(World.Environment.NORMAL))
		{
			// Bees can spawn only in overworld.
			return;
		}

		if (event.getSpecies() != TreeType.TREE &&
			event.getSpecies() != TreeType.BIG_TREE &&
			event.getSpecies() != TreeType.BIRCH &&
			event.getSpecies() != TreeType.TALL_BIRCH)
		{
			// Bee Hives can generate only on oak and birch.
			return;
		}

		Biome biome = event.getWorld().getBiome(
			event.getLocation().getBlockX(),
			event.getLocation().getBlockY(),
			event.getLocation().getBlockZ());

		if (biome != Biome.PLAINS &&
			biome != Biome.SUNFLOWER_PLAINS &&
			biome != Biome.FLOWER_FOREST)
		{
			// Bee Hives can generate only in plains, sunflower plains or flower forest.
			return;
		}

		if (this.addon.getSettings().getBeeHiveChance() < this.beeHiveRandom.nextDouble())
		{
			// Unlucky. BeeHive is not generated.
			return;
		}

		// Place BeeHive in block list.

		// Choose random int for face.
		BlockFace face = blockFaceArray[this.beeHiveRandom.nextInt(4)];

		// create set of locations that will be filled with current tree.
		Set<Location> blockLocations = event.getBlocks().stream().
			map(BlockState::getLocation).
			collect(Collectors.toSet());

		List<Block> emptyTrucks = new ArrayList<>();

		// Collect all places where beehive can be placed.
		event.getBlocks().forEach(blockState -> {
			if (blockState.getBlockData().getMaterial() == Material.OAK_LOG ||
				blockState.getBlockData().getMaterial() == Material.BIRCH_LOG)
			{
				Block block = blockState.getBlock().getRelative(face);
				if (block.isEmpty() &&
					!blockLocations.contains(block.getLocation()))
				{
					emptyTrucks.add(block);
				}
			}
		});

		if (!emptyTrucks.isEmpty())
		{
			if (emptyTrucks.size() > 1)
			{
				// remove first truck to avoid putting it on the ground :)
				emptyTrucks.remove(0);
			}

			// Get random block from empty trucks.
			Block block = emptyTrucks.get(this.beeHiveRandom.nextInt(emptyTrucks.size()));

			Beehive beehive = (Beehive) Material.BEE_NEST.createBlockData();
			beehive.setFacing(face);

			// Add Beehive
			block.setBlockData(beehive);

			Block beeLocation = block.getRelative(face);

			if (!beeLocation.isEmpty())
			{
				if (beeLocation.getRelative(BlockFace.WEST).isEmpty())
				{
					beeLocation = beeLocation.getRelative(BlockFace.WEST);
				}
				else if (beeLocation.getRelative(BlockFace.EAST).isEmpty())
				{
					beeLocation = beeLocation.getRelative(BlockFace.EAST);
				}
				else if (beeLocation.getRelative(BlockFace.SOUTH).isEmpty())
				{
					beeLocation = beeLocation.getRelative(BlockFace.SOUTH);
				}
				else if (beeLocation.getRelative(BlockFace.NORTH).isEmpty())
				{
					beeLocation = beeLocation.getRelative(BlockFace.NORTH);
				}
				else
				{
					// No bees for you.
					return;
				}
			}

			// Try to spawn bee 3 times
			for (int i = 0; i < 3; i++)
			{
				if (this.addon.getSettings().getBeeChance() >= this.beeHiveRandom.nextDouble())
				{
					// Spawn Bee :)
					event.getWorld().spawnEntity(beeLocation.getLocation(), EntityType.BEE);
				}
			}
		}
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Instance of ExtraMobs Addon.
	 */
	private ExtraMobsAddon addon;

	/**
	 * Single random instance.
	 */
	private Random beeHiveRandom;

	/**
	 * This array contains block face how beehive will be placed.
	 */
	private static final BlockFace[] blockFaceArray;

	/**
	 * Populate Block Face array.
	 */
	static
	{
		blockFaceArray = new BlockFace[4];
		blockFaceArray[0] = BlockFace.EAST;
		blockFaceArray[1] = BlockFace.SOUTH;
		blockFaceArray[2] = BlockFace.WEST;
		blockFaceArray[3] = BlockFace.NORTH;
	}
}
