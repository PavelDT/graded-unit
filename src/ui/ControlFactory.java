package ui;

import javafx.scene.control.*;

/**
 * Utility class used to generate similarly configured and initialised controls used accross multiple forms
 * This allows to simplify maintaining "house style" across the application
 */
public class ControlFactory {

    // width of password and text field controls
    private static final double TXT_WIDTH = 250.0d;
    // width of button controls
    private static final double BTN_WIDTH = 250.0d;

    /**
     * Gets a text field control
     * @param text - default control text
     * @param tooltip - tooltip describing control
     * @return Textfield - a textfield control
     */
    public static TextField getTextField(String text, String tooltip) {
        TextField tf = new TextField(text);
        tf.setTooltip(new Tooltip(tooltip));

        return tf;
    }

    /**
     * Gets a password field control
     * @param tooltip - tooltip describing control
     * @return PasswordField control to hold passwords
     */
    public static PasswordField getPasswordField(String tooltip) {
        PasswordField pf = new PasswordField();
        pf.setTooltip(new Tooltip(tooltip));

        return pf;
    }

    /**
     * Gets a button control
     * @param text - Text on the button
     * @param tooltip - tooltip describing control
     * @return Button
     */
    public static Button getButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));

        return btn;
    }

    /**
     * Gets a link control
     * @param text - Text on the link
     * @param tooltip - tooltip describing control
     * @return Hyperlink control
     */
    public static Hyperlink getHyperlink(String text, String tooltip) {
        Hyperlink link = new Hyperlink(text);
        link.setTooltip(new Tooltip(tooltip));

        return link;
    }

    /**
     * Gets a label control
     * @param text - initial text of the label
     * @param tooltip - tooltip describing control
     * @return Label control
     */
    public static Label getLabel(String text, String tooltip) {
        Label label = new Label(text);
        label.setTooltip(new Tooltip(tooltip));

        return label;
    }

    /**
     * Gets a combo-box control that stores strings
     * @param tooltip - tooltip describing control
     * @return Returns a ComboBox that can store strings
     */
    public static ComboBox<String> getComboBox(String tooltip) {
        ComboBox<String> cb = new ComboBox<String>();
        cb.setTooltip(new Tooltip(tooltip));

        return cb;
    }
}