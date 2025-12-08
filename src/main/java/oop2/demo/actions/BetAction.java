package actions;

import java.math.BigDecimal;

/**
 * Hành động đặt cược (Bet).
 */
public class BetAction extends Action {

    /**
     * Hàm khởi tạo.
     * * @param amount
     * Số tiền muốn cược.
     */
    public BetAction(BigDecimal amount) {
        super("Bet", "bets", amount);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("Bet(%d)", getAmount());
    }
    
}