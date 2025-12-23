package oop2.demo.api.features;

/**
 * Các loại bàn hỗ trợ (cấu trúc đặt cược).
 */
public enum TableType {

    /** Texas Hold'em giới hạn cố định (Fixed-Limit). */
    FIXED_LIMIT("Fixed-Limit"),

    /** Texas Hold'em không giới hạn (No-Limit). */
    NO_LIMIT("No-Limit"),

    ;

    /** Tên hiển thị. */
    private String name;

    /**
     * Constructor (Hàm khởi tạo).
     * * @param name
     * Tên hiển thị.
     */
    TableType(String name) {
        this.name = name;
    }

    /**
     * Trả về tên hiển thị.
     * * @return Tên hiển thị.
     */
    public String getName() {
        return name;
    }

}