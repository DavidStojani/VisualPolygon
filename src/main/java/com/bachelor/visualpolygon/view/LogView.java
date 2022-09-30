
package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.logging.Level;
import com.bachelor.visualpolygon.logging.LogRecord;
import com.bachelor.visualpolygon.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.text.SimpleDateFormat;

public class LogView extends ListView<LogRecord> {
    private static final int MAX_ENTRIES = 10_000;

    private static final PseudoClass debug = PseudoClass.getPseudoClass("debug");
    private static final PseudoClass info = PseudoClass.getPseudoClass("info");
    private static final PseudoClass warn = PseudoClass.getPseudoClass("warn");
    private static final PseudoClass error = PseudoClass.getPseudoClass("error");

    private final SimpleDateFormat timestampFormatter = new SimpleDateFormat("HH:mm:ss.SSS");

    private final BooleanProperty showTimestamp = new SimpleBooleanProperty(false);
    private final ObjectProperty<Level> filterLevel = new SimpleObjectProperty<>(null);
    private final BooleanProperty tail = new SimpleBooleanProperty(true);
    private final BooleanProperty paused = new SimpleBooleanProperty(false);


    private final ObservableList<LogRecord> logItems = FXCollections.observableArrayList();

    public BooleanProperty showTimeStampProperty() {
        return showTimestamp;
    }

    public ObjectProperty<Level> filterLevelProperty() {
        return filterLevel;
    }

    public BooleanProperty tailProperty() {
        return tail;
    }

    public BooleanProperty pausedProperty() {
        return paused;
    }

    public LogView(Logger logger) {
        getStyleClass().add("log-view");

        Timeline logTransfer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    logger.drainTo(logItems);

                    if (logItems.size() > MAX_ENTRIES) {
                        logItems.remove(0, logItems.size() - MAX_ENTRIES);
                    }

                    if (tail.get()) {
                        scrollTo(logItems.size());
                    }
                }
                )
        );
        logTransfer.setCycleCount(Animation.INDEFINITE);
        logTransfer.setRate(60);


        this.pausedProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue) && logTransfer.getStatus() == Animation.Status.RUNNING) {
                logTransfer.pause();
            }

            if (Boolean.TRUE.equals(!newValue && logTransfer.getStatus() == Animation.Status.PAUSED) && getParent() != null) {
                logTransfer.play();
            }
        });

        this.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                logTransfer.pause();
            } else {
                if (!paused.get()) {
                    logTransfer.play();
                }
            }
        });

        filterLevel.addListener((observable, oldValue, newValue) -> setItems(new FilteredList<>(logItems, logRecord ->
                logRecord.getLevel().ordinal() >= filterLevel.get().ordinal())
        ));
        filterLevel.set(Level.DEBUG);

        setCellFactory(param -> new ListCell<>() {
            {
                showTimestamp.addListener(observable -> updateItem(this.getItem(), this.isEmpty()));
            }

            @Override
            protected void updateItem(LogRecord item, boolean empty) {
                super.updateItem(item, empty);

                pseudoClassStateChanged(debug, false);
                pseudoClassStateChanged(info, false);
                pseudoClassStateChanged(warn, false);
                pseudoClassStateChanged(error, false);

                if (item == null || empty) {
                    setText(null);
                    return;
                }

                String context = (item.getContext() == null) ? "" : item.getContext() + " ";

                if (showTimestamp.get()) {
                    String timestamp =
                            (item.getTimestamp() == null)
                                    ? ""
                                    : timestampFormatter.format(item.getTimestamp()) + " ";
                    setText(timestamp + context + item.getMessage());
                } else {
                    setText(context + item.getMessage());
                }

                switch (item.getLevel()) {
                    case DEBUG:
                        pseudoClassStateChanged(debug, true);
                        break;

                    case INFO:
                        pseudoClassStateChanged(info, true);
                        break;

                    case WARN:
                        pseudoClassStateChanged(warn, true);
                        break;

                    case ERROR:
                        pseudoClassStateChanged(error, true);
                        break;
                }
            }
        });
    }

    public void resetLogg() {
        logItems.clear();
    }
}
