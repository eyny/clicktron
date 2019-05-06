package com.clicktron;

import java.util.HashMap;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;

public class Controller {

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    @FXML Button stopButton, startButton;
    @FXML Spinner<Integer> hrSpinner, minSpinner, secSpinner, msSpinner;
    @FXML Label hyperlink;
    @FXML VBox tabPane1, tabPane2;
    @FXML AnchorPane tabPane3;
    @FXML NumberField repeatCountField, consecutiveField, xPosField, yPosField;
    @FXML ToggleButton locationButton;
    @FXML TextField buttonField;
    @FXML RadioButton infiniteRadioButton, finiteRadioButton, currentPosRadioButton, customPosRadioButton;

    private Model model = new Model();
    private SettingsHandler settingsHandler = new SettingsHandler(true);
    private DataSender dataSender = new DataSender();
    private Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    private Popup invisiblePopup = new InvisiblePopup();
    private static final HashMap<MouseButton, String> mouseMap = new HashMap<MouseButton, String>() {{
        put(MouseButton.PRIMARY, "Left Mouse Button");
        put(MouseButton.SECONDARY, "Right Mouse Button");
        put(MouseButton.MIDDLE, "Middle Mouse Button");
    }};

    /***************************************************************************
     *                                                                         *
     * Methods                                                                 *
     *                                                                         *
     **************************************************************************/

    public void initialize() {
        settingsHandler.loadSettings();
        sanitizeSpinners();
        setCursorPosMaxLimit();
        unlockUIOnLoopEnd();
        try { dataSender.sendData();
        } catch (Exception ignore) {}
    }

    /***** Repeat Rate Tab Methods *****/
    @FXML
    private void onInfiniteRadioAction() {
        repeatCountField.setDisable(true);
    }

    @FXML
    private void onFiniteRadioAction() {
        repeatCountField.setDisable(false);
    }

    // Add number pattern to spinner for validating inputs
    private void sanitizeSpinners() {
        Pattern numberPattern = Pattern.compile("[0-9]+");
        setSpinnerPattern(hrSpinner, numberPattern);
        setSpinnerPattern(minSpinner, numberPattern);
        setSpinnerPattern(secSpinner, numberPattern);
        setSpinnerPattern(msSpinner, numberPattern);
    }

