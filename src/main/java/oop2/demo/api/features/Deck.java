package oop2.demo.api.features;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Đại diện cho một bộ bài tiêu chuẩn, không có lá joker. <br />
 * <br />
 *
 * <b>LƯU Ý:</b> Lớp này được cài đặt với trọng tâm là hiệu năng
 * (thay vì thiết kế hướng đối tượng thuần túy).
 */
public class Deck implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Số lượng lá bài trong một bộ bài. */
    private static final int NO_OF_CARDS = Card.NO_OF_RANKS * Card.NO_OF_SUITS;

    /** Các lá bài trong bộ bài. */
    private Card[] cards;

    /** Chỉ số của lá bài tiếp theo sẽ được chia. */
    private int nextCardIndex = 0;

    /** Bộ sinh số ngẫu nhiên (chất lượng mật mã). */
    private Random random = new SecureRandom();

    /**
     * Hàm khởi tạo.
     *
     * Khởi tạo một bộ bài đầy đủ và theo thứ tự ban đầu.
     */
    public Deck() {
        cards = new Card[NO_OF_CARDS];
        int index = 0;
        for (int suit = Card.NO_OF_SUITS - 1; suit >= 0; suit--) {
            for (int rank = Card.NO_OF_RANKS - 1; rank >= 0; rank--) {
                cards[index++] = new Card(rank, suit);
            }
        }
    }

    /**
     * Trộn bộ bài.
     */
    public void shuffle() {
        for (int oldIndex = 0; oldIndex < NO_OF_CARDS; oldIndex++) {
            int newIndex = random.nextInt(NO_OF_CARDS);
            Card tempCard = cards[oldIndex];
            cards[oldIndex] = cards[newIndex];
            cards[newIndex] = tempCard;
        }
        nextCardIndex = 0;
    }

    /**
     * Đặt lại trạng thái bộ bài.
     *
     * Không thay đổi thứ tự các lá bài.
     */
    public void reset() {
        nextCardIndex = 0;
    }

    /**
     * Chia một lá bài.
     *
     * @return Lá bài được chia.
     */
    public Card deal() {
        if (nextCardIndex + 1 >= NO_OF_CARDS) {
            throw new IllegalStateException("No cards left in deck");
        }
        return cards[nextCardIndex++];
    }

    /**
     * Chia nhiều lá bài cùng lúc.
     *
     * @param noOfCards
     *            Số lượng lá bài cần chia.
     *
     * @return Danh sách các lá bài được chia.
     *
     * @throws IllegalArgumentException
     *             Nếu số lượng lá bài không hợp lệ.
     * @throws IllegalStateException
     *             Nếu không còn đủ bài trong bộ.
     */
    public List<Card> deal(int noOfCards) {
        if (noOfCards < 1) {
            throw new IllegalArgumentException("noOfCards < 1");
        }
        if (nextCardIndex + noOfCards >= NO_OF_CARDS) {
            throw new IllegalStateException("No cards left in deck");
        }
        List<Card> dealtCards = new ArrayList<>();
        for (int i = 0; i < noOfCards; i++) {
            dealtCards.add(cards[nextCardIndex++]);
        }
        return dealtCards;
    }

    /**
     * Chia một lá bài cụ thể theo rank và suit.
     *
     * @param rank
     *            Rank của lá bài.
     * @param suit
     *            Suit của lá bài.
     *
     * @return Lá bài nếu còn trong bộ, ngược lại trả về null.
     *
     * @throws IllegalStateException
     *             Nếu không còn bài trong bộ.
     */
    public Card deal(int rank, int suit) {
        if (nextCardIndex + 1 >= NO_OF_CARDS) {
            throw new IllegalStateException("No cards left in deck");
        }
        Card card = null;
        int index = -1;
        for (int i = nextCardIndex; i < NO_OF_CARDS; i++) {
            if ((cards[i].getRank() == rank) && (cards[i].getSuit() == suit)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            if (index != nextCardIndex) {
                Card nextCard = cards[nextCardIndex];
                cards[nextCardIndex] = cards[index];
                cards[index] = nextCard;
            }
            card = deal();
        }
        return card;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

}
