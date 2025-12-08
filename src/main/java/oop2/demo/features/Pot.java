package features;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class Pot implements Serializable {
    private BigDecimal bet;
    private Set<Player> contributors; // Khai báo biến

    public Pot(BigDecimal bet) {
        this.bet = bet;
        this.contributors = new HashSet<>(); // <--- QUAN TRỌNG: Phải có dòng này để tránh NullPointerException
    }

    // Sửa lỗi UnsupportedOperationException: Phải trả về giá trị thật
    public BigDecimal getBet() {
        return this.bet;
    }

    public void addContributer(Player p) {
        this.contributors.add(p);
    }

    public boolean hasContributer(Player p) {
        return this.contributors.contains(p);
    }

    public Set<Player> getContributors() {
        return this.contributors;
    }

    public BigDecimal getValue() {
        return bet.multiply(new BigDecimal(contributors.size()));
    }

    public Pot split(Player splitter, BigDecimal partialBet) {
        Pot excessPot = new Pot(this.bet.subtract(partialBet));
        // Copy người chơi cũ sang Pot mới
        for (Player p : this.contributors) {
            excessPot.addContributer(p);
        }
        this.bet = partialBet;
        this.addContributer(splitter);
        return excessPot;
    }

    @Override
    public String toString() {
        return "Pot Value: " + getValue();
    }
}