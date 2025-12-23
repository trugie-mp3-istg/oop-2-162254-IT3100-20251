package oop2.demo.api.features;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Lớp Pot đại diện cho một pot (quỹ cược) mà một hoặc nhiều người chơi đã đóng góp.
 *
 * Mỗi pot có một mức cược cố định áp dụng cho tất cả người chơi tham gia.
 * Trong trường hợp call / bet / raise không đủ (partial),
 * pot sẽ được tách bằng phương thức {@link #split}.
 */
public class Pot {

    /** Mức cược áp dụng cho pot này */
    private BigDecimal bet;

    /** Tập các người chơi đã đóng góp vào pot */
    public final Set<Player> contributors;

    /**
     * Khởi tạo pot
     *
     * @param bet mức cược của pot
     */
    public Pot(BigDecimal bet) {
        this.bet = bet;
        contributors = new HashSet<>();
    }

    /**
     * @return mức cược của pot
     */
    public BigDecimal getBet() {
        return bet;
    }

    /**
     * @return danh sách người chơi đã đóng góp (chỉ đọc)
     */
    public Set<Player> getContributors() {
        return Collections.unmodifiableSet(contributors);
    }

    /**
     * Thêm một người chơi vào danh sách đóng góp
     *
     * @param player người chơi
     */
    public void addContributer(Player player) {
        contributors.add(player);
    }

    /**
     * Kiểm tra một người chơi có đóng góp vào pot hay không
     *
     * @param player người chơi
     * @return true nếu đã đóng góp, ngược lại false
     */
    public boolean hasContributer(Player player) {
        return contributors.contains(player);
    }

    /**
     * Tính tổng giá trị của pot
     *
     * @return tổng tiền trong pot
     */
    public BigDecimal getValue() {
        return bet.multiply(new BigDecimal(String.valueOf(contributors.size())));
    }

    /**
     * Tách pot trong trường hợp người chơi call / bet / raise không đủ tiền.
     *
     * Pot hiện tại sẽ giữ phần cược thấp hơn,
     * pot mới sẽ chứa phần tiền còn lại.
     *
     * @param player     người chơi thực hiện partial
     * @param partialBet số tiền thực sự đã cược
     * @return pot mới chứa phần còn lại
     */
    public Pot split(Player player, BigDecimal partialBet) {
        Pot pot = new Pot(bet.subtract(partialBet));
        for (Player contributer : contributors) {
            pot.addContributer(contributer);
        }
        bet = partialBet;
        contributors.add(player);
        return pot;
    }

    /**
     * Xóa toàn bộ dữ liệu của pot
     */
    public void clear() {
        bet = BigDecimal.ZERO;
        contributors.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(bet));
        sb.append(": {");
        boolean isFirst = true;
        for (Player contributor : contributors) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(", ");
            }
            sb.append(contributor.getName());
        }
        sb.append('}');
        sb.append(" (Total: ");
        sb.append(String.valueOf(getValue()));
        sb.append(')');
        return sb.toString();
    }
}
