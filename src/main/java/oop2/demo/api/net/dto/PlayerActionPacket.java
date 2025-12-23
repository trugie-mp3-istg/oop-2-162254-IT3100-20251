package oop2.demo.api.net.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import oop2.demo.api.actions.Action;

public class PlayerActionPacket implements Serializable {
    // 1. Version UID để đảm bảo khi sửa code không bị lỗi khi deserialize cũ
    private static final long serialVersionUID = 1L;

    // 2. Nên dùng private để đảm bảo tính đóng gói (Encapsulation)
    private Action actionType;
    private BigDecimal amount;

    // Constructor đầy đủ (Dùng cho Raise/Bet)
    public PlayerActionPacket(Action actionType, BigDecimal amount) {
        this.actionType = actionType;
        this.amount = amount;
    }

    // Constructor ngắn gọn (Dùng cho Fold/Check/Call - khi không cần nhập số tiền cụ thể)
    public PlayerActionPacket(Action actionType) {
        this(actionType, BigDecimal.ZERO);
    }

    // 3. Getter (Chỉ cho phép đọc, không cho phép sửa lung tung sau khi tạo)
    public Action getActionType() {
        return actionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    // 4. Rất quan trọng: Hàm toString() để in log xem Client gửi cái gì
    @Override
    public String toString() {
        return "PlayerActionPacket{" +
                "action=" + actionType +
                ", amount=" + amount +
                '}';
    }
}