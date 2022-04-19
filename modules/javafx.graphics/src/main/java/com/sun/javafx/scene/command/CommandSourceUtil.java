package com.sun.javafx.scene.command;

import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.command.Command;
import javafx.scene.command.CommandSource;
import javafx.scene.command.RoutedCommand;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

public final class CommandSourceUtil {

    private CommandSourceUtil() {}

    public static void executeCommand(CommandSource source) {
        Command command = source.getCommand();

        if (command instanceof RoutedCommand) {
            routedCall(source, CommandSourceUtil::executeRoutedCommand);
        } else if (command != null) {
            command.execute(source.getCommandParameter());
        }
    }

    public static void updateExecutable(CommandSource source) {
        if (source.getCommand() instanceof RoutedCommand) {
            routedCall(source, CommandSourceUtil::updateRoutedCommandExecutable);
        }
    }

    private static void routedCall(CommandSource source, EventTargetCall call) {
        EventTarget target = source.getCommandTarget();
        if (target != null) {
            call.apply(source, target);
        } else {
            Scene scene;
            Node focusOwner = source.getCommand() instanceof RoutedCommand routedCommand ?
                RoutedCommandHelper.getLastFocusOwner(routedCommand) : null;

            if (focusOwner != null) {
                if (call.apply(source, focusOwner)) {
                    return;
                }

                Scene thisScene = source.sceneProperty().get();
                Window owner = thisScene != null ? getOwnerWindow(thisScene.getWindow()) : null;
                scene = owner != null ? owner.getScene() : null;
            } else {
                scene = source.sceneProperty().get();
            }

            while (scene != null) {
                focusOwner = scene.getFocusOwner();
                if (focusOwner != null && call.apply(source, focusOwner)) {
                    break;
                }

                Window owner = getOwnerWindow(scene.getWindow());
                scene = owner != null ? owner.getScene() : null;
            }
        }
    }

    private static boolean executeRoutedCommand(CommandSource source, EventTarget target) {
        if (source.getCommand() instanceof RoutedCommand command) {
            return command.execute(target, source.getCommandParameter());
        }

        return false;
    }

    private static boolean updateRoutedCommandExecutable(CommandSource source, EventTarget target) {
        if (source.getCommand() instanceof RoutedCommand command) {
            return RoutedCommandHelper.updateExecutable(command, target);
        }

        return false;
    }

    private static Window getOwnerWindow(Window window) {
        return window instanceof PopupWindow popupWindow ? popupWindow.getOwnerWindow() : null;
    }

    private interface EventTargetCall {
        boolean apply(CommandSource source, EventTarget target);
    }

}
