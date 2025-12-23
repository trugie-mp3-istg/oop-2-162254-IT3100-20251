package oop2.demo.api.features;

import java.io.Serializable;

/**
 * Đại diện cho giá trị của một tay bài Poker. <br />
 * <br />
 *
 * Lớp này cài đặt interface {@code Comparable} với thứ tự so sánh giảm dần
 * (tay bài mạnh hơn sẽ đứng trước).
 */
public class HandValue implements Comparable<HandValue>, Serializable {

    private static final long serialVersionUID = 1L;

    /** Tay bài tương ứng. */
    private final Hand hand;

    /** Kiểu giá trị tay bài (High Card, Pair, Flush, ...). */
    private final HandValueType type;

    /** Giá trị số chính xác của tay bài (dùng để so sánh). */
    private final int value;

    /**
     * Hàm khởi tạo.
     *
     * @param hand
     *            Tay bài cần được đánh giá.
     */
    public HandValue(Hand hand) {
        this.hand = hand;
        HandEvaluator evaluator = new HandEvaluator(hand);
        type = evaluator.getType();
        value = evaluator.getValue();
    }

    /**
     * Trả về tay bài.
     *
     * @return Tay bài.
     */
    public Hand getHand() {
        return hand;
    }

    /**
     * Trả về loại tay bài.
     *
     * @return Kiểu tay bài.
     */
    public HandValueType getType() {
        return type;
    }

    /**
     * Trả về mô tả của loại tay bài.
     *
     * @return Chuỗi mô tả tay bài.
     */
    public String getDescription() {
        return type.getDescription();
    }

    /**
     * Trả về giá trị số chính xác của tay bài.
     *
     * @return Giá trị tay bài.
     */
    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HandValue
                && ((HandValue) obj).getValue() == value;
    }

    /**
     * So sánh hai tay bài theo thứ tự giảm dần.
     *
     * @param handValue
     *            Tay bài cần so sánh.
     * @return
     *            -1 nếu tay bài này mạnh hơn,
     *             1 nếu yếu hơn,
     *             0 nếu bằng nhau.
     */
    @Override
    public int compareTo(HandValue handValue) {
        if (value > handValue.getValue()) {
            return -1;
        } else if (value < handValue.getValue()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", type.getDescription(), value);
    }
}
