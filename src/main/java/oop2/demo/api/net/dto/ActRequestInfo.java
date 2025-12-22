package oop2.demo.api.net.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import oop2.demo.api.actions.Action;

public class ActRequestInfo implements Serializable {
    public BigDecimal minBet;
    public BigDecimal currentBet;
    public Set<Action> allowedActions;

    public ActRequestInfo(BigDecimal min, BigDecimal cur, Set<Action> allowed) {
        this.minBet = min;
        this.currentBet = cur;
        this.allowedActions = allowed;
    }
}