package oop2.demo.api.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import oop2.demo.api.actions.Action;
import oop2.demo.api.features.Card;
import oop2.demo.api.features.Player;
import oop2.demo.api.net.client.GameClient;
import oop2.demo.api.net.dto.ActRequestInfo;
import oop2.demo.api.net.dto.BoardInfoDTO;
import oop2.demo.api.net.dto.ServerPacket;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class GameSceneController {

    // --- KHAI BÁO FXML (Phải trùng khớp fx:id trong file .fxml) ---

    // 1. Các lá bài
    @FXML private ImageView flop1Card, flop2Card, flop3Card, turnCard, riverCard;
    @FXML private ImageView player1Card, player2Card;

    // 2. Các nút điều khiển
    @FXML private Button btnFold;       // Nút Fold (Đỏ)
    @FXML private Button btnCheckCall;  // Nút Check/Call (Xanh dương)
    @FXML private Button btnBetRaise;   // Nút Bet (Xanh lá)

    // 3. Khu vực cược
    @FXML private Slider sliderBet;     // Thanh trượt
    @FXML private Label lblBetAmount;   // Label hiển thị số tiền trên thanh trượt
    @FXML private HBox actionBox;       // Hộp chứa các nút (để ẩn/hiện nếu cần)

    // 4. Thông tin bàn chơi
    @FXML private Label lblTotalPot;    // Label Pot tổng

    // --- CÁC BIẾN LOGIC ---
    private ImageView[] boardViews;
    private final Image cardBackImage = new Image(getClass().getResource("/graphics/bakside.png").toExternalForm());

    // Cấu hình mạng
    final int serverPort = 5555;
    private String playerName = "Player_" + (int)(Math.random() * 1000);
    private GameClient client;

    @FXML
    void initialize() {
        // 1. Gom nhóm bài chung vào mảng
        boardViews = new ImageView[]{flop1Card, flop2Card, flop3Card, turnCard, riverCard};

        // 2. Reset giao diện ban đầu
        resetTableUI();

        // 3. Cấu hình sự kiện cho Slider (Kéo thanh trượt -> đổi số tiền hiển thị)
        sliderBet.valueProperty().addListener((obs, oldVal, newVal) -> {
            int amount = newVal.intValue();
            lblBetAmount.setText(amount + " $");
            // Cập nhật text nút Bet luôn cho ngầu
            if (!btnBetRaise.isDisabled()) {
                String type = btnBetRaise.getText().split(" ")[0]; // Lấy chữ BET hoặc RAISE
                if (type.isEmpty()) type = "BET";
                btnBetRaise.setText(type + " " + amount + "$");
            }
        });

        // 4. Gắn sự kiện Click cho các nút (Vì trong FXML bạn chưa khai báo onAction="#...")
        btnFold.setOnAction(e -> onFold());
        btnCheckCall.setOnAction(e -> onCheckCall());
        btnBetRaise.setOnAction(e -> onBetRaise());

        // 5. Kết nối Server
        connectToServer();
    }

    private void connectToServer() {
        String ip = "localhost"; // GameClient.findServerIP();
        System.out.println("Connecting as " + playerName + "...");
        client = new GameClient(ip, serverPort, playerName, this::handleServerData);
    }

    // --- XỬ LÝ DỮ LIỆU TỪ SERVER ---
    private void handleServerData(Object data) {
        Platform.runLater(() -> {
            if (data instanceof ServerPacket) {
                processPacket((ServerPacket) data);
            }
        });
    }

    private void processPacket(ServerPacket packet) {
        switch (packet.getCommand()) {
            case PLAYER_UPDATED -> {
                if (packet.getData() instanceof Player) {
                    Player p = (Player) packet.getData();
                    if (p.getName().equals(playerName)) {
                        updateMyHand(p);
                    }
                }
            }
            case BOARD_UPDATED -> {
                if (packet.getData() instanceof BoardInfoDTO) {
                    BoardInfoDTO info = (BoardInfoDTO) packet.getData();
                    updateBoard(info);
                }
            }
            case REQUEST_ACT -> {
                if (packet.getData() instanceof ActRequestInfo) {
                    enableControls((ActRequestInfo) packet.getData());
                    actionBox.setVisible(true); // Hiện bảng điều khiển
                }
            }
            case HAND_STARTED -> resetTableUI();
            case GAME_OVER -> {
                disablePlayerControls();
                lblTotalPot.setText("Winner!");
            }
        }
    }

    // --- CẬP NHẬT GIAO DIỆN ---

    private void updateMyHand(Player player) {
        Card[] hand = player.getCards();
        if (hand != null && hand.length >= 2) {
            showCard(player1Card, hand[0]);
            showCard(player2Card, hand[1]);
        } else {
            player1Card.setImage(cardBackImage);
            player2Card.setImage(cardBackImage);
        }
    }

    private void updateBoard(BoardInfoDTO info) {
        // Cập nhật bài chung
        List<Card> cards = info.cards;
        for (int i = 0; i < 5; i++) {
            if (i < cards.size()) {
                showCard(boardViews[i], cards.get(i));
            } else {
                boardViews[i].setImage(null);
            }
        }
        // Cập nhật Pot (Dùng fx:id lblTotalPot)
        lblTotalPot.setText("Pot: $" + info.pot.intValue());
    }

    private void showCard(ImageView view, Card card) {
        if (card == null) return;
        String suit = switch (card.getSuit()) {
            case Card.HEARTS   -> "H";
            case Card.DIAMONDS -> "D";
            case Card.CLUBS    -> "C";
            case Card.SPADES   -> "S";
            default -> "";
        };
        int value = card.getRank() + 2;
        try {
            String path = String.format("/graphics/%s%02d.png", suit, value);
            view.setImage(new Image(getClass().getResource(path).toExternalForm()));
        } catch (Exception e) {
            System.err.println("Missing img: " + suit + value);
        }
    }

    // --- LOGIC ĐIỀU KHIỂN NÚT BẤM (QUAN TRỌNG) ---

    private void enableControls(ActRequestInfo info) {
        Set<Action> allowed = info.allowedActions;

        // 1. Nút FOLD
        btnFold.setDisable(!allowed.contains(Action.FOLD));

        // 2. Nút CHECK / CALL (Dùng chung btnCheckCall)
        if (allowed.contains(Action.CHECK)) {
            btnCheckCall.setText("CHECK");
            btnCheckCall.setDisable(false);
        } else if (allowed.contains(Action.CALL)) {
            btnCheckCall.setText("CALL " + info.callAmount + "$");
            btnCheckCall.setDisable(false);
        } else {
            btnCheckCall.setDisable(true);
            btnCheckCall.setText("CHECK");
        }

        // 3. Nút BET / RAISE và SLIDER (Dùng chung btnBetRaise)
        if (allowed.contains(Action.RAISE) || allowed.contains(Action.BET)) {
            btnBetRaise.setDisable(false);
            sliderBet.setDisable(false);

            // Cấu hình thanh trượt
            double min = info.minRaise.doubleValue();
            double max = info.playerBalance.doubleValue(); // Max là tất cả tiền mình có
            if (max < min) max = min; // Fix lỗi nếu server gửi sai

            sliderBet.setMin(min);
            sliderBet.setMax(max);
            sliderBet.setValue(min);
            lblBetAmount.setText((int)min + " $");

            // Đổi tên nút
            String label = allowed.contains(Action.RAISE) ? "RAISE" : "BET";
            btnBetRaise.setText(label + " " + (int)min + "$");

        } else {
            btnBetRaise.setDisable(true);
            sliderBet.setDisable(true);
            btnBetRaise.setText("BET");
        }
    }

    private void disablePlayerControls() {
        btnFold.setDisable(true);
        btnCheckCall.setDisable(true);
        btnBetRaise.setDisable(true);
        sliderBet.setDisable(true);
    }

    private void resetTableUI() {
        for (ImageView v : boardViews) v.setImage(null);
        player1Card.setImage(cardBackImage);
        player2Card.setImage(cardBackImage);
        disablePlayerControls();
        lblTotalPot.setText("Pot: $0");
    }

    // --- SỰ KIỆN CLICK (Đã gắn trong initialize) ---

    public void onFold() {
        client.sendAction(Action.FOLD);
        disablePlayerControls();
    }

    public void onCheckCall() {
        // Kiểm tra text trên nút để biết gửi lệnh gì
        String text = btnCheckCall.getText();
        if (text.startsWith("CALL")) {
            client.sendAction(Action.CALL);
        } else {
            client.sendAction(Action.CHECK);
        }
        disablePlayerControls();
    }

    public void onBetRaise() {
        BigDecimal amount = BigDecimal.valueOf((long) sliderBet.getValue());
        client.sendAction(Action.RAISE, amount);
        disablePlayerControls();
    }
}