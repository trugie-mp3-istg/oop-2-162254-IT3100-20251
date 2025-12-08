package oop2.demo.api.features;

import actions.Action;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private BigDecimal cash;
    private BigDecimal bet; // Số tiền đã cược trong vòng hiện tại
    private Action action;  // Hành động vừa thực hiện
    private List<Card> cards = new ArrayList<>(); // Bài tẩy (Hole cards)
    private boolean isAllIn = false;

    // [QUAN TRỌNG] Kết nối Socket tới người chơi này.
    // Dùng 'transient' để khi gửi object Player này qua mạng cho người khác,
    // Java sẽ KHÔNG gửi kèm cái kết nối Socket (vì Socket ko gửi được).
    private transient Client client;

    // Constructor cho Server (Có Client)
    public Player(String name, BigDecimal cash, Client client) {
        this.name = name;
        this.cash = (cash != null) ? cash : BigDecimal.ZERO;
        this.client = client;
        this.bet = BigDecimal.ZERO;
    }

    // Constructor cho việc Clone (Không cần Client)
    public Player(String name, BigDecimal cash) {
        this.name = name;
        this.cash = cash;
        this.bet = BigDecimal.ZERO;
    }

    // --- CÁC HÀM GETTER / SETTER CƠ BẢN ---
    public String getName() { return name; }
    public BigDecimal getCash() { return cash; }
    public void setCash(BigDecimal cash) { this.cash = cash; }
    public BigDecimal getBet() { return bet; }
    public void setBet(BigDecimal bet) { this.bet = bet; }
    public Client getClient() { return client; }
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }
    public boolean isAllIn() { return isAllIn; }

    // --- LOGIC GAME ĐƯỢC TABLE GỌI ---

    public void resetHand() {
        this.cards.clear();
        this.bet = BigDecimal.ZERO;
        this.action = null;
        this.isAllIn = false;
    }

    public void resetBet() {
        this.bet = BigDecimal.ZERO;
    }

    // Trừ tiền khi cược
    public void payCash(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;

        if (amount.compareTo(this.cash) >= 0) {
            // Nếu số tiền phải trả >= tiền đang có -> All-in
            this.bet = this.bet.add(this.cash); // Cược nốt số còn lại
            this.cash = BigDecimal.ZERO;
            this.isAllIn = true;
        } else {
            this.cash = this.cash.subtract(amount);
            // Lưu ý: Việc cộng vào this.bet thường được xử lý ở Table hoặc gọi setBet riêng
            // Nhưng để an toàn logic trừ tiền:
            // this.bet = this.bet.add(amount);
        }
    }

    public void postSmallBlind(BigDecimal amount) {
        this.action = new actions.SmallBlindAction();
        payCash(amount);
        this.bet = amount;
    }

    public void postBigBlind(BigDecimal amount) {
        this.action = new actions.BigBlindAction();
        payCash(amount);
        this.bet = amount;
    }

    public void win(BigDecimal amount) {
        this.cash = this.cash.add(amount);
    }

    /**
     * Tạo một bản sao công khai của người chơi này để gửi cho đối thủ.
     * MỤC ĐÍCH: Ẩn bài tẩy (Cards) và ẩn kết nối Client.
     */
    public Player publicClone() {
        Player clone = new Player(this.name, this.cash);
        clone.setBet(this.bet);
        clone.setAction(this.action);
        clone.isAllIn = this.isAllIn;
        // KHÔNG set cards cho clone (để bài null hoặc rỗng) -> Đối thủ không soi được bài
        return clone;
    }

    @Override
    public String toString() {
        return name;
    }
}