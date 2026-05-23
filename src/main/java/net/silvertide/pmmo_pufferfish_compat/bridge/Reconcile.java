package net.silvertide.pmmo_pufferfish_compat.bridge;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum Reconcile implements StringRepresentable {
    NONE("none"),
    ADDITIVE("additive"),
    TRIM_UNSPENT("trim_unspent"),
    STRICT("strict");

    public static final Codec<Reconcile> CODEC = StringRepresentable.fromEnum(Reconcile::values);

    private final String serializedName;

    Reconcile(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
