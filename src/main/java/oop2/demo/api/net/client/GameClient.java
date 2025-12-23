package oop2.demo.api.net.client;

import javafx.application.Platform; // Cần import cái này để xử lý UI
import oop2.demo.api.actions.Action;
import oop2.demo.api.net.dto.PlayerActionPacket;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listener;
    private volatile boolean connected = false;

    // Callback xử lý dữ liệu nhận được
    private final Consumer<Object> onDataReceived;

    private static final int DISCOVERY_PORT = 8888;

    public GameClient(String ip, int port, String playerName, Consumer<Object> onDataReceived) {
        this.onDataReceived = onDataReceived;
        try {
            System.out.println("[CLIENT] Connecting to " + ip + ":" + port + "...");
            socket = new Socket(ip, port); // Có thể thêm timeout connect nếu muốn

            // Luôn tạo Output trước Input để tránh Deadlock header
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Gửi tên ngay khi kết nối (Handshake)
            out.writeObject(playerName);
            out.flush();

            connected = true;
            System.out.println("[CLIENT] Connected as: " + playerName);

            // Bắt đầu lắng nghe
            listener = new Thread(this::listenFromServer);
            listener.setDaemon(true); // Để khi tắt cửa sổ game thì thread này cũng chết theo
            listener.start();

        } catch (IOException e) {
            System.err.println("❌ Lỗi kết nối: " + e.getMessage());
            connected = false;
        }
    }

    private void listenFromServer() {
        try {
            while (connected && !socket.isClosed()) {
                Object data = in.readObject();

                if (onDataReceived != null) {
                    // [QUAN TRỌNG] Đẩy việc xử lý về luồng JavaFX Application Thread
                    // Nếu không có dòng này, khi update Label/Button sẽ bị lỗi Exception
                    Platform.runLater(() -> onDataReceived.accept(data));
                }
            }
        } catch (SocketException | EOFException e) {
            System.out.println("⚠️ Mất kết nối với Server.");
            // Có thể báo cho UI biết là đã mất kết nối
            if (onDataReceived != null) {
                Platform.runLater(() -> onDataReceived.accept("DISCONNECTED"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    // Gửi hành động kèm tiền (Bet, Raise)
    public void sendAction(Action action, BigDecimal amount) {
        sendObject(new PlayerActionPacket(action, amount));
    }

    // Gửi hành động không cần tiền (Fold, Check, Call, All-in)
    // Server sẽ tự tính tiền Call/All-in dựa trên logic
    public void sendAction(Action action) {
        sendObject(new PlayerActionPacket(action, BigDecimal.ZERO));
    }

    // Gửi tin nhắn chat
    public void sendMessage(String message) {
        sendObject(message); // Gửi thẳng chuỗi String
    }

    // Hàm gửi chung (Private) để tránh lặp code
    private void sendObject(Object obj) {
        if (!connected) return;
        try {
            out.writeObject(obj);
            out.flush();
            out.reset(); // Quan trọng: Xóa cache object cũ
            // System.out.println("[CLIENT] Sent: " + obj); // Debug thì mở ra
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) { }
    }

    // Logic tìm IP Server (UDP) - Giữ nguyên của bạn là ổn
    public static String findServerIP() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"))) {
            socket.setSoTimeout(5000); // đợi tối đa 5s
            socket.setBroadcast(true);
            System.out.println("[CLIENT] Searching for server...");

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength());

            if (msg.startsWith("SERVER_IP:")) {
                String[] parts = msg.split(":");
                String serverIP = parts[1];
                int port = Integer.parseInt(parts[2]);
                System.out.println("[CLIENT] Found server at " + serverIP + ":" + port);
                return serverIP;
            }
        } catch (SocketTimeoutException e) {
            System.out.println("[CLIENT] No server found in LAN.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}