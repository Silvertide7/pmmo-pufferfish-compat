package net.silvertide.pmmo_pufferfish_compat.bridge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SkillMap {
    public static final SkillMap EMPTY = new SkillMap(List.of());

    private static final Award[] NO_AWARDS = new Award[0];

    private final Map<String, Award[]> awardsByPmmoSkill;

    public SkillMap(Collection<SkillMapping> mappings) {
        Map<String, List<Award>> accumulator = new HashMap<>();
        for (SkillMapping mapping : mappings) {
            if (mapping.awards().isEmpty()) continue;
            accumulator.computeIfAbsent(mapping.pmmoSkill(), key -> new ArrayList<>())
                    .addAll(mapping.awards());
        }
        Map<String, Award[]> finalized = new HashMap<>(accumulator.size());
        for (Map.Entry<String, List<Award>> entry : accumulator.entrySet()) {
            finalized.put(entry.getKey(), entry.getValue().toArray(new Award[0]));
        }
        this.awardsByPmmoSkill = Map.copyOf(finalized);
    }

    public Award[] awardsFor(String pmmoSkill) {
        if (pmmoSkill == null) return NO_AWARDS;
        Award[] result = awardsByPmmoSkill.get(pmmoSkill);
        return result == null ? NO_AWARDS : result;
    }

    public Set<String> trackedPmmoSkills() {
        return awardsByPmmoSkill.keySet();
    }

    public int totalAwards() {
        int total = 0;
        for (Award[] awards : awardsByPmmoSkill.values()) {
            total += awards.length;
        }
        return total;
    }

    public boolean isEmpty() {
        return awardsByPmmoSkill.isEmpty();
    }
}
