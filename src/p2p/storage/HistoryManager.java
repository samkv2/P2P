package p2p.storage;

import p2p.security.SecurityUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final String KEY_FILE = "history.key";
    private static final String HISTORY_PREFIX = "history_";
    private static final String HISTORY_EXT = ".dat";
    private SecretKey historyKey;

    public HistoryManager() {
        try {
            File keyFile = new File(KEY_FILE);
            if (!keyFile.exists()) {
                // Generate and save new key
                historyKey = SecurityUtils.generateAESKey();
                String keyString = SecurityUtils.secretKeyToString(historyKey);
                Files.write(keyFile.toPath(), keyString.getBytes());
                System.out.println("HistoryManager: Generated new local history key.");
            } else {
                // Load existing key
                String keyString = new String(Files.readAllBytes(keyFile.toPath()));
                historyKey = SecurityUtils.stringToSecretKey(keyString.trim());
                System.out.println("HistoryManager: Loaded existing local history key.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("HistoryManager: Critical Error - Could not initialize history key.");
        }
    }

    public void saveMessage(String peerId, String message, boolean isSender) {
        if (historyKey == null) return;

        try {
            List<String> currentHistory = loadHistory(peerId);
            String prefix = isSender ? "ME" : "PEER";
            // Simple format: ME:Hello World
            String entry = prefix + ":" + message;
            currentHistory.add(entry);

            // Serialize: Join with newlines
            StringBuilder sb = new StringBuilder();
            for (String line : currentHistory) {
                sb.append(line).append("\n");
            }

            // Encrypt
            String encryptedContent = SecurityUtils.encryptAES(sb.toString(), historyKey);

            // Write
            String filename = getFilename(peerId);
            Files.write(new File(filename).toPath(), encryptedContent.getBytes());

        } catch (Exception e) {
            System.err.println("HistoryManager: Failed to save message for " + peerId);
            e.printStackTrace();
        }
    }

    public List<String> loadHistory(String peerId) {
        List<String> messages = new ArrayList<>();
        if (historyKey == null) return messages;

        File historyFile = new File(getFilename(peerId));
        if (!historyFile.exists()) {
            return messages;
        }

        try {
            // Read Encrypted
            String encryptedContent = new String(Files.readAllBytes(historyFile.toPath()));
            
            // Decrypt
            String decryptedContent = SecurityUtils.decryptAES(encryptedContent, historyKey);
            
            // Parse
            String[] lines = decryptedContent.split("\n");
            for (String line : lines) {
                if (!line.isEmpty()) {
                    messages.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("HistoryManager: Failed to load history for " + peerId);
            e.printStackTrace();
        }
        return messages;
    }

    private String getFilename(String peerId) {
        // Sanitize peerId to be safe for filenames
        String safeId = peerId.replaceAll("[^a-zA-Z0-9.-]", "_");
        return HISTORY_PREFIX + safeId + HISTORY_EXT;
    }
}
