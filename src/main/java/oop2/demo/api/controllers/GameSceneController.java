package oop2.demo.api.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import oop2.demo.api.features.Card;
import oop2.demo.api.features.Player;
import oop2.demo.api.features.Table;
import oop2.demo.api.net.client.GameClient;
import oop2.demo.api.net.dto.ServerPacket;

import java.io.File;

public class GameSceneController {
    String card_Back_Path = new File("src/main/resources/graphics/card_back.png").toURI().toString();
    String card10diamond = new File("src/main/resources/graphics/card_diamonds_10.png").toURI().toString();
    String card4Heart = new File("src/main/resources/graphics/card_hearts_04.png").toURI().toString();
    Image card_Back_Image = new Image(card_Back_Path);

    final int serverPort = 5000;

    @FXML
    ImageView flop1Card;

    @FXML
    ImageView flop2Card;

    @FXML
    ImageView flop3Card;

    @FXML
    ImageView turnCard;

    @FXML
    ImageView riverCard;

    @FXML
    ImageView player1Card;

    @FXML
    ImageView player2Card;

    private String playerName;
    private GameClient client;

    @FXML
    void initialize() {

    }
    private void connectToSever() {
        client = new GameClient(
                GameClient.findServerIP(),
                serverPort,
                playerName,
                this::handleServerData
        );
    }
    // Khai báo biến lưu trạng thái lệnh vừa nhận
    private String lastCommand = "";

    private void handleServerData(Object data) {
        Platform.runLater(() -> {
            if (data instanceof ServerPacket) {
                processPacket((ServerPacket) data);
            }
        });
    }

    private void processPacket(ServerPacket packet) {
        switch (packet.getCommand()) {
            case REQUEST_ACT:
                break;
            case PLAYER_UPDATED:
                break;
        }
    }


}
