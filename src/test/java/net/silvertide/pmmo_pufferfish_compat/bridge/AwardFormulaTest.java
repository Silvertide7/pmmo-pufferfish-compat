package net.silvertide.pmmo_pufferfish_compat.bridge;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AwardFormulaTest {

    private static final ResourceLocation TREE = ResourceLocation.fromNamespaceAndPath("test", "tree");

    private static Award award(int skillPoints, int everyXPmmoLevels, int startingPmmoLevel, int maxSkillPoints) {
        return new Award(TREE, skillPoints, everyXPmmoLevels, startingPmmoLevel, maxSkillPoints, Reconcile.ADDITIVE);
    }

    @Test
    void defaultsGiveOnePointPerPmmoLevel() {
        Award a = award(1, 1, 1, Award.UNBOUNDED);
        assertEquals(0, a.expectedFor(0));
        assertEquals(1, a.expectedFor(1));
        assertEquals(5, a.expectedFor(5));
        assertEquals(10, a.expectedFor(10));
    }

    @Test
    void startingLevelGatesBelowThreshold() {
        Award a = award(1, 1, 5, Award.UNBOUNDED);
        assertEquals(0, a.expectedFor(0));
        assertEquals(0, a.expectedFor(4));
        assertEquals(1, a.expectedFor(5));
        assertEquals(2, a.expectedFor(6));
        assertEquals(6, a.expectedFor(10));
    }

    @Test
    void everyXLevelsControlsCadenceWithThreshold() {
        Award a = award(1, 5, 5, Award.UNBOUNDED);
        assertEquals(0, a.expectedFor(4));
        assertEquals(1, a.expectedFor(5));
        assertEquals(1, a.expectedFor(9));
        assertEquals(2, a.expectedFor(10));
        assertEquals(2, a.expectedFor(14));
        assertEquals(3, a.expectedFor(15));
    }

    @Test
    void skillPointsMultipliesEachGrant() {
        Award a = award(2, 5, 5, Award.UNBOUNDED);
        assertEquals(0, a.expectedFor(4));
        assertEquals(2, a.expectedFor(5));
        assertEquals(4, a.expectedFor(10));
        assertEquals(6, a.expectedFor(15));
        assertEquals(20, a.expectedFor(50));
    }

    @Test
    void maxSkillPointsCapsTotal() {
        Award a = award(1, 1, 1, 5);
        assertEquals(0, a.expectedFor(0));
        assertEquals(3, a.expectedFor(3));
        assertEquals(5, a.expectedFor(5));
        assertEquals(5, a.expectedFor(10));
        assertEquals(5, a.expectedFor(1_000_000));
    }

    @Test
    void maxSkillPointsZeroDisablesAward() {
        Award a = award(1, 1, 1, 0);
        assertEquals(0, a.expectedFor(0));
        assertEquals(0, a.expectedFor(1));
        assertEquals(0, a.expectedFor(100));
    }

    @Test
    void skillPointsZeroDisablesAward() {
        Award a = award(0, 1, 1, Award.UNBOUNDED);
        assertEquals(0, a.expectedFor(0));
        assertEquals(0, a.expectedFor(100));
    }

    @Test
    void startingLevelOneAndEveryFiveStartsAtLevelOne() {
        Award a = award(1, 5, 1, Award.UNBOUNDED);
        assertEquals(1, a.expectedFor(1));
        assertEquals(1, a.expectedFor(5));
        assertEquals(2, a.expectedFor(6));
        assertEquals(2, a.expectedFor(10));
        assertEquals(3, a.expectedFor(11));
    }

    @Test
    void overflowSafeWithLargeMultiplier() {
        Award a = award(Integer.MAX_VALUE, 1, 1, Award.UNBOUNDED);
        assertEquals(Integer.MAX_VALUE, a.expectedFor(2));
        assertEquals(Integer.MAX_VALUE, a.expectedFor(100));
    }

    @Test
    void rejectsNullSkillCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> new Award(null, 1, 1, 1, Award.UNBOUNDED, Reconcile.ADDITIVE));
    }

    @Test
    void rejectsNegativeSkillPoints() {
        assertThrows(IllegalArgumentException.class, () -> award(-1, 1, 1, Award.UNBOUNDED));
    }

    @Test
    void rejectsZeroEveryXPmmoLevels() {
        assertThrows(IllegalArgumentException.class, () -> award(1, 0, 1, Award.UNBOUNDED));
    }

    @Test
    void rejectsNegativeEveryXPmmoLevels() {
        assertThrows(IllegalArgumentException.class, () -> award(1, -3, 1, Award.UNBOUNDED));
    }

    @Test
    void rejectsZeroStartingPmmoLevel() {
        assertThrows(IllegalArgumentException.class, () -> award(1, 1, 0, Award.UNBOUNDED));
    }

    @Test
    void rejectsNegativeMaxSkillPoints() {
        assertThrows(IllegalArgumentException.class, () -> award(1, 1, 1, -1));
    }

    @Test
    void rejectsNullReconcile() {
        assertThrows(IllegalArgumentException.class,
                () -> new Award(TREE, 1, 1, 1, Award.UNBOUNDED, null));
    }
}
