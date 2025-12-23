package oop2.demo.api.features;

import oop2.demo.api.actions.Action;
import oop2.demo.api.actions.BetAction;
import oop2.demo.api.actions.RaiseAction;

import java.math.BigDecimal;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Bàn chơi poker Texas Hold'em giới hạn. <br />
 * <br />
 * * Lớp này tạo thành trái tim của engine poker. Nó kiểm soát luồng trò chơi cho một bàn poker đơn lẻ.
 */
public class Table {

    /** Trong các trò chơi giới hạn cố định (fixed-limit), số lần tố (raise) tối đa mỗi vòng cược. */
    private static final int MAX_RAISES = 3;

    /** Liệu người chơi sẽ luôn theo bài khi lật bài (showdown), hay bỏ bài khi không còn cơ hội. */
    private static final boolean ALWAYS_CALL_SHOWDOWN = false;

    /** Loại bàn (biến thể poker). */
    private final TableType tableType;

    /** Kích thước của mù lớn (big blind). */
    private final BigDecimal bigBlind;

    /** Các người chơi tại bàn. */
    private final List<Player> players;

    /** Các người chơi đang hoạt động trong ván bài hiện tại. */
    private final List<Player> activePlayers;

    /** Bộ bài. */
    private final Deck deck;

    /** Các lá bài chung trên bàn. */
    private final List<Card> board;

    /** Vị trí người chia bài (dealer) hiện tại. */
    private int dealerPosition;

    /** Người chia bài hiện tại. */
    private Player dealer;

    /** Vị trí của người chơi đang hành động. */
    private int actorPosition;

    /** Người chơi đang hành động. */
    private Player actor;

    /** Mức cược tối thiểu trong ván bài hiện tại. */
    private BigDecimal minBet;

    /** Mức cược hiện tại trong ván bài hiện tại. */
    private BigDecimal bet;

    /** Tất cả các pot trong ván bài hiện tại (pot chính và các pot phụ). */
    private final List<Pot> pots;

    /** Người chơi đã cược hoặc tố cuối cùng (người tấn công). */
    private Player lastBettor;

    /** Số lần tố trong vòng cược hiện tại. */
    private int raises;

    /**
     * Constructor.
     * * @param bigBlind
     * Kích thước của mù lớn (big blind).
     */
    public Table(TableType type, BigDecimal bigBlind) {
        this.tableType = type;
        this.bigBlind = bigBlind;
        players = new CopyOnWriteArrayList<>();
        activePlayers = new ArrayList<>();
        deck = new Deck();
        board = new ArrayList<>();
        pots = new ArrayList<>();
    }

    /**
     * Thêm một người chơi.
     * * @param player
     * Người chơi.
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Vòng lặp chính của trò chơi.
     */
    public void run() {
        for (Player player : players) {
            player.getClient().joinedTable(tableType, bigBlind, players);
        }
        dealerPosition = -1;
        actorPosition = -1;
        while (true) {
            int noOfActivePlayers = 0;
            for (Player player : players) {
                if (player.getCash().compareTo(bigBlind) >= 0) {
                    noOfActivePlayers++;
                }
            }
            if (noOfActivePlayers > 1) {
                playHand();
            } else {
                break;
            }
        }

        // Trò chơi kết thúc.
        board.clear();
        pots.clear();
        bet = BigDecimal.ZERO;
        notifyBoardUpdated();
        for (Player player : players) {
            player.resetHand();
        }
        notifyPlayersUpdated(false);
        notifyMessage("Game over.");
    }

