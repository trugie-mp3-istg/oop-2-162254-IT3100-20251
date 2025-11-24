package actions;

import java.math.BigDecimal;

/**
 * Hành động tố thêm (Raise) khi đã có người cược trước đó.
 */
public class RaiseAction extends Action {

    /**
     * Hàm khởi tạo.
     * * @param amount
     * Số tiền muốn tố thêm.
     */
    public RaiseAction(BigDecimal amount) {
        super("Raise", "raises", amount);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("Raise(%d)", getAmount());
    }
    
}