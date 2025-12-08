package features;

import java.io.Serializable;

/**
 * Enum loại bàn chơi.
 */
public enum TableType implements Serializable {
    FIXED_LIMIT("Fixed-Limit"),
    NO_LIMIT("No-Limit");

    private String name;

    TableType(String name) { this.name = name; }
    public String getName() { return name; }
}