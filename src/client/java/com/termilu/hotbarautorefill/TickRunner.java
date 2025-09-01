package com.termilu.hotbarautorefill;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayDeque;
import java.util.Queue;

final class TickRunner {
    private static final Queue<Runnable> QUEUE = new ArrayDeque<>();

    static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Runnable r;
            while ((r = poll()) != null) {
                try {
                    r.run();
                } catch (Throwable t) {
                    HotbarAutoRefillClient.LOGGER.error("TickRunner task failed", t);
                }
            }
        });
    }

    static synchronized void schedule(Runnable task) {
        QUEUE.offer(task);
    }

    private static synchronized Runnable poll() {
        return QUEUE.poll();
    }

    private TickRunner() {}
}