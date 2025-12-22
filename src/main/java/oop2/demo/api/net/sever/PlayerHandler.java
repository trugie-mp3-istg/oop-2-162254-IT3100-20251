package oop2.demo.api.net.sever;

import oop2.demo.api.actions.Action;
import oop2.demo.api.net.dto.*;

import oop2.demo.api.features.*;
import oop2.demo.api.net.dto.ActRequestInfo;
import oop2.demo.api.net.dto.ServerPacket;
import oop2.demo.api.net.sever.GameServer;

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

    private Player playerLogic;
    private boolean running = true;

    private final BlockingQueue<PlayerActionPacket> actionQueue = new LinkedBlockingQueue<>();

    public PlayerHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());

            Object nameObj = in.readObject();
            String name = (String) nameObj;

            this.playerLogic = new Player(name, new BigDecimal(1000));
            this.playerLogic.setClient(this);

            System.out.println("[HANDLER] Player initialized: " + name);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Player getPlayerLogic() {
        return playerLogic;
    }

    // --- VÒNG LẶP LẮNG NGHE TỪ CLIENT ---
    @Override
    public void run() {
        try {
            while (running && !socket.isClosed()) {
                Object data = in.readObject();

                if (data instanceof PlayerActionPacket) {
                    actionQueue.offer((PlayerActionPacket) data);
                }
                else if (data instanceof String) {
                    System.out.println("Chat from " + playerLogic.getName() + ": " + data);
                }
            }
        } catch (EOFException | SocketException e) {
            System.out.println(playerLogic.getName() + " disconnected.");
            server.removePlayer(this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void sendPacket(ServerPacket packet) {
        try {
            out.writeObject(packet);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.out.println("Error sending packet to " + playerLogic.getName());
        }
    }

    public void sendMessage(String msg) {
        sendPacket(new ServerPacket(ServerCommand.MESSAGE, msg));
    }

    public void close() {
        running = false;
        try { socket.close(); } catch (IOException e) {}
    }
    @Override
    public void joinedTable(TableType type, BigDecimal bigBlind, List<Player> players) {
        sendPacket(new ServerPacket(ServerCommand.JOINED, players));
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
        sendPacket(new ServerPacket(ServerCommand.BOARD_UPDATED, cards));
    }

    @Override
    public void playerActed(Player player) {
        sendPacket(new ServerPacket(ServerCommand.PLAYER_ACTED, player));
    }

    @Override
    public void messageReceived(String message) {
        sendMessage(message);
    }

    @Override
    public Action act(BigDecimal minBet, BigDecimal currentBet, Set<Action> allowedActions) {
        ActRequestInfo info = new ActRequestInfo(minBet, currentBet, allowedActions);
        sendPacket(new ServerPacket(ServerCommand.REQUEST_ACT, info));

        try {
            PlayerActionPacket response = actionQueue.take();

            if (response.actionType == Action.BET || response.actionType == Action.RAISE) {

            }
            return response.actionType;

        } catch (InterruptedException e) {
            return Action.FOLD;
        }
    }
}