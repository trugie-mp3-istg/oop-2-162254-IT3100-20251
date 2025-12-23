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

    private void showCard(ImageView view, Card card) {
        String suit = switch (card.getSuit()) {
            case Card.HEARTS   -> "H";
            case Card.DIAMONDS -> "D";
            case Card.CLUBS    -> "C";
            case Card.SPADES   -> "S";
            default -> throw new IllegalStateException();
        };

        int value = card.getRank() + 2;

        String path = String.format(
                "/graphics/%s%02d.png",
                suit, value
        );

        view.setImage(new Image(
                getClass().getResource(path).toExternalForm()
        ));
    }

    private final Image cardBackImage =
            new Image(getClass().getResource("/graphics/bakside.png").toExternalForm());

    @FXML private ImageView flop1Card;
    @FXML private ImageView flop2Card;
    @FXML private ImageView flop3Card;
    @FXML private ImageView turnCard;
    @FXML private ImageView riverCard;

    @FXML private ImageView player1Card;
    @FXML private ImageView player2Card;

    private ImageView[] boardViews;


    final int serverPort = 5555;

    private String playerName;
    private GameClient client;

    @FXML
    void initialize() {
        boardViews = new ImageView[]{
                flop1Card, flop2Card, flop3Card, turnCard, riverCard
        };
        for (ImageView v : boardViews) {
            v.setImage(cardBackImage);
        }
        player1Card.setImage(cardBackImage);
        player2Card.setImage(cardBackImage);

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

    private void updatePlayer(Player player) {
        var hand = player.getCards();
        if (hand.size() >= 2) {
            showCard(player1Card, hand.get(0));
            showCard(player2Card, hand.get(1));
        }
    }
    private void updateBoard(Table table) {
        var cards = table.getBoardCards();
        for (int i = 0; i < cards.size(); i++) {
            showCard(boardViews[i], cards.get(i));
        }
    }
    private void processPacket(ServerPacket packet) {
        switch (packet.getCommand()) {
            case PLAYER_UPDATED -> {
                // Lấy data ra (là Object) -> Ép kiểu về (Player)
                if (packet.getData() instanceof Player) {
                    updatePlayer((Player) packet.getData());
                }
            }
            case BOARD_UPDATED -> { // Hoặc BOARD_UPDATED tùy enum của bạn
                // Lấy data ra (là Object) -> Ép kiểu về (Table)
                if (packet.getData() instanceof Table) {
                    updateBoard((Table) packet.getData());
                }
            }
            case REQUEST_ACT -> {
                // Xử lý logic khi đến lượt
            }
        }
    }


}
