package oop2.demo.api.features;

import java.io.Serializable;

/**
 * Kết quả phân tích bài (bao gồm loại bài và điểm số cụ thể).
 * Class này được gửi về Client để hiển thị kết quả thắng thua.
 */
public class HandValue implements Comparable<HandValue>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Hand hand;
    private final HandValueType type;
    private final int value;

    public HandValue(Hand hand) {
        this.hand = hand;
        HandEvaluator evaluator = new HandEvaluator(hand);
        type = evaluator.getType();
        value = evaluator.getValue();
    }

    public Hand getHand() { return hand; }
    public HandValueType getType() { return type; }
    public String getDescription() { return type.getDescription(); }
    public int getValue() { return value; }

    @Override
    public int hashCode() { return value; }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HandValue && ((HandValue) obj).getValue() == value;
    }

    @Override
    public int compareTo(HandValue handValue) {
        // Sắp xếp giảm dần (Điểm cao đứng trước)
        return Integer.compare(handValue.getValue(), this.value);
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", type.getDescription(), value);
    }
}