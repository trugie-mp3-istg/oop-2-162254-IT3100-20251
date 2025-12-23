package oop2.demo.api.features;

import java.io.Serializable;

/**
 * Đại diện cho một lá bài tây trong bộ bài 52 lá.
 * <p>
 * Lớp này implements Serializable để có thể truyền object
 * từ Server sang Client thông qua Socket.
 */
public class Card implements Comparable<Card>, Serializable {

    private static final long serialVersionUID = 1L;

    // Số lượng rank trong bộ bài
    public static final int NO_OF_RANKS = 13;

    // Số lượng suit trong bộ bài
    public static final int NO_OF_SUITS = 4;

    // Các hằng số rank
    public static final int ACE      = 12;
    public static final int KING     = 11;
    public static final int QUEEN    = 10;
    public static final int JACK     = 9;
    public static final int TEN      = 8;
    public static final int NINE     = 7;
    public static final int EIGHT    = 6;
    public static final int SEVEN    = 5;
    public static final int SIX      = 4;
    public static final int FIVE     = 3;
    public static final int FOUR     = 2;
    public static final int THREE    = 1;
    public static final int DEUCE    = 0;

    // Các hằng số suit
    public static final int SPADES   = 3;
    public static final int HEARTS   = 2;
    public static final int CLUBS    = 1;
    public static final int DIAMONDS = 0;

    /** Ký hiệu của các rank. */
    public static final String[] RANK_SYMBOLS = {
            "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"
    };

    /** Ký hiệu của các suit. */
    public static final char[] SUIT_SYMBOLS = { 'r', 'n', 'c', 'b' };

    /** Rank của lá bài. */
    private final int rank;

    /** Suit của lá bài. */
    private final int suit;

    /**
     * Hàm khởi tạo dựa trên rank và suit.
     *
     * @param rank
     *            Giá trị rank của lá bài.
     * @param suit
     *            Giá trị suit của lá bài.
     *
     * @throws IllegalArgumentException
     *             Nếu rank hoặc suit không hợp lệ.
     */
    public Card(int rank, int suit) {
        if (rank < 0 || rank > NO_OF_RANKS - 1) {
            throw new IllegalArgumentException("Invalid rank");
        }
        if (suit < 0 || suit > NO_OF_SUITS - 1) {
            throw new IllegalArgumentException("Invalid suit");
        }
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Hàm khởi tạo dựa trên chuỗi biểu diễn lá bài.
     * <p>
     * Chuỗi phải có cấu trúc hợp lệ gồm 2 ký tự:
     * ký tự đầu là rank, ký tự sau là suit.
     *
     * @param s
     *            Ví dụ: 3C (3 cơ), 7R (7 rô)
     *
     * @throws IllegalArgumentException
     *             Nếu chuỗi null hoặc không hợp lệ.
     */
    public Card(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Null string or of invalid length");
        }
        s = s.trim();
        if (s.length() != 2) {
            throw new IllegalArgumentException("Empty string or invalid length");
        }

        // Xác định rank
        String rankSymbol = s.substring(0, 1);
        char suitSymbol = s.charAt(1);
        int rank = -1;
        for (int i = 0; i < NO_OF_RANKS; i++) {
            if (rankSymbol.equals(RANK_SYMBOLS[i])) {
                rank = i;
                break;
            }
        }
        if (rank == -1) {
            throw new IllegalArgumentException("Unknown rank: " + rankSymbol);
        }

        // Xác định suit
        int suit = -1;
        for (int i = 0; i < NO_OF_SUITS; i++) {
            if (suitSymbol == SUIT_SYMBOLS[i]) {
                suit = i;
                break;
            }
        }
        if (suit == -1) {
            throw new IllegalArgumentException("Unknown suit: " + suitSymbol);
        }

        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Trả về suit của lá bài.
     *
     * @return suit.
     */
    public int getSuit() {
        return suit;
    }

    /**
     * Trả về rank của lá bài.
     *
     * @return rank.
     */
    public int getRank() {
        return rank;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (rank * NO_OF_SUITS + suit);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Card && obj.hashCode() == hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Card card) {
        int thisValue = hashCode();
        int otherValue = card.hashCode();
        return Integer.compare(thisValue, otherValue);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return RANK_SYMBOLS[rank] + SUIT_SYMBOLS[suit];
    }

}
