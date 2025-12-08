package oop2.demo.api.features;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bộ bài (Deck). Dùng SecureRandom để chống hack quy luật chia bài.
 */
public class Deck {

    private static final int NO_OF_CARDS = Card.NO_OF_RANKS * Card.NO_OF_SUITS;
    private Card[] cards;
    private int nextCardIndex = 0;
    private Random random = new SecureRandom(); // Bảo mật cao

    public Deck() {
        cards = new Card[NO_OF_CARDS];
        int index = 0;
        for (int suit = Card.NO_OF_SUITS - 1; suit >= 0; suit--) {
            for (int rank = Card.NO_OF_RANKS - 1; rank >= 0 ; rank--) {
                cards[index++] = new Card(rank, suit);
            }
        }
    }

    public void shuffle() {
        for (int oldIndex = 0; oldIndex < NO_OF_CARDS; oldIndex++) {
            int newIndex = random.nextInt(NO_OF_CARDS);
            Card tempCard = cards[oldIndex];
            cards[oldIndex] = cards[newIndex];
            cards[newIndex] = tempCard;
        }
        nextCardIndex = 0;
    }

    public Card deal() {
        if (nextCardIndex + 1 >= NO_OF_CARDS) throw new IllegalStateException("No cards left");
        return cards[nextCardIndex++];
    }

    public List<Card> deal(int noOfCards) {
        List<Card> dealtCards = new ArrayList<>();
        for (int i = 0; i < noOfCards; i++) dealtCards.add(deal());
        return dealtCards;
    }
}