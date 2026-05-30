package world.bentobox.extramobs.config;


import org.bukkit.entity.EntityType;


/**
 * Represents a single mob-spawn replacement rule used by the per-gamemode
 * configuration.  The rule matches a naturally-spawning entity ({@link #getOld()})
 * and, with a configured probability ({@link #getChance()}), replaces it with a
 * different entity ({@link #getNew()}).
 *
 * <p>Mob names are stored as upper-case {@link EntityType} name strings so that
 * the YAML serialisation layer (which only handles plain {@code Map} / {@code List}
 * / scalar values for nested sections) does not need to know anything about Bukkit
 * enum types.
 */
public class MobSpawnReplacement
{
    // ---------------------------------------------------------------------
    // Section: Constructors
    // ---------------------------------------------------------------------


    /**
     * No-arg constructor required by the YAML serialiser.
     */
    public MobSpawnReplacement()
    {
    }


    /**
     * Convenience constructor.
     *
     * @param old    EntityType name to match (case-insensitive).
     * @param newMob EntityType name to spawn as a replacement (case-insensitive).
     * @param chance Probability in the range [0.0, 1.0].
     */
    public MobSpawnReplacement(String old, String newMob, double chance)
    {
        this.old = old;
        this.newMob = newMob;
        this.chance = chance;
    }


    // ---------------------------------------------------------------------
    // Section: Helpers
    // ---------------------------------------------------------------------


    /**
     * Resolves the {@link EntityType} for the mob that this rule replaces.
     *
     * @return the resolved {@link EntityType}, or {@code null} if the name is
     *         invalid / unknown.
     */
    public EntityType resolveOldEntityType()
    {
        if (old == null || old.isBlank())
        {
            return null;
        }
        try
        {
            return EntityType.valueOf(old.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }


    /**
     * Resolves the {@link EntityType} that should be spawned as the replacement.
     *
     * @return the resolved {@link EntityType}, or {@code null} if the name is
     *         invalid / unknown.
     */
    public EntityType resolveNewEntityType()
    {
        if (newMob == null || newMob.isBlank())
        {
            return null;
        }
        try
        {
            return EntityType.valueOf(newMob.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }


    // ---------------------------------------------------------------------
    // Section: Getters and Setters
    // ---------------------------------------------------------------------


    /**
     * Returns the name of the entity type that this rule matches.
     *
     * @return entity type name (upper-case).
     */
    public String getOld()
    {
        return old;
    }


    /**
     * Sets the name of the entity type that this rule matches.
     *
     * @param old entity type name (case-insensitive).
     */
    public void setOld(String old)
    {
        this.old = old;
    }


    /**
     * Returns the name of the entity type that will be spawned as a replacement.
     *
     * @return entity type name (upper-case).
     */
    public String getNew()
    {
        return newMob;
    }


    /**
     * Sets the name of the entity type to spawn as a replacement.
     *
     * @param newMob entity type name (case-insensitive).
     */
    public void setNew(String newMob)
    {
        this.newMob = newMob;
    }


    /**
     * Returns the spawn-replacement probability in the range [0.0, 1.0].
     *
     * @return chance value.
     */
    public double getChance()
    {
        return chance;
    }


    /**
     * Sets the spawn-replacement probability.
     *
     * @param chance value in the range [0.0, 1.0].
     */
    public void setChance(double chance)
    {
        this.chance = chance;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Name of the entity type that this rule will replace.
     * Field is named {@code old} to mirror the YAML key {@code old:}.
     */
    private String old;

    /**
     * Name of the replacement entity type.
     * The field is named {@code newMob} because {@code new} is a Java keyword;
     * the YAML key is {@code new:}.
     */
    private String newMob;

    /**
     * Probability that the replacement takes place (0.0 = never, 1.0 = always).
     * The YAML key is {@code chance:}.
     */
    private double chance;
}
