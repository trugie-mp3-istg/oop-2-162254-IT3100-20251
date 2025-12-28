package com.poker.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage stage;
    public static Client client;

    @Override
    public void start(Stage primaryStage) throws Exception{

        client = new Client();

        this.stage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loginScreen.fxml"));
        Parent root = loader.load();

        LoginController lc = loader.getController();
        client.setLogInController(lc);

        stage.setTitle("Online Poker - Log In");
        stage.setScene(new Scene(root, 1000, 650));
        
        // Xử lí tắt ứng dụng khi người dùng không bấm logout mà thoát bằng cách khác
        stage.setOnCloseRequest(event -> {
            try {
                if(client != null && client.socket != null && !client.socket.isClosed()) {
                    if(client.username != null) {
                        // Gửi lệnh logout trước khi đóng
                        client.out.println("logout");
                    }
                    client.socket.close();
                }
            } catch (Exception e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        });
        
        stage.show();

    }
    public static void main(String[] args) {
        launch(args);
    }

}
