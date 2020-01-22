package world.bentobox.extramobs;


import org.bukkit.Bukkit;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.extramobs.config.Settings;
import world.bentobox.extramobs.listeners.MobsSpawnListener;


/**
 * Main addon Class. It starts all processes so addon could properly work.
 */
public class ExtraMobsAddon extends Addon
{
	/**
	 * Executes code when loading the addon. This is called before {@link #onEnable()}. This <b>must</b> be
	 * used to setup configuration, worlds and commands.
	 */
	@Override
	public void onLoad()
	{
		super.onLoad();

		this.saveDefaultConfig();

		this.settings = new Config<>(this, Settings.class).loadConfigObject();

		if (this.settings == null)
		{
			// If we failed to load Settings then we should not enable addon.
			// We can log error and set state to DISABLED.

			this.logError("ExtraMobs settings could not load! Addon disabled.");
			this.setState(State.DISABLED);
		}
	}


	/**
	 * Executes code when enabling the addon. This is called after {@link #onLoad()}. <br/> Note that commands
	 * and worlds registration <b>must</b> be done in {@link #onLoad()}, if need be. Failure to do so
	 * <b>will</b> result in issues such as tab-completion not working for commands.
	 */
	@Override
	public void onEnable()
	{
		// Check if it is enabled - it might be loaded, but not enabled.
		if (this.getPlugin() == null || !this.getPlugin().isEnabled())
		{
			Bukkit.getLogger().severe("BentoBox is not available or disabled!");
			this.setState(State.DISABLED);
			return;
		}

		// Check if addon is not disabled before.
		if (this.getState().equals(State.DISABLED))
		{
			Bukkit.getLogger().severe("ExtraMobs Addon is not available or disabled!");
			return;
		}

		// Register game mode in world flag.
		this.getPlugin().getAddonsManager().getGameModeAddons().forEach(
			gameModeAddon -> {
				if (!this.settings.getDisabledGameModes().contains(gameModeAddon.getDescription().getName()))
				{
					this.hooked = true;
				}
			});

		if (this.hooked)
		{
			this.registerListener(new MobsSpawnListener(this));
		}
		else
		{
			this.logError("ExtraMobs could not hook into any GameMode so will not do anything!");
			this.setState(State.DISABLED);
		}
	}


	/**
	 * Executes code when disabling the addon.
	 */
	@Override
	public void onDisable()
	{
		// Do some staff...
	}


	/**
	 * Executes code when reloading the addon.
	 */
	@Override
	public void onReload()
	{
		super.onReload();

		this.settings = new Config<>(this, Settings.class).loadConfigObject();

		if (this.settings == null)
		{
			// If we failed to load Settings then we should not enable addon.
			// We can log error and set state to DISABLED.

			this.logError("ExtraMobs settings could not load! Addon disabled.");
			this.setState(State.DISABLED);
		}
	}


	// ---------------------------------------------------------------------
	// Section: Getters
	// ---------------------------------------------------------------------


	/**
	 * This method returns the hooked value.
	 * @return the value of hooked.
	 */
	public boolean isHooked()
	{
		return hooked;
	}


	/**
	 * This method returns the settings value.
	 * @return the value of settings.
	 */
	public Settings getSettings()
	{
		return settings;
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	/**
	 * Variable indicates if addon is hooked in any game mode
	 */
	private boolean hooked;

	/**
	 * Settings for addon
	 */
	private Settings settings;
}
