package com.sun.javafx.scene.input;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;

public final class KeyboardFocus {

    private static class InstanceHolder {
        static final KeyboardFocus INSTANCE = new KeyboardFocus();
    }

    public static KeyboardFocus getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private KeyboardFocus() {
        Window.getWindows().addListener((ListChangeListener<Window>)change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Window window : change.getRemoved()) {
                        if (window == focusedWindow) {
                            updateFocusedWindow(null);
                        }

                        window.focusedProperty().removeListener(focusedChangeListener);
                        window.sceneProperty().removeListener(sceneChangeListener);

                        Scene scene = window.getScene();
                        if (scene != null) {
                            scene.focusOwnerProperty().removeListener(focusOwnerChangeListener);
                        }
                    }
                }

                if (change.wasAdded()) {
                    for (Window window : change.getAddedSubList()) {
                        window.focusedProperty().addListener(focusedChangeListener);
                        window.sceneProperty().addListener(sceneChangeListener);

                        Scene scene = window.getScene();
                        if (scene != null) {
                            scene.focusOwnerProperty().addListener(focusOwnerChangeListener);
                        }
                    }
                }
            }
        });

        for (Window window : Window.getWindows()) {
            window.focusedProperty().addListener(focusedChangeListener);
            window.sceneProperty().addListener(sceneChangeListener);

            Scene scene = window.getScene();
            if (scene != null) {
                scene.focusOwnerProperty().addListener(focusOwnerChangeListener);
            }

            if (window.isFocused()) {
                updateFocusedWindow(window);
            }
        }
    }

    private static class FocusedScene {
        private Scene scene;

        void update(Scene scene) {
            if (scene != null) {
                Window window = scene.getWindow();
                if (window != null && window.isFocused()) {
                    this.scene = scene;
                }
            }
        }
    }

    private final FocusedScene focusedScene = new FocusedScene();
    private Window focusedWindow;

    private void updateFocusedWindow(Window window) {
        focusedWindow = window;
        Scene scene = window != null ? window.getScene() : null;
        if (scene != null) {
            focusOwner.set(scene.getFocusOwner());
        } else {
            focusOwner.set(null);
        }
    }

    private void updateFocusOwner(Node focusOwner) {

    }

    private final ChangeListener<Node> focusOwnerChangeListener = (observable, oldFocusOwner, newFocusOwner) -> {
        Scene scene = newFocusOwner.getScene();
        if (scene != null) {
            Window window = scene.getWindow();
            if (window != null && window == focusedWindow) {
                updateFocusOwner(newFocusOwner);
            }
        }
    };

    private final ChangeListener<Scene> sceneChangeListener = (observable, oldScene, newScene) -> {
        if (oldScene != null) {
            oldScene.focusOwnerProperty().removeListener(focusOwnerChangeListener);
            focusedScene.update(null);
        }

        if (newScene != null) {
            newScene.focusOwnerProperty().addListener(focusOwnerChangeListener);
            focusedScene.update(newScene);
        }
    };

    private final ChangeListener<Boolean> focusedChangeListener = (observable, oldFocused, newFocused) -> {
        if (newFocused) {
            Window window = (Window)((ReadOnlyBooleanProperty)observable).getBean();
            focusedScene.update(window.getScene());
        }
    };

    private final ReadOnlyObjectWrapper<Node> focusOwner = new ReadOnlyObjectWrapper<>(this, "focusOwner");

    public ReadOnlyObjectProperty<Node> focusOwnerProperty() {
        return focusOwner.getReadOnlyProperty();
    }

    public Node getFocusOwner() {
        return focusOwnerProperty().get();
    }

}
