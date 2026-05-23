package net.silvertide.pmmo_pufferfish_compat.bridge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record SkillMapping(
        String pmmoSkill,
        List<Award> awards
) {
    public static final Codec<SkillMapping> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("pmmo_skill").forGetter(SkillMapping::pmmoSkill),
            Award.CODEC.listOf().fieldOf("awards").forGetter(SkillMapping::awards)
    ).apply(instance, SkillMapping::new));

    public SkillMapping {
        if (pmmoSkill == null || pmmoSkill.isBlank()) {
            throw new IllegalArgumentException("pmmo_skill is required");
        }
        if (awards == null) {
            throw new IllegalArgumentException("awards is required");
        }
        awards = List.copyOf(awards);
    }
}
