package features;

import java.io.Serial;
import java.io.Serializable;

/**
 * Đại diện cho một lá bài tây 52 lá.
 * Implements Serializable để có thể gửi object này từ Server -> Client qua Socket.
 */
public class Card implements Comparable<Card>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // Định danh phiên bản khi gửi qua mạng

    public static final int NO_OF_RANKS = 13;
    public static final int NO_OF_SUITS = 4;

    // Các hằng số Rank (Thứ tự từ 0-12)
    public static final int ACE = 12, KING = 11, QUEEN = 10, JACK = 9, TEN = 8;
    public static final int NINE = 7, EIGHT = 6, SEVEN = 5, SIX = 4, FIVE = 3, FOUR = 2, THREE = 1, DEUCE = 0;

    // Các hằng số Suit (Ro, Co, Bich, Nhep)
    public static final int SPADES = 3, HEARTS = 2, CLUBS = 1, DIAMONDS = 0;

    public static final String[] RANK_SYMBOLS = { "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A" };
    public static final char[] SUIT_SYMBOLS = { 'r', 'c', 'b', 'n' };

    private final int rank;
    private final int suit;

    public Card(int rank, int suit) {
        if (rank < 0 || rank > NO_OF_RANKS - 1) throw new IllegalArgumentException("Invalid rank");
        if (suit < 0 || suit > NO_OF_SUITS - 1) throw new IllegalArgumentException("Invalid suit");
        this.rank = rank;
        this.suit = suit;
    }

    public Card(String s) {
        if (s == null || s.trim().length() != 2) throw new IllegalArgumentException("Invalid string");
        s = s.trim();
        String rankSymbol = s.substring(0, 1);
        char suitSymbol = s.charAt(1);

        int r = -1;
        for (int i = 0; i < NO_OF_RANKS; i++) if (rankSymbol.equals(RANK_SYMBOLS[i])) r = i;
        if (r == -1) throw new IllegalArgumentException("Unknown rank: " + rankSymbol);

        int su = -1;
        for (int i = 0; i < NO_OF_SUITS; i++) if (suitSymbol == SUIT_SYMBOLS[i]) su = i;
        if (su == -1) throw new IllegalArgumentException("Unknown suit: " + suitSymbol);

        this.rank = r;
        this.suit = su;
    }

    public int getSuit() { return suit; }
    public int getRank() { return rank; }

    @Override
    public int hashCode() { return (rank * NO_OF_SUITS + suit); }
    @Override
    public boolean equals(Object obj) { return obj instanceof Card && obj.hashCode() == hashCode(); }
    @Override
    public int compareTo(Card card) { return Integer.compare(this.hashCode(), card.hashCode()); }
    @Override
    public String toString() { return RANK_SYMBOLS[rank] + SUIT_SYMBOLS[suit]; }
}