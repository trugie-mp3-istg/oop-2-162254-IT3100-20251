package oop2.demo.api.features;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Quản lý bộ bài trên tay hoặc bộ bài chung (Board).
 * Tự động sắp xếp bài từ lớn đến nhỏ khi thêm vào.
 */
public final class Hand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final int MAX_NO_OF_CARDS = 7;
    private Card[] cards = new Card[MAX_NO_OF_CARDS];
    private int noOfCards = 0;

    public Hand() { }

    public Hand(Card[] cards) { addCards(cards); }

    public Hand(Collection<Card> cards) {
        if (cards == null) throw new IllegalArgumentException("Null collection");
        for (Card c : cards) addCard(c);
    }

    public Hand(String s) {
        if (s == null || s.length() == 0) throw new IllegalArgumentException("Null string");
        String[] parts = s.split("\s");
        for (String part : parts) addCard(new Card(part));
    }

    /**
     * Thêm bài và giữ thứ tự sắp xếp (Insertion Sort).
     * Quan trọng để HandEvaluator chạy nhanh.
     */
    public void addCard(Card card) {
        if (card == null) throw new IllegalArgumentException("Null card");
        int insertIndex = -1;
        for (int i = 0; i < noOfCards; i++) {
            if (card.compareTo(cards[i]) > 0) {
                insertIndex = i;
                break;
            }
        }
        if (insertIndex == -1) {
            cards[noOfCards++] = card;
        } else {
            System.arraycopy(cards, insertIndex, cards, insertIndex + 1, noOfCards - insertIndex);
            cards[insertIndex] = card;
            noOfCards++;
        }
    }

    public void addCards(Card[] cards2) {
        if (cards2 == null) throw new IllegalArgumentException("Null array");
        for (Card c : cards2) addCard(c);
    }

    public void addCards(List<Card> cards2) {
        if (cards2 == null) throw new IllegalArgumentException("Null list");
        for (Card c : cards2) addCard(c);
    }

    public Card[] getCards() {
        Card[] dest = new Card[noOfCards];
        System.arraycopy(cards, 0, dest, 0, noOfCards);
        return dest;
    }

    public void removeAllCards() { noOfCards = 0; }
    public int size() { return noOfCards; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noOfCards; i++) {
            sb.append(cards[i]);
            if (i < noOfCards - 1) sb.append(' ');
        }
        return sb.toString();
    }
}