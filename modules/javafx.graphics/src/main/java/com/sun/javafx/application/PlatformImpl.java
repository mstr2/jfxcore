/*
 * Copyright (c) 2010, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx.application;

import static com.sun.javafx.FXPermissions.CREATE_TRANSPARENT_WINDOW_PERMISSION;

import com.sun.glass.ui.PlatformFactory;
import com.sun.javafx.PlatformUtil;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.tk.TKListener;
import com.sun.javafx.tk.TKStage;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Logging;
import com.sun.javafx.util.ModuleHelper;

import java.lang.module.ModuleDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.PlatformPreferences;
import javafx.application.PlatformPreferencesListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.StyleTheme;
import javafx.scene.paint.Color;
import javafx.util.FXPermission;

public class PlatformImpl {

    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static AtomicBoolean platformExit = new AtomicBoolean(false);
    private static AtomicBoolean toolkitExit = new AtomicBoolean(false);
    private static CountDownLatch startupLatch = new CountDownLatch(1);
    private static AtomicBoolean listenersRegistered = new AtomicBoolean(false);
    private static TKListener toolkitListener = null;
    private static volatile boolean implicitExit = true;
    private static boolean taskbarApplication = true;
    private static boolean contextual2DNavigation;
    private static AtomicInteger pendingRunnables = new AtomicInteger(0);
    private static AtomicInteger numWindows = new AtomicInteger(0);
    private static volatile boolean firstWindowShown = false;
    private static volatile boolean lastWindowClosed = false;
    private static AtomicBoolean reallyIdle = new AtomicBoolean(false);
    private static Set<FinishListener> finishListeners =
            new CopyOnWriteArraySet<FinishListener>();
    private final static Object runLaterLock = new Object();
    private static Boolean isGraphicsSupported;
    private static Boolean isControlsSupported;
    private static Boolean isMediaSupported;
    private static Boolean isWebSupported;
    private static Boolean isSWTSupported;
    private static Boolean isSwingSupported;
    private static Boolean isFXMLSupported;
    private static Boolean hasTwoLevelFocus;
    private static Boolean hasVirtualKeyboard;
    private static Boolean hasTouch;
    private static Boolean hasMultiTouch;
    private static Boolean hasPointer;
    private static boolean isThreadMerged = false;
    private static String applicationType = "";
    private static BooleanProperty accessibilityActive = new SimpleBooleanProperty();
    private static CountDownLatch allNestedLoopsExitedLatch = new CountDownLatch(1);

    @SuppressWarnings("removal")
    private static final boolean verbose
            = AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                Boolean.getBoolean("javafx.verbose"));

    @SuppressWarnings("removal")
    private static final boolean DEBUG
            = AccessController.doPrivileged((PrivilegedAction<Boolean>) ()
                    -> Boolean.getBoolean("com.sun.javafx.application.debug"));

    // Internal permission used by FXCanvas (SWT interop)
    private static final FXPermission FXCANVAS_PERMISSION =
            new FXPermission("accessFXCanvasInternals");

    /**
     * Set a flag indicating whether this application should show up in the
     * task bar. The default value is true.
     *
     * @param taskbarApplication the new value of this attribute
     */
    public static void setTaskbarApplication(boolean taskbarApplication) {
        PlatformImpl.taskbarApplication = taskbarApplication;
    }

    /**
     * Returns the current value of the taskBarApplication flag.
     *
     * @return the current state of the flag.
     */
    public static boolean isTaskbarApplication() {
        return taskbarApplication;
    }

    /**
     * Sets the name of the this application based on the Application class.
     * This method is called by the launcher, and is not
     * called from the FX Application Thread, so we need to do it in a runLater.
     * We do not need to wait for the result since it will complete before the
     * Application start() method is called regardless.
     *
     * @param appClass the Application class.
     */
    public static void setApplicationName(final Class appClass) {
        runLater(() -> com.sun.glass.ui.Application.GetApplication().setName(appClass.getName()));
    }

    /**
     * Return whether or not focus navigation between controls is context-
     * sensitive.
     * @return true if the context-sensitive algorithm for focus navigation is
     * used
     */
     public static boolean isContextual2DNavigation() {
         return contextual2DNavigation;
     }

    /**
     * This method is invoked typically on the main thread. At this point,
     * the JavaFX Application Thread has not been started. Any attempt
     * to call startup more than once results in all subsequent calls turning into
     * nothing more than a runLater call with the provided Runnable being called.
     * @param r
     */
    public static void startup(final Runnable r) {
        startup(r, false);
    }

    /**
     * This method is invoked typically on the main thread. At this point,
     * the JavaFX Application Thread has not been started. If preventDuplicateCalls
     * is true, calling this method multiple times will result in an
     * IllegalStateException. If it is false, calling this method multiple times
     * will result in all subsequent calls turning into
     * nothing more than a runLater call with the provided Runnable being called.
     * @param r
     * @param preventDuplicateCalls
     */
    public static void startup(final Runnable r, boolean preventDuplicateCalls) {

        // NOTE: if we ever support re-launching an application and/or
        // launching a second application in the same VM/classloader
        // this will need to be changed.
        if (platformExit.get()) {
            throw new IllegalStateException("Platform.exit has been called");
        }

        if (initialized.getAndSet(true)) {
            if (preventDuplicateCalls) {
                throw new IllegalStateException("Toolkit already initialized");
            }

            // If we've already initialized, just put the runnable on the queue.
            runLater(r);
            return;
        }

        final Module module = PlatformImpl.class.getModule();
        final ModuleDescriptor moduleDesc = module.getDescriptor();
        if (!module.isNamed()
                || !"javafx.graphics".equals(module.getName())
                || moduleDesc == null
                || moduleDesc.isAutomatic()
                || moduleDesc.isOpen()) {

            String warningStr = "Unsupported JavaFX configuration: "
                + "classes were loaded from '" + module + "'";
            if (moduleDesc != null) {
                warningStr += ", isAutomatic: " + moduleDesc.isAutomatic();
                warningStr += ", isOpen: " + moduleDesc.isOpen();
            }
            Logging.getJavaFXLogger().warning(warningStr);
        }

        @SuppressWarnings("removal")
        var dummy = AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            applicationType = System.getProperty("com.sun.javafx.application.type");
            if (applicationType == null) applicationType = "";

            contextual2DNavigation = Boolean.getBoolean(
                    "com.sun.javafx.isContextual2DNavigation");
            String s = System.getProperty("com.sun.javafx.twoLevelFocus");
            if (s != null) {
                hasTwoLevelFocus = Boolean.valueOf(s);
            }
            s = System.getProperty("com.sun.javafx.virtualKeyboard");
            if (s != null) {
                if (s.equalsIgnoreCase("none")) {
                    hasVirtualKeyboard = false;
                } else if (s.equalsIgnoreCase("javafx")) {
                    hasVirtualKeyboard = true;
                } else if (s.equalsIgnoreCase("native")) {
                    hasVirtualKeyboard = true;
                }
            }
            s = System.getProperty("com.sun.javafx.touch");
            if (s != null) {
                hasTouch = Boolean.valueOf(s);
            }
            s = System.getProperty("com.sun.javafx.multiTouch");
            if (s != null) {
                hasMultiTouch = Boolean.valueOf(s);
            }
            s = System.getProperty("com.sun.javafx.pointer");
            if (s != null) {
                hasPointer = Boolean.valueOf(s);
            }
            s = System.getProperty("javafx.embed.singleThread");
            if (s != null) {
                isThreadMerged = Boolean.valueOf(s);
                if (isThreadMerged && !isSupported(ConditionalFeature.SWING)) {
                    isThreadMerged = false;
                    if (verbose) {
                        System.err.println(
                        "WARNING: javafx.embed.singleThread ignored (javafx.swing module not found)");
                    }
                }
            }
            return null;
        });

        if (DEBUG) {
            System.err.println("PlatformImpl::startup : applicationType = "
                    + applicationType);
        }
        if ("FXCanvas".equals(applicationType)) {
            initFXCanvas();
        }

        if (!taskbarApplication) {
            @SuppressWarnings("removal")
            var dummy2 = AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                System.setProperty("glass.taskbarApplication", "false");
                return null;
            });
        }

        // Create Toolkit listener and register it with the Toolkit.
        // Call notifyFinishListeners when we get notified.
        toolkitListener = new TKListener() {
            @Override public void changedTopLevelWindows(List<TKStage> windows) {
                numWindows.set(windows.size());
                checkIdle();
            }

            @Override
            public void exitedLastNestedLoop() {
                if (platformExit.get()) {
                    allNestedLoopsExitedLatch.countDown();
                }
                checkIdle();
            }
        };
        Toolkit.getToolkit().addTkListener(toolkitListener);

        Toolkit.getToolkit().startup(() -> {
            startupLatch.countDown();
            r.run();
        });

        //Initialize the thread merging mechanism
        if (isThreadMerged) {
            installFwEventQueue();
        }
    }

    // Pass certain system properties to glass via the device details Map
    private static void initDeviceDetailsFXCanvas() {
        // Read the javafx.embed.eventProc system property and store
        // it in an entry in the glass Application device details map
        final String eventProcProperty = "javafx.embed.eventProc";
        @SuppressWarnings("removal")
        final long eventProc = AccessController.doPrivileged((PrivilegedAction<Long>) () ->
                Long.getLong(eventProcProperty, 0));
        if (eventProc != 0L) {
            // Set the value for the javafx.embed.eventProc
            // key in the glass Application map
            Map map = com.sun.glass.ui.Application.getDeviceDetails();
            if (map == null) {
                map = new HashMap();
                com.sun.glass.ui.Application.setDeviceDetails(map);
            }
            if (map.get(eventProcProperty) == null) {
                map.put(eventProcProperty, eventProc);
            }
        }
    }

    // Add the necessary qualified exports to the calling module
    private static void addExportsToFXCanvas(Class<?> fxCanvasClass) {
        final String[] swtNeededPackages = {
            "com.sun.glass.ui",
            "com.sun.javafx.cursor",
            "com.sun.javafx.embed",
            "com.sun.javafx.stage",
            "com.sun.javafx.tk"
        };

        if (DEBUG) {
            System.err.println("addExportsToFXCanvas: class = " + fxCanvasClass);
        }
        Object thisModule = ModuleHelper.getModule(PlatformImpl.class);
        Object javafxSwtModule = ModuleHelper.getModule(fxCanvasClass);
        for (String pkg : swtNeededPackages) {
            if (DEBUG) {
                System.err.println("add export of " + pkg + " from "
                        + thisModule + " to " + javafxSwtModule);
            }
            ModuleHelper.addExports(thisModule, pkg, javafxSwtModule);
        }
    }

    // FXCanvas-specific initialization
    private static void initFXCanvas() {
        // Verify that we have the appropriate permission
        @SuppressWarnings("removal")
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkPermission(FXCANVAS_PERMISSION);
            } catch (SecurityException ex) {
                System.err.println("FXCanvas: no permission to access JavaFX internals");
                ex.printStackTrace();
                return;
            }
        }

        // Find the calling class, ignoring any stack frames from FX application classes
        Predicate<StackWalker.StackFrame> classFilter = f ->
                !f.getClassName().startsWith("javafx.application.")
                        && !f.getClassName().startsWith("com.sun.javafx.application.");

        @SuppressWarnings("removal")
        final StackWalker walker = AccessController.doPrivileged((PrivilegedAction<StackWalker>) () ->
                StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE));
        Optional<StackWalker.StackFrame> frame = walker.walk(
                s -> s.filter(classFilter).findFirst());

        if (frame.isPresent()) {
            Class<?> caller = frame.get().getDeclaringClass();
            if (DEBUG) {
                System.err.println("callerClassName = " + caller);
            }

            // Verify that the caller is javafx.embed.swt.FXCanvas
            if ("javafx.embed.swt.FXCanvas".equals(caller.getName())) {
                initDeviceDetailsFXCanvas();
                addExportsToFXCanvas(caller);
            }
        }
    }

    private static void installFwEventQueue() {
        invokeSwingFXUtilsMethod("installFwEventQueue");
    }

    private static void removeFwEventQueue() {
        invokeSwingFXUtilsMethod("removeFwEventQueue");
    }

    private static void invokeSwingFXUtilsMethod(final String methodName) {
        //Use reflection in case we are running compact profile
        try {
            Class swingFXUtilsClass = Class.forName("com.sun.javafx.embed.swing.SwingFXUtilsImpl");
            Method installFwEventQueue = swingFXUtilsClass.getDeclaredMethod(methodName);

            waitForStart();
            installFwEventQueue.invoke(null);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Property javafx.embed.singleThread is not supported");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void waitForStart() {
        // If the startup runnable has not yet been called, then wait it.
        // Note that we check the count before calling await() to avoid
        // the try/catch which is unnecessary after startup.
        if (startupLatch.getCount() > 0) {
            try {
                startupLatch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean isFxApplicationThread() {
        return Toolkit.getToolkit().isFxUserThread();
    }

    public static void runLater(final Runnable r) {
        runLater(r, false);
    }

    private static void runLater(final Runnable r, boolean exiting) {
        if (!initialized.get()) {
            throw new IllegalStateException("Toolkit not initialized");
        }

        pendingRunnables.incrementAndGet();
        waitForStart();

        synchronized (runLaterLock) {
            if (!exiting && toolkitExit.get()) {
                // Don't schedule a runnable after we have exited the toolkit
                pendingRunnables.decrementAndGet();
                return;
            }

            @SuppressWarnings("removal")
            final AccessControlContext acc = AccessController.getContext();
            // Don't catch exceptions, they are handled by Toolkit.defer()
            Toolkit.getToolkit().defer(() -> {
                try {
                    @SuppressWarnings("removal")
                    var dummy = AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                        r.run();
                        return null;
                    }, acc);
                } finally {
                    pendingRunnables.decrementAndGet();
                    checkIdle();
                }
            });
        }
    }

    public static void runAndWait(final Runnable r) {
        runAndWait(r, false);
    }

    private static void runAndWait(final Runnable r, boolean exiting) {
        if (isFxApplicationThread()) {
             try {
                 r.run();
             } catch (Throwable t) {
                 System.err.println("Exception in runnable");
                 t.printStackTrace();
             }
        } else {
            final CountDownLatch doneLatch = new CountDownLatch(1);
            runLater(() -> {
                try {
                    r.run();
                } finally {
                    doneLatch.countDown();
                }
            }, exiting);

            if (!exiting && toolkitExit.get()) {
                throw new IllegalStateException("Toolkit has exited");
            }

            try {
                doneLatch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void setImplicitExit(boolean implicitExit) {
        PlatformImpl.implicitExit = implicitExit;
        checkIdle();
    }

    public static boolean isImplicitExit() {
        return implicitExit;
    }

    public static void addListener(FinishListener l) {
        listenersRegistered.set(true);
        finishListeners.add(l);
    }

    public static void removeListener(FinishListener l) {
        finishListeners.remove(l);
        listenersRegistered.set(!finishListeners.isEmpty());
        if (!listenersRegistered.get()) {
            checkIdle();
        }
    }

    private static void notifyFinishListeners(boolean exitCalled) {
        // Notify listeners if any are registered, else exit directly
        if (listenersRegistered.get()) {
            for (FinishListener l : finishListeners) {
                if (exitCalled) {
                    l.exitCalled();
                } else {
                    l.idle(implicitExit);
                }
            }
        } else if (implicitExit || platformExit.get()) {
            tkExit();
        }
    }

    // Check for idle, meaning the last top-level window has been closed and
    // there are no pending Runnables waiting to be run.
    private static void checkIdle() {
        // If we aren't initialized yet, then this method is a no-op.
        if (!initialized.get()) {
            return;
        }

        if (!isFxApplicationThread()) {
            // Add a dummy runnable to the runLater queue, which will then call
            // checkIdle() on the FX application thread.
            runLater(() -> {
            });
            return;
        }

        boolean doNotify = false;

        synchronized (PlatformImpl.class) {
            int numWin = numWindows.get();
            if (numWin > 0) {
                firstWindowShown = true;
                lastWindowClosed = false;
                reallyIdle.set(false);
            } else if (numWin == 0 && firstWindowShown) {
                lastWindowClosed = true;
            }

            // In case there is an event in process, allow for it to show
            // another window. If no new window is shown before all pending
            // runnables (including this one) are done and there is no running
            // nested loops, then we will shutdown.
            if (lastWindowClosed && pendingRunnables.get() == 0
                    && (toolkitExit.get() || !Toolkit.getToolkit().isNestedLoopRunning())) {
//                System.err.println("Last window closed and no pending runnables");
                if (reallyIdle.getAndSet(true)) {
//                    System.err.println("Really idle now");
                    doNotify = true;
                    lastWindowClosed = false;
                } else {
//                    System.err.println("Queuing up a dummy idle check runnable");
                    runLater(() -> {
//                            System.err.println("Dummy runnable");
                    });
                }
            }
        }

        if (doNotify) {
            notifyFinishListeners(false);
        }
    }

    // package scope method for testing
    private static final CountDownLatch platformExitLatch = new CountDownLatch(1);
    static CountDownLatch test_getPlatformExitLatch() {
        return platformExitLatch;
    }

    public static void tkExit() {
        if (toolkitExit.getAndSet(true)) {
            return;
        }

        if (initialized.get()) {
            if (platformExit.get()) {
                PlatformImpl.runAndWait(() -> {
                    if (Toolkit.getToolkit().isNestedLoopRunning()) {
                        Toolkit.getToolkit().exitAllNestedEventLoops();
                    } else {
                        allNestedLoopsExitedLatch.countDown();
                    }
                }, true);

                try {
                    allNestedLoopsExitedLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Could not exit all nested event loops");
                }
            }

            // Always call toolkit exit on FX app thread
//            System.err.println("PlatformImpl.tkExit: scheduling Toolkit.exit");
            PlatformImpl.runAndWait(() -> {
//                System.err.println("PlatformImpl.tkExit: calling Toolkit.exit");
                Toolkit.getToolkit().exit();
            }, true);

            if (isThreadMerged) {
                removeFwEventQueue();
            }

            Toolkit.getToolkit().removeTkListener(toolkitListener);
            toolkitListener = null;
            platformExitLatch.countDown();
        }
    }

    public static BooleanProperty accessibilityActiveProperty() {
        return accessibilityActive;
    }

    public static void exit() {
        platformExit.set(true);
        notifyFinishListeners(true);
    }

    private static Boolean checkForClass(String classname) {
        try {
            Class.forName(classname, false, PlatformImpl.class.getClassLoader());
            return Boolean.TRUE;
        } catch (ClassNotFoundException cnfe) {
            return Boolean.FALSE;
        }
    }

    public static boolean isSupported(ConditionalFeature feature) {
        final boolean supported = isSupportedImpl(feature);
        if (supported && (feature == ConditionalFeature.TRANSPARENT_WINDOW)) {
            // some features require the application to have the corresponding
            // permissions, if the application doesn't have them, the platform
            // will behave as if the feature wasn't supported
            @SuppressWarnings("removal")
            final SecurityManager securityManager =
                    System.getSecurityManager();
            if (securityManager != null) {
                try {
                    securityManager.checkPermission(CREATE_TRANSPARENT_WINDOW_PERMISSION);
                } catch (final SecurityException e) {
                    return false;
                }
            }

            return true;
        }

        return supported;
   }

    public static interface FinishListener {
        public void idle(boolean implicitExit);
        public void exitCalled();
    }

    /**
     * Ensures that the default theme is loaded if no theme is set at this point.
     */
    public static void ensureDefaultTheme() {
        if (isFxApplicationThread()) {
            if (platformTheme.get() == null) {
                platformTheme.set(newThemeInstance(MODENA_THEME));
            }
        } else {
            runLater(PlatformImpl::ensureDefaultTheme);
        }
    }

    private static final String CASPIAN_THEME = "javafx.scene.control.theme.CaspianTheme";
    private static final String MODENA_THEME = "javafx.scene.control.theme.ModenaTheme";

    private static boolean isModena = false;
    private static boolean isCaspian = false;

    /**
     * Current Platform User Agent Stylesheet is Modena.
     *
     * Note: Please think hard before using this as we really want to avoid special cases in the platform for specific
     * themes. This was added to allow tempory work arounds in the platform for bugs.
     *
     * @return true if using modena stylesheet
     */
    public static boolean isModena() {
        return isModena;
    }

    /**
     * Current Platform User Agent Stylesheet is Caspian.
     *
     * Note: Please think hard before using this as we really want to avoid special cases in the platform for specific
     * themes. This was added to allow tempory work arounds in the platform for bugs.
     *
     * @return true if using caspian stylesheet
     */
    public static boolean isCaspian() {
        return isCaspian;
    }

    private static StyleTheme newThemeInstance(String className) {
        try {
            Class<?> themeClass;

            try {
                themeClass = Class.forName(className, false, PlatformImpl.class.getClassLoader());
            } catch (ClassNotFoundException ex) {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null) {
                    themeClass = Class.forName(className, false, contextClassLoader);
                } else {
                    throw ex;
                }
            }

            return (StyleTheme)themeClass.getConstructor().newInstance();
        } catch (Throwable ex) {
            Logging.getJavaFXLogger().severe("Cannot instantiate " + className, ex);
            return null;
        }
    }

    private static final StringProperty platformUserAgentStylesheet = new SimpleStringProperty() {
        @Override
        protected void invalidated() {
            if (Application.STYLESHEET_CASPIAN.equals(get())) {
                platformTheme.set(newThemeInstance(CASPIAN_THEME));
            } else if (Application.STYLESHEET_MODENA.equals(get())) {
                platformTheme.set(newThemeInstance(MODENA_THEME));
            } else {
                onPlatformThemeChanged(get(), platformTheme.get());
            }
        }
    };

    public static StringProperty platformUserAgentStylesheetProperty() {
        return platformUserAgentStylesheet;
    }

    private static final ObjectProperty<StyleTheme> platformTheme = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            if (isCaspian && Application.STYLESHEET_CASPIAN.equals(platformUserAgentStylesheet.get())) {
                platformUserAgentStylesheet.set(null);
            } else if (isModena && Application.STYLESHEET_MODENA.equals(platformUserAgentStylesheet.get())) {
                platformUserAgentStylesheet.set(null);
            } else {
                String userAgentStylesheet = platformUserAgentStylesheet.get();
                if (userAgentStylesheet != null) {
                    userAgentStylesheet = switch (userAgentStylesheet) {
                        case Application.STYLESHEET_CASPIAN, Application.STYLESHEET_MODENA -> null;
                        default -> userAgentStylesheet;
                    };
                }

                onPlatformThemeChanged(userAgentStylesheet, get());
            }
        }
    };

    public static ObjectProperty<StyleTheme> platformThemeProperty() {
        return platformTheme;
    }

    private static void onPlatformThemeChanged(String userAgentStylesheet, StyleTheme theme) {
        String overrideStylesheetUrl = System.getProperty("javafx.userAgentStylesheetUrl");
        if (overrideStylesheetUrl != null) {
            userAgentStylesheet = overrideStylesheetUrl.trim();
        } else if (userAgentStylesheet != null) {
            userAgentStylesheet = userAgentStylesheet.trim();
        }

        String themeName = theme != null ? theme.getClass().getName() : null;
        isCaspian = themeName != null && themeName.equals(CASPIAN_THEME);
        isModena = themeName != null && themeName.equals(MODENA_THEME);

        updateStyleManager(userAgentStylesheet, theme != null ? theme.getStylesheets() : null);
    }

    private static List<String> themeStylesheets;

    private static final ListChangeListener<String> themeStylesheetsChanged =
        change -> updateStyleManager(platformUserAgentStylesheet.get(), themeStylesheets);

    private static void updateStyleManager(String userAgentStylesheet, List<String> stylesheets) {
        if (themeStylesheets instanceof ObservableList<String> list) {
            list.removeListener(themeStylesheetsChanged);
        }

        themeStylesheets = stylesheets;

        if (themeStylesheets instanceof ObservableList<String> list) {
            list.addListener(themeStylesheetsChanged);
        }

        boolean hasUserAgentStylesheet = userAgentStylesheet != null && !userAgentStylesheet.isEmpty();

        if (hasUserAgentStylesheet && themeStylesheets != null) {
            List<String> list = new ArrayList<>(themeStylesheets.size() + 1);
            list.add(userAgentStylesheet);
            list.addAll(themeStylesheets);
            StyleManager.getInstance().setUserAgentStylesheets(list);
        } else if (themeStylesheets != null) {
            StyleManager.getInstance().setUserAgentStylesheets(themeStylesheets);
        } else if (hasUserAgentStylesheet) {
            StyleManager.getInstance().setUserAgentStylesheets(List.of(userAgentStylesheet));
        } else {
            StyleManager.getInstance().setUserAgentStylesheets(List.of());
        }
    }

    private static boolean isSupportedImpl(ConditionalFeature feature) {
        switch (feature) {
            case GRAPHICS:
                if (isGraphicsSupported == null) {
                    isGraphicsSupported = checkForClass("javafx.stage.Stage");
                }
                return isGraphicsSupported;
            case CONTROLS:
                if (isControlsSupported == null) {
                    isControlsSupported = checkForClass(
                            "javafx.scene.control.Control");
                }
                return isControlsSupported;
            case MEDIA:
                if (isMediaSupported == null) {
                    isMediaSupported = checkForClass(
                            "javafx.scene.media.MediaView");
                    if (isMediaSupported && PlatformUtil.isEmbedded()) {
                        @SuppressWarnings("removal")
                        var dummy = AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                            String s = System.getProperty(
                                    "com.sun.javafx.experimental.embedded.media",
                                    "false");
                            isMediaSupported = Boolean.valueOf(s);
                            return null;

                        });
                    }
                }
                return isMediaSupported;
            case WEB:
                if (isWebSupported == null) {
                    isWebSupported = checkForClass("javafx.scene.web.WebView");
                    if (isWebSupported && PlatformUtil.isEmbedded()) {
                        @SuppressWarnings("removal")
                        var dummy = AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                            String s = System.getProperty(
                                    "com.sun.javafx.experimental.embedded.web",
                                    "false");
                            isWebSupported = Boolean.valueOf(s);
                            return null;

                        });
                    }
                }
                return isWebSupported;
            case SWT:
                if (isSWTSupported == null) {
                    isSWTSupported = checkForClass("javafx.embed.swt.FXCanvas");
                }
                return isSWTSupported;
            case SWING:
                if (isSwingSupported == null) {
                    isSwingSupported =
                        // check for JComponent first, it may not be present
                        checkForClass("javax.swing.JComponent") &&
                        checkForClass("javafx.embed.swing.JFXPanel");
                }
                return isSwingSupported;
            case FXML:
                if (isFXMLSupported == null) {
                    isFXMLSupported = checkForClass("javafx.fxml.FXMLLoader")
                            && checkForClass("javax.xml.stream.XMLInputFactory");
                }
                return isFXMLSupported;
            case TWO_LEVEL_FOCUS:
                if (hasTwoLevelFocus == null) {
                    return Toolkit.getToolkit().isSupported(feature);
                }
                return hasTwoLevelFocus;
            case VIRTUAL_KEYBOARD:
                if (hasVirtualKeyboard == null) {
                    return Toolkit.getToolkit().isSupported(feature);
                }
                return hasVirtualKeyboard;
            case INPUT_TOUCH:
                if (hasTouch == null) {
                    return Toolkit.getToolkit().isSupported(feature);
                }
                return hasTouch;
            case INPUT_MULTITOUCH:
                if (hasMultiTouch == null) {
                    return Toolkit.getToolkit().isSupported(feature);
                }
                return hasMultiTouch;
            case INPUT_POINTER:
                if (hasPointer == null) {
                    return Toolkit.getToolkit().isSupported(feature);
                }
                return hasPointer;
            default:
                return Toolkit.getToolkit().isSupported(feature);
        }
    }

    public static class PlatformPreferencesImpl extends AbstractMap<String, Object> implements PlatformPreferences {
        private final Map<String, Object> modifiableMap = new HashMap<>(PlatformFactory.getPlatformFactory().getPreferences());
        private final Set<Entry<String, Object>> unmodifiableEntrySet = Collections.unmodifiableSet(modifiableMap.entrySet());
        private final List<PlatformPreferencesListener> listeners = new ArrayList<>();

        public Map<String, Object> getModifiableMap() {
            return modifiableMap;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return unmodifiableEntrySet;
        }

        @Override
        public String getString(String key) {
            Object value = modifiableMap.get(key);
            if (value instanceof String s) {
                return s;
            }

            return null;
        }

        @Override
        public String getString(String key, String fallbackValue) {
            String value = getString(key);
            return value != null ? value : fallbackValue;
        }

        @Override
        public Boolean getBoolean(String key) {
            Object value = modifiableMap.get(key);
            if (value instanceof Boolean b) {
                return b;
            }

            return null;
        }

        @Override
        public boolean getBoolean(String key, boolean fallbackValue) {
            Boolean value = getBoolean(key);
            return value != null ? value : fallbackValue;
        }

        @Override
        public Color getColor(String key) {
            Object value = modifiableMap.get(key);
            if (value instanceof Color c) {
                return c;
            }

            return null;
        }

        @Override
        public Color getColor(String key, Color fallbackValue) {
            Color value = getColor(key);
            return value != null ? value : fallbackValue;
        }

        @Override
        public synchronized void addListener(PlatformPreferencesListener listener) {
            listeners.add(listener);
        }

        @Override
        public synchronized void removeListener(PlatformPreferencesListener listener) {
            listeners.remove(listener);
        }

        synchronized void firePreferencesChanged(Map<String, Object> changed) {
            for (PlatformPreferencesListener listener : listeners) {
                listener.onPreferencesChanged(this, changed);
            }
        }
    }

    private static final PlatformPreferencesImpl platformPreferences = new PlatformPreferencesImpl();

    public static void updatePreferences(Map<String, Object> newPreferences) {
        Map<String, Object> changed = new HashMap<>();
        for (Map.Entry<String, Object> entry : newPreferences.entrySet()) {
            Object existingValue = platformPreferences.getModifiableMap().get(entry.getKey());
            if (!Objects.equals(existingValue, entry.getValue())) {
                changed.put(entry.getKey(), entry.getValue());
            }
        }

        if (!changed.isEmpty()) {
            platformPreferences.getModifiableMap().putAll(changed);
            platformPreferences.firePreferencesChanged(changed);
        }
    }

    public static PlatformPreferences getPlatformPreferences() {
        return platformPreferences;
    }

}
