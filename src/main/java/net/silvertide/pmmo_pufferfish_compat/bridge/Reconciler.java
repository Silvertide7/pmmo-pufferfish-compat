package net.silvertide.pmmo_pufferfish_compat.bridge;

import harmonised.pmmo.api.APIUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.SkillsAPI;
import net.silvertide.pmmo_pufferfish_compat.PMMOPufferfishCompat;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class Reconciler {
    private Reconciler() {}

    public record Plan(int newGranted, boolean refundSpent) {}

    private static final ConcurrentHashMap<String, ResourceLocation> SOURCE_ID_CACHE = new ConcurrentHashMap<>();

    public static void onLevelChange(ServerPlayer player, String skill, int prevLevel, int currLevel) {
        Award[] awards = PMMOPufferfishCompat.skillMap().awardsFor(skill);
        if (awards.length == 0) return;
        ResourceLocation source = sourceIdFor(skill);
        for (Award award : awards) {
            int delta = award.expectedFor(currLevel) - award.expectedFor(prevLevel);
            if (delta <= 0) continue;
            Optional<Category> opt = SkillsAPI.getCategory(award.skillCategory());
            if (opt.isPresent()) {
                opt.get().addPoints(player, source, delta);
            }
        }
    }

    public static void reconcileSkill(ServerPlayer player, String skill) {
        Award[] awards = PMMOPufferfishCompat.skillMap().awardsFor(skill);
        if (awards.length == 0) return;
        int pmmoLevel = readPmmoLevel(player, skill);
        for (Award award : awards) {
            if (award.reconcile() == Reconcile.NONE) continue;
            applyAward(player, skill, award, pmmoLevel);
        }
    }

    public static void reconcileAll(ServerPlayer player) {
        SkillMap map = PMMOPufferfishCompat.skillMap();
        for (String skill : map.trackedPmmoSkills()) {
            reconcileSkill(player, skill);
        }
    }

    public static Plan plan(Reconcile mode, int expected, int granted, int spent) {
        int delta = expected - granted;
        if (delta >= 0) {
            return new Plan(expected, false);
        }
        return switch (mode) {
            case NONE, ADDITIVE -> new Plan(granted, false);
            case TRIM_UNSPENT -> new Plan(Math.max(expected, spent), false);
            case STRICT -> new Plan(expected, true);
        };
    }

    private static void applyAward(ServerPlayer player, String skill, Award award, int pmmoLevel) {
        Optional<Category> opt = SkillsAPI.getCategory(award.skillCategory());
        if (opt.isEmpty()) return;
        Category category = opt.get();
        ResourceLocation source = sourceIdFor(skill);

        int expected = award.expectedFor(pmmoLevel);
        int granted = category.getPoints(player, source);
        int spent = category.getSpentPoints(player);

        Plan plan = plan(award.reconcile(), expected, granted, spent);

        if (plan.refundSpent()) {
            category.resetSkills(player);
        }

        int delta = plan.newGranted() - granted;
        if (delta > 0) {
            category.addPointsSilently(player, source, delta);
        } else if (delta < 0) {
            category.setPointsSilently(player, source, plan.newGranted());
        }
    }

    private static int readPmmoLevel(ServerPlayer player, String skill) {
        long level = APIUtils.getLevel(skill, player);
        if (level > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (level < 0) return 0;
        return (int) level;
    }

    private static ResourceLocation sourceIdFor(String skill) {
        return SOURCE_ID_CACHE.computeIfAbsent(skill,
                s -> ResourceLocation.fromNamespaceAndPath(PMMOPufferfishCompat.MODID, "pmmo/" + s));
    }
}
