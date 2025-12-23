package oop2.demo.api.net.sever;

import oop2.demo.api.actions.*; // Import các class BetAction, RaiseAction...
import oop2.demo.api.features.*;
import oop2.demo.api.net.dto.*;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayerHandler extends Thread implements Client {

    private final Socket socket;
    private final GameServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private Player playerLogic; // Player trong Logic Game
    private boolean running = true;

    // Hàng đợi để chuyển dữ liệu từ luồng mạng sang luồng game
    private final BlockingQueue<PlayerActionPacket> actionQueue = new LinkedBlockingQueue<>();

    public PlayerHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        // KHÔNG đọc dữ liệu mạng trong Constructor để tránh treo Server
    }

    // --- LOGIC MẠNG (Chạy song song) ---
    @Override
    public void run() {
        try {
            // 1. Khởi tạo luồng (Stream)
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush(); // Đẩy header đi ngay
            this.in = new ObjectInputStream(socket.getInputStream());

            // 2. Handshake (Bắt tay): Đọc tên người chơi
            Object nameObj = in.readObject();
            String name = (String) nameObj;
            System.out.println("[HANDLER] " + name + " connected.");

            // 3. Tạo Player Logic và báo cho Server biết
            // (Tuỳ vào thiết kế của bạn, có thể Server đã tạo Player rồi gán vào đây,
            // nhưng ở đây giả sử Handler tự tạo)
            this.playerLogic = new Player(name, new BigDecimal(10000));
            this.playerLogic.setClient(this);

            // QUAN TRỌNG: Thêm player vào bàn cờ của Server
            server.getTable().addPlayer(this.playerLogic);

            // 4. Vòng lặp lắng nghe tin nhắn
            while (running && !socket.isClosed()) {
                Object data = in.readObject();

                if (data instanceof PlayerActionPacket) {
                    // Đẩy vào hàng đợi để hàm act() xử lý
                    actionQueue.offer((PlayerActionPacket) data);
                }
                else if (data instanceof String) {
                    System.out.println("Chat [" + name + "]: " + data);
                    // Có thể gọi server.broadcast(...) ở đây nếu muốn chat global
                }
            }

        } catch (EOFException | SocketException e) {
            System.out.println("Player disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
            server.removePlayer(this); // Báo server xoá người này
        }
    }

    // --- LOGIC GỬI TIN (Non-blocking) ---
    public synchronized void sendPacket(ServerPacket packet) {
        if (!running || socket.isClosed()) return;
        try {
            out.writeObject(packet);
            out.flush();
            out.reset(); // Rất quan trọng để tránh lỗi cache object cũ
        } catch (IOException e) {
            System.out.println("Error sending to " + (playerLogic != null ? playerLogic.getName() : "Unknown"));
            close();
        }
    }

    public void sendMessage(String msg) {
        sendPacket(new ServerPacket(ServerCommand.MESSAGE, msg));
    }

    public void close() {
        running = false;
        try { socket.close(); } catch (IOException e) {}
    }

    // --- IMPLEMENT CLIENT INTERFACE (Cầu nối Game -> Mạng) ---

    // [FIX] Hàm act quan trọng nhất
    @Override
    public Action act(BigDecimal minBet, BigDecimal currentBet, Set<Action> allowedActions) {
        // 1. Tính toán các thông số còn thiếu
        // Tính số tiền cần bỏ thêm để Call = (Tiền cược bàn - Tiền mình đã bỏ vòng này)
        BigDecimal myCurrentBetInRound = playerLogic.getBet();
        BigDecimal callAmount = currentBet.subtract(myCurrentBetInRound).max(BigDecimal.ZERO);

        BigDecimal playerBalance = playerLogic.getCash(); // Lấy số dư thực tế của người chơi
        BigDecimal totalPot = BigDecimal.ZERO; // Tạm để 0 (Do Table chưa truyền sang, xem lưu ý bên dưới)
        int timeLimit = 15; // Giới hạn 15 giây suy nghĩ

        // 2. Tạo đối tượng với đầy đủ 7 tham số
        ActRequestInfo info = new ActRequestInfo(
                currentBet,      // 1. Cược hiện tại của bàn
                minBet,          // 2. Mức tố tối thiểu (minRaise)
                callAmount,      // 3. Số tiền cần bỏ thêm để Call
                totalPot,        // 4. Tổng Pot (Hũ)
                playerBalance,   // 5. Số dư người chơi (để hiện thanh trượt All-in)
                timeLimit,       // 6. Thời gian đếm ngược
                allowedActions   // 7. Các hành động được phép
        );

        // Gửi đi
        sendPacket(new ServerPacket(ServerCommand.REQUEST_ACT, info));

        try {
            // 2. Chờ (Block) cho đến khi Client gửi gói tin phản hồi
            // Dùng take() thay vì poll() để chờ vô tận (hoặc logic timeout nếu muốn)
            PlayerActionPacket response = actionQueue.take();

            // 3. [QUAN TRỌNG] Convert từ DTO sang Action Object của Logic Game
            Action actionType = response.getActionType(); // Giả sử DTO trả về Enum Action
            BigDecimal amount = response.getAmount();

            // Tạo đúng object Action tương ứng (Vì Table logic cần check instanceof RaiseAction...)
            // Lưu ý: Đoạn này phụ thuộc vào cách bạn viết class Action.
            // Nếu Action chỉ là Enum thì return actionType là đủ.
            // Nhưng nếu Action là Abstract Class (như trong Table.java có dùng instanceof RaiseAction)
            // thì phải làm như sau:

            if (actionType == Action.RAISE) {
                return new RaiseAction(amount); // Class RaiseAction extends Action
            } else if (actionType == Action.BET) {
                return new BetAction(amount);   // Class BetAction extends Action
            }

            // Với các hành động không cần tiền (Fold, Check, Call, All-in)
            return actionType;

        } catch (InterruptedException e) {
            return Action.FOLD; // Nếu bị ngắt giữa chừng thì Fold
        }
    }

    // Các hàm cập nhật trạng thái
    @Override
    public void joinedTable(TableType type, BigDecimal bigBlind, List<Player> players) {
        // Gửi cả cấu hình bàn chơi nếu cần
        sendPacket(new ServerPacket(ServerCommand.JOINED, players)); // Cần chắc chắn List<Player> Serializable
    }

    @Override
    public void handStarted(Player dealer) {
        sendPacket(new ServerPacket(ServerCommand.HAND_STARTED, dealer));
    }

    @Override
    public void actorRotated(Player actor) {
        sendPacket(new ServerPacket(ServerCommand.ACTOR_ROTATED, actor));
    }

    @Override
    public void playerUpdated(Player player) {
        sendPacket(new ServerPacket(ServerCommand.PLAYER_UPDATED, player));
    }

    @Override
    public void boardUpdated(List<Card> cards, BigDecimal bet, BigDecimal pot) {
        // [FIX] Gửi đủ dữ liệu BoardInfo thay vì chỉ cards
        // Bạn có thể cần tạo thêm class BoardInfoDTO hoặc gửi Map/Object[]
        BoardInfoDTO info = new BoardInfoDTO(cards, bet, pot);
        sendPacket(new ServerPacket(ServerCommand.BOARD_UPDATED, info));
    }

    @Override
    public void playerActed(Player player) {
        sendPacket(new ServerPacket(ServerCommand.PLAYER_ACTED, player));
    }

    @Override
    public void messageReceived(String message) {
        sendMessage(message);
    }

    // Getter
    public Player getPlayerLogic() { return playerLogic; }
}