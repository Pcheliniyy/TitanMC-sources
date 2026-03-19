package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DisconnectDuplicationFix {
    private static final ConcurrentHashMap<UUID, TransactionState> activeTransactions = new ConcurrentHashMap<>();

    private DisconnectDuplicationFix() {}

    public static void patch() {
    }

    
    public static void registerTransaction(UUID playerUuid, int containerHash, int slotFrom, int slotTo) {
        activeTransactions.put(playerUuid, new TransactionState(containerHash, slotFrom, slotTo));
    }

    
    public static void completeTransaction(UUID playerUuid) {
        activeTransactions.remove(playerUuid);
    }

    
    public static TransactionState onDisconnect(UUID playerUuid) {
        return activeTransactions.remove(playerUuid);
    }

    
    public static boolean hasActiveTransaction(UUID playerUuid) {
        return activeTransactions.containsKey(playerUuid);
    }

    public static class TransactionState {
        public final int containerHash;
        public final int slotFrom;
        public final int slotTo;

        public TransactionState(int containerHash, int slotFrom, int slotTo) {
            this.containerHash = containerHash;
            this.slotFrom = slotFrom;
            this.slotTo = slotTo;
        }
    }
}
