/*
 * Copyright (c) 2022, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  JFXcore designates this
 * particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package javafx.scene.command;

import com.sun.javafx.scene.command.CommandSourceUtil;
import com.sun.javafx.scene.command.RoutedCommandHelper;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.util.Incubating;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A routed command is a specialized command that, in contrast to all other command implementations,
 * does not encapsulate its operation directly. Instead, when a routed command is invoked, it fires
 * a {@link RoutedCommandEvent} that travels to its target in the scene graph.
 * <p>
 * If it encounters a node along the way that has a {@link RoutedCommandBinding} for this routed command,
 * the binding is invoked. Depending on the {@link CommandBinding#bindingModeProperty() binding mode},
 * the event is either consumed or it continues to travel along the scene graph.
 *
 * @since JFXcore 18
 */
@Incubating
public class RoutedCommand extends CommandBase {

    static {
        RoutedCommandHelper.setAccessor(new RoutedCommandHelper.Accessor() {
            @Override
            public void registerScene(RoutedCommand routedCommand, Scene scene) {
                routedCommand.addScene(scene);
            }

            @Override
            public void unregisterScene(RoutedCommand routedCommand, Scene scene) {
                routedCommand.removeScene(scene);
            }

            @Override
            public boolean updateExecutable(RoutedCommand routedCommand, EventTarget target) {
                return routedCommand.updateExecutable(target);
            }

            @Override
            public Node getLastFocusOwner(RoutedCommand routedCommand) {
                return routedCommand.lastFocusOwner != null ? routedCommand.lastFocusOwner.get() : null;
            }
        });
    }

    private final ChangeListener<Scene> sceneChangedListener = (observable, oldScene, newScene) -> {
        removeScene(oldScene);
        addScene(newScene);
    };

    private final WeakChangeListener<Scene> weakSceneChangedListener =
            new WeakChangeListener<>(sceneChangedListener);

    private final BooleanProperty executable;
    private final Map<Scene, FocusTracker> scenes = new WeakHashMap<>(2);
    private WeakReference<Node> lastFocusOwner;

    /**
     * Creates a new {@code RoutedCommand} instance.
     */
    public RoutedCommand() {
        super(new BooleanPropertyBase(false) {
            @Override public Object getBean() { return null; }
            @Override public String getName() { return null; }
        });

        executable = (BooleanProperty)getCondition();
    }

    public void addOwner(CommandSource source) {
        source.sceneProperty().addListener(weakSceneChangedListener);
        addScene(source.sceneProperty().get());
        CommandSourceUtil.updateExecutable(source);
    }

    public void removeOwner(CommandSource node) {
        node.sceneProperty().removeListener(weakSceneChangedListener);
    }

    /**
     * Executes the routed command on the currently focused node.
     */
    @Override
    public void execute(Object parameter) {
        if (!executable.get()) {
            throw new IllegalStateException("Command is not executable.");
        }

        EventTarget target = getFocusOwner();
        if (target != null) {
            execute(target, parameter);
        }
    }

    /**
     * Executes the routed command on the specified target.
     *
     * @param target the target of the command
     * @return {@code true} if the command was consumed by a {@link CommandBinding}; {@code false} otherwise
     */
    public boolean execute(EventTarget target, Object parameter) {
        if (!executable.get()) {
            throw new IllegalStateException("Command is not executable.");
        }

        var event = new RoutedCommandEvent(target, RoutedCommandEvent.EXECUTE, this, parameter);
        Event.fireEvent(target, event);
        return event.isConsumed();
    }

    /**
     * Updates whether the routed command is executable for the focus owner of the
     * currently focused window. This is trivially false if the currently focused
     * window does not contain a {@link CommandSource} for this routed command.
     */
    void updateExecutable() {
        updateExecutable(getFocusOwner());
    }

    /**
     * Updates whether the routed command is executable for the specified target,
     * and returns whether a command binding consumed the {@link RoutedCommandEvent}.
     *
     * @param target the target for the routed command
     * @return {@code true} if the {@code RoutedCommandEvent} was consumed, {@code false} otherwise
     */
    boolean updateExecutable(EventTarget target) {
        if (target != null) {
            var event = new RoutedCommandEvent(target, RoutedCommandEvent.EXECUTABLE, this, null);
            Event.fireEvent(target, event);
            executable.set(event.isExecutable());
            return event.isConsumed();
        }

        executable.set(false);
        return false;
    }

    /**
     * Gets the focus owner of the currently focused window, or {@code null} if the currently
     * focused window does not contain a {@link CommandSource} for this command.
     */
    private Node getFocusOwner() {
        for (Scene scene : scenes.keySet()) {
            Window window = scene.getWindow();
            if (window != null && window.isFocused()) {
                return scene.getFocusOwner();
            }
        }

        return null;
    }

    private void addScene(Scene scene) {
        FocusTracker focusTracker = scene != null ? scenes.get(scene) : null;
        if (focusTracker != null) {
            ++focusTracker.refCount;
        } else if (scene != null) {
            scenes.put(scene, new FocusTracker(scene));
        }
    }

    private void removeScene(Scene scene) {
        FocusTracker focusTracker = scene != null ? scenes.get(scene) : null;
        if (focusTracker != null) {
            if (focusTracker.refCount > 1) {
                --focusTracker.refCount;
            } else {
                scenes.remove(scene).dispose();
            }
        }
    }

    /**
     * Tracks the focus owners of all scenes in which this routed command is used, and updates
     * the executable state of this routed command when the effective focus owner changes.
     */
    private class FocusTracker implements InvalidationListener, ChangeListener<Window> {
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

}
