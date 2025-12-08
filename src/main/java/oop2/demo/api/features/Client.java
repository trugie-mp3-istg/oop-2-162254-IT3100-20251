package oop2.demo.api.features;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import actions.Action;

public interface Client {
    void messageReceived(String message);
    void joinedTable(TableType type, BigDecimal bigBlind, List<Player> players);
    void handStarted(Player dealer);
    void actorRotated(Player actor);
    void playerUpdated(Player player);
    void boardUpdated(List<Card> cards, BigDecimal bet, BigDecimal pot);
    void playerActed(Player player);
    Action act(BigDecimal minBet, BigDecimal currentBet, Set<Action> allowedActions);
}