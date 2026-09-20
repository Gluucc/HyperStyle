package io.github.gluucc.client.style;

public enum StyleEvent {
    KILL("KILL", 45, 0xFFFFFFFF),
    DOUBLE_KILL("DOUBLE KILL", 25, 0xFFFFA500),
    TRIPLE_KILL("TRIPLE KILL", 50, 0xFFFFA500),
    MULTIKILL("MULTIKILL", 100, 0xFFFFA500),
    INSTAKILL("INSTAKILL", 50,0xFF00FF00),
    FRIED("FRIED", 30, 0xFFFFFFFF),
    EXPLODED("EXPLODED", 50, 0xFFFFFFFF),
    AIRSHOT("AIRSHOT", 50, 0xFF00FFFF),
    FIREWORKS("FIREWORKS", 120, 0xFF00FFFF),
    JUMPSHOT("JUMPSHOT", 25, 0xFF00FFFF);

    private final String eventLabel;
    private final int eventPoints;
    private final int eventColor;

    StyleEvent(String eventLabel, int eventPoints, int eventColor) {
        this.eventLabel = eventLabel;
        this.eventPoints = eventPoints;
        this.eventColor = eventColor;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public int getEventPoints() {
        return eventPoints;
    }

    public int getEventColor() {
        return eventColor;
    }
}
