package net.silvertide.pmmo_pufferfish_compat.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SkillMapReloadListenerTest {

    private static final String GOOD_MINING = """
            { "pmmo_skill": "mining", "awards": [ { "skill_category": "pack:mining_tree" } ] }
            """;

    private static final String GOOD_COMBAT = """
            { "pmmo_skill": "combat", "awards": [ { "skill_category": "pack:combat_tree", "skill_points": 2 } ] }
            """;

    private static final String BAD_MISSING_FIELDS = """
            { "pmmo_skill": "broken" }
            """;

    private static final String BAD_NUMERIC_RANGE = """
            { "pmmo_skill": "thrice_broken", "awards": [ { "skill_category": "p:t", "every_x_pmmo_levels": 0 } ] }
            """;

    @Test
    void emptyInputProducesEmptyMap() {
        SkillMap result = SkillMapReloadListener.parseEntries(Map.of(), (source, error) -> fail("no errors expected"));
        assertTrue(result.isEmpty());
    }

    @Test
    void allValidEntriesAreParsed() {
        Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
        entries.put(rl("pack", "mining"), json(GOOD_MINING));
        entries.put(rl("pack", "combat"), json(GOOD_COMBAT));

        List<String> errors = new ArrayList<>();
        SkillMap result = SkillMapReloadListener.parseEntries(entries, (source, error) -> errors.add(error));

        assertEquals(0, errors.size(), "Expected no errors, got: " + errors);
        assertEquals(2, result.trackedPmmoSkills().size());
        assertEquals(2, result.totalAwards());
        assertEquals(1, result.awardsFor("mining").length);
        assertEquals(2, result.awardsFor("combat")[0].skillPoints());
    }

    @Test
    void invalidEntryIsSkippedAndReported() {
        Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
        entries.put(rl("pack", "good"), json(GOOD_MINING));
        entries.put(rl("pack", "bad"), json(BAD_MISSING_FIELDS));

        List<ResourceLocation> failures = new ArrayList<>();
        SkillMap result = SkillMapReloadListener.parseEntries(entries, (source, error) -> failures.add(source));

        assertEquals(1, failures.size());
        assertEquals(rl("pack", "bad"), failures.get(0));
        assertEquals(1, result.trackedPmmoSkills().size());
        assertEquals(1, result.totalAwards());
        assertTrue(result.trackedPmmoSkills().contains("mining"));
    }

    @Test
    void awardLevelValidationFailureSkipsOnlyTheBadFile() {
        Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
        entries.put(rl("pack", "good"), json(GOOD_MINING));
        entries.put(rl("pack", "bad_award"), json(BAD_NUMERIC_RANGE));

        List<ResourceLocation> failures = new ArrayList<>();
        SkillMap result = SkillMapReloadListener.parseEntries(entries, (source, error) -> failures.add(source));

        assertEquals(1, failures.size());
        assertEquals(rl("pack", "bad_award"), failures.get(0));
        assertEquals(1, result.totalAwards());
    }

    @Test
    void allInvalidProducesEmptyMapAndReportsAll() {
        Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
        entries.put(rl("pack", "bad1"), json(BAD_MISSING_FIELDS));
        entries.put(rl("pack", "bad2"), json(BAD_NUMERIC_RANGE));

        List<ResourceLocation> failures = new ArrayList<>();
        SkillMap result = SkillMapReloadListener.parseEntries(entries, (source, error) -> failures.add(source));

        assertEquals(2, failures.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void multipleEntriesForSameSkillMerge() {
        String secondMining = """
                { "pmmo_skill": "mining", "awards": [ { "skill_category": "pack:gathering_tree" } ] }
                """;
        Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
        entries.put(rl("pack_a", "mining"), json(GOOD_MINING));
        entries.put(rl("pack_b", "mining"), json(secondMining));

        SkillMap result = SkillMapReloadListener.parseEntries(entries, (source, error) -> fail("no errors expected"));

        assertEquals(1, result.trackedPmmoSkills().size());
        assertEquals(2, result.awardsFor("mining").length);
    }

    private static ResourceLocation rl(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static JsonElement json(String text) {
        return JsonParser.parseString(text);
    }
}
