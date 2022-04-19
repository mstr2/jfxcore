package com.sun.javafx.scene.command;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.command.CommandSource;
import javafx.scene.command.RoutedCommand;
import javafx.stage.Window;
import java.lang.ref.WeakReference;

public class FocusTracker implements InvalidationListener, ChangeListener<Window> {

    private final Scene scene;
    private boolean shouldUpdateExecutable;
    int refCount;

    FocusTracker(Scene scene) {
        this.scene = scene;
        this.refCount = 1;

        Window window = scene.getWindow();
        if (window != null) {
            window.focusedProperty().addListener(this);
        }

        scene.windowProperty().addListener((ChangeListener<? super Window>)this);
        scene.focusOwnerProperty().addListener(this);
    }

    public void dispose() {
        Window window = scene.getWindow();
        if (window != null) {
            window.focusedProperty().removeListener(this);
        }

        scene.windowProperty().removeListener((ChangeListener<? super Window>)this);
        scene.focusOwnerProperty().removeListener(this);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void invalidated(Observable observable) {
        boolean focusOwnerChanged = observable == scene.focusOwnerProperty();

        if (focusOwnerChanged) {
            shouldUpdateExecutable = false;

            Node focusOwner = scene.getFocusOwner();
            Node lastFocusOwner = RoutedCommand.this.lastFocusOwner != null ?
                    RoutedCommand.this.lastFocusOwner.get() : null;

            // In most cases, the control that owns the routed command (for example, a button) doesn't
            // have a RoutedCommandBinding for the same command. Since clicking the button usually makes
            // it the focus owner of the scene, the button would immediately be disabled because it doesn't
            // handle its own routed command. This would also make it impossible for a button to repeatedly
            // invoke a routed command on the same target.
            // For these reasons, the routed command always acts on the 'last' focus owner, which is only
            // updated when a control other than the control that owns the routed command becomes the
            // focus owner of the scene.
            boolean isOwnCommand = focusOwner instanceof CommandSource cs && cs.getCommand() == RoutedCommand.this;
            if (lastFocusOwner != focusOwner && !isOwnCommand) {
                RoutedCommand.this.lastFocusOwner = new WeakReference<>(focusOwner);
                updateExecutable(focusOwner);
            }
        } else if (scene.getWindow().isFocused()) {
            shouldUpdateExecutable = true;

            // If a window becomes focused, we don't know yet whether the focus owner will also be changed.
            // In case the focus owner is unchanged, we still need to re-evaluate the routed command
            // since it might need to change its executable status for the newly focused window.
            // So we schedule the re-evaluation for the next pulse, but only actually do it when the
            // focus owner wasn't changed in the meantime.
            Platform.runLater(() -> {
                if (shouldUpdateExecutable) {
                    invalidated(scene.focusOwnerProperty());
                }
            });
        }
    }

    @Override
    public void changed(ObservableValue<? extends Window> observable, Window oldValue, Window newValue) {
        if (oldValue != null) {
            oldValue.focusedProperty().removeListener(this);
        }

        if (newValue != null) {
            newValue.focusedProperty().addListener(this);
        }
    }

}
