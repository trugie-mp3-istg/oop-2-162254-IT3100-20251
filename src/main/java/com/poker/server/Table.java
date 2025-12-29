package com.poker.server;

import java.util.ArrayList;
import java.util.List;

import com.poker.model.CommunityCards;
import com.poker.model.Deck;
import com.poker.model.Hand;

public class Table {

    public List<Server> inGamePlayers;
    public List<Server> players;
    private CommunityCards communityCards;
    private Deck deck;
    public int whichPlayerTurn;
    private int round;
    public int pot;
    public int currentBet;
    public int checkNumber;
    public boolean isStarted;

    Table(){
        players = new ArrayList<>();
        isStarted = false;
    }

    public void startGame() {
        inGamePlayers = new ArrayList<>();
        deck = new Deck();
        communityCards = new CommunityCards();
        currentBet = 0;
        pot = 0;

        for(int i=0; i<players.size(); i++){
            inGamePlayers.add(players.get(i));
            players.get(i).isAllIn = false; 
            players.get(i).isFolded = false; // Reset folded status
            players.get(i).selfBet = 0;      // Reset bet
        }


        for(int i = 0; i< inGamePlayers.size(); i++){

            Server player = inGamePlayers.get(i);

            player.hand = new Hand(deck);
            player.out.println("card#" +  player.hand.cards[0].toString() +
                    "#"+ player.hand.cards[1].toString());

            for(int j = 0; j< inGamePlayers.size(); j++){

                if(j == i) continue;

                inGamePlayers.get(j).out.println("opponentAdded#"+ player.username+
                        "#"+String.valueOf(player.chips) );
            }
        }

        /**pre flop starts**/

        sendDataToAll("round#" + "pre flop");
        round = 1;
        whichPlayerTurn = 0;
        initiateNewRound();

        currentBet = 10;

        // Big Blind logic (Assuming index 0 is BB for simplicity in this code)
        if (inGamePlayers.size() > 0) {
            Server bbPlayer = inGamePlayers.get(0);
            if (bbPlayer.chips >= currentBet) {
                bbPlayer.decreaseChips(currentBet);
                bbPlayer.selfBet = currentBet;
                bbPlayer.out.println("selfbet#" + String.valueOf(bbPlayer.selfBet));
                pot += currentBet;
            } else {
                // Handle case where player can't afford BB (All-in Blind)
                int allInAmt = bbPlayer.chips;
                bbPlayer.handleAllIn(allInAmt); // This will update pot/chips
            }
        }
        
        sendDataToAll("pot#"+ String.valueOf(pot));
        sendDataToAll("currentbet#" + String.valueOf(currentBet));

        changeTurn();
    }

    public void changeTurn(){

        // --- FIX LOGIC: Kiểm tra All-in và Tự động chạy bài ---
        int nonAllInCount = 0;
        for (Server p : inGamePlayers) {
            if (!p.isAllIn) nonAllInCount++;
        }

        // Nếu (Chỉ còn 0 người có tiền) HOẶC (Còn 1 người có tiền NHƯNG tiền cược đã bằng nhau)
        // => Không còn hành động nào có thể xảy ra nữa => SKIP đến Showdown
        if (nonAllInCount == 0 || (nonAllInCount == 1 && isBettingEqual())) {
            runOutAllCards(); 
            return;
        }
        // -----------------------------------------------------

        if(whichPlayerTurn==inGamePlayers.size()-1){
            whichPlayerTurn = 0;
        }
        else whichPlayerTurn++;

        // Skip all-in players (Loop until we find a player who is NOT All-in)
        // Safety check: count ensures we don't loop forever if everyone is all-in (handled above)
        while (inGamePlayers.get(whichPlayerTurn).isAllIn) {
            if(whichPlayerTurn==inGamePlayers.size()-1){
                whichPlayerTurn = 0;
            } else {
                whichPlayerTurn++;
            }
        }

        inGamePlayers.get(whichPlayerTurn).isTurn = true;
        sendDataToAll("whichPturn#"+ inGamePlayers.get(whichPlayerTurn).username);

    }
    
    // --- HELPER METHOD: Kiểm tra xem mọi người đã cược bằng nhau chưa ---
    private boolean isBettingEqual() {
        for (Server p : inGamePlayers) {
            // Nếu có người chưa All-in mà cược ít hơn mức hiện tại -> Chưa cân bằng
            if (!p.isAllIn && p.selfBet < currentBet) {
                return false;
            }
        }
        return true;
    }

    private void runOutAllCards() {
        sendDataToAll("removeaction");

        // Deal remaining cards automatically
        if (round < 2) {
            round = 2;
            communityCards.flop(deck);
            sendDataToAll("flop#" + communityCards.cards[0].toString() + "#" +
                    communityCards.cards[1].toString() + "#" + communityCards.cards[2].toString());
            sendDataToAll("round#" + "flop round");
            try { Thread.sleep(1000); } catch (Exception e){} // Delay effect
        }

        if (round < 3) {
            round = 3;
            communityCards.turn(deck);
            sendDataToAll("turn#" + communityCards.cards[3].toString());
            sendDataToAll("round#" + "turn round");
            try { Thread.sleep(1000); } catch (Exception e){}
        }

        if (round < 4) {
            round = 4;
            communityCards.river(deck);
            sendDataToAll("river#" + communityCards.cards[4].toString());
            sendDataToAll("round#" + "river round");
            try { Thread.sleep(1000); } catch (Exception e){}
        }

        compareAndReset();
    }


