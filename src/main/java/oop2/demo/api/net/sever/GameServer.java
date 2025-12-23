package oop2.demo.api.net.sever;

import oop2.demo.api.features.Table;
import oop2.demo.api.features.TableType;
import oop2.demo.api.features.Player;
import oop2.demo.api.net.dto.ServerPacket;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private static final int PORT = 5555;
    private static final int MAX_PLAYERS = 5;
    private static final int DISCOVERY_PORT = 8888;
    private static final BigDecimal STARTING_CASH = BigDecimal.valueOf(10000); // Tiền khởi điểm

    private ServerSocket serverSocket;
    private final List<PlayerHandler> connections = new CopyOnWriteArrayList<>();
    private volatile boolean running = true;

    // Logic Game Poker
    private Table gameMain;
    private Thread gameThread; // Thread riêng để chạy logic bàn cờ
    private volatile boolean isGameRunning = false;

    public GameServer() {
        // 1. Khởi tạo bàn chơi: Chọn luật NO_LIMIT, BigBlind = 100
        this.gameMain = new Table(TableType.NO_LIMIT, BigDecimal.valueOf(100));
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[SERVER] Started on port " + PORT);

            // Thread Discovery (UDP) giữ nguyên như cũ
            new Thread(this::runDiscoveryLoop, "DiscoveryThread").start();

            while (running) {
                // Chấp nhận kết nối
                Socket socket = serverSocket.accept();

                if (connections.size() < MAX_PLAYERS) {
                    // 2. Tạo kết nối mạng (PlayerHandler)
                    PlayerHandler handler = new PlayerHandler(socket, this);
                    connections.add(handler);
                    handler.start();

                    // 3. Tạo Player (Domain Object) và ném vào bàn cờ
                    // Lưu ý: PlayerHandler cần implement interface Client mà Table yêu cầu
                    String playerName = "Guest_" + (connections.size());
                    Player logicPlayer = new Player(playerName, STARTING_CASH, handler);

                    gameMain.addPlayer(logicPlayer);
                    System.out.println("[SERVER] " + playerName + " joined. Total: " + connections.size());

                    // 4. Kiểm tra điều kiện bắt đầu game
                    checkAndStartGame();

                } else {
                    System.out.println("[SERVER] Room full.");
                    socket.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    /**
     * Logic để bắt đầu vòng lặp game trong Thread riêng
     */
    private synchronized void checkAndStartGame() {
        // Nếu game chưa chạy và đã có đủ 2 người trở lên -> Bắt đầu
        if (!isGameRunning && connections.size() >= 2) {
            isGameRunning = true;
            gameThread = new Thread(() -> {
                System.out.println("[GAME] Table loop started!");
                try {
                    gameMain.run(); // Hàm này chạy vô tận trong class Table
                } catch (Exception e) {
                    System.err.println("[GAME] Error in game loop: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Khi Table.run() break (do activePlayers < 2), reset trạng thái
                    System.out.println("[GAME] Table loop stopped (Not enough players).");
                    isGameRunning = false;
                }
            }, "GameLogicThread");

            gameThread.start();
        }
    }

    private void runDiscoveryLoop() {
        // ... (Giữ nguyên code Discovery ở bước trước) ...
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
            while (running) {
                if (connections.size() < MAX_PLAYERS) {
                    String ip = InetAddress.getLocalHost().getHostAddress();
                    String msg = "SERVER_IP:" + ip + ":" + PORT;
                    byte[] data = msg.getBytes();
                    socket.send(new DatagramPacket(data, data.length, broadcastAddr, DISCOVERY_PORT));
                }
                Thread.sleep(3000);
            }
        } catch (Exception e) {}
    }

    public synchronized void removePlayer(PlayerHandler handler) {
        connections.remove(handler);
        // Lưu ý: Cần thêm logic removePlayer trong class Table nữa nếu muốn xóa hoàn toàn
        // Tuy nhiên Table của bạn đang xử lý bằng activePlayers.clear() mỗi ván nên tạm ổn
        System.out.println("[SERVER] Player disconnected.");
    }

    public Table getTable() {
        return gameMain;
    }

    public void stop() {
        running = false;
        try {
            for (PlayerHandler p : connections) p.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {}
    }

    public static void main(String[] args) {
        new GameServer().start();
    }
}