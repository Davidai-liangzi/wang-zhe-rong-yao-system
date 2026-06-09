import model.*;
import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Swing GUI - Section 10.3 Extra Credit
 * Supports: Player Lookup, Team Overview, Hero Details, Equipment Ranking, Leaderboard, Combat Simulation, Equipment Reco, Hero Reco
 */
public class GameGUI extends JFrame {

    private GameData data;
    private JTabbedPane tabs;

    public GameGUI() {
        data = DataInitializer.initAll();
        setTitle("Honor of Kings IMS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        tabs = new JTabbedPane();
        tabs.addTab("Player Lookup", playerPanel());
        tabs.addTab("Team Overview", teamPanel());
        tabs.addTab("Hero Details", heroPanel());
        tabs.addTab("Equip Ranking", equipPanel());
        tabs.addTab("Leaderboard", leaderboardPanel());
        tabs.addTab("Combat Sim", combatPanel());
        tabs.addTab("Recommend", recommendPanel());

        add(tabs);
    }

    // ========== Player Lookup ==========
    private JPanel playerPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        JPanel top = new JPanel(new FlowLayout());
        JTextField input = new JTextField(12);
        JButton btn = new JButton("Search");
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);

        btn.addActionListener(e -> {
            String name = input.getText().trim();
            if (name.isEmpty()) { result.setText("Please enter a username"); return; }
            result.setText(capturePlayerLookup(name));
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
        JPanel panel = new JPanel(new BorderLayout(5,5));
        JPanel top = new JPanel(new FlowLayout());
        JComboBox<String> combo = new JComboBox<>();
        for (Team t : data.getTeams()) combo.addItem(t.getTeamName());
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("View");
        btn.addActionListener(e -> result.setText(captureTeam((String)combo.getSelectedItem())));
        top.add(new JLabel("Team:"));
        top.add(combo);
        top.add(btn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Hero Details ==========
    private JPanel heroPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        JPanel top = new JPanel(new FlowLayout());
        JComboBox<String> combo = new JComboBox<>();
        for (Hero h : data.getHeroes()) combo.addItem(h.getName());
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("View");

        btn.addActionListener(e -> result.setText(captureHero((String)combo.getSelectedItem())));

        top.add(new JLabel("Hero:"));
        top.add(combo);
        top.add(btn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Equipment Ranking ==========
    private JPanel equipPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("Refresh");
        btn.addActionListener(e -> result.setText(captureEquipmentRanking()));

        panel.add(btn, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Leaderboard ==========
    private JPanel leaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        JTextArea result = new JTextArea(18, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("Refresh");
        btn.addActionListener(e -> result.setText(captureLeaderboard()));

        panel.add(btn, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    // ========== Combat Simulation ==========
    private JPanel combatPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        JPanel top = new JPanel(new FlowLayout());
        JComboBox<String> h1 = new JComboBox<>();
        JComboBox<String> h2 = new JComboBox<>();
        for (Hero h : data.getHeroes()) { h1.addItem(h.getName()); h2.addItem(h.getName()); }
        h2.setSelectedIndex(1);
        JTextArea result = new JTextArea(12, 60);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        result.setEditable(false);
        JButton btn = new JButton("Fight!");

        btn.addActionListener(e -> {
            result.setText(captureCombat((String)h1.getSelectedItem(), (String)h2.getSelectedItem()));
        });

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
        JPanel panel = new JPanel(new BorderLayout(5,5));
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

        btnHeroRec.addActionListener(e ->
                result.setText(captureHeroRecommend((String)playerCombo.getSelectedItem())));
        btnEquipRec.addActionListener(e ->
                result.setText(captureEquipRecommend((String)heroCombo.getSelectedItem())));

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

    // ========== Result capture method (redirect System.out to string) ==========

    private synchronized String capture(Runnable action) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(baos);
        java.io.PrintStream old = System.out;
        System.setOut(ps);
        try {
            action.run();
        } catch (Exception ex) {
            System.setOut(old);
            return "Error: " + ex.getMessage();
        }
        System.setOut(old);
        return baos.toString();
    }

    private String capturePlayerLookup(String name) {
        return capture(() -> SearchService.findPlayerByName(data, name));
    }

    private String captureTeam(String name) {
        return capture(() -> SearchService.findTeamByName(data, name));
    }

    private String captureHero(String name) {
        return capture(() -> SearchService.findHeroByName(data, name));
    }

    private String captureEquipmentRanking() {
        return capture(() -> SearchService.showEquipmentRanking(data));
    }

    private String captureLeaderboard() {
        return capture(() -> SearchService.showLeaderboard(data));
    }

    private String captureCombat(String h1, String h2) {
        return capture(() -> CombatSimulator.simulate(data, h1, h2));
    }

    private String captureHeroRecommend(String player) {
        return capture(() -> RecommendationService.recommendHeroesForPlayer(data, player));
    }

    private String captureEquipRecommend(String hero) {
        return capture(() -> RecommendationService.recommendEquipmentForHero(data, hero));
    }
}
