package net.silvertide.pmmo_pufferfish_compat.bridge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Function;

public record Award(
        ResourceLocation skillCategory,
        int skillPoints,
        int everyXPmmoLevels,
        int startingPmmoLevel,
        int maxSkillPoints,
        Reconcile reconcile
) {
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    public static final Codec<Award> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("skill_category").forGetter(Award::skillCategory),
            strictIntField("skill_points", 0, Integer.MAX_VALUE, 1).forGetter(Award::skillPoints),
            strictIntField("every_x_pmmo_levels", 1, Integer.MAX_VALUE, 1).forGetter(Award::everyXPmmoLevels),
            strictIntField("starting_pmmo_level", 1, Integer.MAX_VALUE, 1).forGetter(Award::startingPmmoLevel),
            strictIntField("max_skill_points", 0, Integer.MAX_VALUE, UNBOUNDED).forGetter(Award::maxSkillPoints),
            Reconcile.CODEC.optionalFieldOf("reconcile", Reconcile.ADDITIVE).forGetter(Award::reconcile)
    ).apply(instance, Award::new));

    private static MapCodec<Integer> strictIntField(String name, int min, int max, int defaultValue) {
        Function<Integer, DataResult<Integer>> rangeCheck = value -> (value < min || value > max)
                ? DataResult.error(() -> name + " must be in [" + min + ", " + max + "], got " + value)
                : DataResult.success(value);
        return Codec.INT.optionalFieldOf(name).flatXmap(
                optional -> optional.isEmpty() ? DataResult.success(defaultValue) : rangeCheck.apply(optional.get()),
                value -> DataResult.success(Optional.of(value))
        );
    }

    public Award {
        if (skillCategory == null) throw new IllegalArgumentException("skill_category is required");
        if (skillPoints < 0) throw new IllegalArgumentException("skill_points must be >= 0, got " + skillPoints);
        if (everyXPmmoLevels < 1) throw new IllegalArgumentException("every_x_pmmo_levels must be >= 1, got " + everyXPmmoLevels);
        if (startingPmmoLevel < 1) throw new IllegalArgumentException("starting_pmmo_level must be >= 1, got " + startingPmmoLevel);
        if (maxSkillPoints < 0) throw new IllegalArgumentException("max_skill_points must be >= 0, got " + maxSkillPoints);
        if (reconcile == null) throw new IllegalArgumentException("reconcile is required");
    }

    public int expectedFor(int pmmoLevel) {
        if (pmmoLevel < startingPmmoLevel) return 0;
        int milestones = (pmmoLevel - startingPmmoLevel) / everyXPmmoLevels + 1;
        long raw = (long) milestones * skillPoints;
        return raw > maxSkillPoints ? maxSkillPoints : (int) raw;
    }
}
