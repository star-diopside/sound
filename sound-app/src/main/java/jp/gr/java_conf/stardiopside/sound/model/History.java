package jp.gr.java_conf.stardiopside.sound.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

public class History {

    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    private final ReadOnlyObjectWrapper<LocalDateTime> dateTime;
    private final ReadOnlyStringWrapper value;
    private final ReadOnlyStringWrapper dateTimeString = new ReadOnlyStringWrapper();

    public History(LocalDateTime dateTime, String value) {
        this.dateTime = new ReadOnlyObjectWrapper<>(dateTime);
        this.value = new ReadOnlyStringWrapper(value);
        this.dateTimeString.bind(Bindings.createStringBinding(
                () -> this.dateTime.get().format(HISTORY_FORMATTER), this.dateTime));
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

    public ReadOnlyStringProperty dateTimeStringProperty() {
        return dateTimeString.getReadOnlyProperty();
    }
}
