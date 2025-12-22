package oop2.demo.api.net.client;

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

    private final Consumer<Object> onDataReceived;

    private static final int DISCOVERY_PORT = 8888;

    public GameClient(String ip, int port, String playerName, Consumer<Object> onDataReceived) {
        this.onDataReceived = onDataReceived;
        try {
            System.out.println("[CLIENT] Connecting to " + ip + ":" + port + "...");
            socket = new Socket(ip, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(playerName);
            out.flush();

            connected = true;
            System.out.println("[CLIENT] Connected as: " + playerName);

            listener = new Thread(this::listenFromServer);
            listener.setDaemon(true);
            listener.start();

        } catch (IOException e) {
            System.err.println("❌ Lỗi kết nối: " + e.getMessage());
            connected = false;
        }
    }

    // Vòng lặp chạy ngầm để hứng dữ liệu
    private void listenFromServer() {
        try {
            while (connected && !socket.isClosed()) {
                Object data = in.readObject();

                if (onDataReceived != null) {
                    onDataReceived.accept(data);
                }
            }
        } catch (SocketException | EOFException e) {
            System.out.println("⚠️ Mất kết nối với Server.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void sendAction(Action action, BigDecimal amount) {
        if (!connected) return;
        try {
            PlayerActionPacket packet = new PlayerActionPacket(action, amount);

            out.writeObject(packet);
            out.flush();
            out.reset();

            System.out.println("[CLIENT] Sent: " + action);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String findServerIP() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"))) {
            socket.setSoTimeout(5000);
            socket.setBroadcast(true);
            System.out.println("[CLIENT] Scanning for server...");

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength());
            if (msg.startsWith("SERVER_IP:")) {
                return msg.split(":")[1];
            }
        } catch (Exception e) {
            System.out.println("[CLIENT] Server scan timeout.");
        }
        return "127.0.0.1";
    }
}