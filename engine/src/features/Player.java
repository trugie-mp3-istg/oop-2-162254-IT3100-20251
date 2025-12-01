package features;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import actions.Action; // Import từ package actions của bạn

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    // transient: KHÔNG gửi biến này qua mạng (để tránh lỗi Socket)
    private final transient Client client;

    private final Hand hand;
    private BigDecimal cash;
    private boolean hasCards;
    private BigDecimal bet;
    private Action action;

    public Player(String name, BigDecimal cash, Client client) {
        this.name = name;
        this.cash = cash;
        this.client = client;
        hand = new Hand();
        resetHand();
    }

    public Client getClient() { return client; }

    public void resetHand() {
        hasCards = false;
        hand.removeAllCards();
        resetBet();
    }

    public void resetBet() {
        bet = BigDecimal.ZERO;
        // Nếu hết tiền mà có bài -> Tự động All-in
        action = (hasCards() && BigDecimal.ZERO.equals(cash)) ? Action.ALL_IN : null;
    }

    public void setCards(List<Card> cards) {
        hand.removeAllCards();
        if (cards != null && cards.size() == 2) {
            hand.addCards(cards);
            hasCards = true;
        }
    }

    // Các Getter/Setter cơ bản
    public boolean hasCards() { return hasCards; }
    public String getName() { return name; }
    public BigDecimal getCash() { return cash; }
    public BigDecimal getBet() { return bet; }
    public void setBet(BigDecimal bet) { this.bet = bet; }
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public boolean isAllIn() { return hasCards() && (BigDecimal.ZERO.equals(cash)); }
    public Card[] getCards() { return hand.getCards(); }

    public void postSmallBlind(BigDecimal blind) {
        action = Action.SMALL_BLIND;
        cash = cash.subtract(blind);
        bet = bet.add(blind);
    }

    public void postBigBlind(BigDecimal blind) {
        action = Action.BIG_BLIND;
        cash = cash.subtract(blind);
        bet = bet.add(blind);
    }

    public void payCash(BigDecimal amount) {
        if (amount.compareTo(cash) > 0) throw new IllegalStateException("Not enough cash");
        cash = cash.subtract(amount);
    }

    public void win(BigDecimal amount) { cash = cash.add(amount); }

    // Tạo bản sao an toàn để gửi cho người khác xem (Che bài tẩy)
    public Player publicClone() {
        Player clone = new Player(name, cash, null);
        clone.hasCards = hasCards;
        clone.bet = bet;
        clone.action = action;
        return clone;
    }

    @Override
    public String toString() { return name; }
}