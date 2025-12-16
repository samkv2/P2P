package p2p.ui;

import p2p.net.PeerNode;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MainFrame extends JFrame {
    private final PeerNode peerNode;
    private final String username;
    private final p2p.storage.HistoryManager historyManager; // New Field
    private final DefaultListModel<String> peerListModel;
    private JList<String> peerList;
    private JPanel chatListPanel; // Replaces JTextArea
    private JTextField messageField;
    private JLabel chatTitle;

    // Session State
    private String currentSession = "GLOBAL"; // Default to Global
    private final Map<String, List<JPanel>> chatHistory = new ConcurrentHashMap<>();

    public MainFrame(PeerNode peerNode, String username) {
        this.peerNode = peerNode;
        this.username = username;
        this.historyManager = new p2p.storage.HistoryManager(); // Init
        this.peerListModel = new DefaultListModel<>();

        // Init Global Chat
        chatHistory.put("GLOBAL", new ArrayList<>());

        setTitle("P2P Secure Chat - " + username);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, Theme.GRADIENT_START, getWidth(), getHeight(),
                        Theme.GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        });
        setLayout(new BorderLayout(20, 20));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        // Setup UI
        JPanel sidePanel = createSidePanel();
        JPanel chatPanel = createChatPanel();

        add(sidePanel, BorderLayout.WEST);
        add(chatPanel, BorderLayout.CENTER);

        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> refreshPeers());
        timer.start();
        refreshPeers();

        // Load Global Chat initially
        loadSession("GLOBAL");
    }

    private JPanel createSidePanel() {
        GlassPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, 0));

        JLabel title = new JLabel("PEERS");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_SECONDARY);
        title.setBorder(new EmptyBorder(20, 20, 10, 20));
        panel.add(title, BorderLayout.NORTH);

        peerList = new JList<>(peerListModel);
        peerList.setOpaque(false);
        peerList.setBackground(new Color(0, 0, 0, 0));
        peerList.setForeground(Theme.TEXT_PRIMARY);
        peerList.setFont(Theme.FONT_REGULAR);
        peerList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBorder(new EmptyBorder(10, 15, 10, 15));
                c.setOpaque(isSelected);
                if (isSelected) {
                    c.setBackground(Theme.ACCENT);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                return c;
            }
        });

        peerList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = peerList.getSelectedValue();
                if (selected != null) {
                    if (selected.equals("Global Chat")) {
                        loadSession("GLOBAL");
                        return;
                    }

                    String[] parts = selected.split(":");
                    String targetUser = parts[0];
                    if (targetUser.equals(username))
                        return;

                    // If session exists, just switch
                    if (chatHistory.containsKey(targetUser)) {
                        loadSession(targetUser);
                    } else {
                        // Request connection
                        int confirm = JOptionPane.showConfirmDialog(this, "Send Chat Request to " + targetUser + "?",
                                "Connect", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            String host = parts[1];
                            int port = Integer.parseInt(parts[2]);
                            chatHistory.put(targetUser, new ArrayList<>()); // Create session placeholder
                            peerNode.connectToPeer(host, port, targetUser);
                            loadSession(targetUser);
                            addSystemMessage(targetUser, "Sending chat request...");
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(peerList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChatPanel() {
        GlassPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout());

        chatTitle = new JLabel("Global Chat");
        chatTitle.setFont(Theme.FONT_TITLE);
        chatTitle.setForeground(Theme.TEXT_PRIMARY);
        chatTitle.setBorder(new EmptyBorder(15, 20, 15, 20));
        panel.add(chatTitle, BorderLayout.NORTH);

        chatListPanel = new JPanel();
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(chatListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        messageField = new CuteTextField();
        // messageField styling is now handled internally by CuteTextField

        CuteButton sendBtn = new CuteButton("Send");
        sendBtn.setColors(Theme.ACCENT, Theme.ACCENT_HOVER);
        sendBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        sendBtn.addActionListener(e -> sendMessage());

        CuteFileButton uploadBtn = new CuteFileButton();
        uploadBtn.addActionListener(e -> {
            if (currentSession.equals("GLOBAL")) {
                JOptionPane.showMessageDialog(this, "File sharing not available in Global Chat.");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File f = chooser.getSelectedFile();
                if (f.length() > 200 * 1024 * 1024) {
                    JOptionPane.showMessageDialog(this, "File too large! Max 200MB.");
                    return;
                }
                peerNode.sendFile(currentSession, f);
                addSystemMessage(currentSession, "Sending file: " + f.getName() + "...");
            }
        });

        inputPanel.add(uploadBtn, BorderLayout.WEST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        messageField.addActionListener(e -> sendMessage());

        panel.add(inputPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadSession(String sessionName) {
        currentSession = sessionName;
        chatTitle.setText(sessionName.equals("GLOBAL") ? "Global Chat" : "Chat with " + sessionName);
        chatListPanel.removeAll();

        // Add Glue to push messages to the bottom
        chatListPanel.add(Box.createVerticalGlue());

        List<JPanel> history = chatHistory.getOrDefault(sessionName, new ArrayList<>());
        
        // Load from disk if empty and not loaded before
        if (history.isEmpty()) {
            java.util.List<String> diskHistory = historyManager.loadHistory(sessionName);
            for (String entry : diskHistory) {
                // Format: ME:Msg or PEER:Msg
                if (entry.contains(":")) {
                    String[] parts = entry.split(":", 2);
                    String sender = parts[0].equals("ME") ? "Me" : sessionName;
                    boolean isMe = parts[0].equals("ME");
                    String msg = parts[1];
                    // Handle system messages if any (optional extension)
                    
                    JPanel bubble = new ChatBubblePanel(sender, msg, isMe);
                    history.add(bubble);
                }
            }
            if (!history.isEmpty()) {
                chatHistory.put(sessionName, history);
            }
        }

        for (JPanel bubble : history) {
            chatListPanel.add(bubble);
        }
        chatListPanel.revalidate();
        chatListPanel.repaint();

        // Auto scroll down
        SwingUtilities.invokeLater(() -> {
            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatListPanel);
            if (scroll != null) {
                JScrollBar vertical = scroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty())
            return;

        if (currentSession.equals("GLOBAL")) {
            peerNode.sendGlobalMessage(content);
            // We don't verify delivery for global, just optimistic add?
            // Better to wait for server echo? Let's optimistic add for responsiveness.
            // Actually, server echoes with GLOBAL_MSG sender content.
            // If we add it here, we might duplicate if we also listen to ourselves.
            // Let's NOT add it here, let the echo handle it.
        } else {
            peerNode.sendMessage(currentSession, content);
            addMessageToSession(currentSession, "Me", content, true);
            historyManager.saveMessage(currentSession, content, true); // Save
        }
        messageField.setText("");
    }

    private void addMessageToSession(String session, String sender, String content, boolean isMe) {
        JPanel bubble = new ChatBubblePanel(sender, content, isMe);

        chatHistory.computeIfAbsent(session, k -> new ArrayList<>()).add(bubble);

        if (currentSession.equals(session)) {
            chatListPanel.add(bubble);
            chatListPanel.revalidate();
            chatListPanel.repaint();

            // Scroll down
            SwingUtilities.invokeLater(() -> {
                JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatListPanel);
                if (scroll != null) {
                    JScrollBar vertical = scroll.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                }
            });
        }
    }

    private void addSystemMessage(String session, String msg) {
        // reuse bubble panel logic or just text
        addMessageToSession(session, "System", msg, false);
    }

    private void refreshPeers() {
        peerNode.fetchPeers(peers -> SwingUtilities.invokeLater(() -> {
            peerListModel.clear();
            peerListModel.addElement("Global Chat"); // Always top
            for (String p : peers) {
                if (!p.startsWith(username + ":")) {
                    peerListModel.addElement(p);
                }
            }
        }));
    }

    public void onMessageReceived(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.startsWith("GLOBAL [")) {
                // Format: GLOBAL [User]: Content
                int endBracket = msg.indexOf("]: ");
                String sender = msg.substring(8, endBracket); // "User"
                String content = msg.substring(endBracket + 3);

                boolean isMe = sender.equals(username);
                addMessageToSession("GLOBAL", sender, content, isMe);
            } else if (msg.contains(": ")) {
                // Format: User: Content
                String[] parts = msg.split(": ", 2);
                String sender = parts[0];
                String content = parts[1];

                addMessageToSession(sender, sender, content, false);
                historyManager.saveMessage(sender, content, false); // Save incoming

                // If not current session, maybe notification? (TODO)
            } else {
                // System message
                if (currentSession != null) {
                    addSystemMessage(currentSession, msg);
                }
            }
        });
    }

    public void onChatRequest(String requestor, Consumer<Boolean> decisionCallback) {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Incoming Chat Request from " + requestor + ".\nAccept?",
                    "Chat Request",
                    JOptionPane.YES_NO_OPTION);

            boolean accepted = (result == JOptionPane.YES_OPTION);
            if (accepted) {
                chatHistory.put(requestor, new ArrayList<>());
                loadSession(requestor);
                addSystemMessage(requestor, "Chat started with " + requestor);
            }
            decisionCallback.accept(accepted);
        });
    }

    public void onChatFeedback(String user, boolean accepted) {
        SwingUtilities.invokeLater(() -> {
            if (accepted) {
                JOptionPane.showMessageDialog(this, user + " accepted your chat request! You can now start chatting.",
                        "Request Accepted", JOptionPane.INFORMATION_MESSAGE);
                chatHistory.put(user, new ArrayList<>());
                loadSession(user);
                addSystemMessage(user, "Chat accepted by " + user);
            } else {
                JOptionPane.showMessageDialog(this, user + " denied your chat request.", "Request Denied",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void onFileRequest(String requestInfo, Consumer<Boolean> confirmCallback) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Processing File Request: " + requestInfo);
                // requestInfo: sender:filename:size
                int firstColon = requestInfo.indexOf(':');
                int lastColon = requestInfo.lastIndexOf(':');

                if (firstColon == -1 || lastColon == -1 || firstColon == lastColon) {
                    System.err.println("Invalid file request format: " + requestInfo);
                    confirmCallback.accept(false);
                    return;
                }

                String sender = requestInfo.substring(0, firstColon);
                String filename = requestInfo.substring(firstColon + 1, lastColon);
                String sizeStr = requestInfo.substring(lastColon + 1);
                long size = Long.parseLong(sizeStr);

                String displaySize = (size / 1024) + " KB";
                if (size > 1024 * 1024)
                    displaySize = (size / (1024 * 1024)) + " MB";

                int result = JOptionPane.showConfirmDialog(this,
                        sender + " wants to send you a file: " + filename + " (" + displaySize + ").\nAccept?",
                        "Incoming File",
                        JOptionPane.YES_NO_OPTION);

                boolean accepted = (result == JOptionPane.YES_OPTION);
                if (accepted) {
                    addSystemMessage(sender, "Downloading file " + filename + "...");
                    CuteProgressBar bar = new CuteProgressBar();
                    fileProgressBars.put(sender, bar);
                    incomingFilenames.put(sender, filename);
                }
                confirmCallback.accept(accepted);
            } catch (Exception e) {
                e.printStackTrace();
                confirmCallback.accept(false);
            }
        });
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) chatListPanel.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private final Map<String, CuteProgressBar> fileProgressBars = new ConcurrentHashMap<>();
    private final Map<String, String> incomingFilenames = new ConcurrentHashMap<>();

    public void onFileProgress(String sender, int progress) {
        SwingUtilities.invokeLater(() -> {
            // Logic to update bar if we had one visible
            if (progress == -1) {
                fileProgressBars.remove(sender);
                String filename = incomingFilenames.remove(sender);

                if (filename != null) {
                    FileBubble bubble = new FileBubble(filename, sender, false);
                    chatHistory.computeIfAbsent(sender, k -> new ArrayList<>()).add(bubble);
                    if (currentSession.equals(sender)) {
                        chatListPanel.add(bubble);
                        chatListPanel.revalidate();
                        chatListPanel.repaint();
                        scrollToBottom();
                    }
                }
                // Also show a popup or system message?
                // JOptionPane.showMessageDialog(this, "File transfer from " + sender + "
                // complete!");
            }
        });
    }

    // PeerNode callback fix:
    // We actually receive "System: File ... received" in onMessageReceived.
    // We should trap that to show the bubble? Or update PeerNode to explicitly
    // callback "onFileComplete".
    // Let's modify PeerNode to call onFileProgress with specific code or add
    // onFileComplete.
    // Current PeerNode calls onFileProgress(user, -1).
    // We can't easily get filename there.
    // Let's rely on the System Message for now OR better, change onFileProgress to
    // take an Object?
    // Let's stick to Map. But map key is Sender. Value is ProgressBar.
    // We can extend Map to store Filename.

    // Actually, PeerNode sends `node.onMessageReceived.accept("System: File " +
    // parts[1] + " received.");`
    // We can intercept that in the lambda passed to PeerNode in App.java? No.

    // Let's just create the FileBubble when we receive that specific system
    // message?
    // Risky string parsing.

    // Plan B: Just show "File Received" text for now, and I will update PeerNode to
    // pass filename.
}
