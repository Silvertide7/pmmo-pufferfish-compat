package net.silvertide.pmmo_pufferfish_compat.bridge;

import net.silvertide.pmmo_pufferfish_compat.bridge.Reconciler.Plan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReconcilerPlanTest {

    @Test
    void positiveDeltaNone() {
        Plan p = Reconciler.plan(Reconcile.NONE, 10, 5, 3);
        assertEquals(10, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void positiveDeltaAdditive() {
        Plan p = Reconciler.plan(Reconcile.ADDITIVE, 10, 5, 3);
        assertEquals(10, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void positiveDeltaTrimUnspent() {
        Plan p = Reconciler.plan(Reconcile.TRIM_UNSPENT, 10, 5, 3);
        assertEquals(10, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void positiveDeltaStrict() {
        Plan p = Reconciler.plan(Reconcile.STRICT, 10, 5, 3);
        assertEquals(10, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void zeroDeltaAllModesNoOp() {
        for (Reconcile mode : Reconcile.values()) {
            Plan p = Reconciler.plan(mode, 10, 10, 5);
            assertEquals(10, p.newGranted(), "mode=" + mode);
            assertFalse(p.refundSpent(), "mode=" + mode);
        }
    }

    @Test
    void negativeDeltaNoneKeepsExcess() {
        Plan p = Reconciler.plan(Reconcile.NONE, 5, 10, 3);
        assertEquals(10, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void negativeDeltaAdditiveKeepsExcess() {
        Plan p = Reconciler.plan(Reconcile.ADDITIVE, 5, 10, 3);
        assertEquals(10, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void negativeDeltaTrimUnspentClawsToExpectedWhenPossible() {
        Plan p = Reconciler.plan(Reconcile.TRIM_UNSPENT, 5, 10, 3);
        assertEquals(5, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void negativeDeltaTrimUnspentClampsAtSpent() {
        Plan p = Reconciler.plan(Reconcile.TRIM_UNSPENT, 3, 10, 7);
        assertEquals(7, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void negativeDeltaTrimUnspentAllSpentNoOp() {
        Plan p = Reconciler.plan(Reconcile.TRIM_UNSPENT, 5, 10, 10);
        assertEquals(10, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void negativeDeltaTrimUnspentCanGoToZero() {
        Plan p = Reconciler.plan(Reconcile.TRIM_UNSPENT, 0, 5, 0);
        assertEquals(0, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void negativeDeltaStrictRefundsAndSetsToExpected() {
        Plan p = Reconciler.plan(Reconcile.STRICT, 5, 10, 7);
        assertEquals(5, p.newGranted());
        assertTrue(p.refundSpent());
    }

    @Test
    void negativeDeltaStrictCanGoToZero() {
        Plan p = Reconciler.plan(Reconcile.STRICT, 0, 5, 3);
        assertEquals(0, p.newGranted());
        assertTrue(p.refundSpent());
    }

    @Test
    void workedExampleAdditiveKeepsExcess() {
        Plan p = Reconciler.plan(Reconcile.ADDITIVE, 10, 20, 12);
        assertEquals(20, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void workedExampleTrimUnspentTrimsDownToSpent() {
        Plan p = Reconciler.plan(Reconcile.TRIM_UNSPENT, 10, 20, 12);
        assertEquals(12, p.newGranted());
        assertFalse(p.refundSpent());
    }

    @Test
    void workedExampleStrictRespecs() {
        Plan p = Reconciler.plan(Reconcile.STRICT, 10, 20, 12);
        assertEquals(10, p.newGranted());
        assertTrue(p.refundSpent());
    }
}
