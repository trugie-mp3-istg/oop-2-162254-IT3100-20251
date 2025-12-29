package com.poker.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController{

    @FXML private TextField userName_log;
    @FXML private PasswordField password_log;
    @FXML private Button loginBtn;
    @FXML private Button createAccountBtn;

    @FXML
    public void initialize() {
        // Thêm hiệu ứng cho các nút
        if (loginBtn != null) ButtonEffects.addAllEffects(loginBtn);
        if (createAccountBtn != null) ButtonEffects.addAllEffects(createAccountBtn);
    }

    public void logIn() {
        if (userName_log.getText() != null && password_log.getText() != null) {

           Main.client.out.println("login#"+userName_log.getText()+"#"+password_log.getText());
        }
    }

    public void createAccount(){

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/signupScreen.fxml"));
            Parent root = loader.load();

            SignupController sc = loader.getController();
            Main.client.setSignUpController(sc);

            Main.stage.close();
            Main.stage.setTitle("Online Poker - Sign Up");
            Main.stage.setScene(new Scene(root, 950, 520));
            Main.stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}


