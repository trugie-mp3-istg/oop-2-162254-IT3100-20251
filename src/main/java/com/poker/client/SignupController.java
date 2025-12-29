package com.poker.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    @FXML private TextField userName_sign;
    @FXML private PasswordField password_sign;
    @FXML private Button signUpBtn;
    @FXML private Button backBtn;

    @FXML
    public void initialize() {
        // Thêm hiệu ứng cho nút Sign Up
        if (signUpBtn != null) ButtonEffects.addAllEffects(signUpBtn);
        // Thêm hiệu ứng cho nút Back
        if (backBtn != null) ButtonEffects.addAllEffects(backBtn);
    }

    public void signUp(){
        if (!userName_sign.getText().trim().isEmpty() && !password_sign.getText().trim().isEmpty()) {

            Main.client.out.println("signup#"+userName_sign.getText()+"#"+password_sign.getText());
        }
    }

    public void backToLogin(){
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/loginScreen.fxml"));
            javafx.scene.Parent root = loader.load();

            LoginController lc = loader.getController();
            Main.client.setLogInController(lc);

            Main.stage.setTitle("Online Poker - Log In");
            Main.stage.setScene(new javafx.scene.Scene(root, 1000, 650));
            Main.stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

}