    public void changeRound(){
        
        // --- FIX LOGIC: Nếu vào vòng mới mà mọi người đã All-in hết rồi -> Run luôn
        int nonAllInCount = 0;
        for (Server p : inGamePlayers) {
            if (!p.isAllIn) nonAllInCount++;
        }
        if (nonAllInCount <= 1) {
            runOutAllCards();
            return;
        }
        // -------------------------------------------------------------------

        sendDataToAll("removeaction");

        if(round == 4) compareAndReset();

        else round++;

        if(round==2){                        //flop
            communityCards.flop(deck);
            initiateNewRound();
            sendDataToAll("flop#" + communityCards.cards[0].toString() + "#" +
                    communityCards.cards[1].toString() + "#" + communityCards.cards[2].toString()
            );
            sendDataToAll("round#" + "flop round");
        }

        if(round==3){                        //turn
            communityCards.turn(deck);
            initiateNewRound();
            sendDataToAll("turn#" + communityCards.cards[3].toString());
            sendDataToAll("round#" + "turn round");
        }

        if(round==4){                       //river
            communityCards.river(deck);
            initiateNewRound();
            sendDataToAll("river#" + communityCards.cards[4].toString());
            sendDataToAll("round#" + "river round");
        }
    }

    public void sendDataToAll(String data){
        for(Server player: players){
            player.out.println(data);
        }
    }

    public void sendDataToAllActive(String data){
        for(Server player: inGamePlayers){
            player.out.println(data);
        }
    }

    private void initiateNewRound(){
        checkNumber = 0;
        currentBet = 0;
        sendDataToAll("currentbet#" + String.valueOf(currentBet));

        for(int i = 0; i<inGamePlayers.size(); i++){
            inGamePlayers.get(i).selfBet = 0;
            inGamePlayers.get(i).out.println("selfbet#" +
                    String.valueOf(inGamePlayers.get(i).selfBet));
        }
    }

    public void compareAndReset(){

        inGamePlayers.get(0).isTurn = false;

        for (int j = 0; j < inGamePlayers.size(); j++) {        //getting community cards
            inGamePlayers.get(j).hand.getCommunityCards(communityCards.cards);
        }

        /* start comparing*/

        Server temp = inGamePlayers.get(0);
        for (int j = 0; j < inGamePlayers.size(); j++) {

            if (temp.hand.compareTo(inGamePlayers.get(j).hand) == -1)
                temp = inGamePlayers.get(j);

            sendDataToAll("cardshow#" + inGamePlayers.get(j).username + "#" +
                    inGamePlayers.get(j).hand.cards[0].toString() + "#"
                    + inGamePlayers.get(j).hand.cards[1].toString());
        }

        // --- FIX LOGIC: CỘNG TIỀN CHO NGƯỜI THẮNG ---
        // 1. Cộng tiền
        temp.chips += pot; 
        
        // 2. Thông báo thắng
        sendDataToAll("winner#Winner: " + temp.username + " Won with a " + temp.hand.display()+"!");

        // 3. Cập nhật tiền mới về Client
        sendDataToAll("chips#" + temp.username + "#" + temp.chips);

        // 4. Reset Pot
        pot = 0;
        sendDataToAll("pot#0");
        // --------------------------------------------

        /*wait in the interval**/
        for (Server player : players) {
            player.sleep();
        }

        /* getting new player from waiting room**/
        while (true) {
            getNewPlayers();

            if(players.size() > 1){ // Đổi điều kiện thành > 1 để chắc chắn có đối thủ
                sendDataToAll("cardReset");
                startGame();
                break;
            }
            else {
                // Nếu chỉ còn 1 người, gửi thông báo chờ
                if(players.size() == 1) {
                     players.get(0).out.println("message#Waiting for more players...");
                }
                // Chờ một chút rồi kiểm tra lại để tránh vòng lặp quá nhanh
                try { Thread.sleep(1000); } catch(Exception e){}
                continue;
            }
        }

    }

    private void getNewPlayers() {
        while(players.size() < 5 && Server.waitingUsers.size() > 0) {

            players.add(Server.waitingUsers.get(0));

            int j = players.size()-1;
            players.get(j).table = this;

            players.get(j).out.println("login done#" +
                    players.get(j).username + "#" +
                    String.valueOf(players.get(j).chips));

            Server.waitingUsers.remove(0);
        }
    }

    public void Reset(){

        Server player = inGamePlayers.get(0);
        player.isTurn = false;

        sendDataToAll("winner#" + "\" Muck! " + player.username + " Won"+ "!\"");
        player.chips += pot;
        pot = 0;

        sendDataToAll("pot#" + String.valueOf(pot));
        sendDataToAll("chips#" + player.username + "#" + String.valueOf(player.chips));

        /*wait in the interval*/
        for (Server playah : players) {
            playah.out.println("sleep");
            playah.sleep();
            playah.out.println("alive");

        }


        /* getting new player from waiting room**/
        while (true) {
            getNewPlayers();

            if(players.size() > 1){
                sendDataToAll("cardReset");
                startGame();
                break;
            }
             else {
                try { Thread.sleep(1000); } catch(Exception e){}
                continue;
            }
        }
    }
}