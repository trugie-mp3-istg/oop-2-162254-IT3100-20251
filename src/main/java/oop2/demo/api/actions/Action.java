package oop2.demo.api.actions;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Lớp cha Action.
 */
public abstract class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    // Các biến static này gọi đến các constructor của lớp con.
    // Lưu ý: Các lớp con (BetAction, FoldAction...) cũng phải sửa về "package actions;"
    public static final Action ALL_IN = new AllInAction();
    public static final Action BET = new BetAction(BigDecimal.ZERO);
    public static final Action BIG_BLIND = new BigBlindAction();
    public static final Action CALL = new CallAction();
    public static final Action CHECK = new CheckAction();
    public static final Action FOLD = new FoldAction();
    public static final Action RAISE = new RaiseAction(BigDecimal.ZERO);
    public static final Action SMALL_BLIND = new SmallBlindAction();

    private final String name;
    private final String verb;
    private final BigDecimal amount;

    public Action(String name, String verb) {
        this(name, verb, BigDecimal.ZERO);
    }

    public Action(String name, String verb, BigDecimal amount) {
        this.name = name;
        this.verb = verb;
        this.amount = amount;
    }

    public final String getName() { return name; }
    public final String getVerb() { return verb; }
    public final BigDecimal getAmount() { return amount; }

    @Override
    public String toString() { return name; }
}