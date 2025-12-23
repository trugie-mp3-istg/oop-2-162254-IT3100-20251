package oop2.demo.api.features;

import java.math.BigDecimal;
import java.util.List;
import oop2.demo.api.actions.Action;

/**
 * Lớp biểu diễn một người chơi trong Texas Hold'em.
 *
 * Hành vi của người chơi được ủy quyền cho {@link Client},
 * có thể là người chơi thật hoặc bot AI.
 */
public class Player {

    /** Tên người chơi */
    private final String name;

    /** Client điều khiển hành vi của người chơi */
    private final Client client;

    /** Tay bài của người chơi */
    private final Hand hand;

    /** Số tiền hiện tại */
    private BigDecimal cash;

    /** Trạng thái đã được chia bài tẩy hay chưa */
    private boolean hasCards;

    /** Số tiền đang cược */
    private BigDecimal bet;

    /** Hành động gần nhất */
    private Action action;

    /**
     * Khởi tạo người chơi
     *
     * @param name   tên người chơi
     * @param cash   số tiền ban đầu
     * @param client client điều khiển hành vi
     */
    public Player(String name, BigDecimal cash, Client client) {
        this.name = name;
        this.cash = cash;
        this.client = client;
        this.hand = new Hand();
        resetHand();
    }

    /**
     * @return client điều khiển người chơi
     */
    public Client getClient() {
        return client;
    }

    /**
     * Chuẩn bị cho một ván mới
     */
    public void resetHand() {
        hasCards = false;
        hand.removeAllCards();
        resetBet();
    }

    /**
     * Reset tiền cược của người chơi
     */
    public void resetBet() {
        bet = BigDecimal.ZERO;
        action = (hasCards() && BigDecimal.ZERO.equals(cash)) ? Action.ALL_IN : null;
    }

    /**
     * Thiết lập 2 lá bài tẩy
     *
     * @param cards danh sách bài
     */
    public void setCards(List<Card> cards) {
        hand.removeAllCards();
        if (cards != null) {
            if (cards.size() == 2) {
                hand.addCards(cards);
                hasCards = true;
                System.out.format("[CHEAT] %s's cards:\t%s\n", name, hand);
            } else {
                throw new IllegalArgumentException("Số lượng bài không hợp lệ");
            }
        }
    }

    /**
     * @return true nếu đã có bài tẩy
     */
    public boolean hasCards() {
        return hasCards;
    }

    /**
     * @return tên người chơi
     */
    public String getName() {
        return name;
    }

    /**
     * @return số tiền hiện có
     */
    public BigDecimal getCash() {
        return cash;
    }

    /**
     * @return số tiền đang cược
     */
    public BigDecimal getBet() {
        return bet;
    }

    /**
     * Cập nhật tiền cược
     *
     * @param bet số tiền cược
     */
    public void setBet(BigDecimal bet) {
        this.bet = bet;
    }

    /**
     * @return hành động gần nhất
     */
    public Action getAction() {
        return action;
    }

    /**
     * Cập nhật hành động
     *
     * @param action hành động mới
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * @return true nếu người chơi đã all-in
     */
    public boolean isAllIn() {
        return hasCards() && BigDecimal.ZERO.equals(cash);
    }

    /**
     * @return mảng bài tẩy
     */
    public Card[] getCards() {
        return hand.getCards();
    }

    /**
     * Đặt tiền small blind
     *
     * @param blind số tiền blind
     */
    public void postSmallBlind(BigDecimal blind) {
        action = Action.SMALL_BLIND;
        cash = cash.subtract(blind);
        bet = bet.add(blind);
    }

    /**
     * Đặt tiền big blind
     *
     * @param blind số tiền blind
     */
    public void postBigBlind(BigDecimal blind) {
        action = Action.BIG_BLIND;
        cash = cash.subtract(blind);
        bet = bet.add(blind);
    }

    /**
     * Trả tiền cược
     *
     * @param amount số tiền phải trả
     */
    public void payCash(BigDecimal amount) {
        if (amount.compareTo(cash) > 0) {
            throw new IllegalStateException("Người chơi không đủ tiền để trả!");
        }
        cash = cash.subtract(amount);
    }

    /**
     * Nhận tiền thắng
     *
     * @param amount số tiền thắng
     */
    public void win(BigDecimal amount) {
        cash = cash.add(amount);
    }

    /**
     * Tạo bản sao chỉ chứa thông tin công khai
     *
     * @return player clone
     */
    public Player publicClone() {
        Player clone = new Player(name, cash, null);
        clone.hasCards = hasCards;
        clone.bet = bet;
        clone.action = action;
        return clone;
    }

    @Override
    public String toString() {
        return name;
    }
}
