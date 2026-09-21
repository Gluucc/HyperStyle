package io.github.gluucc.client.style;

import io.github.gluucc.client.api.StyleEvent;

public record StyleEntry(StyleEvent event, int count, int createdAt){}
