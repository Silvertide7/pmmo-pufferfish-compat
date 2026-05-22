package net.silvertide.pmmo_pufferfish_compat;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(PMMOPufferfishCompat.MODID)
public class PMMOPufferfishCompat {
    public static final String MODID = "pmmo_pufferfish_compat";
    public static final Logger LOGGER = LogUtils.getLogger();
    public PMMOPufferfishCompat(IEventBus modEventBus, ModContainer modContainer) {
    }
}
