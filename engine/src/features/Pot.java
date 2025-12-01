package features;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Hũ tiền (Pot).
 * Hỗ trợ logic Side-Pot (chia hũ phụ) khi có người All-in thiếu tiền.
 */
public class Pot implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Mức cược trần của Pot này. */
    private BigDecimal bet;

    /** Những người chơi tham gia vào Pot này. */
    public final Set<Player> contributors;

    public Pot(BigDecimal bet) {
        this.bet = bet;
        contributors = new HashSet<>();
    }

    public BigDecimal getBet() { return bet; }
    public Set<Player> getContributors() { return Collections.unmodifiableSet(contributors); }

    public void addContributer(Player player) { contributors.add(player); }
    public boolean hasContributer(Player player) { return contributors.contains(player); }

    public BigDecimal getValue() {
        return bet.multiply(new BigDecimal(contributors.size()));
    }

    /**
     * Tách Pot hiện tại thành 2 (Main Pot và Side Pot) khi có người chơi All-in ít tiền hơn mức cược.
     */
    public Pot split(Player player, BigDecimal partialBet) {
        Pot pot = new Pot(bet.subtract(partialBet));
        for (Player contributer : contributors) {
            pot.addContributer(contributer);
        }
        bet = partialBet;
        contributors.add(player);
        return pot;
    }

    public void clear() {
        bet = BigDecimal.ZERO;
        contributors.clear();
    }

    @Override
    public String toString() {
        return "Pot: " + getValue();
    }
}