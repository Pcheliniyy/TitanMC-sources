package com.titanmc.server.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.management.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TitanGUI extends JFrame {

    private static TitanGUI instance;
    private static final Color BG_DARK = new Color(24, 24, 32);
    private static final Color BG_PANEL = new Color(32, 34, 44);
    private static final Color BG_CONSOLE = new Color(18, 18, 24);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 230);
    private static final Color TEXT_SECONDARY = new Color(140, 145, 160);
    private static final Color ACCENT_ORANGE = new Color(255, 165, 0);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_RED = new Color(255, 80, 80);
    private static final Color ACCENT_BLUE = new Color(80, 140, 255);
    private static final Color ACCENT_YELLOW = new Color(255, 210, 60);
    private static final Color GRID_LINE = new Color(45, 48, 58);
    private GraphPanel ramGraph;
    private GraphPanel cpuGraph;
    private GraphPanel tpsGraph;
    private JLabel ramLabel;
    private JLabel cpuLabel;
    private JLabel tpsLabel;
    private JLabel playersLabel;
    private JLabel uptimeLabel;
    private JLabel entitiesLabel;
    private JLabel chunksLabel;
    private JLabel dupeFixLabel;
    private JLabel exploitFixLabel;
    private JLabel optimizationLabel;
    private JTextPane consoleOutput;
    private JTextField consoleInput;
    private StyledDocument consoleDoc;
    private final Deque<Double> ramHistory = new ConcurrentLinkedDeque<>();
    private final Deque<Double> cpuHistory = new ConcurrentLinkedDeque<>();
    private final Deque<Double> tpsHistory = new ConcurrentLinkedDeque<>();
    private static final int HISTORY_SIZE = 120;

    private long startTime;
    private Timer updateTimer;
    private java.util.function.Consumer<String> commandHandler;
    private long lastTickTime = System.nanoTime();
    private final Deque<Long> tickTimes = new ConcurrentLinkedDeque<>();
    private static final int TPS_SAMPLE_SIZE = 100;
    private volatile int dupeFixCount = 0;
    private volatile int exploitFixCount = 0;
    private volatile int optimizationCount = 0;
    private volatile int playerCount = 0;
    private volatile int entityCount = 0;
    private volatile int chunkCount = 0;

    public TitanGUI() {
        instance = this;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < HISTORY_SIZE; i++) {
            ramHistory.add(0.0);
            cpuHistory.add(0.0);
            tpsHistory.add(20.0);
        }

        setupFrame();
        setupComponents();
        startUpdateTimer();
        redirectOutput();
    }

    private void setupFrame() {
        setTitle("TitanMC 1.16.5 - Server Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    TitanGUI.this,
                    "Stop the server?",
                    "TitanMC",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    if (commandHandler != null) {
                        commandHandler.accept("stop");
                    }
                }
            }
        });

        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
        try {
            setIconImage(createIcon());
        } catch (Exception ignored) {}
    }

    private void setupComponents() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_PANEL);
        topBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel titleLabel = new JLabel("TitanMC 1.16.5");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_ORANGE);
        topBar.add(titleLabel, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        topRight.setOpaque(false);

        uptimeLabel = createInfoLabel("Uptime: 0:00:00");
        playersLabel = createInfoLabel("Players: 0");
        topRight.add(playersLabel);
        topRight.add(uptimeLabel);
        topBar.add(topRight, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        JPanel mainContent = new JPanel(new BorderLayout(8, 8));
        mainContent.setBackground(BG_DARK);
        mainContent.setBorder(new EmptyBorder(8, 8, 8, 8));
        JPanel graphsPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        graphsPanel.setOpaque(false);
        ramGraph = new GraphPanel("RAM", ACCENT_BLUE, "%");
        ramLabel = ramGraph.getValueLabel();
        graphsPanel.add(ramGraph);
        cpuGraph = new GraphPanel("CPU", ACCENT_ORANGE, "%");
        cpuLabel = cpuGraph.getValueLabel();
        graphsPanel.add(cpuGraph);
        tpsGraph = new GraphPanel("TPS", ACCENT_GREEN, "");
        tpsGraph.setMaxValue(20.0);
        tpsLabel = tpsGraph.getValueLabel();
        graphsPanel.add(tpsGraph);
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(240, 0));
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);

        statsPanel.add(createStatCard("Server Info", new String[]{
            "Version: Paper 1.16.5 + TitanMC",
            "Java: " + System.getProperty("java.version")
        }));
        JPanel perfCard = createStatsCard("Performance");
        entitiesLabel = addStatRow(perfCard, "Entities:", "0");
        chunksLabel = addStatRow(perfCard, "Loaded Chunks:", "0");
        statsPanel.add(perfCard);
        JPanel protCard = createStatsCard("Protection");
        dupeFixLabel = addStatRow(protCard, "Dupe Fixes:", "0");
        exploitFixLabel = addStatRow(protCard, "Exploit Fixes:", "0");
        optimizationLabel = addStatRow(protCard, "Optimizations:", "0");
        statsPanel.add(protCard);

        rightPanel.add(statsPanel, BorderLayout.NORTH);
        JPanel topContent = new JPanel(new BorderLayout(8, 0));
        topContent.setOpaque(false);
        topContent.setPreferredSize(new Dimension(0, 380));
        topContent.add(graphsPanel, BorderLayout.CENTER);
        topContent.add(rightPanel, BorderLayout.EAST);

        mainContent.add(topContent, BorderLayout.NORTH);
        JPanel consolePanel = new JPanel(new BorderLayout(0, 4));
        consolePanel.setOpaque(false);
        consolePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(GRID_LINE),
            " Console ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            TEXT_SECONDARY
        ));

        consoleOutput = new JTextPane();
        consoleOutput.setEditable(false);
        consoleOutput.setBackground(BG_CONSOLE);
        consoleOutput.setForeground(TEXT_PRIMARY);
        consoleOutput.setFont(new Font("Consolas", Font.PLAIN, 12));
        consoleOutput.setCaretColor(ACCENT_ORANGE);
        consoleDoc = consoleOutput.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(consoleOutput);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CONSOLE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        consolePanel.add(scrollPane, BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new BorderLayout(4, 0));
        inputPanel.setOpaque(false);

        JLabel promptLabel = new JLabel(" > ");
        promptLabel.setForeground(ACCENT_ORANGE);
        promptLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        inputPanel.add(promptLabel, BorderLayout.WEST);

        consoleInput = new JTextField();
        consoleInput.setBackground(BG_CONSOLE);
        consoleInput.setForeground(TEXT_PRIMARY);
        consoleInput.setCaretColor(ACCENT_ORANGE);
        consoleInput.setFont(new Font("Consolas", Font.PLAIN, 13));
        consoleInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_LINE),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        consoleInput.addActionListener(e -> {
            String cmd = consoleInput.getText().trim();
            if (!cmd.isEmpty() && commandHandler != null) {
                appendConsole("> " + cmd + "\n", ACCENT_ORANGE);
                commandHandler.accept(cmd);
                consoleInput.setText("");
            }
        });
        inputPanel.add(consoleInput, BorderLayout.CENTER);

        consolePanel.add(inputPanel, BorderLayout.SOUTH);
        mainContent.add(consolePanel, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void startUpdateTimer() {
        updateTimer = new Timer("TitanMC-GUI", true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStats();
            }
        }, 1000, 1000);
    }

    private void updateStats() {
        Runtime rt = Runtime.getRuntime();
        long usedMem = rt.totalMemory() - rt.freeMemory();
        long maxMem = rt.maxMemory();
        double ramPercent = (usedMem * 100.0) / maxMem;
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = -1;
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
            }
        } catch (Exception e) {
            cpuLoad = osBean.getSystemLoadAverage();
            if (cpuLoad < 0) cpuLoad = 0;
        }
        if (cpuLoad < 0) cpuLoad = 0;
        double tps = calculateTPS();
        addToHistory(ramHistory, ramPercent);
        addToHistory(cpuHistory, cpuLoad);
        addToHistory(tpsHistory, tps);
        long uptime = System.currentTimeMillis() - startTime;
        String uptimeStr = formatDuration(uptime);

        final double fRam = ramPercent;
        final double fCpu = cpuLoad;
        final double fTps = tps;
        final long fUsed = usedMem / (1024 * 1024);
        final long fMax = maxMem / (1024 * 1024);
        final String fUptime = uptimeStr;

        SwingUtilities.invokeLater(() -> {
            ramLabel.setText(String.format("%.1f%%  (%dMB / %dMB)", fRam, fUsed, fMax));
            ramLabel.setForeground(fRam > 85 ? ACCENT_RED : fRam > 65 ? ACCENT_YELLOW : ACCENT_BLUE);

            cpuLabel.setText(String.format("%.1f%%", fCpu));
            cpuLabel.setForeground(fCpu > 80 ? ACCENT_RED : fCpu > 50 ? ACCENT_YELLOW : ACCENT_ORANGE);

            tpsLabel.setText(String.format("%.2f", fTps));
            tpsLabel.setForeground(fTps >= 19.5 ? ACCENT_GREEN : fTps >= 17 ? ACCENT_YELLOW : ACCENT_RED);

            uptimeLabel.setText("Uptime: " + fUptime);
            playersLabel.setText("Players: " + playerCount);
            entitiesLabel.setText(String.valueOf(entityCount));
            chunksLabel.setText(String.valueOf(chunkCount));
            dupeFixLabel.setText(String.valueOf(dupeFixCount));
            exploitFixLabel.setText(String.valueOf(exploitFixCount));
            optimizationLabel.setText(String.valueOf(optimizationCount));
            ramGraph.setData(ramHistory);
            cpuGraph.setData(cpuHistory);
            tpsGraph.setData(tpsHistory);
        });
    }

    
    public void onTick() {
        long now = System.nanoTime();
        tickTimes.addLast(now);
        while (tickTimes.size() > TPS_SAMPLE_SIZE) {
            tickTimes.pollFirst();
        }
    }

    private double calculateTPS() {
        if (tickTimes.size() < 2) return 20.0;

        Long[] times = tickTimes.toArray(new Long[0]);
        if (times.length < 2) return 20.0;

        long elapsed = times[times.length - 1] - times[0];
        if (elapsed <= 0) return 20.0;

        double tps = (times.length - 1) / (elapsed / 1_000_000_000.0);
        return Math.min(20.0, tps);
    }

    public void setCommandHandler(java.util.function.Consumer<String> handler) {
        this.commandHandler = handler;
    }

    public void setStats(int dupeFixes, int exploitFixes, int optimizations) {
        this.dupeFixCount = dupeFixes;
        this.exploitFixCount = exploitFixes;
        this.optimizationCount = optimizations;
    }

    public void setPlayerCount(int count) { this.playerCount = count; }
    public void setEntityCount(int count) { this.entityCount = count; }
    public void setChunkCount(int count) { this.chunkCount = count; }

    public void appendConsole(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = consoleOutput.addStyle("colored", null);
                StyleConstants.setForeground(style, color);
                consoleDoc.insertString(consoleDoc.getLength(), text, style);
                consoleOutput.setCaretPosition(consoleDoc.getLength());
                if (consoleDoc.getLength() > 200000) {
                    consoleDoc.remove(0, consoleDoc.getLength() - 150000);
                }
            } catch (BadLocationException ignored) {}
        });
    }

    public void appendConsole(String text) {
        Color color = TEXT_PRIMARY;
        if (text.contains("ERROR") || text.contains("SEVERE")) {
            color = ACCENT_RED;
        } else if (text.contains("WARN")) {
            color = ACCENT_YELLOW;
        } else if (text.contains("[TitanMC]")) {
            color = ACCENT_ORANGE;
        } else if (text.contains("INFO")) {
            color = TEXT_PRIMARY;
        }
        appendConsole(text, color);
    }

    private void redirectOutput() {
        PrintStream guiOut = new PrintStream(new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                char c = (char) b;
                buffer.append(c);
                if (c == '\n') {
                    String line = buffer.toString();
                    buffer.setLength(0);
                    appendConsole(line);
                }
            }
        }, true);
        PrintStream original = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                original.write(b);
                guiOut.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                original.write(b, off, len);
                guiOut.write(b, off, len);
            }
        }, true));
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                originalErr.write(b);
                guiOut.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                originalErr.write(b, off, len);
                guiOut.write(b, off, len);
            }
        }, true));
    }

    private void addToHistory(Deque<Double> history, double value) {
        history.addLast(value);
        while (history.size() > HISTORY_SIZE) {
            history.pollFirst();
        }
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%d:%02d:%02d", h, m, s);
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    private JPanel createStatCard(String title, String[] lines) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_LINE),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(ACCENT_ORANGE);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));

        for (String line : lines) {
            JLabel lbl = new JLabel(line);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(TEXT_SECONDARY);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(lbl);
        }

        return card;
    }

    private JPanel createStatsCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_LINE),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(ACCENT_ORANGE);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(6));

        return card;
    }

    private JLabel addStatRow(JPanel card, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLbl.setForeground(TEXT_SECONDARY);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLbl.setForeground(ACCENT_GREEN);

        row.add(labelLbl, BorderLayout.WEST);
        row.add(valueLbl, BorderLayout.EAST);
        card.add(row);

        return valueLbl;
    }

    private Image createIcon() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32, 32,
            java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ACCENT_ORANGE);
        g.fillRoundRect(0, 0, 32, 32, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g.drawString("T", 8, 25);
        g.dispose();
        return img;
    }

    public static TitanGUI getInstance() { return instance; }

    public static TitanGUI createAndShow() {
        TitanGUI gui = new TitanGUI();
        gui.setVisible(true);
        return gui;
    }

    
    static class GraphPanel extends JPanel {
        private final String name;
        private final Color lineColor;
        private final String suffix;
        private final JLabel valueLabel;
        private double maxValue = 100.0;
        private Double[] data = new Double[0];

        GraphPanel(String name, Color lineColor, String suffix) {
            this.name = name;
            this.lineColor = lineColor;
            this.suffix = suffix;

            setBackground(BG_PANEL);
            setBorder(new EmptyBorder(4, 4, 4, 4));
            setLayout(new BorderLayout());
            JPanel titleBar = new JPanel(new BorderLayout());
            titleBar.setOpaque(false);
            titleBar.setBorder(new EmptyBorder(4, 8, 2, 8));

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(lineColor);
            titleBar.add(nameLabel, BorderLayout.WEST);

            valueLabel = new JLabel("0" + suffix);
            valueLabel.setFont(new Font("Consolas", Font.BOLD, 13));
            valueLabel.setForeground(lineColor);
            titleBar.add(valueLabel, BorderLayout.EAST);

            add(titleBar, BorderLayout.NORTH);
        }

        JLabel getValueLabel() { return valueLabel; }

        void setMaxValue(double max) { this.maxValue = max; }

        void setData(Deque<Double> history) {
            this.data = history.toArray(new Double[0]);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.length < 2) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x0 = 8, y0 = 28;
            int w = getWidth() - 16;
            int h = getHeight() - 36;

            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }
            g2.setColor(GRID_LINE);
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i <= 4; i++) {
                int y = y0 + (h * i / 4);
                g2.drawLine(x0, y, x0 + w, y);
            }
            int len = data.length;
            int[] xPoints = new int[len];
            int[] yPoints = new int[len];

            for (int i = 0; i < len; i++) {
                xPoints[i] = x0 + (w * i / (len - 1));
                double val = Math.max(0, Math.min(data[i], maxValue));
                yPoints[i] = y0 + h - (int) (h * val / maxValue);
            }
            int[] fillX = new int[len + 2];
            int[] fillY = new int[len + 2];
            System.arraycopy(xPoints, 0, fillX, 0, len);
            System.arraycopy(yPoints, 0, fillY, 0, len);
            fillX[len] = xPoints[len - 1];
            fillY[len] = y0 + h;
            fillX[len + 1] = xPoints[0];
            fillY[len + 1] = y0 + h;

            g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 30));
            g2.fillPolygon(fillX, fillY, len + 2);
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(xPoints, yPoints, len);

            g2.dispose();
        }
    }
}
