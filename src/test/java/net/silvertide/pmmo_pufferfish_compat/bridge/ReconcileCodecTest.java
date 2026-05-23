package net.silvertide.pmmo_pufferfish_compat.bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReconcileCodecTest {

    @Test
    void serializedNamesMatchDatapackContract() {
        assertEquals("none", Reconcile.NONE.getSerializedName());
        assertEquals("additive", Reconcile.ADDITIVE.getSerializedName());
        assertEquals("trim_unspent", Reconcile.TRIM_UNSPENT.getSerializedName());
        assertEquals("strict", Reconcile.STRICT.getSerializedName());
    }

    @Test
    void enumDeclarationCoversEverySerializedName() {
        assertEquals(4, Reconcile.values().length);
    }

    @Test
    void codecDecodesEveryValue() {
        assertEquals(Reconcile.NONE, decode("\"none\""));
        assertEquals(Reconcile.ADDITIVE, decode("\"additive\""));
        assertEquals(Reconcile.TRIM_UNSPENT, decode("\"trim_unspent\""));
        assertEquals(Reconcile.STRICT, decode("\"strict\""));
    }

    @Test
    void codecEncodesEveryValue() {
        assertEquals("\"none\"", encode(Reconcile.NONE));
        assertEquals("\"additive\"", encode(Reconcile.ADDITIVE));
        assertEquals("\"trim_unspent\"", encode(Reconcile.TRIM_UNSPENT));
        assertEquals("\"strict\"", encode(Reconcile.STRICT));
    }

    @Test
    void codecRoundTripForEveryValue() {
        for (Reconcile value : Reconcile.values()) {
            JsonElement encoded = Reconcile.CODEC.encodeStart(JsonOps.INSTANCE, value).getOrThrow();
            Reconcile decoded = Reconcile.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
            assertEquals(value, decoded);
        }
    }

    @Test
    void codecRejectsUnknownValue() {
        DataResult<Reconcile> result = Reconcile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("\"banana\""));
        assertTrue(result.isError(), "Expected error for unknown reconcile value, got: " + result.result());
    }

    private static Reconcile decode(String json) {
        return Reconcile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    }

    private static String encode(Reconcile value) {
        return Reconcile.CODEC.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString();
    }
}
