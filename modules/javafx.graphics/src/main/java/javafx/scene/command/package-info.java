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

/**
 * Provides the set of classes for the Commanding API.
 *
 * <h2>Contents</h2>
 * <ol style="list-style-type: none">
 *     <li>1. <a href="#Overview">Overview</a>
 *     <li>2. <a href="#CreatingCommands">Creating and using commands</a>
 *     <ol style="list-style-type: none">
 *         <li>2.1. <a href="#EventBinding">Event bindings</a>
 *         <li>2.1. <a href="#DisabledState">How commands affect the <em>disabled</em> state of a node</a>
 *     </ol>
 *     <li>3. <a href="#AsyncCommand">Asynchronous commands</a>
 *     <ol style="list-style-type: none">
 *         <li>3.1. <a href="#Cancellation">Cancelling a running operation</a>
 *         <li>3.2. <a href="#ProgressReporting">Progress reporting for asynchronous operations</a>
 *     </ol>
 *     <li>4. <a href="#CommandBehaviors">Command behaviors</a>
 *     <ol style="list-style-type: none">
 *         <li>4.1. <a href="#CommandBasedBehaviors">Command-based behaviors</a>
 *         <li>4.2. <a href="#ControlBasedBehaviors">Control-based behaviors</a>
 *     </ol>
 * </ol>
 *
 * <a id="Overview"></a>
 * <h2>1. Overview</h2>
 * Commanding enables applications to handle user input in a loosely coupled, object-oriented, and reusable way.
 * <p>
 * A {@link javafx.scene.command.Command} represents an operation that can be invoked in various ways, such as
 * by clicking a button, pressing a key, or typing a shortcut.
 * Decoupling the command implementation from its mode of invocation allows applications to define an operation
 * once, and use it in different places and with different input modalities.
 * <p>
 * Commands also communicate their state to the user interface. For example, if a command is not executable,
 * a button that invokes the command is automatically {@link javafx.scene.Node#disabledProperty() disabled}
 * to reflect the state of the command.
 *
 * <a id="CreatingCommands"></a>
 * <h2>2. Creating and using commands</h2>
 * A command can be created by extending the {@link javafx.scene.command.Command} class.
 * For ease of use, JFXcore comes with several predefined command implementations.
 * In this example, {@link javafx.scene.command.RelayCommand} is used to create a simple command that prints
 * some text when invoked:
 * <pre>{@code
 *     Command myCommand = new RelayCommand<>(() -> {
 *         System.out.println("Command was executed.");
 *     });
 * }</pre>
 * In order to use this command, it must be bound to an event of a UI control:
 * <pre>{@code
 *     var button = new Button();
 *     button.setOnAction(new ActionEventBinding(myCommand));
 * }</pre>
 * Now, clicking the button will invoke the command.
 *
 * <a id="EventBinding"></a>
 * <h3>2.1. Event bindings</h3>
 * Bindings between commands and events are represented by an {@link javafx.scene.command.EventBinding}.
 * Since {@code EventBinding} implements the {@link javafx.event.EventHandler} interface, it can be assigned
 * to event handler properties like {@link javafx.scene.Node#onKeyPressedProperty() onKeyPressed} or
 * {@link javafx.scene.control.Button#onActionProperty() onAction}.
 * <p>
 * JFXcore comes with {@code EventBinding} implementations for the most commonly used event types:
 * <table border="1">
 *     <caption></caption>
 *     <tr><th>Event type</th><th>Event binding type</th></tr>
 *     <tr><td>{@link javafx.event.ActionEvent}</td><td>{@link javafx.scene.command.ActionEventBinding}</td></tr>
 *     <tr><td>{@link javafx.scene.input.KeyEvent}</td><td>{@link javafx.scene.command.KeyEventBinding}</td></tr>
 *     <tr><td>{@link javafx.scene.input.MouseEvent}</td><td>{@link javafx.scene.command.MouseEventBinding}</td></tr>
 *     <tr><td>{@link javafx.scene.input.TouchEvent}</td><td>{@link javafx.scene.command.TouchEventBinding}</td></tr>
 * </table>
 * When {@code EventBinding} receives an event, it calls the {@link javafx.scene.command.Command#execute(Object)}
 * method of the bound command with the value of its {@link javafx.scene.command.EventBinding#parameterProperty() parameter}
 * property as an argument. The {@code parameter} property can be used to pass a value from the invoker of the
 * command to the command implementation.
 *
 * <a id="DisabledState"></a>
 * <h3>2.2. How commands affect the <em>disabled</em> state of a node</h3>
 * When a command is bound to an event of a UI control using {@code EventBinding}, the control
 * is automatically {@link javafx.scene.Node#disabledProperty() disabled} when the command is
 * not {@link javafx.scene.command.Command#executableProperty() executable}.
 * When multiple commands are bound to the same control, the control will be disabled when any
 * of the bound commands are not executable.
 * <p>
 * When the command is an instance of {@link javafx.scene.command.AsyncCommand}, the control will
 * also be disabled while the operation is running (as indicated by {@code AsyncCommand}'s
 * {@link javafx.scene.command.AsyncCommand#executingProperty() executing} property), preventing
 * users from accidentally invoking a running command multiple times.
 * This behavior can be configured by setting the {@code EventBinding}'s
 * {@link javafx.scene.command.EventBinding#disabledWhenExecutingProperty() disabledWhenExecuting} property.
 *
 * <a id="AsyncCommand"></a>
 * <h2>3. Asynchronous commands</h2>
 * Commands that encapsulate asynchronous operations can be created by extending the
 * {@link javafx.scene.command.AsyncCommand} class.
 * <p>
 * JFXcore comes with two implementations of {@code AsyncCommand}:
 * <ul>
 *     <li>{@link javafx.scene.command.TaskCommand}, which creates and executes a {@link javafx.concurrent.Task}
 *         when the command is invoked
 *     <li>{@link javafx.scene.command.ServiceCommand}, which wraps a {@link javafx.concurrent.Service} instance
 * </ul>
 * In this example, {@code TaskCommand} is used to create a command that wraps a long-running asynchronous operation:
 * <pre>{@code
 *     AsyncCommand myAsyncCommand = new TaskCommand<>(() -> new Task<Void>() {
 *         @Override
 *         protected Void call() throws InterruptedException {
 *             System.out.println("Task started");
 *             Thread.sleep(1000);
 *             System.out.println("Task completed");
 *             return null;
 *         }
 *     });
 * }</pre>
 *
 * <a id="Cancellation"></a>
 * <h3>3.1. Cancelling a running operation</h3>
 * The {@link javafx.scene.command.AsyncCommand} class introduces the
 * {@link javafx.scene.command.AsyncCommand#cancel()} method, which can be used to cancel a running operation.
 * <p>
 * When {@link javafx.scene.command.EventBinding} receives an event that invokes its bound command, and
 * <ol>
 *     <li>if the operation is not running, {@code EventBinding} calls {@link javafx.scene.command.Command#execute(Object)}
 *         to start the operation;
 *     <li>if the operation is running, {@code EventBinding} calls {@link javafx.scene.command.AsyncCommand#cancel()}
 *         to cancel the operation.
 * </ol>
 * <p>
 * In this example, a user interface for a cancellable operation is implemented in two different ways:
 * <ol>
 *     <li><b>Using a single button to start and stop the operation</b>
 * <pre>{@code
 *     // Set disabledWhenExecuting to false, which keeps the button enabled when the command is running
 *     var binding = new ActionEventBinding(myAsyncCommand);
 *     binding.setDisabledWhenExecuting(false);
 *
 *     var button = new Button();
 *     button.setOnAction(binding);
 * }</pre>
 *     <li><b>Using separate buttons to start and stop the operation</b>
 * <pre>{@code
 *     // Create a button that invokes the command
 *     var startButton = new Button("Start");
 *     var startBinding = new ActionEventBinding(myAsyncCommand);
 *     startButton.setOnAction(startBinding);
 *
 *     // Create a button that cancels the command, and is only enabled when the command is running
 *     var cancelButton = new Button("Cancel");
 *     var cancelBinding = new ActionEventBinding(myAsyncCommand);
 *     cancelBinding.setDisabledWhenExecuting(false);
 *     cancelButton.setOnAction(cancelBinding);
 *     cancelButton.disableProperty().bind(myAsyncCommand.executingProperty().not());
 * }</pre>
 * </ol>
 *
 * <a id="ProgressReporting"></a>
 * <h3>3.2. Progress reporting for asynchronous operations</h3>
 * It is often desirable for asynchronous operations to report their progress back to the user interface.
 * For example, a user interface might show a progress bar to visualize the progress of a long-running operation.
 * <p>
 * The {@link javafx.scene.command.ProgressiveCommand} class extends {@link javafx.scene.command.AsyncCommand}
 * to support this scenario by adding the {@link javafx.scene.command.ProgressiveCommand#progressProperty() progress}
 * property.
 * <p>
 * Note: {@link javafx.scene.command.TaskCommand} and {@link javafx.scene.command.ServiceCommand} implement
 * the {@code ProgressiveCommand} class to support progress reporting.
 *
 * <a id="CommandBehaviors"></a>
 * <h2>4. Command behaviors</h2>
 * Commands can communicate their state back to the user interface in various ways. By default, a control will
 * be {@link javafx.scene.Node#disabledProperty() disabled} when one of its bound commands is either not executable
 * (as indicated by the {@link javafx.scene.command.Command#executableProperty() executable} property) or
 * currently executing (as indicated by the {@link javafx.scene.command.AsyncCommand#executingProperty() executing}
 * property). Behaviors such as these are called <em>command behaviors</em>.
 * <p>
 * Custom command behaviors can be implemented in two different ways, depending on the purpose of the behavior:
 * <ul>
 *     <li><b>Command-based behaviors</b> are implemented by overriding the command's
 *         {@link javafx.scene.command.Command#onAttached(javafx.scene.Node)} and
 *         {@link javafx.scene.command.Command#onDetached(javafx.scene.Node)} methods.
 *         Since the behavior is a part of the command itself, it generally applies to
 *         all controls to which the command is bound.
 *     <li><b>Control-based behaviors</b> are implemented as a {@link javafx.scene.command.CommandHandler}
 *         that is attached to a specific {@link javafx.scene.control.Control}.
 *         Since a control-based behavior only applies to a specific control, it allows
 *         more fine-grained customization when compared to a command-based behavior.
 * </ul>
 * While it is legal to have multiple behaviors affect a single control, care must be taken to ensure that
 * the different implementations do not interfere with each other. For example, when a command-based behavior
 * sets the control's {@link javafx.scene.control.Labeled#textProperty()}, no {@code CommandHandler} should
 * be added that also sets the same property.
 *
 * <a id="CommandBasedBehaviors"></a>
 * <h3>4.1. Command-based behavior</h3>
 * A command-based behavior is implemented by overriding the
 * {@link javafx.scene.command.Command#onAttached(javafx.scene.Node)} and
 * {@link javafx.scene.command.Command#onDetached(javafx.scene.Node)} methods.
 * <p>
 * In this example, the {@code MyLabeledCommand} class implements a behavior that binds the
 * {@link javafx.scene.control.Labeled#textProperty() text} and
 * {@link javafx.scene.control.Labeled#textProperty() graphic} properties of a {@link javafx.scene.control.Labeled}
 * control to corresponding properties of the command.
 *
 * <pre>{@code
 *     public class MyLabeledCommand extends RelayCommand<Void> {
 *         private final StringProperty text = new SimpleStringProperty(this, "text");
 *         public final StringProperty textProperty() { return text; }
 *
 *         private final StringProperty iconUrl = new SimpleStringProperty(this, "iconUrl");
 *         public final StringProperty iconUrlProperty() { return iconUrl; }
 *
 *         public MyLabeledCommand(Runnable execute) {
 *             super(execute);
 *         }
 *
 *         @Override
 *         protected void onAttached(Node node) {
 *             if (node instanceof Labeled labeled) {
 *                 labeled.textProperty().bind(text);
 *                 labeled.graphicProperty().bind(
 *                     Bindings.createObjectBinding(
 *                         () -> {
 *                             if (iconUrl.get() == null) return null;
 *                             return new ImageView(iconUrl.get());
 *                         },
 *                         iconUrl));
 *             }
 *         }
 *
 *         @Override
 *         protected void onDetached(Node node) {
 *             if (node instanceof Labeled labeled) {
 *                 labeled.textProperty().unbind();
 *                 labeled.graphicProperty().unbind();
 *             }
 *         }
 *     }
 * }</pre>
 *
 * <a id="ControlBasedBehaviors"></a>
 * <h3>4.2. Control-based behavior</h3>
 * A control-based behavior is created by implementing the {@link javafx.scene.command.CommandHandler} interface.
 * In this example, the {@code MyActivityBehavior} class adds an activity indicator graphic to a button,
 * visualizing a running {@link javafx.scene.command.AsyncCommand}.
 * <pre>{@code
 *     public class MyActivityBehavior implements CommandHandler {
 *         @Override
 *         public void onAttached(Node node, Command command) {
 *             if (node instanceof Labeled l && command instanceof AsyncCommand c) {
 *                 l.graphicProperty().bind(
 *                     Bindings.createObjectBinding(
 *                         () -> {
 *                             if (c.isExecuting())
 *                                 return new ImageView("activityIndicator.gif");
 *                             return null;
 *                         },
 *                         c.executingProperty()));
 *             }
 *         }
 *
 *         @Override
 *         public void onDetached(Node node, Command command) {
 *             if (node instanceof Labeled l) {
 *                 l.graphicProperty().unbind();
 *             }
 *         }
 *     }
 * }</pre>
 *
 * The previously created behavior can then be added to a control by setting the control's
 * {@link javafx.scene.control.Control#commandHandlerProperty() commandHandler} property:
 * <pre>{@code
 *     // Using the control-based behavior with a button:
 *     var button = new Button();
 *     button.setCommandHandler(new MyActivityBehavior());
 * }</pre>
 *
 * Similarly, the same effect can also be achieved by subclassing a control and adding the behavior in its
 * constructor. This approach is useful when the behavior implementation needs to access non-public methods
 * or fields of the control.
 * <p>
 * Note that in this example, the {@link javafx.scene.Node#addCommandHandler(javafx.scene.command.CommandHandler)}
 * API is used instead of setting the control's {@link javafx.scene.control.Control#commandHandlerProperty() commandHandler}
 * property. This keeps the behavior an implementation detail of the control, and doesn't leak it to the
 * outside world.
 * <pre>{@code
 *     public class MyActivityButton extends Button {
 *         public MyActivityButton() {
 *             addCommandHandler(new MyActivityBehavior());
 *         }
 *     }
 * }</pre>
 *
 * As another option, the behavior can be added to a custom {@link javafx.scene.control.Skin Skin}.
 * This approach has the advantage of being able to use CSS to apply behavioral skins to controls.
 * As before, note the use of the {@link javafx.scene.Node#addCommandHandler(javafx.scene.command.CommandHandler)}
 * and {@link javafx.scene.Node#removeCommandHandler(javafx.scene.command.CommandHandler)} APIs:
 * <pre>{@code
 *     public class MyActivitySkin extends ButtonSkin {
 *         private final CommandHandler behavior = new MyActivityBehavior();
 *
 *         public MyActivitySkin(Button control) {
 *             super(control);
 *             control.addCommandHandler(behavior);
 *         }
 *
 *         @Override
 *         public void dispose() {
 *             getSkinnable().removeCommandHandler(behavior);
 *             super.dispose();
 *         }
 *     }
 * }</pre>
 *
 */
package javafx.scene.command;