    /**
     * Chơi một ván bài đơn lẻ.
     */
    private void playHand() {
        resetHand();

        // Mù nhỏ (Small blind).
        if (activePlayers.size() > 2) {
            rotateActor();
        }
        postSmallBlind();

        // Mù lớn (Big blind).
        rotateActor();
        postBigBlind();

        // Tiền Flop (Pre-Flop).
        dealHoleCards();
        doBettingRound();

        // Flop.
        if (activePlayers.size() > 1) {
            bet = BigDecimal.ZERO;
            dealCommunityCards("Flop", 3);
            doBettingRound();

            // Turn.
            if (activePlayers.size() > 1) {
                bet = BigDecimal.ZERO;
                dealCommunityCards("Turn", 1);
                minBet = bigBlind.add(bigBlind);
                doBettingRound();

                // River.
                if (activePlayers.size() > 1) {
                    bet = BigDecimal.ZERO;
                    dealCommunityCards("River", 1);
                    doBettingRound();

                    // Lật bài (Showdown).
                    if (activePlayers.size() > 1) {
                        bet = BigDecimal.ZERO;
                        doShowdown();
                    }
                }
            }
        }
    }

    /**
     * Đặt lại trò chơi cho một ván bài mới.
     */
    private void resetHand() {
        // Xóa bàn chơi.
        board.clear();
        pots.clear();
        notifyBoardUpdated();

        // Xác định các người chơi đang hoạt động.
        activePlayers.clear();
        for (Player player : players) {
            player.resetHand();
            // Người chơi phải có đủ khả năng trả ít nhất là mù lớn.
            if (player.getCash().compareTo(bigBlind) >= 0) {
                activePlayers.add(player);
            }
        }

        // Xoay nút dealer.
        dealerPosition = (dealerPosition + 1) % activePlayers.size();
        dealer = activePlayers.get(dealerPosition);

        // Trộn bộ bài.
        deck.shuffle();

        // Xác định người chơi đầu tiên hành động.
        actorPosition = dealerPosition;
        actor = activePlayers.get(actorPosition);

        // Đặt mức cược ban đầu bằng mù lớn.
        minBet = bigBlind;
        bet = minBet;

        // Thông báo cho tất cả client rằng ván bài mới đã bắt đầu.
        for (Player player : players) {
            player.getClient().handStarted(dealer);
        }
        notifyPlayersUpdated(false);
        notifyMessage("New hand, %s is the dealer.", dealer);
    }

    /**
     * Xoay vị trí của người chơi đến lượt (người hành động).
     */
    private void rotateActor() {
        actorPosition = (actorPosition + 1) % activePlayers.size();
        actor = activePlayers.get(actorPosition);
        for (Player player : players) {
            player.getClient().actorRotated(actor);
        }
    }

    /**
     * Đặt mù nhỏ.
     */
    private void postSmallBlind() {
        final BigDecimal smallBlind = bigBlind.divide(BigDecimal.valueOf(2));
        actor.postSmallBlind(smallBlind);
        contributePot(smallBlind);
        notifyBoardUpdated();
        notifyPlayerActed();
    }

    /**
     * Đặt mù lớn.
     */
    private void postBigBlind() {
        actor.postBigBlind(bigBlind);
        contributePot(bigBlind);
        notifyBoardUpdated();
        notifyPlayerActed();
    }

    /**
     * Chia bài tẩy (Hole Cards).
     */
    private void dealHoleCards() {
        for (Player player : activePlayers) {
            player.setCards(deck.deal(2));
        }
        System.out.println();
        notifyPlayersUpdated(false);
        notifyMessage("%s deals the hole cards.", dealer);
    }

    /**
     * Chia một số lượng lá bài chung.
     * * @param phaseName
     * Tên của giai đoạn.
     * @param noOfCards
     * Số lượng lá bài cần chia.
     */
    private void dealCommunityCards(String phaseName, int noOfCards) {
        for (int i = 0; i < noOfCards; i++) {
            board.add(deck.deal());
        }
        notifyPlayersUpdated(false);
        notifyMessage("%s deals the %s.", dealer, phaseName);
    }

