import model.*;
import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Swing GUI — Section 10.3 Extra Credit.
 * Enhanced with login screen, role-based access, and CRUD management tab.
 * Uses Searchable + Persistable interfaces for polymorphic design.
 */
public class GameGUI extends JFrame {

    private final Searchable searchService = new SearchService();
    private final Persistable persistence = new JsonPersistence();
    private GameData data;
    private JTabbedPane tabs;
    private String currentUser;
    private boolean isAdmin;

    public GameGUI() {
        setTitle("Honor of Kings IMS");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (data != null) persistence.save(data);
                dispose();
                System.exit(0);
            }
        });
        setSize(860, 680);
        setLocationRelativeTo(null);

        // Login first
        if (!showLogin()) {
            System.exit(0);
        }

        buildTabs();
        add(tabs);
    }

    /** Login dialog — authenticate admin or player */
    private boolean showLogin() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Admin", "Player"});
        loginPanel.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        loginPanel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        loginPanel.add(passField, gbc);

        // Load data
        data = persistence.load();
        if (data == null) {
            data = DataInitializer.initAll();
        }

        int result = JOptionPane.showConfirmDialog(this, loginPanel,
                "Login — Honor of Kings IMS", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return false;

        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if ("Admin".equals(role)) {
            for (Admin a : data.getAdmins()) {
                if (a.getUsername().equals(username) && a.getPassword().equals(password)) {
                    currentUser = a.getUsername();
                    isAdmin = true;
                    return true;
                }
            }
        } else {
            for (Player p : data.getPlayers()) {
                if (p.getUsername().equalsIgnoreCase(username) && p.getPassword().equals(password)) {
                    currentUser = p.getUsername();
                    isAdmin = false;
                    return true;
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Invalid credentials!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        return showLogin(); // retry
    }

    /** Build all tabs based on user role */
    private void buildTabs() {
        tabs = new JTabbedPane();
        tabs.addTab("Player Lookup", playerPanel());
        tabs.addTab("Team Overview", teamPanel());
        tabs.addTab("Hero Details", heroPanel());
        tabs.addTab("Equip Ranking", equipPanel());
        tabs.addTab("Leaderboard", leaderboardPanel());
        tabs.addTab("Match History", matchHistoryPanel());
        tabs.addTab("Combat Sim", combatPanel());
        tabs.addTab("Recommend", recommendPanel());
        if (isAdmin) {
            tabs.addTab("CRUD Management", crudPanel());
        }
    }

    // ========== Player Lookup ==========
    private JPanel playerPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel top = new JPanel(new FlowLayout());
        JTextField input = new JTextField(12);
        JButton btn = new JButton("Search");
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);

        btn.addActionListener(e -> {
            String name = input.getText().trim();
            if (name.isEmpty()) { result.setText("Please enter a username"); return; }
            result.setText(searchService.findPlayerByName(data, name));
        });

        top.add(new JLabel("Username:"));
        top.add(input);
        top.add(btn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Team Overview ==========
    private JPanel teamPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel top = new JPanel(new FlowLayout());
        JComboBox<String> combo = new JComboBox<>();
        for (Team t : data.getTeams()) combo.addItem(t.getTeamName());
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("View");
        btn.addActionListener(e -> result.setText(
                searchService.findTeamByName(data, (String) combo.getSelectedItem())));
        top.add(new JLabel("Team:"));
        top.add(combo);
        top.add(btn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Hero Details ==========
    private JPanel heroPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel top = new JPanel(new FlowLayout());
        JComboBox<String> combo = new JComboBox<>();
        for (Hero h : data.getHeroes()) combo.addItem(h.getName());
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("View");
        btn.addActionListener(e -> result.setText(
                searchService.findHeroByName(data, (String) combo.getSelectedItem())));
        top.add(new JLabel("Hero:"));
        top.add(combo);
        top.add(btn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Equipment Ranking ==========
    private JPanel equipPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("Refresh");
        btn.addActionListener(e -> result.setText(
                searchService.showEquipmentRanking(data)));
        panel.add(btn, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Leaderboard ==========
    private JPanel leaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("Refresh");
        btn.addActionListener(e -> result.setText(
                searchService.showLeaderboard(data)));
        panel.add(btn, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Match History ==========
    private JPanel matchHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel top = new JPanel(new FlowLayout());
        JTextField input = new JTextField(12);
        JButton btn = new JButton("Search");
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        btn.addActionListener(e -> {
            String name = input.getText().trim();
            if (name.isEmpty()) { result.setText("Please enter a player or team name"); return; }
            result.setText(searchService.showMatchHistory(data, name));
        });
        top.add(new JLabel("Player/Team:"));
        top.add(input);
        top.add(btn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Combat Simulation ==========
    private JPanel combatPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel top = new JPanel(new FlowLayout());
        JComboBox<String> h1 = new JComboBox<>();
        JComboBox<String> h2 = new JComboBox<>();
        for (Hero h : data.getHeroes()) { h1.addItem(h.getName()); h2.addItem(h.getName()); }
        h2.setSelectedIndex(1);
        JTextArea result = new JTextArea(12, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("Fight!");
        btn.addActionListener(e -> result.setText(
                CombatSimulator.simulate(data, (String) h1.getSelectedItem(), (String) h2.getSelectedItem())));
        top.add(new JLabel("Hero A:"));
        top.add(h1);
        top.add(new JLabel("  VS  Hero B:"));
        top.add(h2);
        top.add(btn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Recommendation Engine ==========
    private JPanel recommendPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel top = new JPanel(new FlowLayout());
        JComboBox<String> playerCombo = new JComboBox<>();
        for (Player p : data.getPlayers()) playerCombo.addItem(p.getUsername());
        JComboBox<String> heroCombo = new JComboBox<>();
        for (Hero h : data.getHeroes()) heroCombo.addItem(h.getName());
        JTextArea result = new JTextArea(14, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btnHeroRec = new JButton("Recommend Heroes");
        JButton btnEquipRec = new JButton("Recommend Equipment");
        btnHeroRec.addActionListener(e -> result.setText(
                RecommendationService.recommendHeroesForPlayer(data, (String) playerCombo.getSelectedItem())));
        btnEquipRec.addActionListener(e -> result.setText(
                RecommendationService.recommendEquipmentForHero(data, (String) heroCombo.getSelectedItem())));
        top.add(new JLabel("Player:"));
        top.add(playerCombo);
        top.add(btnHeroRec);
        top.add(new JLabel("| Hero:"));
        top.add(heroCombo);
        top.add(btnEquipRec);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== CRUD Management (Admin only) ==========
    private JPanel crudPanel() {
        JTabbedPane crudTabs = new JTabbedPane();
        crudTabs.addTab("Player", playerCrudPanel());
        crudTabs.addTab("Hero", heroCrudPanel());
        crudTabs.addTab("Equipment", equipmentCrudPanel());
        crudTabs.addTab("Team", teamCrudPanel());

        JPanel panel = new JPanel(new BorderLayout());
        JTextArea log = new JTextArea(8, 60);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));
        log.setEditable(false);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder("Operation Log"));
        bottom.add(new JScrollPane(log), BorderLayout.CENTER);

        panel.add(crudTabs, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel playerCrudPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(10);
        JTextField rankField = new JTextField(6);
        JTextField wrField = new JTextField(5);
        JTextField matchesField = new JTextField(5);
        JTextArea log = new JTextArea(6, 40);
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));

        g.gridx = 0; g.gridy = 0; panel.add(new JLabel("Username:"), g);
        g.gridx = 1; panel.add(nameField, g);
        g.gridx = 0; g.gridy = 1; panel.add(new JLabel("Rank:"), g);
        g.gridx = 1; panel.add(rankField, g);
        g.gridx = 0; g.gridy = 2; panel.add(new JLabel("Win Rate (%):"), g);
        g.gridx = 1; panel.add(wrField, g);
        g.gridx = 0; g.gridy = 3; panel.add(new JLabel("Matches:"), g);
        g.gridx = 1; panel.add(matchesField, g);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Player");
        JButton delBtn = new JButton("Delete Player");
        btnPanel.add(addBtn);
        btnPanel.add(delBtn);

        addBtn.addActionListener(ev -> {
            final String nm = nameField.getText().trim();
            if (nm.isEmpty()) { log.setText("Error: username cannot be empty."); return; }
            final String rk = rankField.getText().trim().isEmpty() ? "Gold" : rankField.getText().trim();
            final double wr = parseDoubleField(wrField, 50);
            final int m = parseIntField(matchesField, 0);
            String result = DataManager.addPlayer(data, nm, rk, wr, m);
            log.setText(result);
            nameField.setText("");
            rankField.setText("");
            wrField.setText("");
            matchesField.setText("");
        });

        delBtn.addActionListener(ev -> {
            final String nm = nameField.getText().trim();
            if (nm.isEmpty()) { log.setText("Error: enter username to delete."); return; }
            String result = DataManager.removePlayer(data, nm);
            log.setText(result);
            nameField.setText("");
        });

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; panel.add(btnPanel, g);
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        panel.add(new JScrollPane(log), g);

        return panel;
    }

    private JPanel heroCrudPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(10);
        JComboBox<String> roleCombo = new JComboBox<>(
                new String[]{"WARRIOR", "MAGE", "ASSASSIN", "TANK", "MARKSMAN", "SUPPORT"});
        JTextField hpField = new JTextField(5);
        JTextField atkField = new JTextField(5);
        JTextField defField = new JTextField(5);
        JTextArea log = new JTextArea(6, 40);
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));

        g.gridx = 0; g.gridy = 0; panel.add(new JLabel("Hero Name:"), g);
        g.gridx = 1; panel.add(nameField, g);
        g.gridx = 0; g.gridy = 1; panel.add(new JLabel("Role:"), g);
        g.gridx = 1; panel.add(roleCombo, g);
        g.gridx = 0; g.gridy = 2; panel.add(new JLabel("HP/ATK/DEF:"), g);
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        statsPanel.add(hpField);
        statsPanel.add(atkField);
        statsPanel.add(defField);
        g.gridx = 1; panel.add(statsPanel, g);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Hero");
        JButton delBtn = new JButton("Delete Hero");
        btnPanel.add(addBtn);
        btnPanel.add(delBtn);

        addBtn.addActionListener(ev -> {
            final String nm = nameField.getText().trim();
            if (nm.isEmpty()) { log.setText("Error: hero name cannot be empty."); return; }
            final HeroRole role = HeroRole.valueOf((String) roleCombo.getSelectedItem());
            final int hp = parseIntField(hpField, 3000);
            final int atk = parseIntField(atkField, 100);
            final int def = parseIntField(defField, 80);
            String result = DataManager.addHero(data, nm, role, hp, atk, def);
            log.setText(result);
            nameField.setText(""); hpField.setText(""); atkField.setText(""); defField.setText("");
        });

        delBtn.addActionListener(ev -> {
            final String nm = nameField.getText().trim();
            if (nm.isEmpty()) { log.setText("Error: enter hero name to delete."); return; }
            String result = DataManager.removeHero(data, nm);
            log.setText(result);
            nameField.setText("");
        });

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; panel.add(btnPanel, g);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        panel.add(new JScrollPane(log), g);

        return panel;
    }

    private JPanel equipmentCrudPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(10);
        JComboBox<String> typeCombo = new JComboBox<>(
                new String[]{"ATTACK", "DEFENSE", "MAGIC", "MOVEMENT", "JUNGLE"});
        JTextField atkField = new JTextField(4);
        JTextField defField = new JTextField(4);
        JTextField hpField = new JTextField(4);
        JTextField priceField = new JTextField(5);
        JTextArea log = new JTextArea(6, 40);
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));

        g.gridx = 0; g.gridy = 0; panel.add(new JLabel("Name:"), g);
        g.gridx = 1; panel.add(nameField, g);
        g.gridx = 0; g.gridy = 1; panel.add(new JLabel("Type:"), g);
        g.gridx = 1; panel.add(typeCombo, g);
        g.gridx = 0; g.gridy = 2; panel.add(new JLabel("ATK/DEF/HP/Price:"), g);
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        statsPanel.add(atkField); statsPanel.add(defField); statsPanel.add(hpField); statsPanel.add(priceField);
        g.gridx = 1; panel.add(statsPanel, g);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Equipment");
        JButton delBtn = new JButton("Delete Equipment");
        btnPanel.add(addBtn);
        btnPanel.add(delBtn);

        addBtn.addActionListener(ev -> {
            final String nm = nameField.getText().trim();
            if (nm.isEmpty()) { log.setText("Error: equipment name cannot be empty."); return; }
            final EquipmentType type = EquipmentType.valueOf((String) typeCombo.getSelectedItem());
            final int atk = parseIntField(atkField, 0);
            final int def = parseIntField(defField, 0);
            final int hp = parseIntField(hpField, 0);
            final int price = parseIntField(priceField, 1000);
            String result = DataManager.addEquipment(data, nm, type, atk, def, hp, price);
            log.setText(result);
            nameField.setText(""); atkField.setText(""); defField.setText(""); hpField.setText(""); priceField.setText("");
        });

        delBtn.addActionListener(ev -> {
            final String nm = nameField.getText().trim();
            if (nm.isEmpty()) { log.setText("Error: enter equipment name to delete."); return; }
            String result = DataManager.removeEquipment(data, nm);
            log.setText(result);
            nameField.setText("");
        });

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; panel.add(btnPanel, g);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        panel.add(new JScrollPane(log), g);

        return panel;
    }

    private JPanel teamCrudPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextArea log = new JTextArea(6, 40);
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));

        g.gridx = 0; g.gridy = 0; panel.add(new JLabel("Team Name:"), g);
        g.gridx = 1; panel.add(nameField, g);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Team");
        JButton delBtn = new JButton("Delete Team");
        btnPanel.add(addBtn);
        btnPanel.add(delBtn);

        addBtn.addActionListener(ev -> {
            final String nm = nameField.getText().trim();
            if (nm.isEmpty()) { log.setText("Error: team name cannot be empty."); return; }
            String result = DataManager.addTeam(data, nm);
            log.setText(result);
            nameField.setText("");
        });

        delBtn.addActionListener(ev -> {
            final String nm = nameField.getText().trim();
            if (nm.isEmpty()) { log.setText("Error: enter team name to delete."); return; }
            String result = DataManager.removeTeam(data, nm);
            log.setText(result);
            nameField.setText("");
        });

        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; panel.add(btnPanel, g);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        panel.add(new JScrollPane(log), g);

        return panel;
    }

    // ========== Utility Methods ==========

    private int parseIntField(JTextField field, int defaultValue) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDoubleField(JTextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
