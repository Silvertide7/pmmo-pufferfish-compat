package net.silvertide.pmmo_pufferfish_compat.bridge;

import harmonised.pmmo.api.events.XpEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.silvertide.pmmo_pufferfish_compat.PMMOPufferfishCompat;

@EventBusSubscriber(modid = PMMOPufferfishCompat.MODID)
public final class CompatEvents {
    private CompatEvents() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PMMOPufferfishCompat.skillMapListener());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(BridgeCommand.root());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        for (ServerPlayer player : event.getRelevantPlayers().toList()) {
            scheduleReconcile(player);
        }
    }

    private static void scheduleReconcile(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        // Defer a tick so PMMO's per-player data has loaded by the time we read it
        server.tell(new TickTask(server.getTickCount() + 1, () -> {
            if (player.hasDisconnected()) return;
            Reconciler.reconcileAll(player);
        }));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onXpEvent(XpEvent event) {
        if (event.isCanceled()) return;
        if (!event.isLevelUp() && !event.isLevelDown()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        int prevLevel = clampPmmoLevel(event.startLevel());
        int currLevel = clampPmmoLevel(event.endLevel());
        Reconciler.onLevelChange(player, event.skill, prevLevel, currLevel);
    }

    private static int clampPmmoLevel(long level) {
        if (level > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (level < 0) return 0;
        return (int) level;
    }
}
