package com.clicktron;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public class Model {

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    // Sets if the loop will not end until it is stopped by user
    private boolean loopInfinite;
    public boolean isLoopInfinite() {return loopInfinite;}
    public void setLoopInfinite(boolean loopInfinite) { this.loopInfinite = loopInfinite; }

    // Sets number of iteration in a finite loop
    private int loopCount;
    public int getLoopCount() {return loopCount;}
    public void setLoopCount(int loopCount) { this.loopCount = loopCount; }

    // Sets interval time in milliseconds between each iteration
    private int interval;
    public int getInterval() {return interval;}
    public void setInterval(int interval) { this.interval = interval; }

    // Sets consecutive press count for the button
    private int pressCount;
    public int getPressCount() {return pressCount;}
    public void setPressCount(int pressCount) { this.pressCount = pressCount; }

    // Sets if the cursor position is predetermined by user
    private boolean cursorCurrent;
    public boolean isCursorCurrent() {return cursorCurrent;}
    public void setCursorCurrent(boolean cursorCurrent) { this.cursorCurrent = cursorCurrent; }

    // Sets custom cursor position
    private int[] customCursorPos = new int[2];
    public int getCustomCursorPos(int index) {return customCursorPos[index];}
    public void setCustomCursorPos(int x, int y) {
        this.customCursorPos[0] = x;
        this.customCursorPos[1] = y;
    }

    // Controller can assign a function to be called whenever the loop is ended
    public Runnable onLoopEnded;

    // Button storage and converter
    public ButtonStorage buttonStorage;

    // Private fields
    private Thread outerThread;
    private Robot robot;

    /***************************************************************************
     *                                                                         *
     * Methods                                                                 *
     *                                                                         *
     **************************************************************************/

    // Constructor
    public Model() {
        buttonStorage = new ButtonStorage();
        try {
            // The robot does not catch exception while working thus leave that job to Thread.sleep
            robot = new Robot() {
                @Override
                public synchronized void delay(int ms) { return; }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The loop to press mouse or keyboard button repeatedly
    private Runnable outerLoop = () -> {
        for (int i = 0; i < getLoopCount(); i++) {
            if (isLoopInfinite()) { i = 0; }
            try {
                Thread.sleep(getInterval());
            } catch (InterruptedException e) {
                break;
            }
            for (int j = 0; j < getPressCount(); j++) {
                if (!isCursorCurrent()) {
                    robot.mouseMove(getCustomCursorPos(0), getCustomCursorPos(1));
                }
                if (this.buttonStorage.hasKeyCode) {
                    int keyCode = this.buttonStorage.getKeyCode();
                    if (keyCode != 0) {
                        robot.keyPress(keyCode);
                        robot.keyRelease(keyCode);
                    }
                } else {
                    robot.mousePress(this.buttonStorage.getAwtMouseValue());
                    robot.mouseRelease(this.buttonStorage.getAwtMouseValue());
                }
            }
            System.out.println(i + 1 + ". step is completed!");
        }
        if (onLoopEnded != null) onLoopEnded.run();
    };

    // Start the loop
    public void startLoop() {
        outerThread = new Thread(outerLoop);
        outerThread.start();
    }

    // Stop the loop
    public void stopLoop() {
        outerThread.interrupt();
    }

    /***************************************************************************
     *                                                                         *
     * Inner Classes                                                           *
     *                                                                         *
     **************************************************************************/

    // Stores mouse and keyboard button information
    // Converts javaFX keyboard keys to AWT keys (Not required in JavaFX 11 due to javafx.scene.robot)
    public class ButtonStorage {
        private int awtKeyValue;
        private int awtMouseValue;
        private boolean hasKeyCode;

        private int generateAwtCode(MouseButton fxMouseButton) {
            if (fxMouseButton == MouseButton.PRIMARY) return InputEvent.BUTTON1_DOWN_MASK;
            else if (fxMouseButton == MouseButton.MIDDLE) return InputEvent.BUTTON2_DOWN_MASK;
            else if (fxMouseButton == MouseButton.SECONDARY) return InputEvent.BUTTON3_DOWN_MASK;
            return -1;
        }

        private int generateAwtCode(KeyCode fxKeyCode) {
            int finalCode = 0;
            String awtString = fxKeyCode.toString();

            // Turn DIGIT[number] to [number]
            awtString = awtString.replaceAll("DIGIT","");

            // Turn CAPS into CAPS_LOCK
            if (awtString.equals("CAPS")) awtString = "CAPS_LOCK";

            // Add VK_ prefix
            awtString = "VK_" + awtString;
            try {
                finalCode = KeyEvent.class.getField(awtString).getInt(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return finalCode;
        }

        public int getAwtMouseValue() { return this.awtMouseValue; }

        public int getKeyCode() { return this.awtKeyValue; }

        public void setButton(MouseButton fxMouseButton) {
            this.hasKeyCode = false;
            this.awtMouseValue = generateAwtCode(fxMouseButton);
        }

        public void setButton(KeyCode fxKeyCode) {
            this.hasKeyCode = true;
            awtKeyValue = generateAwtCode(fxKeyCode);
        }
    }
}
