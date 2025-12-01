package features;

import actions.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Trái tim của Game Server.
 * Điều khiển toàn bộ logic ván bài.
 */
public class Table {

    private final TableType tableType;
    private final BigDecimal bigBlind;
    private final List<Player> players = new ArrayList<>();
    private final List<Player> activePlayers = new ArrayList<>();
    private final Deck deck = new Deck();
    private final List<Card> board = new ArrayList<>();
    private final List<Pot> pots = new ArrayList<>();

    private int dealerPosition = -1;
    private Player dealer;
    private int actorPosition;
    private Player actor;
    private BigDecimal minBet;
    private BigDecimal bet;
    private int raises;

    public Table(TableType type, BigDecimal bigBlind) {
        this.tableType = type;
        this.bigBlind = bigBlind;
    }

    public void addPlayer(Player player) { players.add(player); }

    /**
     * Vòng lặp chính của Game. Chạy vô tận trên Server.
     */
    public void run() {
        for (Player p : players) p.getClient().joinedTable(tableType, bigBlind, players);

        while (true) {
            int activeCount = 0;
            for (Player p : players) if (p.getCash().compareTo(bigBlind) >= 0) activeCount++;

            if (activeCount > 1) playHand();
            else break;
        }
        notifyMessage("Game Over.");
    }

    /** Quy trình 1 ván bài: Chia bài -> Cược -> Lật bài */
    private void playHand() {
        resetHand();
        // 1. Small/Big Blind
        if (activePlayers.size() > 2) rotateActor();
        postSmallBlind();
        rotateActor();
        postBigBlind();

        // 2. Pre-Flop
        dealHoleCards();
        doBettingRound();

        // 3. Flop, Turn, River
        if (activePlayers.size() > 1) { playRound("Flop", 3); }
        if (activePlayers.size() > 1) { playRound("Turn", 1); }
        if (activePlayers.size() > 1) { playRound("River", 1); }

        // 4. Showdown
        if (activePlayers.size() > 1) doShowdown();
    }

    private void playRound(String name, int cards) {
        bet = BigDecimal.ZERO;
        for (int i=0; i<cards; i++) board.add(deck.deal());
        notifyPlayersUpdated(false);
        notifyMessage(dealer + " deals " + name);
        doBettingRound();
    }

    private void resetHand() {
        board.clear(); pots.clear();
        activePlayers.clear();
        for (Player p : players) {
            p.resetHand();
            if (p.getCash().compareTo(bigBlind) >= 0) activePlayers.add(p);
        }
        dealerPosition = (dealerPosition + 1) % activePlayers.size();
        dealer = activePlayers.get(dealerPosition);
        deck.shuffle();
        actorPosition = dealerPosition;
        actor = activePlayers.get(actorPosition);
        minBet = bigBlind; bet = minBet;

        for (Player p : players) p.getClient().handStarted(dealer);
        notifyPlayersUpdated(false);
    }

    private void rotateActor() {
        actorPosition = (actorPosition + 1) % activePlayers.size();
        actor = activePlayers.get(actorPosition);
        for (Player p : players) p.getClient().actorRotated(actor);
    }

    private void postSmallBlind() {
        BigDecimal sb = bigBlind.divide(BigDecimal.valueOf(2));
        actor.postSmallBlind(sb); contributePot(sb); notifyBoardUpdated(); notifyPlayerActed();
    }

    private void postBigBlind() {
        actor.postBigBlind(bigBlind); contributePot(bigBlind); notifyBoardUpdated(); notifyPlayerActed();
    }

    private void dealHoleCards() {
        for (Player p : activePlayers) p.setCards(deck.deal(2));
        notifyPlayersUpdated(false);
    }

