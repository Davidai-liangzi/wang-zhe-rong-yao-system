import model.*;
import service.*;
import java.util.Scanner;
import java.util.List;
import java.util.InputMismatchException;
import java.time.LocalDate;
import javax.swing.SwingUtilities;

/**
 * Main entry point for Honor of Kings IMS.
 * Uses Searchable + Persistable interfaces for polymorphic design.
 * Supports console mode (1) and GUI mode (2).
 */
public class Main {
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final Searchable searchService = new SearchService();
    private static final Persistable persistence = new JsonPersistence();

    public static void main(String[] args) {
        System.out.println("=== Honor of Kings IMS ===");
        System.out.println("Select mode:");
        System.out.println("1. Console");
        System.out.println("2. GUI (Swing)");
        System.out.print("Choice: ");

        Scanner sc = new Scanner(System.in);
        int mode = readInt(sc);

        if (mode == 2) {
            SwingUtilities.invokeLater(() -> {
                GameGUI gui = new GameGUI();
                gui.setVisible(true);
            });
            sc.close();
            return;
        }

        GameData data = persistence.load();
        if (data == null) data = DataInitializer.initAll();
        System.out.println("System ready. Loaded " + data.getPlayers().size() + " players.");

        int userRole = 0;
        System.out.println("\n=== Login ===");
        System.out.println("Select role:");
        System.out.println("1. Admin");
        System.out.println("2. Player");
        System.out.print("Choice: ");
        userRole = readInt(sc);

        if (userRole == 1) {
            if (authenticateAdmin(data, sc) == null) { sc.close(); return; }
            showAdminMenu();
        } else if (userRole == 2) {
            Player currentPlayer = authenticatePlayer(data, sc);
            if (currentPlayer == null) { sc.close(); return; }
            System.out.println("Welcome, " + currentPlayer.getUsername() + "!");
            showPlayerMenu();
            runPlayerMenuLoop(sc, data, currentPlayer);
            return;
        } else {
            System.out.println("Invalid choice. Exiting.");
            sc.close();
            return;
        }

        // Admin menu loop
        while (true) {
            System.out.print("Choice: ");
            int choice = readInt(sc);
            if (choice == 0) { System.out.println("Goodbye!"); persistence.save(data); break; }
            handleAdminChoice(sc, data, choice);
        }
        sc.close();
    }

    private static void runPlayerMenuLoop(Scanner sc, GameData data, Player currentPlayer) {
        while (true) {
            System.out.print("Choice: ");
            int choice = readInt(sc);
            if (choice == 0) { System.out.println("Goodbye!"); persistence.save(data); break; }
            handlePlayerChoice(sc, data, currentPlayer, choice);
        }
    }

