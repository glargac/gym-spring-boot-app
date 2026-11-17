package com.wellness.web;

import java.sql.SQLException;

/**
 * Tells database errors apart by their SQLSTATE, the standard 5-character code that H2 and PostgreSQL share.
 * Catching the broad DataIntegrityViolationException and assuming "it must be the duplicate email" is a trap:
 * a CHECK violation, a value too long or a NOT NULL error would be reported as a duplicate and the real cause hidden.
 */
public final class DbErrors {

    private static final String UNIQUE_VIOLATION = "23505";
    private static final String FOREIGN_KEY_VIOLATION = "23503";

    private DbErrors() {}

    public static boolean isUniqueViolation(Throwable error) {
        return hasSqlState(error, UNIQUE_VIOLATION);
    }

    public static boolean isForeignKeyViolation(Throwable error) {
        return hasSqlState(error, FOREIGN_KEY_VIOLATION);
    }

    /** Walks the cause chain: Spring wraps Hibernate's exception, which wraps the driver's SQLException. */
    private static boolean hasSqlState(Throwable error, String sqlState) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && sqlState.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
