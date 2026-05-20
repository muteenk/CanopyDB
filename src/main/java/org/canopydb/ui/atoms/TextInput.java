package org.canopydb.ui.atoms;

import javafx.scene.control.TextField;

public class TextInput {
    private final TextField textField;

    public TextInput(String placeholder){
        textField = new TextField();
        textField.setPromptText(placeholder);
    }

    public TextField getTextField(){
        return textField;
    }
}
