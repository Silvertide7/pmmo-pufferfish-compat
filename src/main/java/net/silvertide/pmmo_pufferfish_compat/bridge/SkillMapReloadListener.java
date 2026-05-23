package net.silvertide.pmmo_pufferfish_compat.bridge;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class SkillMapReloadListener extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "pmmo_pufferfish_compat/skill_map";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private volatile SkillMap current = SkillMap.EMPTY;

    public SkillMapReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        SkillMap next = parseEntries(entries,
                (source, error) -> LOGGER.warn("Skipping skill_map {}: {}", source, error));
        this.current = next;
        LOGGER.info("Loaded {} award(s) across {} PMMO skill(s) from skill_map datapacks",
                next.totalAwards(), next.trackedPmmoSkills().size());
    }

    public SkillMap current() {
        return current;
    }

    static SkillMap parseEntries(Map<ResourceLocation, JsonElement> entries, BiConsumer<ResourceLocation, String> onError) {
        List<SkillMapping> parsed = new ArrayList<>(entries.size());
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            ResourceLocation source = entry.getKey();
            SkillMapping.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> onError.accept(source, error))
                    .ifPresent(parsed::add);
        }
        return new SkillMap(parsed);
    }
}
