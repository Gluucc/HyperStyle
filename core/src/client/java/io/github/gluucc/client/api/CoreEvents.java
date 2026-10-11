package io.github.gluucc.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class CoreEvents {
    public static final Event<AddKillEvent> ADD_KILL_EVENT = EventFactory.createArrayBacked(
            AddKillEvent.class,
            callbacks -> (record, category) -> {
                for (AddKillEvent callback : callbacks) {
                    callback.addKillEvent(record, category);
                }
            }
    );

    @FunctionalInterface
    public interface AddKillEvent {
        void addKillEvent(DamageRecord record, StyleCategory category);
    }
}
