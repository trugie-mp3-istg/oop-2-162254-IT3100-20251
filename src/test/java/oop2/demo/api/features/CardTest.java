package oop2.demo.api.features;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

    /**
     * Kiểm tra cơ bản.
     */
    @Test
    public void basics() {
        Card card = new Card(Card.ACE, Card.HEARTS);
        assertNotNull(card);
        assertEquals(Card.ACE, card.getRank());
        assertEquals(Card.HEARTS, card.getSuit());
        assertEquals("Ac", card.toString());

        card = new Card("   Kb "); // Tự động loại bỏ khoảng trắng.
        assertNotNull(card);
        assertEquals(Card.KING, card.getRank());
        assertEquals(Card.SPADES, card.getSuit());
        assertEquals("Kb", card.toString());
    }

    /**
     * Kiểm tra constructor.
     */
    @Test
    public void testConstructors() {

        assertThrows(IllegalArgumentException.class,
                () -> new Card(-1, 0));                     // Rank quá thấp

        assertThrows(IllegalArgumentException.class,
                () -> new Card(Card.NO_OF_RANKS, 0));       // Rank quá cao 

        assertThrows(IllegalArgumentException.class,
                () -> new Card(0, -1));                     // Suit quá thấp

        assertThrows(IllegalArgumentException.class,
                () -> new Card(0, Card.NO_OF_SUITS));       // Suit quá cao

        assertThrows(IllegalArgumentException.class,
                () -> new Card(null));                         // Chuỗi null 

        assertThrows(IllegalArgumentException.class,
                () -> new Card(""));                           // Chuỗi rỗng

        assertThrows(IllegalArgumentException.class,
                () -> new Card("A"));                          // Chuỗi quá ngắn

        assertThrows(IllegalArgumentException.class,
                () -> new Card("Ahx"));                        // Chuỗi quá dài

        assertThrows(IllegalArgumentException.class,
                () -> new Card("xh"));                         // Rank không hợp lệ

        assertThrows(IllegalArgumentException.class,
                () -> new Card("Ax"));                         // Suit không hợp lệ
        
        //Kiểm tra khởi tạo đúng
        Card card = new Card("Kc");
        assertNotNull(card);
        assertEquals(Card.KING, card.getRank());
        assertEquals(Card.HEARTS, card.getSuit());
        assertEquals("Kc", card.toString());
    }

    /**
     * Kiểm tra thứ tự theo hashCode.
     */
    @Test
    public void sortOrder() {
        // Rô nhỏ hơn Cơ (DIAMONDS < HEARTS).
        Card _2r = new Card("2r");
        Card _3r = new Card("3r");
        Card _2c = new Card("2c");
        Card _3c = new Card("3c");

        assertEquals(_2r, _2r);
        assertNotEquals(_2r, _3r);
        assertNotEquals(_2r, _2c);

        assertEquals(0, _2r.hashCode());
        assertEquals(2, _2c.hashCode());
        assertEquals(4, _3r.hashCode());
        assertEquals(6, _3c.hashCode());

        assertEquals(0, _2r.compareTo(_2r));
        assertTrue(_2r.compareTo(_3r) < 0);
        assertTrue(_3r.compareTo(_2r) > 0);
        assertTrue(_2r.compareTo(_2c) < 0);
        assertTrue(_2c.compareTo(_2r) > 0);
    }
}