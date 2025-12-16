package p2p;

import p2p.storage.HistoryManager;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class HistoryTest {
    public static void main(String[] args) {
        System.out.println("Running History Manager Test...");

        try {
            // Setup
            HistoryManager manager = new HistoryManager();
            String peer = "TestPeer";
            String msg = "Hello from History Test";

            // 1. Save Message
            System.out.println("Saving message...");
            manager.saveMessage(peer, msg, true); // true = sender (Me)

            // 2. Load History
            System.out.println("Loading history...");
            List<String> history = manager.loadHistory(peer);

            // 3. Verify Content
            if (history.isEmpty()) {
                throw new RuntimeException("FAIL: History is empty");
            }
            String loaded = history.get(0);
            System.out.println("Loaded: " + loaded);

            if (!loaded.equals("ME:" + msg)) {
                throw new RuntimeException("FAIL: Message mismatch. Expected 'ME:" + msg + "' but got '" + loaded + "'");
            }
            System.out.println("SUCCESS: Content verification passed.");

            // 4. Verify Encryption (File Inspection)
            File historyFile = new File("history_" + peer + ".dat");
            if (!historyFile.exists()) {
                throw new RuntimeException("FAIL: History file not created");
            }

            String fileContent = new String(Files.readAllBytes(historyFile.toPath()));
            System.out.println("File Content (Raw): " + fileContent);

            if (fileContent.contains("Hello from History Test")) {
                throw new RuntimeException("FAIL: File content is NOT encrypted! Found plaintext.");
            } else {
                System.out.println("SUCCESS: File content appears encrypted (plaintext not found).");
            }

            // Unhappy Path: Corrupt Key? (Skip for now)

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
