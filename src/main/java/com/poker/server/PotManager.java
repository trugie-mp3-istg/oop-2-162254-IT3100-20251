package com.poker.server;

import java.util.ArrayList;
import java.util.List;

/**
 * PotManager handles side pot calculations and distribution.
 * Manages main pot and side pots when players go all-in.
 */
public class PotManager {

    public class SidePot {
        public int amount;
        public List<Server> eligiblePlayers;

        public SidePot(int amount) {
            this.amount = amount;
            this.eligiblePlayers = new ArrayList<>();
        }
    }

    private List<SidePot> sidePots;
    private List<Server> activePlayers;

    public PotManager(List<Server> players) {
        this.sidePots = new ArrayList<>();
        this.activePlayers = new ArrayList<>(players);
    }

    /**
     * Adds a bet from a player and recalculates pots if necessary.
     * @param player The player making the bet
     * @param amount The amount being bet
     */
    public void addBet(Server player, int amount) {
        // If no pots exist yet, create main pot
        if (sidePots.isEmpty()) {
            SidePot mainPot = new SidePot(0);
            mainPot.eligiblePlayers.addAll(activePlayers);
            sidePots.add(mainPot);
        }

        // Find which pot(s) this bet goes into
        int remainingBet = amount;
        for (SidePot pot : sidePots) {
            if (remainingBet <= 0) break;
            pot.amount += remainingBet;
            remainingBet = 0;
        }
    }

    /**
     * Handles all-in situation: creates side pots when a player goes all-in.
     * @param player The player going all-in
     * @param allInAmount The all-in bet amount
     */
    public void handleAllIn(Server player, int allInAmount) {
        // Reconstruct pots based on all-in amount
        List<SidePot> newPots = new ArrayList<>();
        int previousThreshold = 0;

        // Create a sorted list of all bet amounts
        List<Integer> betAmounts = new ArrayList<>();
        betAmounts.add(allInAmount);
        for (Server p : activePlayers) {
            if (p != player && p.selfBet > 0 && p.selfBet != allInAmount) {
                if (!betAmounts.contains(p.selfBet)) {
                    betAmounts.add(p.selfBet);
                }
            }
        }

        java.util.Collections.sort(betAmounts);

        // Build side pots based on bet thresholds
        for (int threshold : betAmounts) {
            int potSize = 0;
            List<Server> potEligible = new ArrayList<>();

            for (Server p : activePlayers) {
                if (p.isFolded) continue; // Folded players don't contribute or win

                int contribution = Math.min(p.selfBet, threshold) - previousThreshold;
                if (contribution > 0) {
                    potSize += contribution;
                }

                // Player is eligible for this pot if they bet at least up to this threshold
                if (p.selfBet >= threshold || p.isAllIn) {
                    if (!potEligible.contains(p)) {
                        potEligible.add(p);
                    }
                }
            }

            if (potSize > 0) {
                SidePot pot = new SidePot(potSize);
                pot.eligiblePlayers = potEligible;
                newPots.add(pot);
            }

            previousThreshold = threshold;
        }

        sidePots = newPots;
    }

    /**
     * Gets the total pot amount.
     * @return Total amount in all pots
     */
    public int getTotalPot() {
        int total = 0;
        for (SidePot pot : sidePots) {
            total += pot.amount;
        }
        return total;
    }

    /**
     * Distributes winnings from pots to the winner(s).
     * @param winners List of winning players (for ties)
     * @return Map of player to winnings
     */
    public java.util.Map<Server, Integer> distributeWinnings(List<Server> winners) {
        java.util.Map<Server, Integer> winnings = new java.util.HashMap<>();

        for (Server winner : winners) {
            winnings.put(winner, 0);
        }

        // For each pot, distribute to eligible winners
        for (SidePot pot : sidePots) {
            List<Server> eligibleWinners = new ArrayList<>();

            // Find which winners are eligible for this pot
            for (Server winner : winners) {
                if (pot.eligiblePlayers.contains(winner)) {
                    eligibleWinners.add(winner);
                }
            }

            // Distribute pot among eligible winners
            if (!eligibleWinners.isEmpty()) {
                int amountPerWinner = pot.amount / eligibleWinners.size();
                int remainder = pot.amount % eligibleWinners.size();

                for (int i = 0; i < eligibleWinners.size(); i++) {
                    Server winner = eligibleWinners.get(i);
                    int amount = amountPerWinner + (i == 0 ? remainder : 0);
                    winnings.put(winner, winnings.get(winner) + amount);
                }
            }
        }

        return winnings;
    }

    /**
     * Gets all side pots for UI display.
     * @return List of side pots
     */
    public List<SidePot> getSidePots() {
        return new ArrayList<>(sidePots);
    }

    /**
     * Resets pots for a new hand.
     */
    public void reset() {
        sidePots.clear();
    }

    /**
     * Updates active players (e.g., when someone folds).
     * @param players Updated list of active players
     */
    public void updateActivePlayers(List<Server> players) {
        this.activePlayers = new ArrayList<>(players);
    }
}
