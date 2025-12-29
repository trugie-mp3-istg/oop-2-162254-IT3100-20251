package com.poker.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.poker.model.Hand;

public class Server implements Runnable {

    
    private BufferedReader in;
    public PrintWriter out;
    private boolean isLoggedIn;
    public boolean isTurn;
    public boolean isFolded;
    public boolean isAllIn;
    public Hand hand;
    public String username;
    public int chips;
    public int selfBet;
    public int tableID;
    public int playerIndex = -1;
    private static List<Server> loggedInUsers;
    public static List<Server> waitingUsers;
    private static Table table1;
    private static Table table2;
    public Table table;

    Server(Socket socket) {
        isLoggedIn = false;
        isFolded = true;
        isAllIn = false;
        username = null;
        chips = 0;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        loggedInUsers = new ArrayList<>();
        waitingUsers = new ArrayList<>();
        table1 = new Table();
        table2 = new Table();
        new Thread(() -> {
           
             try {
                DatagramSocket udpSocket = new DatagramSocket(8888, java.net.InetAddress.getByName("0.0.0.0"));
                udpSocket.setBroadcast(true);
                byte[] buffer = new byte[1024];
                System.out.println(">>> Đã bật chức năng tìm kiếm IP (UDP Port 8888)");
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    if (message.equals("DISCOVER_POKER_SERVER")) {
                        String response = "POKER_SERVER_HERE";
                        byte[] sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        udpSocket.send(sendPacket);
                    }
                }
            } catch (Exception e) {}
        }).start();

