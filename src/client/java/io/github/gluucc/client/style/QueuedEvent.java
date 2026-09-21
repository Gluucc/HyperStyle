package io.github.gluucc.client.style;

import io.github.gluucc.client.api.StyleCategory;
import io.github.gluucc.client.api.StyleEvent;

public record QueuedEvent(StyleEvent event, StyleCategory category){}