    private void setSpinnerPattern(Spinner<Integer> spinner, Pattern pattern) {
        spinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (pattern.matcher(newValue).matches()) {
                spinner.getValueFactory().setValue(Integer.parseInt(newValue));
            } else {
                spinner.getEditor().setText(oldValue);
            }
        });
    }

    /***** Button Options Tab Methods *****/
    @FXML
    private void onButtonFieldClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            TextField textField = (TextField) event.getSource();
            textField.clear();
            textField.setEditable(true);
            invisiblePopup.getContent().get(0).setOnMouseClicked((MouseEvent e)->{
                textField.setText(mouseMap.get(e.getButton()));
                textField.setEditable(false);
                invisiblePopup.hide();
            });
            invisiblePopup.getContent().get(0).setOnKeyPressed((KeyEvent e) -> {
                textField.setText(e.getCode().getName());
                textField.setEditable(false);
                invisiblePopup.hide();
            });
            invisiblePopup.getContent().get(0).requestFocus();
            invisiblePopup.show(textField.getScene().getWindow());
        }
    }

    /***** Cursor Position Tab Methods *****/
    @FXML
    private void onCurrentPosRadioAction() {
        locationButton.setDisable(true);
        xPosField.setDisable(true);
        yPosField.setDisable(true);
    }

    @FXML
    private void onCustomPosRadioAction() {
        locationButton.setDisable(false);
        xPosField.setDisable(false);
        yPosField.setDisable(false);
    }

    private void setCursorPosMaxLimit() {
        xPosField.setMaxNumber((int) screenBounds.getWidth() - 1);
        yPosField.setMaxNumber((int) screenBounds.getHeight() - 1);
    }

    @FXML
    private void onPickLocationAction(ActionEvent event) {
        ToggleButton toggleButton = (ToggleButton) event.getSource();
        invisiblePopup.getContent().get(0).setOnMouseClicked((MouseEvent e)->{
            // Determine x and y values
            int xPos = (int) e.getX();
            int yPos = (int) e.getY();
            xPosField.setNumber(xPos);
            yPosField.setNumber(yPos);
            // Set toggle button
            toggleButton.setSelected(false);
            toggleButton.getParent().requestFocus();
            invisiblePopup.hide();
        });
        invisiblePopup.getContent().get(0).setOnKeyPressed((KeyEvent e) -> {});
        invisiblePopup.show(toggleButton.getScene().getWindow());
    }

    /*****  About Tab Methods *****/
    @FXML
    private void onHyperlinkClicked() {
        Main.getInstance().getHostServices().showDocument(hyperlink.getText());
    }

    /***** Start and Stop Button Methods ******/
    // Shortcut for start and stop buttons (Working only when application is focused)
    @FXML
    private void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.F9 && !startButton.isDisable()) {
            onStartButtonAction();
        }
        else if (e.getCode() == KeyCode.F10 && !stopButton.isDisable()) {
            onStopButtonAction();
        }
    }

    @FXML
    private void onStartButtonAction() {
         try {
             dataSender.sendData();
             lockUI();
             model.startLoop();
         } catch (Exception e) {
             Alert alert = new Alert(AlertType.ERROR, e.getMessage());
             alert.initOwner(startButton.getScene().getWindow());
             alert.setHeaderText(null);
             alert.showAndWait();
         }
    }

    @FXML
    private void onStopButtonAction() {
        model.stopLoop();
        unlockUI();
    }

    private void unlockUIOnLoopEnd() {
        model.onLoopEnded = this::unlockUI;
    }

    private void lockUI() {
        startButton.setDisable(true);
        stopButton.setDisable(false);
        tabPane1.setDisable(true);
        tabPane2.setDisable(true);
        tabPane3.setDisable(true);
    }

    private void unlockUI() {
        startButton.setDisable(false);
        stopButton.setDisable(true);
        tabPane1.setDisable(false);
        tabPane2.setDisable(false);
        tabPane3.setDisable(false);
    }

    /***************************************************************************
     *                                                                         *
     * Inner Classes                                                           *
     *                                                                         *
     **************************************************************************/

    /***** SettingsHandler class to save and load changes made by the user *****/
    public class SettingsHandler {

        private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

        public SettingsHandler(boolean saveSettingsOnExit) {
            if (saveSettingsOnExit) {
                Main.getInstance().onAppExit = this::saveSettings;
            }
        }

        public void saveSettings() {
            prefs.putInt(repeatCountField.getId(), repeatCountField.getNumber());
            prefs.putInt(hrSpinner.getId(), hrSpinner.getValue());
            prefs.putInt(minSpinner.getId(), minSpinner.getValue());
            prefs.putInt(secSpinner.getId(), secSpinner.getValue());
            prefs.putInt(msSpinner.getId(), msSpinner.getValue());
            prefs.put(buttonField.getId(), buttonField.getText());
            prefs.putInt(consecutiveField.getId(), consecutiveField.getNumber());
            prefs.putInt(xPosField.getId(), xPosField.getNumber());
            prefs.putInt(yPosField.getId(), yPosField.getNumber());
            prefs.putBoolean(infiniteRadioButton.getId(), infiniteRadioButton.isSelected());
            prefs.putBoolean(currentPosRadioButton.getId(), currentPosRadioButton.isSelected());
        }

        public void loadSettings() {
            System.out.println(repeatCountField.getId());
            repeatCountField.setNumber(prefs.getInt(repeatCountField.getId(), repeatCountField.getNumber()));
            hrSpinner.getValueFactory().setValue(prefs.getInt(hrSpinner.getId(), hrSpinner.getValue()));
            minSpinner.getValueFactory().setValue(prefs.getInt(minSpinner.getId(), minSpinner.getValue()));
            secSpinner.getValueFactory().setValue(prefs.getInt(secSpinner.getId(), secSpinner.getValue()));
            msSpinner.getValueFactory().setValue(prefs.getInt(msSpinner.getId(), msSpinner.getValue()));
            buttonField.setText(prefs.get(buttonField.getId(), buttonField.getText()));
            consecutiveField.setNumber(prefs.getInt(consecutiveField.getId(), consecutiveField.getNumber()));
            xPosField.setNumber(prefs.getInt(xPosField.getId(), xPosField.getNumber()));
            yPosField.setNumber(prefs.getInt(yPosField.getId(), yPosField.getNumber()));
            if (prefs.getBoolean(infiniteRadioButton.getId(), infiniteRadioButton.isSelected())) {
                infiniteRadioButton.setSelected(true);
                infiniteRadioButton.getOnAction().handle(new ActionEvent());
            } else {
                finiteRadioButton.setSelected(true);
                finiteRadioButton.getOnAction().handle(new ActionEvent());
            }
            if (prefs.getBoolean(currentPosRadioButton.getId(), currentPosRadioButton.isSelected())) {
                currentPosRadioButton.setSelected(true);
                currentPosRadioButton.getOnAction().handle(new ActionEvent());
            } else {
                customPosRadioButton.setSelected(true);
                customPosRadioButton.getOnAction().handle(new ActionEvent());
            }
        }
    }

    /***** InvisiblePopup class to create invisible full screen popup *****/
    public class InvisiblePopup extends Popup {
        public InvisiblePopup() {
            super();
            Pane pane = new Pane();
            this.getContent().add(pane);
            this.setX(0);
            this.setY(0);
            pane.setPrefWidth(screenBounds.getWidth());
            pane.setPrefHeight(screenBounds.getHeight());
            pane.setStyle("-fx-background-color: rgba(0,0,0,0.005);");
        }
    }

    /***** DataSender class to update fields of the model *****/
    private class DataSender {

        // Get data from view and send them to the model
        public void sendData() throws Exception {

            // Validate view data
            int interval = timeToMs(hrSpinner.getValue(), minSpinner.getValue(),
                    secSpinner.getValue(), msSpinner.getValue());
            String errMsg = validateView(interval);
            if (!errMsg.isEmpty()) { throw new Exception(errMsg); }

            // Repeat rate tab
            model.setLoopInfinite(repeatCountField.isDisable());
            model.setLoopCount(repeatCountField.getNumber());
            model.setInterval(interval);

            // Button options tab
            String fieldText = buttonField.getText();
            if (mouseMap.containsValue(fieldText)) {
                model.buttonStorage.setButton(getHashMapKey(mouseMap, fieldText));
            } else {
                KeyCode keyCode = KeyCode.getKeyCode(fieldText);
                model.buttonStorage.setButton(keyCode);
            }
            model.setPressCount(consecutiveField.getNumber());

            // Cursor position tab
            boolean currentCursorPosSelected = currentPosRadioButton.isSelected();
            model.setCursorCurrent(currentCursorPosSelected);
            if (!currentCursorPosSelected) {
                model.setCustomCursorPos(xPosField.getNumber(), yPosField.getNumber());
            }
        }

        // Converts time defined as hours, minutes, seconds and milliseconds to milliseconds
        private int timeToMs(int hrs, int mins, int secs, int ms) {
            return ms + secs * 1000 + mins * 60000 + hrs * 3600000;
        }

        // Returns the error message if data is invalid; an empty string otherwise
        private String validateView(int interval) {
            String errMsg = "";
            if (interval == 0) {
                errMsg = "The repeat interval must be at least 1 millisecond.";
            }
            else if (customPosRadioButton.isSelected()) {
                if ((xPosField.getNumber() == -1) || (yPosField.getNumber() == -1)) {
                    errMsg = "The X and Y values for cursor position cannot be left blank.";
                }
            }
            return (errMsg);
        }

        // Poor O(n) performance but will be used for only 3 entries
        private <K, V> K getHashMapKey(HashMap<K, V> map, V value) {
            return map.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(value))
                    .findFirst().get().getKey();
        }
    }
}
