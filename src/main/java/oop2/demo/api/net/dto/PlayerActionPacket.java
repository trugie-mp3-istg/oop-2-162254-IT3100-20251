package oop2.demo.api.net.dto; // DTO = Data Transfer Object

import java.io.Serializable;
import java.math.BigDecimal;
import oop2.demo.api.actions.Action;

public class PlayerActionPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    public Action actionType;
    public BigDecimal amount;

    public PlayerActionPacket(Action actionType, BigDecimal amount) {
        this.actionType = actionType;
        this.amount = amount;
    }

    public PlayerActionPacket(Action actionType) {
        this(actionType, BigDecimal.ZERO);
    }
}