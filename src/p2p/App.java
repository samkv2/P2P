//**Authored by Shivam Kumar on 2025-12-06 with open source community ideas and services
// and with the help of my project team Tushar, Saurabh, and Shreya */

package p2p;

import p2p.net.PeerNode;
import p2p.ui.MainFrame;
import p2p.ui.Theme;
import javax.swing.*;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            // Force standard rendering to avoid Linux GTK theme overrides (Black Text Field fix)
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> {
            String username = JOptionPane.showInputDialog("Enter Username:");
            if (username == null || username.trim().isEmpty()) {
                System.exit(0);
            }

            // Random port for now, or ask user
            int port = 20000 + new Random().nextInt(10000);

            try {
                // Initialize PeerNode
                // Circular dependency: PeerNode needs callback for message, UI needs PeerNode.
                // Solution: Pass simple callback to PeerNode, then update UI later or use
                // setter.
                // Here we will use a temporary wrapper or set it up.

                // Let's modify MainFrame to handle the callback or pass the node later.
                // Better: Create PeerNode first with a lambda that delegates to MainFrame
                // (which we create after).
                // Actually Java requires effective final.

                final MainFrame[] frameHolder = new MainFrame[1];

                PeerNode node = new PeerNode(username, port,
                        msg -> {
                            if (frameHolder[0] != null) {
                                frameHolder[0].onMessageReceived(msg);
                            }
                        },
                        (requestor, decisionCallback) -> {
                            if (frameHolder[0] != null) {
                                frameHolder[0].onChatRequest(requestor, decisionCallback);
                            }
                        },
                        (user, accepted) -> {
                            if (frameHolder[0] != null) {
                                frameHolder[0].onChatFeedback(user, accepted);
                            }
                        },
                        (requestInfo, confirmCallback) -> {
                            if (frameHolder[0] != null) {
                                frameHolder[0].onFileRequest(requestInfo, confirmCallback);
                            }
                        },
                        (sender, progress) -> {
                            if (frameHolder[0] != null) {
                                frameHolder[0].onFileProgress(sender, progress);
                            }
                        });

                MainFrame frame = new MainFrame(node, username);
                frameHolder[0] = frame;

                node.start();
                frame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error starting app: " + e.getMessage());
            }
        });
    }
}
