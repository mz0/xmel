package net.x320.xmel.app;

import java.util.List;

public class Record {
    public final String id;
    public final String name;
    public final List<RecordField> fields;

    public Record(String id, String name, List<RecordField> fieldList) {
        this.id = id;
        this.name = name;
        this.fields = fieldList;
    }
}
