package com.mycompany.poker;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Deck {
    
    //Trung cx kbiet hàm này để làm j nma xóa thì lỗi ae
    //Hnhu chỗ này dùng test code được
    public static void main(String[] args){
    }
    
    private Random random = new Random();
    
    private static final int NO_OF_CARDS = Card.NO_OF_RANKS * Card.NO_OF_SUITS;
    private Card[] cards;
    
    /*
    Tưởng tượng bộ deck sau khi shuffle hay ko thì vẫn thu đủ về 52 lá xếp chồng lên nhau
    và chắc chắn lá trên cùng sẽ đc bốc/phát
    thì lá đấy là lá thứ nhất trong 52 lá được deal (0 trên 51)
    */
    private int nctd = 0; //next_card_to_deal nma e lười
    
    /*
    Constructor cho 1 bộ full 52 lá lúc đầu, đủ 13rank * 4suit ko tính joker
    */
    public Deck() {
        cards = new Card[NO_OF_CARDS];
        int i = 0;
        for (int suit = 0; suit <= Card.NO_OF_SUITS - 1; suit++) {
            for (int rank = 0; rank <= Card.NO_OF_RANKS - 1 ; rank++) {
                cards[i++] = new Card(rank, suit);
            }
        }
    }
    
    public void shuffle() {
        for (int i = 0; i < NO_OF_CARDS; i++) {
            int j = random.nextInt(NO_OF_CARDS);
            Card temp = cards[i];
            cards[i] = cards[j];
            cards[j] = temp;
        }
        nctd = 0;
    }
    
    public void reset() {
        nctd = 0;
    }
    
    public List<Card> deal(int num) {
        List<Card> card_list = new ArrayList<>();
        if (nctd + num >= NO_OF_CARDS) {
            if (NO_OF_CARDS - nctd > 0) {
                for (int i = 0; i < NO_OF_CARDS - nctd; i++) {
                    card_list.add(cards[nctd++]);
                }
            }
            else {
                throw new IllegalStateException("Hết lá bài!");
            }
        }
        else {
            for (int i = 0; i < num; i++) {
                card_list.add(cards[nctd++]);
            }
        }
        return card_list;
    }
    
    public Card deal_specific(int rank, int suit) {
        if (nctd + 1 >= NO_OF_CARDS) {
            throw new IllegalStateException("Hết lá bài!");
        }
        Card card = null;
        int i = -1;
        for (int j = nctd; j < NO_OF_CARDS; j++) {
            if ((cards[j].getRank() == rank) && (cards[j].getSuit() == suit)) {
                i = j;
                break;
            }
        }
        if (i != -1) {
            if (i != nctd) {
                Card nextCard = cards[nctd];
                cards[nctd] = cards[i];
                cards[i] = nextCard;
            }
            card = cards[nctd++];
        }
        return card;
    }
}
