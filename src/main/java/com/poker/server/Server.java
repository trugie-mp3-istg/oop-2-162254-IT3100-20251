package com.poker.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

public class Server implements Runnable{

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

    public int playerIndex = -1; // Chỉ số vị trí trong bàn ( 0 = leader, 1, 2, 3, 4)

    private static List<Server> loggedInUsers;
    public static List<Server> waitingUsers;
    private static Table table1;
    private static Table table2;
    public Table table;


    Server(Socket socket){

        isLoggedIn = false;
        isFolded = true;
        isAllIn = false;
        username = null;
        chips = 0;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String [] args){

        loggedInUsers = new ArrayList<>();
        waitingUsers = new ArrayList<>();
        table1 = new Table();
        table2 = new Table();
        new Thread(() -> {
            try {
                // Mở cổng 8888 để nghe
                DatagramSocket udpSocket = new DatagramSocket(8888, java.net.InetAddress.getByName("0.0.0.0"));
                udpSocket.setBroadcast(true);

                byte[] buffer = new byte[1024];
                System.out.println(">>> Đã bật chức năng tìm kiếm IP (UDP Port 8888)");

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet); // Chờ Client hét lên "DISCOVER_..."

                    String message = new String(packet.getData(), 0, packet.getLength());

                    // Nếu đúng mật khẩu Client gửi
                    if (message.equals("DISCOVER_POKER_SERVER")) {
                        String response = "POKER_SERVER_HERE";
                        byte[] sendData = response.getBytes();

                        // Gửi trả lại IP cho Client đó
                        DatagramPacket sendPacket = new DatagramPacket(
                                sendData, sendData.length, packet.getAddress(), packet.getPort()
                        );
                        udpSocket.send(sendPacket);
                        System.out.println(">>> Đã gửi địa chỉ IP cho: " + packet.getAddress().getHostAddress());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Lỗi UDP: Không thể bật chức năng tìm kiếm IP.");
            }
        }).start(); // Nhớ có .start() để nó chạy song song
        try {
            ServerSocket ss = new ServerSocket(7777);
            while (true){
                Socket s = ss.accept();
                System.out.println("connected.");
                new Thread(new Server(s)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while(true){
            try {
                String data = in.readLine();
                
                // Nếu client disconnect (in.readLine() return null)
                if(data == null) {
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
    
    // Xử lí logout tự động nếu ngắt kết nối
    private void handleClientDisconnect() {
        System.out.println("Client disconnect: " + username);
        
        if(!isLoggedIn) return; // Chưa login thì không cần xử lý
        
        // Logout tự động
        isLoggedIn = false;
        
        if(table != null) {
            table.sendDataToAll("logout#" + username);
            
            if(isTurn){
                isTurn = false;
                table.changeTurn();
                table.inGamePlayers.remove(this);
                table.players.remove(this);
                table.whichPlayerTurn--;

                if(table.checkNumber == table.inGamePlayers.size())
                    table.changeRound();

                if(table.inGamePlayers.size() == 1){
                    table.Reset();
                }
            }
            else {
                if(!isFolded){
                    table.inGamePlayers.remove(this);
                    table.players.remove(this);
                    table.whichPlayerTurn--;
                    if(table.inGamePlayers.size() == 1){
                        table.Reset();
                    }
                }
                else {
                    table.players.remove(this);
                }
            }
        }
        
        loggedInUsers.remove(this);
        waitingUsers.remove(this);
    }



    private void parseData(String data) {

        String [] message = data.split("#");

        switch (message[0]){

            case "login":
                if(isLoggedIn)
                    break;

                if(validateLogIn(message[1],message[2])){
                    initiateUserEntry(message[1]);
                }
                else out.println("decline"); // invalid message
                break;

            case "signup":
                if(isLoggedIn)
                    break;

                if(validateSignUp(message[1],message[2])){
                    initiateUserEntry(message[1]);
                }
                else out.println("decline"); // invalid message
                break;

            case "call":
                table.sendDataToAll("move#" + username + "#Call");
                decreaseChips(table.currentBet - selfBet);
                table.pot += table.currentBet - selfBet;
                selfBet = table.currentBet;

                table.sendDataToAll("pot#" + String.valueOf(table.pot));
                table.changeTurn();
                 // Check if all non-all-in players have acted
                int callNonAllInCount = 0;
                int callNonAllInActedCount = 0;
                for (Server p : table.inGamePlayers) {
                    if (!p.isAllIn) {
                        callNonAllInCount++;
                        if (p.selfBet == table.currentBet) callNonAllInActedCount++;
                    }
                }
                if(callNonAllInCount > 0 && callNonAllInActedCount == callNonAllInCount) {
                    table.changeRound();
                }
                isTurn = false;
                break;

            case "check":
                table.sendDataToAll("move#" + username + "#Check");
                table.checkNumber++;
                table.changeTurn();
                // Count only non-all-in players for showdown check
                int checkNonAllInCount = 0;
                for (Server p : table.inGamePlayers) {
                    if (!p.isAllIn) checkNonAllInCount++;
                }
                if(table.checkNumber == checkNonAllInCount)
                    table.changeRound();
                isTurn = false;
                break;

            case "raise":
                table.sendDataToAll("move#" + username + "#Raise " + message[1]);
                //Only count non-all-in players for checkNumber
                table.checkNumber = 0;

                table.currentBet = Integer.parseInt(message[1]);
                decreaseChips(table.currentBet - selfBet);
                table.pot += table.currentBet - selfBet;
                selfBet = Integer.parseInt(message[1]);

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

               // Check if remaining players have all checked (only count non-all-in players)
                int foldNonAllInCount = 0;
                for (Server p : table.inGamePlayers) {
                    if (!p.isAllIn) foldNonAllInCount++;
                }
                if(foldNonAllInCount > 0 && table.checkNumber == foldNonAllInCount){
                    table.changeRound();
                }
                if(table.inGamePlayers.size() == 1){
                    table.Reset();
                }
                break;

             case "allin":
                int allInAmount = chips;  // All remaining chips go into pot
                handleAllIn(allInAmount);
                break;

            case "logout":
                System.out.println("[Logout start] " + username + " is logging out...");
                System.out.println("[Before logout] Table " + getTableNumber(table) + ": " 
                    + table.players.size() + " players, isStarted: " + table.isStarted);
                
                updateChips();
                // Reset login status to allow re-login
                isLoggedIn = false;
                table.sendDataToAll("logout#" + username);

                boolean wasLeader = (this.playerIndex == 0);
                boolean gameStarted = table.isStarted;
                Table affectedTable = table;

                // BƯỚC 1: Xóa người logout khỏi bàn
                table.inGamePlayers.remove(this);
                table.players.remove(this);
                loggedInUsers.remove(this);
                
                System.out.println("[After Remove] Table " + getTableNumber(affectedTable) + ": " 
                    + affectedTable.players.size() + " players remaining");

                // BƯỚC 2: Cập nhật playerIndex cho những người còn lại
                if(affectedTable.players.size() > 0) {
                    updatePlayerIndices(affectedTable);
                }
                
                // BƯỚC 3: KIỂM TRA SỐ NGƯỜI CÒN LẠI
                if(affectedTable.players.size() == 0) {
                    // ➜ Bàn trống hoàn toàn → reset để người mới vào
                    System.out.println("[Table empty] No players left. Resetting table for new players.");
                    resetTableForNewGame(affectedTable);
                }
                else if(affectedTable.players.size() == 1) {
                    // ➜ Chỉ còn 1 người
                    Server lastPlayer = affectedTable.players.get(0);
                    
                    if(gameStarted) {
                        // Game đang diễn ra → người đó thắng (MUCK) → RESET
                        System.out.println("[One player left] Game was started. " + lastPlayer.username + " wins!");
                        lastPlayer.chips += affectedTable.pot;
                        affectedTable.sendDataToAll("winner#\" MUCK! " + lastPlayer.username + " won! Pot: " 
                            + affectedTable.pot + " \"");
                        affectedTable.sendDataToAll("chips#" + lastPlayer.username + "#" + lastPlayer.chips);
                        affectedTable.sendDataToAll("pot#0");
                        
                        // QUAN TRỌNG: Reset để bàn trở về trạng thái ban đầu
                        // Nếu không reset, người mới vào sẽ thành leader mới thay vì lastPlayer
                        System.out.println("[Reset after game] Resetting table so " + lastPlayer.username 
                            + " remains as leader for next game");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            System.err.println("[ERROR] Sleep interrupted");
                        }
                        resetTableForNewGameKeepLeader(affectedTable, lastPlayer);
                    } else {
                        // Game chưa start → ĐỨ LẠI, KHÔNG RESET
                        System.out.println("[One player left] Game NOT started. Waiting for more players...");
                        lastPlayer.out.println("message# Chỉ còn bạn (Leader). Chờ người chơi khác vào...");
                        lastPlayer.out.println("message# Startgame sẽ sáng lại khi có người mới vào bàn");
                        
                        // QUAN TRỌNG: KHÔNG RESET → người mới vào sẽ join vào bàn này
                        System.out.println("[No reset] Waiting for new players to join Table " + getTableNumber(affectedTable));
                    }
                }
                else {
                    // ➜ Còn 2+ người → tiếp tục chơi
                    System.out.println("[Multiple players left] " + affectedTable.players.size() + " players remaining.");
                    
                    if(wasLeader) {
                        // Leader logout → gán leader mới (người đầu tiên trong list)
                        System.out.println("[Leader changed] Old leader logged out. New leader assigned.");
                        notifyNewLeader(affectedTable);
                    }
                    
                    // Nếu game đang diễn ra, xử lý lượt chơi
                    if(gameStarted) {
                        System.out.println("[Game ongoing] Adjusting turn...");
                        affectedTable.whichPlayerTurn--;
                        if(affectedTable.whichPlayerTurn < 0) {
                            affectedTable.whichPlayerTurn = affectedTable.inGamePlayers.size() - 1;
                        }
                        if(affectedTable.checkNumber == affectedTable.inGamePlayers.size())
                            affectedTable.changeRound();
                    }
                }
                
                System.out.println("[Logout end] " + username + " logged out. Table " 
                    + getTableNumber(affectedTable) + ": " + affectedTable.players.size() + " players, isStarted: " 
                    + affectedTable.isStarted);
                break;

            case "logoutwait":
                System.out.println("[Logout wait] " + username + " leaves waiting room");
                isLoggedIn = false;
                loggedInUsers.remove(this);
                waitingUsers.remove(this);
                break;
            
            case "startgame": // Thêm start
                if (table != null && !table.isStarted) {
                    if(this.playerIndex != 0) {
                        out.println("message#Only the table leader can start the game!");
                        System.out.println("[Error] " + username + " tried to start game but is not the leader!");
                        break;
                    }

                    if (table.players.size() >= 2) { // Tối thiểu 2 người
                        table.startGame();
                        table.isStarted = true;
                        System.out.println("Game started by " + username + " (Leader)");
                        table.sendDataToAll("message#Game started!");
                    } else {
                        out.println("message#At least 2 player to start.");
                    }
                } else {
                    out.println("message#Game has been started or error!");
                }
                break;
        }
    }

    private void updateChips(){
        File originalFile = new File("data.txt");
        
        if(!originalFile.exists()) {
            System.err.println("[ERROR] data.txt not found!");
            return;
        }
        
        FileReader fin = null;
        Scanner in = null;
        PrintWriter pw = null;
        
        try {
            fin = new FileReader(originalFile);
            in = new Scanner(fin);
            
            File tempFile = new File("tempdata.txt");
            pw = new PrintWriter(new FileWriter(tempFile), true);

            String line;
            String[] entry;

            while(in.hasNextLine()){
                line = in.nextLine().trim();
                
                if(line.isEmpty()) continue;
                
                entry = line.split("#");
                
                if(entry.length != 3) {
                    pw.println(line);
                    continue;
                }
                
                int storedChips = 0;
                try {
                    storedChips = Integer.parseInt(entry[2].trim());
                    if(storedChips < 0) storedChips = 0;
                } catch (NumberFormatException e) {
                    System.err.println("[ERROR] Invalid chips: " + entry[2]);
                    storedChips = 1000;
                }
                
                if(entry[0].trim().equals(username)){
                    storedChips = chips;
                    System.out.println("[Chips Updated] " + username + " → " + chips + " chips");
                }
                
                line = entry[0].trim() + "#" + entry[1].trim() + "#" + storedChips;
                pw.println(line);
            }

            in.close();
            pw.close();
            fin.close();

            if (!originalFile.delete()) {
                System.err.println("[ERROR] Could not delete original file");
                return;
            }

            if (!tempFile.renameTo(originalFile)) {
                System.err.println("[ERROR] Could not rename temp file");
            } else {
                System.out.println("[SUCCESS] data.txt updated!");
            }
            
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR] File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[ERROR] IO Exception: " + e.getMessage());
        } finally {
            try {
                if(in != null) in.close();
                if(fin != null) fin.close();
                if(pw != null) pw.close();
            } catch (IOException e) {
                System.err.println("[ERROR] Error closing resources: " + e.getMessage());
            }
        }
    }

    private boolean validateLogIn(String username, String password) {

        FileReader fin = null;
        try {
            fin = new FileReader("data.txt");
        } catch (FileNotFoundException e) {
            System.err.println("couldn't read file");
        }

        Scanner in = new Scanner(fin);
        String [] entry;

        while(in.hasNextLine()){
            entry = in.nextLine().split("#");

            if(entry[0].equals(username) && entry[1].equals(password)){
                in.close();
                this.chips = Integer.parseInt(entry[2]);
                return true;
            }
        }

        in.close();
        return false;
    }

    private boolean validateSignUp(String username, String password) {

        // checking whether same username already exists
        FileReader fin = null;
        try {
            fin = new FileReader("data.txt");
        } catch (FileNotFoundException e) {
            System.err.println("couldn't read file");
        }

        Scanner in = new Scanner(fin);
        String [] entry;

        while(in.hasNextLine()){
            entry = in.nextLine().split("#");

            if(entry[0].equals(username)){
                in.close();
                return false;
            }
        }
        in.close();

        // adding new member info to file
        try {
            FileWriter fout = new FileWriter("data.txt",true);
            fout.write("\n"+username+"#"+password+"#1000");
            this.chips = 10000;
            fout.close();
        } catch (IOException e) {
            System.err.println("couldn't open file to read");
        }

        return true;
    }

    private void initiateUserEntry(String username){
        // Kiểm tra xem username này đã đăng nhập chưa
        // Nếu có, hãy loại bỏ entry cũ (có thể là từ kết nối trước đó)
        loggedInUsers.removeIf(user -> user.username != null && user.username.equals(username));
        
        isLoggedIn = true;
        this.username = username;
        loggedInUsers.add(this);
        out.println("login done#"+username+"#"+String.valueOf(chips));
        

        // Kiểm tra Table 1 có chỗ trống không
        if(!table1.isStarted && table1.players.size() < 5){
            table = table1;
            
            // ← BƯỚC 1: Gửi opponentAdded cho NGƯỜI MỚI
            // Để họ thấy những người ĐANG CÓ trong bàn
            System.out.println("[Send opponents to new player] " + username);
            for(Server existingPlayer : table.players) {
                System.out.println("  → Sending opponent: " + existingPlayer.username);
                out.println("opponentAdded#" + existingPlayer.username + "#" + existingPlayer.chips);
            }
            
            // ← BƯỚC 2: THÊM người mới vào bàn
            this.playerIndex = table.players.size();
            table.players.add(this);
            System.out.println("[Table 1] " + username + " joins! (" + table.players.size() + "/5) - Index: " + playerIndex);
            
            // ← BƯỚC 3: Gửi opponentAdded cho NHỮNG NGƯỜI CÓ TRONG BÀN
            // Để họ thấy NGƯỜI MỚI VỪA VÀO
            System.out.println("[Send new player to others] " + username);
            for(Server existingPlayer : table.players) {
                // ← QUAN TRỌNG: Không gửi cho chính người vừa join!
                if(!existingPlayer.username.equals(username)) {
                    System.out.println("  → Sending to: " + existingPlayer.username);
                    existingPlayer.out.println("opponentAdded#" + username + "#" + chips);
                }
            }
            
            // Gửi message cho TẤT CẢ
            table.sendDataToAll("message#" + username + " joins Table 1! (" + table.players.size() + "/5)");
            
            // Xác định leader
            if(this.playerIndex == 0) {
                out.println("isleader#true");
                System.out.println("[Table 1] " + username + " is the TABLE LEADER (Index 0)");
            }
            else {
                out.println("isleader#false");
                System.out.println("[Table 1] " + username + " is NOT the leader (Index " + playerIndex + ")");
            }
        }
        // Nếu Table 1 đã đầy hoặc đã bắt đầu, kiểm tra Table 2
        else if(!table2.isStarted && table2.players.size() < 5){
            table = table2;
            
            // ← BƯỚC 1: Gửi opponentAdded cho NGƯỜI MỚI
            System.out.println("[Send Opponents to New Player] " + username);
            for(Server existingPlayer : table.players) {
                System.out.println("  → Sending opponent: " + existingPlayer.username);
                out.println("opponentAdded#" + existingPlayer.username + "#" + existingPlayer.chips);
            }
            
            // ← BƯỚC 2: THÊM người mới vào bàn
            this.playerIndex = table.players.size();
            table.players.add(this);
            System.out.println("[Table 2] " + username + " joins! (" + table.players.size() + "/5) - Index: " + playerIndex);
            
            // ← BƯỚC 3: Gửi opponentAdded cho NHỮNG NGƯỜI CÓ TRONG BÀN
            System.out.println("[Send New Player to Others] " + username);
            for(Server existingPlayer : table.players) {
                if(!existingPlayer.username.equals(username)) {
                    System.out.println("  → Sending to: " + existingPlayer.username);
                    existingPlayer.out.println("opponentAdded#" + username + "#" + chips);
                }
            }
            
            // Gửi message cho TẤT CẢ
            table.sendDataToAll("message#" + username + " joins Table 2! (" + table.players.size() + "/5)");
            
            // Xác định leader
            if(this.playerIndex == 0) {
                out.println("isleader#true");
                System.out.println("[Table 2] " + username + " is the TABLE LEADER (Index 0)");
            }
            else {
                out.println("isleader#false");
                System.out.println("[Table 2] " + username + " is NOT the leader (Index " + playerIndex + ")");
            }
        }
            // Cả 2 bàn đầy hoặc đã bắt đầu, vào waiting room
        else {
            out.println("wait");
            waitingUsers.add(this);
            System.out.println("[Waiting room] " + username + " joins waiting room. Total waiting: " + waitingUsers.size());
        }
    }

    public void decreaseChips(int i) {

        chips -= i;
        table.sendDataToAll("chips#" + this.username+ "#" +String.valueOf(chips));
    }

    public void sleep(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.err.println("couldn't sleep");
        }
    }

    /**
     * Handles the all-in action: player bets all remaining chips.
     * @param amount The amount going all-in
     */
    public void handleAllIn(int amount) {
        if (amount <= 0) return;

         // Mark player as all-in
        isAllIn = true;
        isTurn = false;

        // Deduct chips and add to pot
        decreaseChips(amount);
        int actualBet = amount + selfBet;  // Total bet including previous

        // Update selfBet to reflect total commitment
        selfBet = actualBet;

        // Notify table to handle side pot calculation
        table.handleAllIn(this, actualBet);

        // Broadcast all-in action to all clients
        table.sendDataToAll("move#" + username + "#All-in " + amount);
        table.sendDataToAll("pot#" + String.valueOf(table.getTotalPot()));
        table.sendDataToAll("chips#" + username + "#" + this.chips);

        // Advance to next player
        table.changeTurn();
    }


    private void updatePlayerIndices(Table table) {
        if(table.players.size() == 0) {
            System.out.println("[Update indices] Table is empty!");
            return;
        }
        
        System.out.println("[Update indices] Reassigning indices for " + table.players.size() + " players");
        
        for(int i = 0; i < table.players.size(); i++) {
            Server player = table.players.get(i);
            int oldIndex = player.playerIndex;
            player.playerIndex = i;
            
            System.out.println("[Index change] " + player.username + ": " + oldIndex + " → " + i);
            
            // Nếu trở thành index 0 (leader mới)
            if(i == 0 && oldIndex != 0) {
                System.out.println("[New leader] " + player.username + " is now leader! (Index 0)");
                if(!table.isStarted) {
                    player.out.println("isleader#true");
                    player.out.println("message# You are Table Leader! Click 'Start game' when ready");
                }
            }
            // Những người khác không phải leader
            else if(i != 0 && oldIndex == 0) {
                System.out.println("[Lost Leader] " + player.username + " is no longer leader");
                player.out.println("isleader#false");
            }
        }
    }

    private void notifyNewLeader(Table table) {
        if(table.players.size() == 0) {
            System.out.println("[No leader] Table is empty!");
            return;
        }
        
        if(table.isStarted) {
            System.out.println("[Leader not changed] Game already started, no leader change!");
            return;
        }
        
        Server newLeader = table.players.get(0);
        System.out.println("[New Leader Notification] " + newLeader.username 
            + " (Index 0) is now the TABLE LEADER!");
        
        // Gửi cho leader mới
        newLeader.out.println("isleader#true");
        newLeader.out.println("message# You are Table Leader! You can start the game");
        
        // Gửi cho những người khác
        for(int i = 1; i < table.players.size(); i++) {
            Server player = table.players.get(i);
            player.out.println("isleader#false");
            player.out.println("message# " + newLeader.username + " is leader. Wait for starting...");
        }
        
        // Gửi thông báo chung
        table.sendDataToAll("message# New Leader: " + newLeader.username);
        
        System.out.println("[Leader notification sent]");
    }

    private void resetTableForNewGame(Table table) {
        System.out.println("[Reset table start] Resetting table " + getTableNumber(table) + "...");
        
        // Xóa tất cả player
        table.players.clear();
        table.inGamePlayers.clear();
        
        // Set isStarted = FALSE
        table.isStarted = false;
        
        // Reset game state
        table.pot = 0;
        table.currentBet = 0;
        table.checkNumber = 0;
        table.whichPlayerTurn = 0;
        
        // Gửi cardReset để client clear UI
        table.sendDataToAll("cardReset");
        
        System.out.println("[Reset table end] Table " + getTableNumber(table) 
            + " is ready for new players! (isStarted = false)");
    }

    private void resetTableForNewGameKeepLeader(Table table, Server leader) {
        System.out.println("[Reset table keep leader] Resetting table " + getTableNumber(table) 
            + " with " + leader.username + " as leader...");
        
        // XÓA tất cả player
        table.players.clear();
        table.inGamePlayers.clear();
        
        // THÊM LẠI leader với index 0
        table.players.add(leader);
        leader.playerIndex = 0;
        
        // Set isStarted = FALSE
        table.isStarted = false;
        
        // Reset game state
        table.pot = 0;
        table.currentBet = 0;
        table.checkNumber = 0;
        table.whichPlayerTurn = 0;
        
        // Gửi cardReset để client clear UI
        table.sendDataToAll("cardReset");
        
        // Thông báo
        leader.out.println("isleader#true");
        leader.out.println("message# You are Table Leader! You can start the game now. Wait for others to join...");
        
        System.out.println("[Reset table end] Table " + getTableNumber(table) 
            + " ready! " + leader.username + " is still the LEADER (isStarted = false)");
    }

    private int getTableNumber(Table table) {
        if(table == Server.table1) return 1;
        if(table == Server.table2) return 2;
        return -1;
    }
}

