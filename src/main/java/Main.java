import model.*;
import service.*;
import java.util.Scanner;
import java.util.List;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Honor of Kings IMS ===");
        System.out.println("Select mode:");
        System.out.println("1. Console");
        System.out.println("2. GUI (Swing)");
        System.out.print("Choice: ");

        Scanner sc = new Scanner(System.in);
        int mode = sc.nextInt();
        sc.nextLine();

        if (mode == 2) {
            SwingUtilities.invokeLater(() -> {
                GameGUI gui = new GameGUI();
                gui.setVisible(true);
            });
            return;
        }

        // Try loading from save, fall back to initial data
        GameData data = FilePersistence.loadData();
        if (data == null) {
            data = DataInitializer.initAll();
        }
        System.out.println("System ready. Loaded " + data.getPlayers().size() + " players.");

        Player currentPlayer = null;

        // Simple login: choose role
        System.out.println("Select role:");
        System.out.println("1. Admin");
        System.out.println("2. Player");
        System.out.print("Choice: ");
        int role = sc.nextInt();
        sc.nextLine(); // consume newline

        if (role == 1) {
            showAdminMenu();
        } else if (role == 2) {
            System.out.print("Enter your username: ");
            String loginName = sc.nextLine();
            currentPlayer = findPlayer(data, loginName);
            if (currentPlayer == null) {
                System.out.println("Player not found. Exiting.");
                sc.close();
                return;
            }
            System.out.println("Welcome, " + currentPlayer.getUsername() + "!");
            showPlayerMenu();
        } else {
            System.out.println("Invalid choice. Exiting.");
            sc.close();
            return;
        }

        // Menu loop
        while (true) {
            System.out.print("Choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            if (choice == 0) {
                System.out.println("Goodbye!");
                FilePersistence.saveData(data);
                break;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter player username: ");
                    String name = sc.nextLine();
                    SearchService.findPlayerByName(data, name);
                    break;
                case 2:
                    System.out.print("Enter team name: ");
                    String teamName = sc.nextLine();
                    SearchService.findTeamByName(data, teamName);
                    break;
                case 3:
                    System.out.print("Enter hero name: ");
                    String heroName = sc.nextLine();
                    SearchService.findHeroByName(data, heroName);
                    break;
                case 4:
                    SearchService.showEquipmentRanking(data);
                    break;
                case 5:
                    System.out.print("Enter player or team name: ");
                    String matchInput = sc.nextLine();
                    SearchService.showMatchHistory(data, matchInput);
                    break;
                case 6:
                    SearchService.showLeaderboard(data);
                    break;
                case 7:
                    if (role == 1) {
                        dataManageMenu(sc, data);
                    } else if (currentPlayer != null) {
                        SearchService.findPlayerByName(data, currentPlayer.getUsername());
                    } else {
                        System.out.println("Access denied.");
                    }
                    break;
                case 8:
                    if (currentPlayer != null) {
                        editOwnInfo(sc, currentPlayer);
                    } else {
                        System.out.println("Access denied.");
                    }
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }

        sc.close();
    }

    /** Find player by username */
    static Player findPlayer(GameData data, String name) {
        for (Player p : data.getPlayers()) {
            if (p.getUsername().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    /** Player edits own info */
    static void editOwnInfo(Scanner sc, Player p) {
        System.out.println();
        System.out.println("=== Edit Profile ===");
        System.out.println("Current: rank=" + p.getRank() + ", winRate=" + p.getWinRate() + "%, matches=" + p.getMatchesPlayed());
        System.out.print("New rank (Enter to skip): ");
        String newRank = sc.nextLine();
        if (!newRank.isEmpty()) p.setRank(newRank);
        System.out.print("New winRate (-1 to skip): ");
        double newWR = sc.nextDouble(); sc.nextLine();
        if (newWR >= 0) p.setWinRate(newWR);
        System.out.print("New matches (-1 to skip): ");
        int newMatches = sc.nextInt(); sc.nextLine();
        if (newMatches >= 0) p.setMatchesPlayed(newMatches);
        System.out.println("Profile updated.");
    }

    // Admin Menu
    static void showAdminMenu() {
        System.out.println();
        System.out.println("=== Admin Menu ===");
        System.out.println("1. Player Lookup");
        System.out.println("2. Team Overview");
        System.out.println("3. Hero Details");
        System.out.println("4. Equipment Stats");
        System.out.println("5. Match History");
        System.out.println("6. Leaderboard");
        System.out.println("7. Data Management (CRUD)");
        System.out.println("0. Exit");
        System.out.println();
    }

    // Player Menu
    static void showPlayerMenu() {
        System.out.println();
        System.out.println("=== Player Menu ===");
        System.out.println("1. Player Lookup");
        System.out.println("2. Team Overview");
        System.out.println("3. Hero Details");
        System.out.println("4. Equipment Stats");
        System.out.println("5. Match History");
        System.out.println("6. Leaderboard");
        System.out.println("7. View My Info");
        System.out.println("8. Edit My Info");
        System.out.println("0. Exit");
        System.out.println();
    }

    // Admin Data Management Submenu
    static void dataManageMenu(Scanner sc, GameData data) {
        while (true) {
            System.out.println();
            System.out.println("=== Data Management ===");
            System.out.println("[Player] 1.Add  2.Delete  3.Modify");
            System.out.println("[Hero]   4.Add  5.Delete  6.Modify");
            System.out.println("[Equip]  7.Add  8.Delete  9.Modify");
            System.out.println("[Team]  10.Add 11.Delete 12.Modify");
            System.out.println("[Match] 13.Add 14.Delete");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            int cmd = sc.nextInt();
            sc.nextLine();

            if (cmd == 0) break;

            switch (cmd) {
                // --- Player ---
                case 1:
                    System.out.print("Username: "); String pn = sc.nextLine();
                    System.out.print("Rank: "); String pr = sc.nextLine();
                    System.out.print("WinRate(%): "); double pw = sc.nextDouble();
                    System.out.print("Matches: "); int pm = sc.nextInt(); sc.nextLine();
                    DataManager.addPlayer(data, pn, pr, pw, pm);
                    break;
                case 2:
                    System.out.print("Player to delete: ");
                    DataManager.removePlayer(data, sc.nextLine());
                    break;
                case 3:
                    System.out.print("Player to modify: ");
                    String mpn = sc.nextLine();
                    System.out.print("New rank: "); String mpr = sc.nextLine();
                    System.out.print("New WinRate(%): "); double mpw = sc.nextDouble();
                    System.out.print("New matches: "); int mpm = sc.nextInt(); sc.nextLine();
                    DataManager.modifyPlayer(data, mpn, mpr, mpw, mpm);
                    break;
                // --- Hero ---
                case 4:
                    System.out.print("Hero name: "); String hn = sc.nextLine();
                    System.out.print("Role (WARRIOR/MAGE/ASSASSIN/TANK/MARKSMAN/SUPPORT): ");
                    HeroRole hr = HeroRole.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("HP ATK DEF (space-separated): ");
                    int hhp = sc.nextInt(); int ha = sc.nextInt(); int hd = sc.nextInt(); sc.nextLine();
                    DataManager.addHero(data, hn, hr, hhp, ha, hd);
                    break;
                case 5:
                    System.out.print("Hero to delete: ");
                    DataManager.removeHero(data, sc.nextLine());
                    break;
                case 6:
                    System.out.print("Hero to modify: ");
                    String mhn = sc.nextLine();
                    System.out.print("New role (WARRIOR/MAGE/ASSASSIN/TANK/MARKSMAN/SUPPORT): ");
                    HeroRole mhr = HeroRole.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("New HP ATK DEF (space-separated): ");
                    int mhhp = sc.nextInt(); int mha = sc.nextInt(); int mhd = sc.nextInt(); sc.nextLine();
                    DataManager.modifyHero(data, mhn, mhr, mhhp, mha, mhd);
                    break;
                // --- Equipment ---
                case 7:
                    System.out.print("Equipment name: "); String en = sc.nextLine();
                    System.out.print("Type (ATTACK/DEFENSE/MAGIC/MOVEMENT/JUNGLE): ");
                    EquipmentType et = EquipmentType.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("ATK DEF HP Price (space-separated): ");
                    int ea = sc.nextInt(); int ed = sc.nextInt(); int eh = sc.nextInt(); int ep = sc.nextInt(); sc.nextLine();
                    DataManager.addEquipment(data, en, et, ea, ed, eh, ep);
                    break;
                case 8:
                    System.out.print("Equipment to delete: ");
                    DataManager.removeEquipment(data, sc.nextLine());
                    break;
                case 9:
                    System.out.print("Equipment to modify: ");
                    String men = sc.nextLine();
                    System.out.print("New type (ATTACK/DEFENSE/MAGIC/MOVEMENT/JUNGLE): ");
                    EquipmentType met = EquipmentType.valueOf(sc.nextLine().toUpperCase());
                    System.out.print("New ATK DEF HP Price (space-separated): ");
                    int mea = sc.nextInt(); int med = sc.nextInt(); int meh = sc.nextInt(); int mep = sc.nextInt(); sc.nextLine();
                    DataManager.modifyEquipment(data, men, met, mea, med, meh, mep);
                    break;
                // --- Team ---
                case 10:
                    System.out.print("Team name: ");
                    DataManager.addTeam(data, sc.nextLine());
                    break;
                case 11:
                    System.out.print("Team to delete: ");
                    DataManager.removeTeam(data, sc.nextLine());
                    break;
                case 12:
                    System.out.print("Team to modify: ");
                    String mtn = sc.nextLine();
                    System.out.print("New wins: "); int mw = sc.nextInt();
                    System.out.print("New losses: "); int ml = sc.nextInt(); sc.nextLine();
                    DataManager.modifyTeam(data, mtn, mw, ml);
                    break;
                // --- Match Record ---
                case 13:
                    System.out.println("Select both teams:");
                    List<Team> teams = data.getTeams();
                    for (int i = 0; i < teams.size(); i++) {
                        System.out.println("  " + (i+1) + ". " + teams.get(i).getTeamName());
                    }
                    System.out.print("Team A (number): ");
                    int taIdx = sc.nextInt() - 1; sc.nextLine();
                    System.out.print("Team B (number): ");
                    int tbIdx = sc.nextInt() - 1; sc.nextLine();
                    if (taIdx >= 0 && taIdx < teams.size() && tbIdx >= 0 && tbIdx < teams.size() && taIdx != tbIdx) {
                        System.out.print("Score A: "); int sa = sc.nextInt();
                        System.out.print("Score B: "); int sb = sc.nextInt(); sc.nextLine();
                        DataManager.addMatchRecord(data, teams.get(taIdx), teams.get(tbIdx), sa, sb, java.time.LocalDate.now());
                    } else {
                        System.out.println("Invalid team selection.");
                    }
                    break;
                case 14:
                    System.out.print("Match record ID to delete (e.g. M01): ");
                    DataManager.removeMatchRecord(data, sc.nextLine());
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
