package world.bentobox.extramobs.config;


import java.util.HashSet;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;


/**
 * Settings that implements ConfigObject is powerful and dynamic Config Objects that
 * does not need custom parsing. If it is correctly loaded, all its values will be available.
 *
 * Without Getter and Setter this class will not work.
 *
 * To specify location for config object to be stored, you should use @StoreAt(filename="{config file name}", path="{Path to your addon}")
 * To save comments in config file you should use @ConfigComment("{message}") that adds any message you want to be in file.
 */
@StoreAt(filename="config.yml", path="addons/ExtraMobs")
@ConfigComment("ExtraMobs Configuration [version]")
@ConfigComment("This config file is dynamic and saved when the server is shutdown.")
@ConfigComment("")
public class Settings implements ConfigObject
{
	// ---------------------------------------------------------------------
	// Section: Getters and Setters
	// ---------------------------------------------------------------------


	/**
	 * This method returns the disabledGameModes value.
	 *
	 * @return the value of disabledGameModes.
	 */
	public Set<String> getDisabledGameModes()
	{
		return disabledGameModes;
	}


	/**
	 * This method sets the disabledGameModes value.
	 *
	 * @param disabledGameModes the disabledGameModes new value.
	 */
	public void setDisabledGameModes(Set<String> disabledGameModes)
	{
		this.disabledGameModes = disabledGameModes;
	}


	/**
	 * This method returns the witherSkeletonChance value.
	 * @return the value of witherSkeletonChance.
	 */
	public double getWitherSkeletonChance()
	{
		return witherSkeletonChance;
	}


	/**
	 * This method sets the witherSkeletonChance value.
	 * @param witherSkeletonChance the witherSkeletonChance new value.
	 *
	 */
	public void setWitherSkeletonChance(double witherSkeletonChance)
	{
		this.witherSkeletonChance = witherSkeletonChance;
	}


	/**
	 * This method returns the blazeChance value.
	 * @return the value of blazeChance.
	 */
	public double getBlazeChance()
	{
		return blazeChance;
	}


	/**
	 * This method sets the blazeChance value.
	 * @param blazeChance the blazeChance new value.
	 *
	 */
	public void setBlazeChance(double blazeChance)
	{
		this.blazeChance = blazeChance;
	}


	/**
	 * This method returns the shulkerChance value.
	 * @return the value of shulkerChance.
	 */
	public double getShulkerChance()
	{
		return shulkerChance;
	}


	/**
	 * This method sets the shulkerChance value.
	 * @param shulkerChance the shulkerChance new value.
	 *
	 */
	public void setShulkerChance(double shulkerChance)
	{
		this.shulkerChance = shulkerChance;
	}


	/**
	 * This method returns the beeHiveChance value.
	 * @return the value of beeHiveChance.
	 */
	public double getBeeHiveChance()
	{
		return beeHiveChance;
	}


	/**
	 * This method sets the beeHiveChance value.
	 * @param beeHiveChance the beeHiveChance new value.
	 *
	 */
	public void setBeeHiveChance(double beeHiveChance)
	{
		this.beeHiveChance = beeHiveChance;
	}


	/**
	 * This method returns the guardianChance value.
	 * @return the value of guardianChance.
	 */
	public double getGuardianChance()
	{
		return guardianChance;
	}


	/**
	 * This method sets the guardianChance value.
	 * @param guardianChance the guardianChance new value.
	 *
	 */
	public void setGuardianChance(double guardianChance)
	{
		this.guardianChance = guardianChance;
	}


	/**
	 * This method returns the beeChance value.
	 * @return the value of beeChance.
	 */
	public double getBeeChance()
	{
		return beeChance;
	}


	/**
	 * This method sets the beeChance value.
	 * @param beeChance the beeChance new value.
	 *
	 */
	public void setBeeChance(double beeChance)
	{
		this.beeChance = beeChance;
	}

// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	@ConfigComment("")
	@ConfigComment("This list stores GameModes in which Likes addon should not work.")
	@ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
	@ConfigComment("disabled-gamemodes:")
	@ConfigComment(" - BSkyBlock")
	@ConfigEntry(path = "disabled-gamemodes")
	private Set<String> disabledGameModes = new HashSet<>();

	@ConfigComment("Chance to spawn Wither Skeleton instead of Zombie Pigmen.")
	@ConfigEntry(path = "nether-chances.wither-skeleton")
	private double witherSkeletonChance;

	@ConfigComment("Chance to spawn Blaze instead of Zombie Pigmen.")
	@ConfigEntry(path = "nether-chances.blaze")
	private double blazeChance;

	@ConfigComment("Chance to spawn Shulker instead of Enderman.")
	@ConfigEntry(path = "end-chances.shulker")
	private double shulkerChance;

	@ConfigComment("Chance to spawn Guardian instead of a fish.")
	@ConfigEntry(path = "overworld-chance.guardian")
	private double guardianChance;

	@ConfigComment("Chance to spawn Bee hive on growing Birch or Oak tree.")
	@ConfigEntry(path = "overworld-chance.bee-hive")
	private double beeHiveChance;

	@ConfigComment("Chance to spawn Bee in Beehive. Spawning will try to happen 3 times.")
	@ConfigEntry(path = "overworld-chance.bee")
	private double beeChance;
}
