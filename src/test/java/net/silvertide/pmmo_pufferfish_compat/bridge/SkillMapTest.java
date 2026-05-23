package net.silvertide.pmmo_pufferfish_compat.bridge;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillMapTest {

    private static Award awardFor(String namespace, String path) {
        return new Award(
                ResourceLocation.fromNamespaceAndPath(namespace, path),
                1, 1, 1, Award.UNBOUNDED, Reconcile.ADDITIVE);
    }

    @Test
    void emptyConstantIsEmpty() {
        assertTrue(SkillMap.EMPTY.isEmpty());
        assertEquals(0, SkillMap.EMPTY.totalAwards());
        assertEquals(0, SkillMap.EMPTY.trackedPmmoSkills().size());
    }

    @Test
    void unknownSkillReturnsEmptyNonNullArray() {
        SkillMap map = new SkillMap(List.of());
        Award[] awards = map.awardsFor("nonexistent");
        assertNotNull(awards);
        assertEquals(0, awards.length);
    }

    @Test
    void awardsForNullReturnsEmptyArrayInsteadOfNpe() {
        SkillMap map = new SkillMap(List.of(
                new SkillMapping("mining", List.of(awardFor("pack", "tree")))));
        Award[] awards = map.awardsFor(null);
        assertNotNull(awards);
        assertEquals(0, awards.length);
    }

    @Test
    void singleMappingExposesAwards() {
        SkillMapping mapping = new SkillMapping("mining",
                List.of(awardFor("pack", "mining_tree")));
        SkillMap map = new SkillMap(List.of(mapping));
        assertFalse(map.isEmpty());
        assertEquals(1, map.totalAwards());
        assertEquals(1, map.awardsFor("mining").length);
        assertEquals(ResourceLocation.fromNamespaceAndPath("pack", "mining_tree"),
                map.awardsFor("mining")[0].skillCategory());
    }

    @Test
    void awardsForSamePmmoSkillAcrossFilesAreMerged() {
        SkillMapping fileA = new SkillMapping("mining",
                List.of(awardFor("pack", "mining_tree")));
        SkillMapping fileB = new SkillMapping("mining",
                List.of(awardFor("pack", "gathering_tree")));
        SkillMap map = new SkillMap(List.of(fileA, fileB));
        Award[] miningAwards = map.awardsFor("mining");
        assertEquals(2, miningAwards.length);
        assertEquals(1, map.trackedPmmoSkills().size());
    }

    @Test
    void differentPmmoSkillsAreTrackedIndependently() {
        SkillMapping mining = new SkillMapping("mining",
                List.of(awardFor("pack", "mining_tree")));
        SkillMapping combat = new SkillMapping("combat",
                List.of(awardFor("pack", "combat_tree"), awardFor("pack", "archery_tree")));
        SkillMap map = new SkillMap(List.of(mining, combat));
        assertEquals(3, map.totalAwards());
        assertEquals(2, map.trackedPmmoSkills().size());
        assertEquals(1, map.awardsFor("mining").length);
        assertEquals(2, map.awardsFor("combat").length);
    }

    @Test
    void mappingWithMultipleAwardsFlattensCorrectly() {
        SkillMapping mining = new SkillMapping("mining",
                List.of(awardFor("a", "t"), awardFor("b", "t"), awardFor("c", "t")));
        SkillMap map = new SkillMap(List.of(mining));
        assertEquals(3, map.awardsFor("mining").length);
    }

    @Test
    void awardsForReturnsSameArrayInstanceForRepeatedCalls() {
        SkillMapping mapping = new SkillMapping("mining",
                List.of(awardFor("pack", "mining_tree")));
        SkillMap map = new SkillMap(List.of(mapping));
        assertTrue(map.awardsFor("mining") == map.awardsFor("mining"),
                "awardsFor should return the same backing array on repeated calls (no per-call allocation)");
    }
}
