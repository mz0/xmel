package net.x320.xmel.app;

public class RecordField {
    public final String id;
    public final String name;
    public final boolean isStandAlone;
    private int usage;

    public RecordField(String id, String name, boolean standAlone) {
        this.id = id;
        this.name = name;
        this.isStandAlone = standAlone;
    }

    public int getUsage() {
        return usage;
    }

    public int addUsage() {
        return ++usage;
    }
}
