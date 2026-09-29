/*
 * Decompiled with CFR 0.152.
 */
package night.accounts;

public enum AccountType {
    OFFLINE("Offline"),
    MICROSOFT("Microsoft"),
    SESSION("Session");

    private final String displayName;

    private AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}

