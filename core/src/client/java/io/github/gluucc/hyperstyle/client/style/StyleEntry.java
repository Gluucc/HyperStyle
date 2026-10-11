package io.github.gluucc.hyperstyle.client.style;

import io.github.gluucc.hyperstyle.client.api.StyleEvent;

public record StyleEntry(StyleEvent event, int count, int createdAt){}
