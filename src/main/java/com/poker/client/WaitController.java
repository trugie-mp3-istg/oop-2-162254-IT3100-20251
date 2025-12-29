package com.poker.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class WaitController {

    @FXML private Button logoutBtn;

    @FXML
    public void initialize() {
        // Thêm hiệu ứng cho nút Logout
        if (logoutBtn != null) ButtonEffects.addAllEffects(logoutBtn);
    }

    public void logoutFromWait(){

        Main.client.out.println("logoutwait");
        Platform.exit();
        System.exit(0);
    }
}
