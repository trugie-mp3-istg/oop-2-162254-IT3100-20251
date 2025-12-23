package oop2.demo.api.features;

/**
 * Các loại giá trị tay bài trong Poker.
 */
public enum HandValueType {

    /** Royal Flush (Straight Flush cao nhất với lá Át). */
    ROYAL_FLUSH("Royal Flush", 9),

    /** Straight Flush (Straight + Flush, không phải Át cao nhất). */
    STRAIGHT_FLUSH("Straight Flush", 8),

    /** Four of a Kind (4 lá bài cùng rank). */
    FOUR_OF_A_KIND("Four of a Kind", 7),

    /** Full House (1 bộ ba + 1 đôi). */
    FULL_HOUSE("Full House", 6),

    /** Flush (5 lá cùng chất). */
    FLUSH("Flush", 5),

    /** Straight (5 lá liên tiếp). */
    STRAIGHT("Straight", 4),

    /** Three of a Kind (3 lá cùng rank). */
    THREE_OF_A_KIND("Three of a Kind", 3),

    /** Two Pairs (2 đôi). */
    TWO_PAIRS("Two Pairs", 2),

    /** One Pair (1 đôi). */
    ONE_PAIR("One Pair", 1),

    /** High Card (lá bài cao nhất). */
    HIGH_CARD("High Card", 0);

    /** Mô tả loại tay bài */
    private final String description;

    /** Giá trị số dùng để so sánh tay bài */
    private final int value;

    /**
     * Khởi tạo loại giá trị tay bài
     *
     * @param description mô tả tay bài
     * @param value       giá trị số tương ứng
     */
    HandValueType(String description, int value) {
        this.description = description;
        this.value = value;
    }

    /**
     * @return mô tả tay bài
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return giá trị số của tay bài
     */
    public int getValue() {
        return value;
    }
}
