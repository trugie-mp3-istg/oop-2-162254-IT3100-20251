package oop2.demo.api.features;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import oop2.demo.api.actions.Action;

/**
 * Client đại diện cho một người chơi, có nhiệm vụ hiển thị thông tin bàn chơi
 * và thực hiện hành động thay mặt người chơi.
 */
public interface Client {

    /**
     * Xử lý một thông báo trong game.
     *
     * @param message
     *            Nội dung thông báo.
     */
    void messageReceived(String message);

    /**
     * Xử lý sự kiện người chơi tham gia vào một bàn chơi.
     *
     * @param type
     *            Loại bàn chơi (cấu trúc cược).
     * @param bigBlind
     *            Mức big blind của bàn.
     * @param players
     *            Danh sách người chơi tại bàn (bao gồm cả người chơi này).
     */
    void joinedTable(TableType type, BigDecimal bigBlind, List<Player> players);

    /**
     * Xử lý sự kiện bắt đầu một ván bài mới.
     *
     * @param dealer
     *            Người chia bài (dealer).
     */
    void handStarted(Player dealer);

    /**
     * Xử lý sự kiện chuyển lượt hành động
     * (người chơi đến lượt hành động).
     *
     * @param actor
     *            Người chơi hiện tại được phép hành động.
     */
    void actorRotated(Player actor);

    /**
     * Xử lý cập nhật thông tin của người chơi này.
     *
     * @param player
     *            Thông tin người chơi đã được cập nhật.
     */
    void playerUpdated(Player player);

    /**
     * Xử lý cập nhật thông tin bàn chơi.
     *
     * @param cards
     *            Các lá bài chung trên bàn.
     * @param bet
     *            Mức cược hiện tại.
     * @param pot
     *            Tổng pot hiện tại.
     */
    void boardUpdated(List<Card> cards, BigDecimal bet, BigDecimal pot);

    /**
     * Xử lý sự kiện một người chơi thực hiện hành động.
     *
     * @param player
     *            Người chơi vừa thực hiện hành động.
     */
    void playerActed(Player player);

    /**
     * Yêu cầu người chơi này thực hiện hành động,
     * chọn một trong các hành động được phép.
     *
     * @param minBet
     *            Mức cược tối thiểu.
     * @param currentBet
     *            Mức cược hiện tại.
     * @param allowedActions
     *            Tập các hành động được phép.
     *
     * @return Hành động được người chơi lựa chọn.
     */
    Action act(BigDecimal minBet, BigDecimal currentBet, Set<Action> allowedActions);

}
