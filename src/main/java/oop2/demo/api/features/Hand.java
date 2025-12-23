package oop2.demo.api.features;

import java.io.Serializable;
import java.util.Collection;

/**
 * Đại diện cho một tay bài (hand) tổng quát trong game. <br />
 * <br />
 *
 * Các lá bài được sắp xếp theo thứ tự từ mạnh đến yếu. <br />
 * <br />
 *
 * <b>LƯU Ý:</b> Lớp này được cài đặt với trọng tâm là hiệu năng
 * (thay vì thiết kế hướng đối tượng thuần túy).
 */
public class Hand implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Số lượng lá bài tối đa trong một tay bài. */
    private static final int MAX_NO_OF_CARDS = 7;

    /** Các lá bài trong tay bài. */
    private Card[] cards = new Card[MAX_NO_OF_CARDS];

    /** Số lượng lá bài hiện tại trong tay. */
    private int noOfCards = 0;

    /**
     * Hàm khởi tạo tay bài rỗng.
     */
    public Hand() {
        // Không có xử lý khởi tạo đặc biệt.
    }

    /**
     * Hàm khởi tạo với mảng các lá bài ban đầu.
     *
     * @param cards
     *            Mảng các lá bài ban đầu.
     *
     * @throws IllegalArgumentException
     *             Nếu mảng null hoặc số lượng lá bài không hợp lệ.
     */
    public Hand(Card[] cards) {
        addCards(cards);
    }

    /**
     * Hàm khởi tạo với một tập hợp các lá bài ban đầu.
     *
     * @param cards
     *            Tập hợp các lá bài ban đầu.
     */
    public Hand(Collection<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Null collection");
        }
        for (Card card : cards) {
            addCard(card);
        }
    }

    /**
     * Hàm khởi tạo từ chuỗi biểu diễn các lá bài.
     *
     * Chuỗi phải chứa ít nhất một lá bài.
     * Mỗi lá bài được biểu diễn bằng rank và suit.
     * Các lá bài được phân tách bằng dấu cách.
     *
     * Ví dụ: "Kh 7d 4c As Js"
     *
     * @param s
     *            Chuỗi cần phân tích.
     *
     * @throws IllegalArgumentException
     *             Nếu chuỗi không hợp lệ hoặc số lượng lá bài vượt quá giới hạn.
     */
    public Hand(String s) {
        if (s == null || s.length() == 0) {
            throw new IllegalArgumentException("Null or empty string");
        }

        String[] parts = s.split("\\s");
        if (parts.length > MAX_NO_OF_CARDS) {
            throw new IllegalArgumentException("Too many cards in hand");
        }
        for (String part : parts) {
            addCard(new Card(part));
        }
    }

    /**
     * Trả về số lượng lá bài hiện tại.
     *
     * @return Số lượng lá bài.
     */
    public int size() {
        return noOfCards;
    }

    /**
     * Thêm một lá bài vào tay bài.
     *
     * Lá bài sẽ được chèn vào đúng vị trí để đảm bảo
     * tay bài luôn được sắp xếp từ mạnh đến yếu.
     *
     * @param card
     *            Lá bài cần thêm.
     *
     * @throws IllegalArgumentException
     *             Nếu lá bài null.
     */
    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Null card");
        }

        int insertIndex = -1;
        for (int i = 0; i < noOfCards; i++) {
            if (card.compareTo(cards[i]) > 0) {
                insertIndex = i;
                break;
            }
        }
        if (insertIndex == -1) {
            // Không tìm được vị trí phù hợp, thêm vào cuối.
            cards[noOfCards++] = card;
        } else {
            System.arraycopy(cards, insertIndex, cards, insertIndex + 1, noOfCards - insertIndex);
            cards[insertIndex] = card;
            noOfCards++;
        }
    }

    /**
     * Thêm nhiều lá bài cùng lúc từ mảng.
     *
     * @param cards
     *            Mảng các lá bài cần thêm.
     */
    public void addCards(Card[] cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Null array");
        }
        if (cards.length > MAX_NO_OF_CARDS) {
            throw new IllegalArgumentException("Too many cards");
        }
        for (Card card : cards) {
            addCard(card);
        }
    }

    /**
     * Thêm nhiều lá bài cùng lúc từ một tập hợp.
     *
     * @param cards
     *            Tập hợp các lá bài cần thêm.
     */
    public void addCards(Collection<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Null collection");
        }
        if (cards.size() > MAX_NO_OF_CARDS) {
            throw new IllegalArgumentException("Too many cards");
        }
        for (Card card : cards) {
            addCard(card);
        }
    }

    /**
     * Trả về mảng các lá bài hiện tại.
     *
     * @return Mảng các lá bài.
     */
    public Card[] getCards() {
        Card[] dest = new Card[noOfCards];
        System.arraycopy(cards, 0, dest, 0, noOfCards);
        return dest;
    }

    /**
     * Xóa toàn bộ lá bài trong tay.
     */
    public void removeAllCards() {
        noOfCards = 0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noOfCards; i++) {
            sb.append(cards[i]);
            if (i < (noOfCards - 1)) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

}
