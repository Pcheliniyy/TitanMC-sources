package com.titanmc.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TitanEvent {

    
    public static class DuplicationBlockedEvent {
        private final UUID playerUuid;
        private final String playerName;
        private final String dupeType;
        private final String details;
        private final long timestamp;

        public DuplicationBlockedEvent(@NotNull UUID playerUuid, @NotNull String playerName,
                                        @NotNull String dupeType, @NotNull String details) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.dupeType = dupeType;
            this.details = details;
            this.timestamp = System.currentTimeMillis();
        }

        public @NotNull UUID getPlayerUuid() { return playerUuid; }
        public @NotNull String getPlayerName() { return playerName; }
        public @NotNull String getDupeType() { return dupeType; }
        public @NotNull String getDetails() { return details; }
        public long getTimestamp() { return timestamp; }
    }

    
    public static class ExploitBlockedEvent {
        private final UUID playerUuid;
        private final String playerName;
        private final String exploitType;
        private final String details;
        private final Severity severity;
        private final long timestamp;

        public ExploitBlockedEvent(@NotNull UUID playerUuid, @NotNull String playerName,
                                    @NotNull String exploitType, @NotNull String details,
                                    @NotNull Severity severity) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.exploitType = exploitType;
            this.details = details;
            this.severity = severity;
            this.timestamp = System.currentTimeMillis();
        }

        public @NotNull UUID getPlayerUuid() { return playerUuid; }
        public @NotNull String getPlayerName() { return playerName; }
        public @NotNull String getExploitType() { return exploitType; }
        public @NotNull String getDetails() { return details; }
        public @NotNull Severity getSeverity() { return severity; }
        public long getTimestamp() { return timestamp; }
    }

    
    public static class PacketRateLimitEvent {
        private final UUID playerUuid;
        private final int packetsPerSecond;
        private final int violations;

        public PacketRateLimitEvent(@NotNull UUID playerUuid, int packetsPerSecond, int violations) {
            this.playerUuid = playerUuid;
            this.packetsPerSecond = packetsPerSecond;
            this.violations = violations;
        }

        public @NotNull UUID getPlayerUuid() { return playerUuid; }
        public int getPacketsPerSecond() { return packetsPerSecond; }
        public int getViolations() { return violations; }
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
