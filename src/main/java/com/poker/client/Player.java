package com.poker.client;

public abstract class Player {
    protected String username;
    protected int chips; // Biến duy nhất giữ tiền

    public Player(String username, int chips) {
        this.username = username;
        this.chips = chips;
    }

    public String getUsername() {
        return username;
    }

    public int getChips() {
        return chips;
    }

    public abstract String getDisplayInfo();
}
