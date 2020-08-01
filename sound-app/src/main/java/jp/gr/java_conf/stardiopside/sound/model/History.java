package jp.gr.java_conf.stardiopside.sound.model;

import java.time.LocalDateTime;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

public class History {

    private final ReadOnlyObjectWrapper<LocalDateTime> dateTime;
    private final ReadOnlyStringWrapper value;

    public History(LocalDateTime dateTime, String value) {
        this.dateTime = new ReadOnlyObjectWrapper<>(dateTime);
        this.value = new ReadOnlyStringWrapper(value);
    }

    public ReadOnlyObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTime.getReadOnlyProperty();
    }

    public LocalDateTime getDateTime() {
        return dateTime.get();
    }

    public ReadOnlyStringProperty valueProperty() {
        return value.getReadOnlyProperty();
    }

    public String getValue() {
        return value.get();
    }
}
