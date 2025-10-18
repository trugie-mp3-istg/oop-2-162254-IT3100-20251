package com.mycompany.poker;

public class Card {
    
    //Trung cx kbiet hàm này để làm j nma xóa thì lỗi ae
    //Hnhu chỗ này dùng test code được
    public static void main(String[] args){
    }
    
    //13 ranks
    public static final int NO_OF_RANKS = 13;
    public static final int ACE = 12;
    public static final int KING = 11;
    public static final int QUEEN = 10;
    public static final int JACK = 9;
    public static final int TEN = 8;
    public static final int NINE = 7;
    public static final int EIGHT = 6;
    public static final int SEVEN = 5;
    public static final int SIX = 4;
    public static final int FIVE = 3;
    public static final int FOUR = 2;
    public static final int THREE = 1;
    public static final int TWO = 0;
    public static final String[] RANK_SYMBOL = {
        "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A",
    };

    //4 suits
    public static final int NO_OF_SUITS = 4;
    public static final int SPADE = 3;
    public static final int HEART = 2;
    public static final int CLUB = 1;
    public static final int DIAMOND = 0;
    public static final String[] SUIT_SYMBOL = {
        "d", "c", "h", "s",
    };
    
    private final int rank;
    private final int suit;
    
    /*
    Constructor số
    e.g. Card card = new Card(1, 2) //Three of Hearts
    */
    public Card(int rank, int suit) {
        if (rank < 0 || rank > NO_OF_RANKS - 1) {
            throw new IllegalArgumentException("Invalid rank!");
        }
        if (suit < 0 || suit > NO_OF_SUITS - 1) {
            throw new IllegalArgumentException("Invalid suit!");
        }
        this.rank = rank; this.suit = suit;
    }
    
    /*
    Constructor chữ
    e.g. Card card = new Card("3h") //Three of Hearts
    */
    public Card(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Null string!");
        }

        s = s.trim();
        if (s.length() != 2) {
            throw new IllegalArgumentException("Invalid string length!");
        }
        
        //input string đúng format thì tách 2 chữ trước làm rank và sau làm suit
        
        //Rank
        int rank = -1;
        String rankSymbol = s.substring(0, 1).toUpperCase(); //Chuyển hết về in hoa
        String suitSymbol = s.substring(1).toLowerCase(); //Chuyển hết về in thường
        
        for (int i = 0; i < Card.NO_OF_RANKS; i++) {
            if (rankSymbol.equals(RANK_SYMBOL[i])) {
                rank = i;
                break;
            }
        }
        if (rank == -1) {
            throw new IllegalArgumentException("Invalid rank: " + rankSymbol);
        }
        
        //Suit
        int suit = -1;
        for (int i = 0; i < Card.NO_OF_SUITS; i++) {
            if (suitSymbol.equals(SUIT_SYMBOL[i])) {
                suit = i;
                break;
            }
        }
        if (suit == -1) {
            throw new IllegalArgumentException("Invalid suit: " + suitSymbol);
        }
        this.rank = rank;
        this.suit = suit;
    }
    
    /*
    2 constructor cho đa dạng input chứ ko bó buộc chữ hay số
    */
    
    //Get rank, suit
    public int getRank() {
        return rank;
    }
    public int getSuit() {
        return suit;
    }
}
