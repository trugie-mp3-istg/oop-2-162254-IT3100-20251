package oop2.demo.api.features;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class HandTest {

    /**
     * Kiểm tra cơ bản.
     */
    @Test
    public void basics() {
        Hand hand = new Hand();
        Assertions.assertNotNull(hand);
        Assertions.assertEquals(0, hand.size());

        Card[] cards = hand.getCards();
        Assertions.assertNotNull(cards);
        Assertions.assertEquals(0, cards.length);

        hand.addCard(new Card("Tc"));
        Assertions.assertEquals(1, hand.size());
        cards = hand.getCards();
        Assertions.assertNotNull(cards);
        Assertions.assertEquals(1, cards.length);
        Assertions.assertNotNull(cards[0]);
        Assertions.assertEquals("Tc", cards[0].toString());

        hand.addCards(new Card[]{new Card("2r"), new Card("Jn")});
        Assertions.assertEquals(3, hand.size());
        cards = hand.getCards();
        Assertions.assertNotNull(cards);
        Assertions.assertEquals(3, cards.length);
        //Đã sắp xếp từ lớn đến nhỏ
        Assertions.assertEquals("Jn", cards[0].toString());
        Assertions.assertEquals("Tc", cards[1].toString());
        Assertions.assertEquals("2r", cards[2].toString());

        hand.removeAllCards();
        Assertions.assertEquals(0, hand.size());
    }

    /**
     * Kiểm tra constructor.
     */
    @Test
    public void constructors() {

        // Thử khởi tạo hand với mảng Card null.
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Hand((Card[]) null));

        // Thử khởi tạo hand với mảng Card chứa 1 lá null.
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Card[] cards = new Card[1];
            new Hand(cards);
        });

        // Thử khởi tạo hand với mảng Card chứa quá nhiều lá.
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Card[] cards = new Card[11];
            new Hand(cards);
        });

        // Thử khởi tạo hand với Collection Card null.
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Hand((Collection<Card>) null));

        // Thử khởi tạo hand với Collection Card chứa 1 lá null.
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Collection<Card> cards = new ArrayList<>();
            cards.add(null);
            new Hand(cards);
        });

        //Thử khởi tạo hand với chuỗi null.
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Hand((String) null));

        // Thử khởi tạo hand với chuỗi rỗng.
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Hand(""));

        // Khởi tạo hand hợp lệ.
        Hand hand = new Hand("Ac Kb Tc 3n 2r");
        Assertions.assertNotNull(hand);
        Assertions.assertEquals(5, hand.size());

        Card[] cards = hand.getCards();
        Assertions.assertNotNull(cards);
    }
}
