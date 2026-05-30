package world.bentobox.extramobs.config;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	 * Returns the raw per-gamemode settings map as loaded from {@code config.yml}.
	 * The map structure is:
	 * <pre>
	 * gamemodeName -&gt; {
	 *   "nether" -&gt; List&lt;Map&lt;String, Object&gt;&gt;,
	 *   "end"    -&gt; List&lt;Map&lt;String, Object&gt;&gt;,
	 *   "world"  -&gt; List&lt;Map&lt;String, Object&gt;&gt;
	 * }
	 * </pre>
	 * Use {@link #getReplacements(String, String)} for convenient typed access.
	 *
	 * @return mutable map; never {@code null}.
	 */
	public Map<String, Object> getGamemodeSettings()
	{
		return gamemodeSettings;
	}


	/**
	 * Sets the raw per-gamemode settings map.
	 *
	 * @param gamemodeSettings new value (may be {@code null}; stored as empty map).
	 */
	public void setGamemodeSettings(Map<String, Object> gamemodeSettings)
	{
		this.gamemodeSettings = gamemodeSettings != null ? gamemodeSettings : new LinkedHashMap<>();
	}


	/**
	 * Returns the list of {@link MobSpawnReplacement} rules configured for the
	 * given game mode and environment ({@code "world"}, {@code "nether"}, or
	 * {@code "end"}).
	 *
	 * <p>Returns an empty list when no per-gamemode overrides exist, allowing
	 * callers to fall back to global settings without extra null-checks.
	 *
	 * @param gameModeName name of the GameMode addon (e.g. {@code "BSkyBlock"}).
	 * @param environment  one of {@code "world"}, {@code "nether"}, {@code "end"}.
	 * @return immutable-safe list of replacement rules; never {@code null}.
	 */
	public List<MobSpawnReplacement> getReplacements(String gameModeName, String environment)
	{
		if (gamemodeSettings == null || gameModeName == null || environment == null)
		{
			return List.of();
		}

		Object rawGM = gamemodeSettings.get(gameModeName);

		if (!(rawGM instanceof Map<?, ?> gmMap))
		{
			return List.of();
		}

		Object rawEnv = gmMap.get(environment);

		if (!(rawEnv instanceof List<?> envList))
		{
			return List.of();
		}

		List<MobSpawnReplacement> result = new ArrayList<>();

		for (Object rawEntry : envList)
		{
			if (!(rawEntry instanceof Map<?, ?> entryMap))
			{
				continue;
			}

			Object oldVal = entryMap.get("old");
			Object newVal = entryMap.get("new");
			Object chanceVal = entryMap.get("chance");

			if (oldVal == null || newVal == null)
			{
				continue;
			}

			double chance = 0.0;

			if (chanceVal instanceof Number n)
			{
				chance = n.doubleValue();
			}

			result.add(new MobSpawnReplacement(
				oldVal.toString(),
				newVal.toString(),
				chance));
		}

		return result;
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

	@ConfigComment("")
	@ConfigComment("Per-gamemode settings that override the global defaults above.")
	@ConfigComment("Each key is the exact GameMode addon name (case-sensitive).")
	@ConfigComment("Each gamemode may define up to three environment sections:")
	@ConfigComment("  world:  - replacements for the overworld")
	@ConfigComment("  nether: - replacements for the nether")
	@ConfigComment("  end:    - replacements for the end")
	@ConfigComment("Each section is a list of replacement rules with the following fields:")
	@ConfigComment("  old:    EntityType name of the mob to replace (e.g. ZOMBIFIED_PIGLIN)")
	@ConfigComment("  new:    EntityType name of the replacement mob  (e.g. WITHER_SKELETON)")
	@ConfigComment("  chance: Probability in the range 0.0-1.0")
	@ConfigComment("Example:")
	@ConfigComment("  gamemode-settings:")
	@ConfigComment("    BSkyBlock:")
	@ConfigComment("      nether:")
	@ConfigComment("        - old: ZOMBIFIED_PIGLIN")
	@ConfigComment("          new: WITHER_SKELETON")
	@ConfigComment("          chance: 0.05")
	@ConfigComment("      end:")
	@ConfigComment("        - old: ENDERMAN")
	@ConfigComment("          new: SHULKER")
	@ConfigComment("          chance: 0.3")
	@ConfigEntry(path = "gamemode-settings")
	private Map<String, Object> gamemodeSettings = new LinkedHashMap<>();
}
