package ui;

import javafx.scene.control.*;


public class ControlFactory {

    // width of password and text field controls
    private static final double TXT_WIDTH = 250.0d;
    // width of button controls
    private static final double BTN_WIDTH = 250.0d;

    public static TextField getTextField(String text, String tooltip) {
        TextField tf = new TextField(text);
        tf.setTooltip(new Tooltip(tooltip));

        return tf;
    }

    public static PasswordField getPasswordField(String tooltip) {
        PasswordField pf = new PasswordField();
        pf.setTooltip(new Tooltip(tooltip));

        return pf;
    }

    public static Button getButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));

        return btn;
    }

    public static Hyperlink getHyperlink(String text, String tooltip) {
        Hyperlink link = new Hyperlink(text);
        link.setTooltip(new Tooltip(tooltip));

        return link;
    }

    public static Label getLabel(String text, String tooltip) {
        Label label = new Label(text);
        label.setTooltip(new Tooltip(tooltip));

        return label;
    }

    public static ComboBox<String> getComboBox(String text, String tooltip) {
        ComboBox<String> cb = new ComboBox<String>();
        cb.setTooltip(new Tooltip(tooltip));

        return cb;
    }
}