package net.silvertide.pmmo_pufferfish_compat.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AwardCodecTest {

    private static Award parseAward(String json) {
        JsonElement element = JsonParser.parseString(json);
        return Award.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
    }

    private static DataResult<Award> tryParseAward(String json) {
        JsonElement element = JsonParser.parseString(json);
        return Award.CODEC.parse(JsonOps.INSTANCE, element);
    }

    private static SkillMapping parseMapping(String json) {
        JsonElement element = JsonParser.parseString(json);
        return SkillMapping.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
    }

    @Test
    void minimalAwardFillsInAllDefaults() {
        Award a = parseAward("""
                { "skill_category": "test:tree" }
                """);
        assertEquals(ResourceLocation.fromNamespaceAndPath("test", "tree"), a.skillCategory());
        assertEquals(1, a.skillPoints());
        assertEquals(1, a.everyXPmmoLevels());
        assertEquals(1, a.startingPmmoLevel());
        assertEquals(Award.UNBOUNDED, a.maxSkillPoints());
        assertEquals(Reconcile.ADDITIVE, a.reconcile());
    }

    @Test
    void fullAwardParsesEveryField() {
        Award a = parseAward("""
                {
                    "skill_category": "my_pack:mining_tree",
                    "skill_points": 2,
                    "every_x_pmmo_levels": 5,
                    "starting_pmmo_level": 10,
                    "max_skill_points": 30,
                    "reconcile": "strict"
                }
                """);
        assertEquals(ResourceLocation.fromNamespaceAndPath("my_pack", "mining_tree"), a.skillCategory());
        assertEquals(2, a.skillPoints());
        assertEquals(5, a.everyXPmmoLevels());
        assertEquals(10, a.startingPmmoLevel());
        assertEquals(30, a.maxSkillPoints());
        assertEquals(Reconcile.STRICT, a.reconcile());
    }

    @Test
    void parsesEachReconcileMode() {
        assertEquals(Reconcile.NONE, parseAward("""
                { "skill_category": "t:t", "reconcile": "none" }
                """).reconcile());
        assertEquals(Reconcile.ADDITIVE, parseAward("""
                { "skill_category": "t:t", "reconcile": "additive" }
                """).reconcile());
        assertEquals(Reconcile.TRIM_UNSPENT, parseAward("""
                { "skill_category": "t:t", "reconcile": "trim_unspent" }
                """).reconcile());
        assertEquals(Reconcile.STRICT, parseAward("""
                { "skill_category": "t:t", "reconcile": "strict" }
                """).reconcile());
    }

    @Test
    void rejectsMissingSkillCategory() {
        DataResult<Award> result = tryParseAward("{}");
        assertTrue(result.isError(), "Expected parse error, got: " + result.result());
    }

    @Test
    void rejectsZeroEveryXPmmoLevels() {
        DataResult<Award> result = tryParseAward("""
                { "skill_category": "test:t", "every_x_pmmo_levels": 0 }
                """);
        assertTrue(result.isError(), "Expected parse error for every_x_pmmo_levels=0");
    }

    @Test
    void rejectsNegativeSkillPoints() {
        DataResult<Award> result = tryParseAward("""
                { "skill_category": "test:t", "skill_points": -1 }
                """);
        assertTrue(result.isError(), "Expected parse error for skill_points=-1");
    }

    @Test
    void rejectsZeroStartingPmmoLevel() {
        DataResult<Award> result = tryParseAward("""
                { "skill_category": "test:t", "starting_pmmo_level": 0 }
                """);
        assertTrue(result.isError(), "Expected parse error for starting_pmmo_level=0");
    }

    @Test
    void rejectsUnknownReconcileMode() {
        DataResult<Award> result = tryParseAward("""
                { "skill_category": "test:t", "reconcile": "banana" }
                """);
        assertTrue(result.isError(), "Expected parse error for unknown reconcile mode");
    }

    @Test
    void mappingWithMultipleAwards() {
        SkillMapping mapping = parseMapping("""
                {
                    "pmmo_skill": "mining",
                    "awards": [
                        { "skill_category": "pack:mining_tree" },
                        { "skill_category": "pack:gathering_tree", "skill_points": 2 }
                    ]
                }
                """);
        assertEquals("mining", mapping.pmmoSkill());
        assertEquals(2, mapping.awards().size());
        assertEquals(1, mapping.awards().get(0).skillPoints());
        assertEquals(2, mapping.awards().get(1).skillPoints());
    }

    @Test
    void mappingRejectsMissingPmmoSkill() {
        JsonElement element = JsonParser.parseString("""
                { "awards": [ { "skill_category": "p:t" } ] }
                """);
        DataResult<SkillMapping> result = SkillMapping.CODEC.parse(JsonOps.INSTANCE, element);
        assertTrue(result.isError(), "Expected parse error for missing pmmo_skill");
    }

    @Test
    void mappingRejectsMissingAwards() {
        JsonElement element = JsonParser.parseString("""
                { "pmmo_skill": "mining" }
                """);
        DataResult<SkillMapping> result = SkillMapping.CODEC.parse(JsonOps.INSTANCE, element);
        assertTrue(result.isError(), "Expected parse error for missing awards");
    }

    @Test
    void awardCodecRoundTripPreservesAllFields() {
        Award original = new Award(
                ResourceLocation.fromNamespaceAndPath("pack", "mining_tree"),
                3, 5, 10, 50, Reconcile.STRICT);
        JsonElement encoded = Award.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        Award decoded = Award.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }

    @Test
    void awardCodecRoundTripPreservesDefaults() {
        Award original = new Award(
                ResourceLocation.fromNamespaceAndPath("t", "t"),
                1, 1, 1, Award.UNBOUNDED, Reconcile.ADDITIVE);
        JsonElement encoded = Award.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        Award decoded = Award.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }

    @Test
    void skillMappingCodecRoundTrip() {
        SkillMapping original = new SkillMapping("mining", java.util.List.of(
                new Award(ResourceLocation.fromNamespaceAndPath("p", "a"),
                        1, 1, 1, Award.UNBOUNDED, Reconcile.ADDITIVE),
                new Award(ResourceLocation.fromNamespaceAndPath("p", "b"),
                        2, 3, 4, 15, Reconcile.TRIM_UNSPENT)
        ));
        JsonElement encoded = SkillMapping.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        SkillMapping decoded = SkillMapping.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }
}
