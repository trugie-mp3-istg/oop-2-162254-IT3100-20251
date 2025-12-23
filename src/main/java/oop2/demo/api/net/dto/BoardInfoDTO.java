package oop2.demo.api.net.dto;

import oop2.demo.api.features.Card;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class BoardInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public List<Card> cards;      // Danh sách bài chung trên bàn
    public BigDecimal currentBet; // Mức cược hiện tại
    public BigDecimal pot;        // Tổng tiền trong hũ

    public BoardInfoDTO(List<Card> cards, BigDecimal currentBet, BigDecimal pot) {
        this.cards = cards;
        this.currentBet = currentBet;
        this.pot = pot;
    }
}