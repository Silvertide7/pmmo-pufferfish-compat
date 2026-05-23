package net.silvertide.pmmo_pufferfish_compat;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.silvertide.pmmo_pufferfish_compat.bridge.SkillMap;
import net.silvertide.pmmo_pufferfish_compat.bridge.SkillMapReloadListener;
import org.slf4j.Logger;

@Mod(PMMOPufferfishCompat.MODID)
public class PMMOPufferfishCompat {
    public static final String MODID = "pmmo_pufferfish_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final SkillMapReloadListener SKILL_MAP_LISTENER = new SkillMapReloadListener();

    public PMMOPufferfishCompat(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("PMMO -> Pufferfish bridge initialized");
    }

    public static SkillMap skillMap() {
        return SKILL_MAP_LISTENER.current();
    }

    public static SkillMapReloadListener skillMapListener() {
        return SKILL_MAP_LISTENER;
    }
}
