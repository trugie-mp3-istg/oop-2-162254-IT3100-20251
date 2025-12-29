package com.poker.client;

public class BasePlayer extends Player {

    public BasePlayer(String username, int chips) {
        super(username, chips); // Gọi constructor của cha để gán tên và tiền
    }

    @Override
    public String getDisplayInfo() {
        // Ví dụ: Hiển thị tên kèm số tiền định dạng đẹp
        // "Alex ($1000)"
        return this.username + " ($" + this.chips + ")";
    }
}