    private static void handleAdminChoice(Scanner sc, GameData data, int choice) {
        switch (choice) {
            case 1: System.out.print(searchService.findPlayerByName(data, readLine(sc, "Enter player username: "))); break;
            case 2: System.out.print(searchService.findTeamByName(data, readLine(sc, "Enter team name: "))); break;
            case 3: System.out.print(searchService.findHeroByName(data, readLine(sc, "Enter hero name: "))); break;
            case 4: System.out.print(searchService.showEquipmentRanking(data)); break;
            case 5: System.out.print(searchService.showMatchHistory(data, readLine(sc, "Enter player or team name: "))); break;
            case 6: System.out.print(searchService.showLeaderboard(data)); break;
            case 7: dataManageMenu(sc, data); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void handlePlayerChoice(Scanner sc, GameData data, Player currentPlayer, int choice) {
        switch (choice) {
            case 1: System.out.print(searchService.findPlayerByName(data, readLine(sc, "Enter player username: "))); break;
            case 2: System.out.print(searchService.findTeamByName(data, readLine(sc, "Enter team name: "))); break;
            case 3: System.out.print(searchService.findHeroByName(data, readLine(sc, "Enter hero name: "))); break;
            case 4: System.out.print(searchService.showEquipmentRanking(data)); break;
            case 5: System.out.print(searchService.showMatchHistory(data, readLine(sc, "Enter player or team name: "))); break;
            case 6: System.out.print(searchService.showLeaderboard(data)); break;
            case 7: System.out.print(searchService.findPlayerByName(data, currentPlayer.getUsername())); break;
            case 8: editOwnInfo(sc, currentPlayer); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    // ======================
    // Authentication
    // ======================

    static Admin authenticateAdmin(GameData data, Scanner sc) {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            System.out.print("Admin username: ");
            String username = sc.nextLine().trim();
            System.out.print("Password: ");
            String password = sc.nextLine();
            for (Admin a : data.getAdmins()) {
                if (a.getUsername().equals(username) && a.getPassword().equals(password)) {
                    System.out.println("Welcome, Admin " + a.getUsername() + "!");
                    return a;
                }
            }
            if (attempt < MAX_LOGIN_ATTEMPTS)
                System.out.println("Invalid credentials. " + (MAX_LOGIN_ATTEMPTS - attempt) + " attempt(s) remaining.");
        }
        System.out.println("Authentication failed. Exiting.");
        return null;
    }

    static Player authenticatePlayer(GameData data, Scanner sc) {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            System.out.print("Password: ");
            String password = sc.nextLine();
            for (Player p : data.getPlayers()) {
                if (p.getUsername().equalsIgnoreCase(username) && p.getPassword().equals(password)) return p;
            }
            if (attempt < MAX_LOGIN_ATTEMPTS)
                System.out.println("Invalid credentials. " + (MAX_LOGIN_ATTEMPTS - attempt) + " attempt(s) remaining.");
        }
        System.out.println("Authentication failed. Exiting.");
        return null;
    }

    // ======================
    // Input helpers
    // ======================

    static int readInt(Scanner sc) {
        while (true) {
            try { int value = sc.nextInt(); sc.nextLine(); return value; }
            catch (InputMismatchException e) { System.out.print("Invalid input. Please enter a number: "); sc.nextLine(); }
        }
    }

    static double readDouble(Scanner sc) {
        while (true) {
            try { double value = sc.nextDouble(); sc.nextLine(); return value; }
            catch (InputMismatchException e) { System.out.print("Invalid input. Please enter a number: "); sc.nextLine(); }
        }
    }

    // ======================
    // Profile editing
    // ======================

    static void editOwnInfo(Scanner sc, Player p) {
        System.out.println("\n=== Edit Profile ===");
        System.out.println("Current: rank=" + p.getRank() + ", winRate=" + p.getWinRate() + "%, matches=" + p.getMatchesPlayed());
        System.out.print("New rank (Enter to skip): ");
        String newRank = sc.nextLine();
        if (!newRank.isEmpty()) p.setRank(newRank);
        System.out.print("New winRate (-1 to skip): ");
        try { double newWR = sc.nextDouble(); sc.nextLine(); if (newWR >= 0) p.setWinRate(newWR); }
        catch (InputMismatchException e) { sc.nextLine(); }
        System.out.print("New matches (-1 to skip): ");
        try { int newMatches = sc.nextInt(); sc.nextLine(); if (newMatches >= 0) p.setMatchesPlayed(newMatches); }
        catch (InputMismatchException e) { sc.nextLine(); }
        System.out.println("Profile updated.");
    }

    // ======================
    // Menus
    // ======================

    static void showAdminMenu() {
        System.out.println("\n=== Admin Menu ===");
        for (String s : new String[]{"1. Player Lookup","2. Team Overview","3. Hero Details",
                "4. Equipment Stats","5. Match History","6. Leaderboard","7. Data Management (CRUD)","0. Exit"})
            System.out.println(s);
    }

    static void showPlayerMenu() {
        System.out.println("\n=== Player Menu ===");
        for (String s : new String[]{"1. Player Lookup","2. Team Overview","3. Hero Details",
                "4. Equipment Stats","5. Match History","6. Leaderboard","7. View My Info","8. Edit My Info","0. Exit"})
            System.out.println(s);
    }

    // ======================
    // Data Management (refactored into sub-methods)
    // ======================

    static void dataManageMenu(Scanner sc, GameData data) {
        while (true) {
            System.out.println("\n=== Data Management ===");
            System.out.println("[Player] 1.Add  2.Delete  3.Modify");
            System.out.println("[Hero]   4.Add  5.Delete  6.Modify");
            System.out.println("[Equip]  7.Add  8.Delete  9.Modify");
            System.out.println("[Team]  10.Add 11.Delete 12.Modify");
            System.out.println("[Match] 13.Add 14.Delete");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            int cmd = readInt(sc);
            if (cmd == 0) break;
            switch (cmd) {
                case 1: case 2: case 3: handlePlayerCrud(sc, data, cmd); break;
                case 4: case 5: case 6: handleHeroCrud(sc, data, cmd); break;
                case 7: case 8: case 9: handleEquipmentCrud(sc, data, cmd); break;
                case 10: case 11: case 12: handleTeamCrud(sc, data, cmd); break;
                case 13: case 14: handleMatchCrud(sc, data, cmd); break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void handlePlayerCrud(Scanner sc, GameData data, int cmd) {
        switch (cmd) {
            case 1:
                System.out.println(DataManager.addPlayer(data,
                        readLine(sc, "Username: "), readLine(sc, "Rank: "),
                        readDoublePrompt(sc, "WinRate(%): "), readIntPrompt(sc, "Matches: ")));
                break;
            case 2:
                System.out.println(DataManager.removePlayer(data, readLine(sc, "Player to delete: ")));
                break;
            case 3:
                System.out.println(DataManager.modifyPlayer(data,
                        readLine(sc, "Player to modify: "), readLine(sc, "New rank: "),
                        readDoublePrompt(sc, "New WinRate(%): "), readIntPrompt(sc, "New matches: ")));
                break;
        }
    }

    private static void handleHeroCrud(Scanner sc, GameData data, int cmd) {
        switch (cmd) {
            case 4:
                String hn = readLine(sc, "Hero name: ");
                System.out.print("Role (WARRIOR/MAGE/ASSASSIN/TANK/MARKSMAN/SUPPORT): ");
                HeroRole hr = HeroRole.valueOf(sc.nextLine().toUpperCase());
                System.out.println(DataManager.addHero(data, hn, hr,
                        readIntPrompt(sc, "HP: "), readIntPrompt(sc, "ATK: "), readIntPrompt(sc, "DEF: ")));
                break;
            case 5:
                System.out.println(DataManager.removeHero(data, readLine(sc, "Hero to delete: ")));
                break;
            case 6:
                String mhn = readLine(sc, "Hero to modify: ");
                System.out.print("New role (WARRIOR/MAGE/ASSASSIN/TANK/MARKSMAN/SUPPORT): ");
                HeroRole mhr = HeroRole.valueOf(sc.nextLine().toUpperCase());
                System.out.println(DataManager.modifyHero(data, mhn, mhr,
                        readIntPrompt(sc, "New HP: "), readIntPrompt(sc, "New ATK: "), readIntPrompt(sc, "New DEF: ")));
                break;
        }
    }

    private static void handleEquipmentCrud(Scanner sc, GameData data, int cmd) {
        switch (cmd) {
            case 7:
                String en = readLine(sc, "Equipment name: ");
                System.out.print("Type (ATTACK/DEFENSE/MAGIC/MOVEMENT/JUNGLE): ");
                EquipmentType et = EquipmentType.valueOf(sc.nextLine().toUpperCase());
                System.out.println(DataManager.addEquipment(data, en, et,
                        readIntPrompt(sc, "ATK: "), readIntPrompt(sc, "DEF: "),
                        readIntPrompt(sc, "HP: "), readIntPrompt(sc, "Price: ")));
                break;
            case 8:
                System.out.println(DataManager.removeEquipment(data, readLine(sc, "Equipment to delete: ")));
                break;
            case 9:
                String men = readLine(sc, "Equipment to modify: ");
                System.out.print("New type (ATTACK/DEFENSE/MAGIC/MOVEMENT/JUNGLE): ");
                EquipmentType met = EquipmentType.valueOf(sc.nextLine().toUpperCase());
                System.out.println(DataManager.modifyEquipment(data, men, met,
                        readIntPrompt(sc, "New ATK: "), readIntPrompt(sc, "New DEF: "),
                        readIntPrompt(sc, "New HP: "), readIntPrompt(sc, "New Price: ")));
                break;
        }
    }

    private static void handleTeamCrud(Scanner sc, GameData data, int cmd) {
        switch (cmd) {
            case 10:
                System.out.println(DataManager.addTeam(data, readLine(sc, "Team name: ")));
                break;
            case 11:
                System.out.println(DataManager.removeTeam(data, readLine(sc, "Team to delete: ")));
                break;
            case 12:
                System.out.println(DataManager.modifyTeam(data,
                        readLine(sc, "Team to modify: "),
                        readIntPrompt(sc, "New wins: "), readIntPrompt(sc, "New losses: ")));
                break;
        }
    }

    private static void handleMatchCrud(Scanner sc, GameData data, int cmd) {
        switch (cmd) {
            case 13:
                List<Team> teams = data.getTeams();
                System.out.println("Select both teams:");
                for (int i = 0; i < teams.size(); i++)
                    System.out.println("  " + (i+1) + ". " + teams.get(i).getTeamName());
                int taIdx = readIntPrompt(sc, "Team A (number): ") - 1;
                int tbIdx = readIntPrompt(sc, "Team B (number): ") - 1;
                if (taIdx >= 0 && taIdx < teams.size() && tbIdx >= 0 && tbIdx < teams.size() && taIdx != tbIdx) {
                    System.out.println(DataManager.addMatchRecord(data,
                            teams.get(taIdx), teams.get(tbIdx),
                            readIntPrompt(sc, "Score A: "), readIntPrompt(sc, "Score B: "), LocalDate.now()));
                } else System.out.println("Invalid team selection.");
                break;
            case 14:
                System.out.println(DataManager.removeMatchRecord(data, readLine(sc, "Match record ID to delete (e.g. M01): ")));
                break;
        }
    }

    private static int readIntPrompt(Scanner sc, String prompt) {
        System.out.print(prompt);
        return readInt(sc);
    }

    private static double readDoublePrompt(Scanner sc, String prompt) {
        System.out.print(prompt);
        return readDouble(sc);
    }
}
