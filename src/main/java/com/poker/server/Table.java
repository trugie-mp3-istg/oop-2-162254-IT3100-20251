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
    public PotManager potManager;

    Table(){
        players = new ArrayList<>();
        isStarted = false;
        potManager = null;
    }

    public void startGame() {
        inGamePlayers = new ArrayList<>();
        deck = new Deck();
        communityCards = new CommunityCards();
        currentBet = 0;
        pot = 0;
        potManager = new PotManager(players);  // Initialize pot manager

        for(int i=0; i<players.size(); i++){
            inGamePlayers.add(players.get(i));
            players.get(i).isAllIn = false;  // Reset all-in status
        }


        for(int i = 0; i< inGamePlayers.size(); i++){

            Server player = inGamePlayers.get(i);

            player.isFolded = false;

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

        inGamePlayers.get(0).decreaseChips(currentBet);
        inGamePlayers.get(0).selfBet = currentBet;
        inGamePlayers.get(0).out.println("selfbet#" + String.valueOf(inGamePlayers.get(0).selfBet));
        pot += currentBet;
        sendDataToAll("pot#"+ String.valueOf(pot));
        sendDataToAll("currentbet#" + String.valueOf(currentBet));

        changeTurn();
    }

    public void changeTurn(){

        // Check if all remaining players are all-in
        int nonAllInCount = 0;
        for (Server p : inGamePlayers) {
            if (!p.isAllIn) nonAllInCount++;
        }
        
        // If all players are all-in, immediately deal all remaining cards and go to showdown
        if (nonAllInCount == 0) {
            runOutAllCards();  // Deal flop, turn, river automatically
            return;
        }

        if(whichPlayerTurn==inGamePlayers.size()-1){
            whichPlayerTurn = 0;
        }

        else whichPlayerTurn++;

        // Skip all-in players in turn order
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
    
    /**
     * When all players are all-in, automatically deal remaining community cards
     * and proceed to showdown
     */
    private void runOutAllCards() {
        sendDataToAll("removeaction");
        
        // Deal remaining cards: flop (round 2), turn (round 3), river (round 4)
        // Only deal cards for rounds we haven't reached yet
        
        // If we're before flop, deal flop
        if (round < 2) {
            round = 2;
            communityCards.flop(deck);
            initiateNewRound();
            sendDataToAll("flop#" + communityCards.cards[0].toString() + "#" +
                    communityCards.cards[1].toString() + "#" + communityCards.cards[2].toString());
            sendDataToAll("round#" + "flop round");
        }
        
        // Deal turn (always needed if we're at flop)
        if (round < 3) {
            round = 3;
            communityCards.turn(deck);
            initiateNewRound();
            sendDataToAll("turn#" + communityCards.cards[3].toString());
            sendDataToAll("round#" + "turn round");
        }
        
        // Deal river (final card)
        if (round < 4) {
            round = 4;
            communityCards.river(deck);
            initiateNewRound();
            sendDataToAll("river#" + communityCards.cards[4].toString());
            sendDataToAll("round#" + "river round");
        }
        
        // Now all cards are dealt, proceed to showdown
        compareAndReset();
    }


    public void changeRound(){

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

        // Distribute side pots if any all-in players exist
        java.util.Map<Server, Integer> winnings = new java.util.HashMap<>();
        boolean hasAllInPlayer = false;
        for (Server p : inGamePlayers) {
            if (p.isAllIn) {
                hasAllInPlayer = true;
                break;
            }
        }

        if (hasAllInPlayer && potManager != null) {
            // Distribute using side pots
            List<Server> winners = new ArrayList<>();
            winners.add(temp);
            winnings = potManager.distributeWinnings(winners);

            int totalWinnings = 0;
            for (int w : winnings.values()) {
                totalWinnings += w;
            }

            sendDataToAll("winner#" + "\" " + temp.username + " Won with a " + temp.hand.display() + "!\"");
            for (Server w : winnings.keySet()) {
                w.chips += winnings.get(w);
                sendDataToAll("chips#" + w.username + "#" + String.valueOf(w.chips));
            }
            pot = 0;
        } else {
            // Standard single pot distribution
            sendDataToAll("winner#" + "\" " + temp.username + " Won with a " + temp.hand.display() + "!\"");
            temp.chips += pot;
            pot = 0;
            sendDataToAll("chips#" + temp.username + "#" + String.valueOf(temp.chips));
        }

        sendDataToAll("pot#" + String.valueOf(pot));

        /*wait in the interval**/
        for (Server player : players) {
            player.sleep();
        }

        /* getting new player from waiting room**/

        while (true) {
            getNewPlayers();

            if(players.size()>1){
                sendDataToAll("cardReset");
                startGame();
                break;
            }
            else continue;
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

            if(players.size()>1){
                sendDataToAll("cardReset");
                startGame();
                break;
            }
            else continue;
        }
    }

    /**
     * Handles all-in situation: creates side pots when a player goes all-in.
     * @param player The player going all-in
     * @param betAmount The total bet amount (including previous bets in round)
     */
    public void handleAllIn(Server player, int betAmount) {
        if (potManager != null) {
            potManager.handleAllIn(player, betAmount);
            // Update pot display
            pot = getTotalPot();
        }
    }

    /**
     * Gets the total pot amount from pot manager.
     * @return Total amount in all pots
     */
    public int getTotalPot() {
        if (potManager != null) {
            return potManager.getTotalPot();
        }
        return pot;
    }

}




