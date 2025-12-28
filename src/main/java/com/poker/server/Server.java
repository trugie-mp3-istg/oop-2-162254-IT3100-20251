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

    public boolean isLoggedIn;
    public boolean isTurn;
    public boolean isFolded;
    public boolean isAllIn;

    public Hand hand;
    public String username;
    public int chips;
    public int selfBet;
    public int tableID;

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
                
                // Calculate how much more the player needs to bet
                int amountNeeded = table.currentBet - selfBet;
                
                // If player doesn't have enough chips, they go all-in with what they have
                if (amountNeeded > chips) {
                    amountNeeded = chips;  // Bet all remaining chips
                    isAllIn = true;  // Player is now all-in
                }
                
                decreaseChips(amountNeeded);
                table.pot += amountNeeded;
                selfBet += amountNeeded;  // Update bet, not replace it

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
                int raiseAmount = Integer.parseInt(message[1]);
                
                // Limit raise to available chips
                if (raiseAmount > chips) {
                    raiseAmount = chips;  // Can't raise more than you have
                    isAllIn = true;  // Going all-in
                }
                
                table.sendDataToAll("move#" + username + "#Raise " + raiseAmount);
                // Only count checks from non-all-in players
                table.checkNumber = 0;

                table.currentBet = raiseAmount;
                decreaseChips(raiseAmount - selfBet);
                table.pot += raiseAmount - selfBet;
                selfBet = raiseAmount;

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
                if(foldNonAllInCount > 0 && table.checkNumber == foldNonAllInCount)
                    table.changeRound();

                if(table.inGamePlayers.size() == 1){
                    table.Reset();
                }
                break;

            case "allin":
                int allInAmount = chips;  // All remaining chips go into pot
                handleAllIn(allInAmount);
                break;

            case "logout":
                updateChips();
                // Reset login status to allow re-login
                isLoggedIn = false;
                table.sendDataToAll("logout#" + username);

                if(isTurn){
                    isTurn = false;
                    table.changeTurn();
                    table.inGamePlayers.remove(this);
                    table.players.remove(this);
                    loggedInUsers.remove(this);
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
                        loggedInUsers.remove(this);

                        table.whichPlayerTurn--;
                        if(table.inGamePlayers.size() == 1){
                            table.Reset();
                        }
                    }
                    else {
                        table.players.remove(this);
                        loggedInUsers.remove(this);
                    }
                }
                break;

            case "logoutwait":
                isLoggedIn = false;
                loggedInUsers.remove(this);
                waitingUsers.remove(this);
                break;
        }
    }

    private void updateChips(){
        File originalFile = new File("data.txt"); // original file
        FileReader fin = null;
        try {
            fin = new FileReader(originalFile);
        } catch (FileNotFoundException e) {
            System.err.println("couldn't find file");
        }
        Scanner in = new Scanner(fin);

        File tempFile = new File("tempdata.txt"); // temporary file
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileWriter(tempFile),true);
        } catch (IOException e) {
            System.err.println("IO error in temp file");
        }

        String line;
        String [] entry;

        while(true){
            entry = in.nextLine().split("#");

            if(entry[0].equals(username)){
                entry[2]=String.valueOf(chips);
            }

            line = entry[0]+ "#"+ entry[1]+ "#"+ entry[2];
            System.out.println(line);

            pw.println(line);

            if(!in.hasNextLine()) break;
        }

        in.close();
        pw.close();

        if (!originalFile.delete()) {
            System.out.println("Could not delete file");
            return;
        }

        if (!tempFile.renameTo(originalFile))
            System.out.println("Could not rename file");
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
        for (Server user : loggedInUsers) {
            if (user.username.equals(username)) {
                out.println("decline"); // từ chối đăng nhập
                isLoggedIn = false;
                return;
            }
        }
        
        isLoggedIn = true;
        this.username = username;
        loggedInUsers.add(this);
        out.println("login done#"+username+"#"+String.valueOf(chips));

        if(!table1.isStarted){
            table = table1;
            table.players.add(this);

            if(table.players.size()==2){
                table.startGame();
                table.isStarted = true;
            }

        }

       else if(!table2.isStarted){
            table = table2;
            table.players.add(this);

            if(table.players.size()==2){
                table.startGame();
                table.isStarted = true;
            }
        }
        else {
            out.println("wait");
            waitingUsers.add(this);
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

}

