package com.poker.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    @FXML private TextField userName_sign;
    @FXML private PasswordField password_sign;
    @FXML private Button signUpBtn;

    @FXML
    public void initialize() {
        // Thêm hiệu ứng cho nút Sign Up
        if (signUpBtn != null) ButtonEffects.addAllEffects(signUpBtn);
    }

    public void signUp(){
        if (userName_sign.getText() != null && password_sign.getText() != null) {

            Main.client.out.println("signup#"+userName_sign.getText()+"#"+password_sign.getText());
        }
    }

}
