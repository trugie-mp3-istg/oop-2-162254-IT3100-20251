package oop2.demo.api.net.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import oop2.demo.api.actions.Action;

public class ActRequestInfo implements Serializable {
    // 1. Thông tin cơ bản về lượt cược
    public BigDecimal currentBet;      // Mức cược cao nhất hiện tại trên bàn (VD: 100)
    public BigDecimal minRaise;        // Số tiền tối thiểu phải Tố thêm (VD: Tố thêm ít nhất 50)
    public BigDecimal callAmount;      // Số tiền CẦN BỎ THÊM để theo (Quan trọng cho UI nút Call)

    // 2. Thông tin hỗ trợ ra quyết định
    public BigDecimal totalPot;        // Tổng tiền trong hũ hiện tại
    public BigDecimal playerBalance;   // Số dư hiện tại của người chơi (để biết max có thể All-in bao nhiêu)

    // 3. Giới hạn thời gian
    public int timeLimitSeconds;       // Ví dụ: 15 giây để suy nghĩ

    // 4. Các hành động hợp lệ (Fold, Check, Call, Raise...)
    public Set<Action> allowedActions;

    // Constructor đầy đủ
    public ActRequestInfo(BigDecimal currentBet, BigDecimal minRaise, BigDecimal callAmount,
                          BigDecimal totalPot, BigDecimal playerBalance,
                          int timeLimitSeconds, Set<Action> allowedActions) {
        this.currentBet = currentBet;
        this.minRaise = minRaise;
        this.callAmount = callAmount;
        this.totalPot = totalPot;
        this.playerBalance = playerBalance;
        this.timeLimitSeconds = timeLimitSeconds;
        this.allowedActions = allowedActions;
    }
}