package io.github.gluucc.hyperstyle.client.style;

import io.github.gluucc.hyperstyle.client.api.StyleCategory;
import io.github.gluucc.hyperstyle.client.api.StyleEvent;

public record QueuedEvent(StyleEvent event, StyleCategory category){}