    /**
     * Thực hiện một vòng cược.
     */
    private void doBettingRound() {
        // Xác định số lượng người chơi cần hành động.
        int playersToAct = activePlayers.size();
        // Xác định người chơi ban đầu và kích thước cược.
        if (board.size() == 0) {
            // Tiền Flop; người bên trái mù lớn bắt đầu, cược là mù lớn.
            bet = bigBlind;
        } else {
            // Ngược lại, người bên trái dealer bắt đầu, không có cược ban đầu.
            actorPosition = dealerPosition;
            bet = BigDecimal.ZERO;
        }

        if (playersToAct == 2) {
            // Chế độ Heads Up (1-1); người không phải dealer bắt đầu.
            actorPosition = dealerPosition;
        }

        lastBettor = null;
        raises = 0;
        notifyBoardUpdated();

        while (playersToAct > 0) {
            rotateActor();
            Action action;
            if (actor.isAllIn()) {
                // Người chơi đã all-in (tất tay), nên phải kiểm tra (check).
                action = Action.CHECK;
                playersToAct--;
            } else {
                // Ngược lại cho phép client hành động.
                Set<Action> allowedActions = getAllowedActions(actor);
                action = actor.getClient().act(minBet, bet, allowedActions);
                // Xác minh hành động đã chọn để bảo vệ chống lại các client bị lỗi (vô tình hoặc cố ý).
                if (!allowedActions.contains(action)) {
                    if (action instanceof BetAction && !allowedActions.contains(Action.BET)) {
                        throw new IllegalStateException(String.format("Player '%s' acted with illegal Bet action", actor));
                    } else if (action instanceof RaiseAction && !allowedActions.contains(Action.RAISE)) {
                        throw new IllegalStateException(String.format("Player '%s' acted with illegal Raise action", actor));
                    }
                }
                playersToAct--;
                if (action == Action.CHECK) {
                    // Không làm gì cả.
                } else if (action == Action.CALL) {
                    BigDecimal betIncrement = bet.subtract(actor.getBet());
                    if (betIncrement.compareTo(actor.getCash()) > 0) {
                        betIncrement = actor.getCash();
                    }
                    actor.payCash(betIncrement);
                    actor.setBet(actor.getBet().add(betIncrement));
                    contributePot(betIncrement);
                } else if (action instanceof BetAction) {
                    BigDecimal amount = (tableType == TableType.FIXED_LIMIT) ? minBet : action.getAmount();
                    if (amount.compareTo(minBet) < 0 && amount.compareTo(actor.getCash()) < 0) {
                        throw new IllegalStateException("Illegal client action: bet less than minimum bet!");
                    }
                    actor.setBet(amount);
                    actor.payCash(amount);
                    contributePot(amount);
                    bet = amount;
                    minBet = amount;
                    lastBettor = actor;
                    playersToAct = activePlayers.size();
                } else if (action instanceof RaiseAction) {
                    BigDecimal amount = (tableType == TableType.FIXED_LIMIT) ? minBet : action.getAmount();
                    if (amount.compareTo(minBet) < 0 && amount.compareTo(actor.getCash()) < 0) {
                        throw new IllegalStateException("Illegal client action: raise less than minimum bet!");
                    }
                    bet = bet.add(amount);
                    minBet = amount;
                    BigDecimal betIncrement = bet.subtract(actor.getBet());
                    if (betIncrement.compareTo(actor.getCash()) > 0) {
                        betIncrement = actor.getCash();
                    }
                    actor.setBet(bet);
                    actor.payCash(betIncrement);
                    contributePot(betIncrement);
                    lastBettor = actor;
                    raises++;
                    if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                        // Tất cả người chơi có thêm một lượt nữa.
                        playersToAct = activePlayers.size();
                    } else {
                        // Đã đạt số lần tố tối đa; các người chơi khác có thêm một lượt.
                        playersToAct = activePlayers.size() - 1;
                    }
                } else if (action == Action.FOLD) {
                    actor.setCards(null);
                    activePlayers.remove(actor);
                    actorPosition--;
                    if (activePlayers.size() == 1) {
                        // Chỉ còn một người chơi, nên người đó thắng toàn bộ pot.
                        notifyBoardUpdated();
                        notifyPlayerActed();
                        Player winner = activePlayers.get(0);
                        BigDecimal amount = getTotalPot();
                        winner.win(amount);
                        notifyBoardUpdated();
                        notifyMessage("%s wins $ %d.", winner, amount);
                        playersToAct = 0;
                    }
                } else {
                    // Lỗi lập trình, không bao giờ nên xảy ra.
                    throw new IllegalStateException("Invalid action: " + action);
                }
            }
            actor.setAction(action);
            if (playersToAct > 0) {
                notifyBoardUpdated();
                notifyPlayerActed();
            }
        }

