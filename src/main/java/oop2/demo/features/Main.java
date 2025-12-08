package features;

import java.math.BigDecimal;
import java.util.*;

// Import package actions nếu cần dùng enum Action (tùy code Player của bạn)
import actions.*;

public class Main {

    // Danh sách các Pot trên bàn (Giả lập class Table)
    static List<Pot> pots = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   POKER GAME ENGINE TEST (REAL CLASSES)");
        System.out.println("==========================================\n");

        // 1. KHỞI TẠO NGƯỜI CHƠI (Sử dụng Player.java thật)
        // Lưu ý: Đảm bảo Constructor của Player.java khớp với tham số này
        Player pA = new Player("Alice (Giàu)", new BigDecimal("5000"));
        Player pB = new Player("Bob (Nghèo)", new BigDecimal("50"));
        Player pC = new Player("Charlie (Vừa)", new BigDecimal("200"));

        System.out.println("--- TRẠNG THÁI BAN ĐẦU ---");
        System.out.println(pA.getName() + ": " + pA.getCash());
        System.out.println(pB.getName() + ": " + pB.getCash());
        System.out.println(pC.getName() + ": " + pC.getCash());
        System.out.println("------------------------------------------");

        // 2. CHẠY KỊCH BẢN CƯỢC (Test chia Side Pot)

        // Alice Bet 1000$
        System.out.println("\n[1] Alice Bet 1000$");
        handleBet(pA, new BigDecimal("1000"));

        // Bob All-in 50$ (Thiếu tiền -> Phải tách Pot)
        System.out.println("\n[2] Bob All-in 50$");
        handleBet(pB, new BigDecimal("50"));

        // Charlie All-in 200$ (Kẹp giữa -> Tách tiếp Pot)
        System.out.println("\n[3] Charlie All-in 200$");
        handleBet(pC, new BigDecimal("200"));

        // 3. KIỂM TRA POT TRƯỚC KHI REFUND
        System.out.println("\n--- DANH SÁCH POT (CHƯA REFUND) ---");
        printPots();

        // 4. CHẠY LOGIC TRẢ TIỀN THỪA (REFUND)
        System.out.println("\n>>> ĐANG XỬ LÝ REFUND...");
        processRefunds();

        // 5. KẾT QUẢ CUỐI CÙNG
        System.out.println("\n--- KẾT QUẢ CUỐI CÙNG ---");
        printPots();

        System.out.println("\nKiểm tra ví Alice (5000 - 1000 + 800 refund = 4800):");
        System.out.println("Số dư hiện tại: " + pA.getCash());
    }

    // =================================================================
    // CÁC HÀM XỬ LÝ LOGIC (Controller)
    // =================================================================

    /**
     * Hàm xử lý logic đặt tiền vào Pot (bao gồm cả Split Pot)
     */
    public static void handleBet(Player actor, BigDecimal amount) {
        // Trừ tiền trong ví người chơi
        // YÊU CẦU: Player.java phải có hàm setCash và getCash
        actor.setCash(actor.getCash().subtract(amount));

        ListIterator<Pot> iterator = pots.listIterator();

        while (iterator.hasNext()) {
            Pot pot = iterator.next();

            // Nếu người chơi chưa tham gia Pot này
            if (!pot.hasContributer(actor)) {

                // Trường hợp 1: Đủ tiền để theo mức cược của Pot này
                if (amount.compareTo(pot.getBet()) >= 0) {
                    pot.addContributer(actor);
                    amount = amount.subtract(pot.getBet());
                }
                // Trường hợp 2: Không đủ tiền -> Phải chia tách (Split) Pot
                else {
                    // YÊU CẦU: Pot.java phải có hàm split logic đúng như đã bàn
                    Pot excessPot = pot.split(actor, amount);

                    // Thêm Pot thừa (Side Pot) vào ngay sau Pot hiện tại
                    iterator.add(excessPot);

                    amount = BigDecimal.ZERO; // Đã dốc hết tiền
                }
            }
            // Nếu hết tiền thì dừng vòng lặp
            if (amount.compareTo(BigDecimal.ZERO) == 0) break;
        }

        // Nếu duyệt hết các Pot cũ mà vẫn còn tiền (Raise mới / Bet mới)
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            Pot newPot = new Pot(amount);
            newPot.addContributer(actor);
            pots.add(newPot);
        }
    }

    /**
     * Hàm xử lý trả lại tiền thừa cho người chơi (Refund)
     * Khi một Pot chỉ có duy nhất 1 người chơi
     */
    public static void processRefunds() {
        Iterator<Pot> iterator = pots.iterator();
        while (iterator.hasNext()) {
            Pot pot = iterator.next();

            // YÊU CẦU: Pot.java nên có hàm contributors.size() hoặc getContributors()
            if (pot.getContributors().size() == 1) {
                Player luckyPlayer = pot.getContributors().iterator().next();

                // YÊU CẦU: Pot.java phải có hàm getValue() tính tổng tiền
                BigDecimal refundVal = pot.getValue();

                System.out.println("  -> Refund: Trả lại " + refundVal + "$ cho " + luckyPlayer.getName());

                // Cộng lại tiền vào ví
                luckyPlayer.setCash(luckyPlayer.getCash().add(refundVal));

                // Xóa Pot này đi vì đã hoàn tiền rồi
                iterator.remove();
            }
        }
    }

    // --- Hàm in ấn hiển thị ---
    private static void printPots() {
        if (pots.isEmpty()) System.out.println("  (Không có Pot nào)");
        for (int i = 0; i < pots.size(); i++) {
            Pot p = pots.get(i);
            // YÊU CẦU: Player.java phải có toString() trả về tên để in cho đẹp
            System.out.println("  POT #" + i +
                    " [Mức cược: " + p.getBet() + "$]" +
                    " | Tổng tiền: " + p.getValue() + "$" +
                    " | Người chơi: " + p.getContributors());
        }
    }
}