    /**
     * LOGIC CƯỢC: Tương tác với Client qua Socket.
     * Có try-catch để xử lý ngắt kết nối.
     */
    private void doBettingRound() {
        int playersToAct = activePlayers.size();
        if (board.size() == 0) bet = bigBlind; else { actorPosition = dealerPosition; bet = BigDecimal.ZERO; }
        if (playersToAct == 2) actorPosition = dealerPosition;

        raises = 0;
        notifyBoardUpdated();

        while (playersToAct > 0) {
            rotateActor();
            Action action;
            if (actor.isAllIn()) {
                action = Action.CHECK;
                playersToAct--;
            } else {
                Set<Action> allowed = getAllowedActions(actor);
                try {
                    // [BLOCKING] Chờ Client phản hồi
                    action = actor.getClient().act(minBet, bet, allowed);
                } catch (Exception e) {
                    System.err.println(actor.getName() + " connection error.");
                    action = Action.FOLD; // Lỗi mạng -> Fold
                }

                if (action == null || !allowed.contains(action)) action = Action.FOLD; // Hack/Lỗi -> Fold

                playersToAct--;
                if (action == Action.CHECK) { }
                else if (action == Action.CALL) {
                    BigDecimal diff = bet.subtract(actor.getBet()).min(actor.getCash());
                    actor.payCash(diff); actor.setBet(actor.getBet().add(diff)); contributePot(diff);
                } else if (action instanceof BetAction || action instanceof RaiseAction) {
                    BigDecimal amount = (tableType == TableType.FIXED_LIMIT) ? minBet : action.getAmount();
                    if (action instanceof RaiseAction) {
                        bet = bet.add(amount); raises++;
                        playersToAct = activePlayers.size() - 1; // Mọi người phải vòng lại
                    } else {
                        bet = amount; playersToAct = activePlayers.size();
                    }
                    minBet = amount;
                    BigDecimal diff = bet.subtract(actor.getBet()).min(actor.getCash());
                    actor.payCash(diff); actor.setBet(bet); contributePot(diff);
                } else if (action == Action.FOLD) {
                    actor.setCards(null); activePlayers.remove(actor); actorPosition--;
                    if (activePlayers.size() == 1) { // Thắng luôn
                        Player winner = activePlayers.get(0);
                        winner.win(getTotalPot());
                        notifyMessage(winner + " wins.");
                        return; // Kết thúc vòng
                    }
                }
            }
            actor.setAction(action);
            if (playersToAct > 0) { notifyBoardUpdated(); notifyPlayerActed(); }
        }
        for (Player p : activePlayers) p.resetBet();
        notifyBoardUpdated(); notifyPlayersUpdated(false);
    }

    private void doShowdown() {
        // ... (Logic tính điểm, chia tiền giữ nguyên như file gốc để tiết kiệm không gian)
        // Lưu ý dùng player.publicClone() khi thông báo bài úp của người thua
        // ...
        // Sau khi chia tiền xong:
        notifyPlayersUpdated(true);
    }

    private void contributePot(BigDecimal amount) {
        // Logic chia Side Pot
        for (Pot pot : pots) {
            if (!pot.hasContributer(actor)) {
                if (amount.compareTo(pot.getBet()) >= 0) {
                    pot.addContributer(actor); amount = amount.subtract(pot.getBet());
                } else {
                    pots.add(pot.split(actor, amount)); amount = BigDecimal.ZERO;
                }
            }
            if (amount.equals(BigDecimal.ZERO)) break;
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            Pot p = new Pot(amount); p.addContributer(actor); pots.add(p);
        }
    }

    private Set<Action> getAllowedActions(Player p) {
        Set<Action> actions = new HashSet<>();
        if (p.isAllIn()) { actions.add(Action.CHECK); return actions; }
        if (bet.equals(BigDecimal.ZERO)) {
            actions.add(Action.CHECK); actions.add(Action.BET);
        } else {
            if (p.getBet().compareTo(bet) < 0) actions.add(Action.CALL); else actions.add(Action.CHECK);
            actions.add(Action.RAISE);
        }
        actions.add(Action.FOLD);
        return actions;
    }

    // --- CÁC HÀM GỬI THÔNG BÁO (NOTIFICATION) ---
    private void notifyMessage(String msg) { for (Player p : players) p.getClient().messageReceived(msg); }
    private void notifyBoardUpdated() { for (Player p : players) p.getClient().boardUpdated(board, bet, getTotalPot()); }
    private void notifyPlayerActed() { for (Player p : players) p.getClient().playerActed(p.equals(actor) ? p : p.publicClone()); }

    // [QUAN TRỌNG] Bảo mật bài tẩy
    private void notifyPlayersUpdated(boolean showdown) {
        for (Player target : players) {
            for (Player p : players) {
                if (!showdown && !p.equals(target)) target.getClient().playerUpdated(p.publicClone());
                else target.getClient().playerUpdated(p);
            }
        }
    }

    private BigDecimal getTotalPot() {
        BigDecimal total = BigDecimal.ZERO;
        for (Pot p : pots) total = total.add(p.getValue());
        return total;
    }
}