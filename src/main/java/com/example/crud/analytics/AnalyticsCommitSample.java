package com.example.crud.analytics;

/**
 * Sample Composer-authored class used to generate a visible
 * /analytics/ai-code/commits record when committed from Cursor Source Control.
 */
public final class AnalyticsCommitSample {

    private AnalyticsCommitSample() {
    }

    public static String eventName() {
        return "analytics-ai-code-commit-sample";
    }

    public static String endpoint() {
        return "/analytics/ai-code/commits";
    }
}
