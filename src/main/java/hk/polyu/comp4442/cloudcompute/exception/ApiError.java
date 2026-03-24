package hk.polyu.comp4442.cloudcompute.exception;

import java.time.LocalDateTime;

public record ApiError(
        String code,
        String message,
        LocalDateTime timestamp
) {
}