        try {
            ServerSocket ss = new ServerSocket(7777);
            while (true) {
                Socket s = ss.accept();
                System.out.println("connected.");
                new Thread(new Server(s)).start();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String data = in.readLine();
                if (data == null) {
                    handleClientDisconnect();
                    break;
                }
                System.out.println(data);
                parseData(data);
            } catch (IOException e) {
                handleClientDisconnect();
                break;
            }
        }
    }
    
    
     private void handleClientDisconnect() {
      
        System.out.println("Client disconnect: " + username);
        if (!isLoggedIn) return;
        isLoggedIn = false;
        if (table != null) {
            table.sendDataToAll("logout#" + username);
            if (isTurn) {
                isTurn = false;
                table.changeTurn();
                table.inGamePlayers.remove(this);
                table.players.remove(this);
                table.whichPlayerTurn--;
                if (table.checkNumber == table.inGamePlayers.size()) table.changeRound();
                if (table.inGamePlayers.size() == 1) table.Reset();
            } else {
                if (!isFolded) {
                    table.inGamePlayers.remove(this);
                    table.players.remove(this);
                    table.whichPlayerTurn--;
                    if (table.inGamePlayers.size() == 1) table.Reset();
                } else {
                    table.players.remove(this);
                }
            }
        }
        loggedInUsers.remove(this);
        waitingUsers.remove(this);
    }

    private void parseData(String data) {
        String[] message = data.split("#");
        switch (message[0]) {
            case "login":
              
                if (isLoggedIn) { out.println("decline#already_logged_in"); break; }
                if (validateLogIn(message[1], message[2])) initiateUserEntry(message[1]);
                else out.println("decline#invalid_credentials");
                break;

            case "signup":
            
                if (isLoggedIn) { out.println("decline#already_logged_in"); break; }
                if (validateSignUp(message[1], message[2])) initiateUserEntry(message[1]);
                else out.println("decline#username_exists");
                break;

            case "call":
                table.sendDataToAll("move#" + username + "#Call");
                decreaseChips(table.currentBet - selfBet);
                table.pot += table.currentBet - selfBet;
                selfBet = table.currentBet;
                table.sendDataToAll("pot#" + String.valueOf(table.pot));
                table.changeTurn(); // Table.java now handles skipping all-ins smartly
                
                // Logic check vòng cược giữ nguyên, nhưng Table.changeTurn sẽ quyết định
                // ...
                 // Check logic dưới đây có thể giữ lại hoặc đơn giản hóa vì changeTurn đã lo rồi
                int callNonAllInCount = 0;
                int callNonAllInActedCount = 0;
                for (Server p : table.inGamePlayers) {
                    if (!p.isAllIn) {
                        callNonAllInCount++;
                        if (p.selfBet == table.currentBet) callNonAllInActedCount++;
                    }
                }
                if (callNonAllInCount > 0 && callNonAllInActedCount == callNonAllInCount) {
                    table.changeRound();
                }
                isTurn = false;
                break;

            case "check":
                table.sendDataToAll("move#" + username + "#Check");
                table.checkNumber++;
                table.changeTurn();
                
                int checkNonAllInCount = 0;
                for (Server p : table.inGamePlayers) {
                    if (!p.isAllIn) checkNonAllInCount++;
                }
                if (table.checkNumber == checkNonAllInCount) {
                    table.changeRound();
                }
                isTurn = false;
                break;

            case "raise":
                table.sendDataToAll("move#" + username + "#Raise " + message[1]);
                table.checkNumber = 0;
                int raiseAmount = Integer.parseInt(message[1]);
                table.currentBet = raiseAmount; // Đã sửa lại gán trực tiếp amount thay vì cộng dồn sai logic
                
                decreaseChips(table.currentBet - selfBet);
                table.pot += table.currentBet - selfBet;
                selfBet = table.currentBet;

                table.sendDataToAll("pot#" + String.valueOf(table.pot));
                table.sendDataToAll("currentbet#" + String.valueOf(table.currentBet));
                isTurn = false;
                table.changeTurn();
                break;

            case "fold":
                table.sendDataToAll("move#" + username + "#Fold");
                isFolded = true;
                isTurn = false;
                table.changeTurn();
                table.inGamePlayers.remove(this);
                table.whichPlayerTurn--;

                // Logic này giữ nguyên để xác định thắng thua khi fold
                int foldNonAllInCount = 0;
                for (Server p : table.inGamePlayers) {
                    if (!p.isAllIn) foldNonAllInCount++;
                }
                if (foldNonAllInCount > 0 && table.checkNumber == foldNonAllInCount) {
                    table.changeRound();
                }
                if (table.inGamePlayers.size() == 1) {
                    table.Reset(); // Hàm này đã OK
                }
                break;

            case "allin":
                int allInAmount = chips; 
                handleAllIn(allInAmount);
                break;

            // ... (Các case logout, startgame giữ nguyên) ...
            case "logout":
                 // ... Copy code cũ ...
                 System.out.println("[Logout start] " + username);
                 updateChips();
                 isLoggedIn = false;
                 table.sendDataToAll("logout#" + username);
                 table.inGamePlayers.remove(this);
                 table.players.remove(this);
                 loggedInUsers.remove(this);
                 if (table.players.size() > 0) updatePlayerIndices(table);
                 if (table.players.size() == 0) resetTableForNewGame(table);
                 else if (table.players.size() == 1) {
                     Server lastPlayer = table.players.get(0);
                     if (table.isStarted) {
                         lastPlayer.chips += table.pot;
                         table.sendDataToAll("winner#\" MUCK! " + lastPlayer.username + " won! Pot: " + table.pot + " \"");
                         table.sendDataToAll("chips#" + lastPlayer.username + "#" + lastPlayer.chips);
                         table.sendDataToAll("pot#0");
                         resetTableForNewGameKeepLeader(table, lastPlayer);
                     }
                 } else {
                     if (table.isStarted) {
                         table.whichPlayerTurn--;
                         if (table.whichPlayerTurn < 0) table.whichPlayerTurn = table.inGamePlayers.size() - 1;
                         if (table.checkNumber == table.inGamePlayers.size()) table.changeRound();
                     }
                 }
                break;

            case "logoutwait":
                // ... Code cũ ...
                isLoggedIn = false;
                loggedInUsers.remove(this);
                waitingUsers.remove(this);
                break;

            case "startgame":
                 // ... Code cũ ...
                 if (table != null && !table.isStarted) {
                    if (this.playerIndex != 0) {
                        out.println("message#Only the table leader can start the game!");
                    } else if (table.players.size() >= 2) {
                        table.isStarted = true;
                        table.startGame();
                    } else {
                        out.println("message#At least 2 player to start.");
                    }
                 }
                break;
        }
    }

    // ... (Giữ nguyên các hàm updateChips, validateLogin, etc.) ...
    private void updateChips() { /* Giữ nguyên */ 
        // Code đọc ghi file giữ nguyên
        File originalFile = new File("data.txt");
        if (!originalFile.exists()) return;
        // ... (copy y hệt logic cũ)
         try {
            FileReader fin = new FileReader(originalFile);
            Scanner in = new Scanner(fin);
            File tempFile = new File("tempdata.txt");
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile), true);
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] entry = line.split("#");
                if (entry.length != 3) { pw.println(line); continue; }
                int storedChips = Integer.parseInt(entry[2].trim());
                if (entry[0].trim().equals(username)) storedChips = chips;
                pw.println(entry[0].trim() + "#" + entry[1].trim() + "#" + storedChips);
            }
            in.close(); pw.close(); fin.close();
            originalFile.delete();
            tempFile.renameTo(originalFile);
        } catch (Exception e) {}
    }
    private boolean validateLogIn(String u, String p) { /* Giữ nguyên */ 
         try {
            Scanner in = new Scanner(new FileReader("data.txt"));
            while (in.hasNextLine()) {
                String[] entry = in.nextLine().split("#");
                if (entry[0].equals(u) && entry[1].equals(p)) {
                    this.chips = Integer.parseInt(entry[2]);
                    in.close(); return true;
                }
            }
            in.close();
        } catch (Exception e) {}
        return false;
    }
    private boolean validateSignUp(String u, String p) { /* Giữ nguyên */ 
        try {
            Scanner in = new Scanner(new FileReader("data.txt"));
            while (in.hasNextLine()) {
                if (in.nextLine().split("#")[0].equals(u)) { in.close(); return false; }
            }
            in.close();
            FileWriter fout = new FileWriter("data.txt", true);
            fout.write("\n" + u + "#" + p + "#10000");
            this.chips = 10000;
            fout.close();
            return true;
        } catch (Exception e) {}
        return false;
    }
    private void initiateUserEntry(String u) { /* Giữ nguyên */ 
        // Logic add table, gửi info giữ nguyên
        loggedInUsers.removeIf(user -> user.username != null && user.username.equals(u));
        isLoggedIn = true; username = u; loggedInUsers.add(this);
        out.println("login done#" + username + "#" + chips);
        if (!table1.isStarted && table1.players.size() < 5) {
            table = table1;
            // Gửi info ...
            for(Server p : table.players) out.println("opponentAdded#" + p.username + "#" + p.chips);
            this.playerIndex = table.players.size();
            table.players.add(this);
            for(Server p : table.players) if(!p.username.equals(u)) p.out.println("opponentAdded#" + u + "#" + chips);
            if(this.playerIndex == 0) out.println("isleader#true"); else out.println("isleader#false");
        } else if (!table2.isStarted && table2.players.size() < 5) {
             table = table2;
             // Tương tự ...
             for(Server p : table.players) out.println("opponentAdded#" + p.username + "#" + p.chips);
             this.playerIndex = table.players.size();
             table.players.add(this);
             for(Server p : table.players) if(!p.username.equals(u)) p.out.println("opponentAdded#" + u + "#" + chips);
             if(this.playerIndex == 0) out.println("isleader#true"); else out.println("isleader#false");
        } else {
            out.println("wait"); waitingUsers.add(this);
        }
    }
    public void decreaseChips(int i) { chips -= i; table.sendDataToAll("chips#" + this.username + "#" + chips); }
    public void sleep() { try { Thread.sleep(5000); } catch (Exception e) {} }

    public void handleAllIn(int amount) {
        if (amount <= 0) return;
        isAllIn = true;
        isTurn = false;
        selfBet += amount;
        chips = 0;
        table.pot += amount;
        if (selfBet > table.currentBet) {
            table.currentBet = selfBet;
            table.checkNumber = 0;
            table.sendDataToAll("currentbet#" + table.currentBet);
        }
        table.sendDataToAll("move#" + username + "#All-in " + amount);
        table.sendDataToAll("pot#" + table.pot);
        table.sendDataToAll("chips#" + username + "#0");
        table.changeTurn(); // Sẽ tự động xử lý việc skip hoặc run all cards
    }

    private void updatePlayerIndices(Table t) { /* Giữ nguyên */ 
         for (int i = 0; i < t.players.size(); i++) {
            Server p = t.players.get(i);
            int old = p.playerIndex;
            p.playerIndex = i;
            if (i == 0 && old != 0 && !t.isStarted) {
                p.out.println("isleader#true");
                p.out.println("message# You are Table Leader!");
            }
        }
    }
    private void notifyNewLeader(Table t) { /* Giữ nguyên */ }
    private void resetTableForNewGame(Table t) { /* Giữ nguyên */ 
        t.players.clear(); t.inGamePlayers.clear(); t.isStarted = false;
        t.pot = 0; t.currentBet = 0; t.checkNumber = 0; t.whichPlayerTurn = 0;
        t.sendDataToAll("cardReset");
    }
    private void resetTableForNewGameKeepLeader(Table t, Server l) { /* Giữ nguyên */ 
        t.players.clear(); t.inGamePlayers.clear(); t.players.add(l); l.playerIndex = 0;
        t.isStarted = false; t.pot = 0; t.currentBet = 0; t.checkNumber = 0; t.whichPlayerTurn = 0;
        t.sendDataToAll("cardReset");
        l.out.println("isleader#true");
        l.out.println("message# You are Table Leader!");
    }
    private int getTableNumber(Table t) {
        if (t == Server.table1) return 1;
        if (t == Server.table2) return 2;
        return -1;
    }
}