        // Đặt lại cược của người chơi.
        for (Player player : activePlayers) {
            player.resetBet();
        }
        notifyBoardUpdated();
        notifyPlayersUpdated(false);
    }

    /**
     * Trả về các hành động được phép của một người chơi cụ thể.
     * * @param player
     * Người chơi.
     * * @return Các hành động được phép.
     */
    private Set<Action> getAllowedActions(Player player) {
        Set<Action> actions = new HashSet<>();
        if (player.isAllIn()) {
            actions.add(Action.CHECK);
        } else {
            BigDecimal actorBet = actor.getBet();
            if (bet.equals(BigDecimal.ZERO)) {
                actions.add(Action.CHECK);
                if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                    actions.add(Action.BET);
                }
            } else {
                if (actorBet.compareTo(bet) < 0) {
                    actions.add(Action.CALL);
                    if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                        actions.add(Action.RAISE);
                    }
                } else {
                    actions.add(Action.CHECK);
                    if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                        actions.add(Action.RAISE);
                    }
                }
            }
            actions.add(Action.FOLD);
        }
        return actions;
    }

    /**
     * Đóng góp vào pot.
     * * @param amount
     * Số tiền đóng góp.
     */
    private void contributePot(BigDecimal amount) {
        for (Pot pot : pots) {
            if (!pot.hasContributer(actor)) {
                BigDecimal potBet = pot.getBet();
                if (amount.compareTo(potBet) >= 0) {
                    // Call, bet hoặc raise thông thường.
                    pot.addContributer(actor);
                    amount = amount.subtract(pot.getBet());
                } else {
                    // Call một phần (all-in); phân phối lại các pot.
                    pots.add(pot.split(actor, amount));
                    amount = BigDecimal.ZERO;
                }
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            Pot pot = new Pot(amount);
            pot.addContributer(actor);
            pots.add(pot);
        }
    }

    /**
     * Thực hiện lật bài (showdown).
     */
    private void doShowdown() {
//        System.out.println("\n[DEBUG] Pots:");
//        for (Pot pot : pots) {
//            System.out.format("  %s\n", pot);
//        }
//        System.out.format("[DEBUG]  Total: %d\n", getTotalPot());

        // Xác định thứ tự lật bài; bắt đầu với những người chơi all-in...
        List<Player> showingPlayers = new ArrayList<>();
        for (Pot pot : pots) {
            for (Player contributor : pot.getContributors()) {
                if (!showingPlayers.contains(contributor) && contributor.isAllIn()) {
                    showingPlayers.add(contributor);
                }
            }
        }
        // ...sau đó là người chơi cuối cùng đã cược hoặc tố (người tấn công)...
        if (lastBettor != null) {
            if (!showingPlayers.contains(lastBettor)) {
                showingPlayers.add(lastBettor);
            }
        }
        //...và cuối cùng là những người chơi còn lại, bắt đầu từ bên trái của nút dealer.
        int pos = (dealerPosition + 1) % activePlayers.size();
        while (showingPlayers.size() < activePlayers.size()) {
            Player player = activePlayers.get(pos);
            if (!showingPlayers.contains(player)) {
                showingPlayers.add(player);
            }
            pos = (pos + 1) % activePlayers.size();
        }

        // Người chơi tự động lật bài hoặc bỏ bài theo thứ tự.
        boolean firstToShow = true;
        int bestHandValue = -1;
        for (Player playerToShow : showingPlayers) {
            Hand hand = new Hand(board);
            hand.addCards(playerToShow.getCards());
            HandValue handValue = new HandValue(hand);
            boolean doShow = ALWAYS_CALL_SHOWDOWN;
            if (!doShow) {
                if (playerToShow.isAllIn()) {
                    // Người chơi all-in phải luôn lật bài.
                    doShow = true;
                    firstToShow = false;
                } else if (firstToShow) {
                    // Người chơi đầu tiên phải luôn lật bài.
                    doShow = true;
                    bestHandValue = handValue.getValue();
                    firstToShow = false;
                } else {
                    // Những người chơi còn lại chỉ lật bài khi có cơ hội thắng.
                    if (handValue.getValue() >= bestHandValue) {
                        doShow = true;
                        bestHandValue = handValue.getValue();
                    }
                }
            }
            if (doShow) {
                // Lật bài.
                for (Player player : players) {
                    player.getClient().playerUpdated(playerToShow);
                }
                notifyMessage("%s has %s.", playerToShow, handValue.getDescription());
            } else {
                // Bỏ bài (Fold).
                playerToShow.setCards(null);
                activePlayers.remove(playerToShow);
                for (Player player : players) {
                    if (player.equals(playerToShow)) {
                        player.getClient().playerUpdated(playerToShow);
                    } else {
                        // Ẩn thông tin bí mật đối với các người chơi khác.
                        player.getClient().playerUpdated(playerToShow.publicClone());
                    }
                }
                notifyMessage("%s folds.", playerToShow);
            }
        }

        // Sắp xếp người chơi theo giá trị bài (từ cao xuống thấp).
        Map<HandValue, List<Player>> rankedPlayers = new TreeMap<>();
        for (Player player : activePlayers) {
            // Tạo một bộ bài với các lá bài chung và bài tẩy của người chơi.
            Hand hand = new Hand(board);
            hand.addCards(player.getCards());
            // Lưu trữ người chơi cùng với những người chơi khác có cùng giá trị bài.
            HandValue handValue = new HandValue(hand);
//            System.out.format("[DEBUG] %s: %s\n", player, handValue);
            List<Player> playerList = rankedPlayers.get(handValue);
            if (playerList == null) {
                playerList = new ArrayList<>();
            }
            playerList.add(player);
            rankedPlayers.put(handValue, playerList);
        }

        // Theo xếp hạng (người thắng đơn lẻ hoặc nhiều người thắng), tính toán phân phối pot.
        BigDecimal totalPot = getTotalPot();
        Map<Player, BigDecimal> potDivision = new HashMap<>();
        for (HandValue handValue : rankedPlayers.keySet()) {
            List<Player> winners = rankedPlayers.get(handValue);
            for (Pot pot : pots) {
                // Xác định có bao nhiêu người thắng chia sẻ pot này.
                int noOfWinnersInPot = 0;
                for (Player winner : winners) {
                    if (pot.hasContributer(winner)) {
                        noOfWinnersInPot++;
                    }
                }
                if (noOfWinnersInPot > 0) {
                    // Chia pot cho những người thắng.
                    BigDecimal potShare = pot.getValue().divide(new BigDecimal(String.valueOf(noOfWinnersInPot)));
                    for (Player winner : winners) {
                        if (pot.hasContributer(winner)) {
                            BigDecimal oldShare = potDivision.get(winner);
                            if (oldShare != null) {
                                potDivision.put(winner, oldShare.add(potShare));
                            } else {
                                potDivision.put(winner, potShare);
                            }

                        }
                    }
                    // Xác định xem chúng ta có còn dư chip lẻ nào trong pot không.
                    BigDecimal oddChips = pot.getValue().remainder(new BigDecimal(String.valueOf(noOfWinnersInPot)));
                    if (oddChips.compareTo(BigDecimal.ZERO) > 0) {
                        // Chia chip lẻ cho những người thắng, bắt đầu từ bên trái của dealer.
                        pos = dealerPosition;
                        while (oddChips.compareTo(BigDecimal.ZERO) > 0) {
                            pos = (pos + 1) % activePlayers.size();
                            Player winner = activePlayers.get(pos);
                            BigDecimal oldShare = potDivision.get(winner);
                            if (oldShare != null) {
                                potDivision.put(winner, oldShare.add(BigDecimal.ONE));
//                                System.out.format("[DEBUG] %s receives an odd chip from the pot.\n", winner);
                                oddChips = oddChips.subtract(BigDecimal.ONE);
                            }
                        }

                    }
                    pot.clear();
                }
            }
        }

        // Chia tiền thắng.
        StringBuilder winnerText = new StringBuilder();
        BigDecimal totalWon = BigDecimal.ZERO;
        for (Player winner : potDivision.keySet()) {
            BigDecimal potShare = potDivision.get(winner);
            winner.win(potShare);
            totalWon = totalWon.add(potShare);
            if (winnerText.length() > 0) {
                winnerText.append(", ");
            }
            winnerText.append(String.format("%s wins $ %d", winner, potShare.intValue()));
            notifyPlayersUpdated(true);
        }
        winnerText.append('.');
        notifyMessage(winnerText.toString());

        // Kiểm tra tính hợp lý.
        if (!totalWon.equals(totalPot)) {
            throw new IllegalStateException("Incorrect pot division!");
        }
    }

    /**
     * Thông báo cho các listener với một tin nhắn tùy chỉnh của trò chơi.
     * * @param message
     * Tin nhắn đã được định dạng.
     * @param args
     * Bất kỳ đối số nào.
     */
    private void notifyMessage(String message, Object... args) {
        message = String.format(message, args);
        for (Player player : players) {
            player.getClient().messageReceived(message);
        }
    }

    /**
     * Thông báo cho các client rằng bàn chơi đã được cập nhật.
     */
    private void notifyBoardUpdated() {
        BigDecimal pot = getTotalPot();
        for (Player player : players) {
            player.getClient().boardUpdated(board, bet, pot);
        }
    }

    /**
     * Trả về tổng kích thước pot.
     * * @return Tổng kích thước pot.
     */
    private BigDecimal getTotalPot() {
        BigDecimal totalPot = BigDecimal.ZERO;
        for (Pot pot : pots) {
            totalPot = totalPot.add(pot.getValue());
        }
        return totalPot;
    }

    /**
     * Thông báo cho các client rằng một hoặc nhiều người chơi đã được cập nhật. <br />
     * <br />
     * * Thông tin bí mật của một người chơi chỉ được gửi đến client của chính nó; các client khác
     * chỉ nhìn thấy thông tin công khai của người chơi đó.
     * * @param showdown
     * Liệu chúng ta có đang ở giai đoạn lật bài (showdown) hay không.
     */
    private void notifyPlayersUpdated(boolean showdown) {
        for (Player playerToNotify : players) {
            for (Player player : players) {
                if (!showdown && !player.equals(playerToNotify)) {
                    // Ẩn thông tin bí mật đối với các người chơi khác.
                    player = player.publicClone();
                }
                playerToNotify.getClient().playerUpdated(player);
            }
        }
    }

    /**
     * Thông báo cho các client rằng một người chơi đã hành động.
     */
    private void notifyPlayerActed() {
        for (Player p : players) {
            Player playerInfo = p.equals(actor) ? actor : actor.publicClone();
            p.getClient().playerActed(playerInfo);
        }
    }
    // Trong file oop2.demo.api.features.Table.java

    public List<Card> getBoardCards() {
        return this.board;
    }
}