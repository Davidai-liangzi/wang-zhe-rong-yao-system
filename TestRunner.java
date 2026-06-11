import model.*;
import service.*;
import java.util.List;

/**
 * Automated verification test - exercises all core modules.
 * Run: java -cp out;. TestRunner
 */
public class TestRunner {
    static int passed = 0, failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Full Feature Verification ===\n");

        // === Data Loading ===
        GameData data = DataInitializer.initAll();
        test("Data loaded (players >= 10)", data.getPlayers().size() >= 10);
        test("Heroes >= 15", data.getHeroes().size() >= 15);
        test("Equipment >= 20", data.getEquipments().size() >= 20);
        test("Teams >= 3", data.getTeams().size() >= 3);
        test("Match records >= 10", data.getMatchRecords().size() >= 10);
        test("Admins exist", data.getAdmins().size() >= 1);

        // === Model Integrity ===
        Player p = data.getPlayers().get(0);
        test("Player has ID", p.getId() != null && !p.getId().isEmpty());
        test("Player has username", p.getUsername() != null && !p.getUsername().isEmpty());
        test("Player has team", p.getTeam() != null);
        test("Player has heroes (>= 3)", p.getHeroPool().size() >= 3);
        test("Team has >= 5 members", p.getTeam().getMembers().size() >= 5);

        Hero h = p.getHeroPool().get(0);
        test("Hero has role", h.getHeroRole() != null);
        test("Hero has stats", h.getHp() > 0 && h.getAtk() > 0);
        test("Hero has compatible equipment (>= 2)", 
             h.getCompatibleEquipments() != null && h.getCompatibleEquipments().size() >= 2);

        Equipment e = data.getEquipments().get(0);
        test("Equipment has type", e.getType() != null);
        test("Equipment has name", e.getName() != null && !e.getName().isEmpty());

        // === Service: Player Lookup ===
        try { SearchService.findPlayerByName(data, "p2_jiucheng"); test("Player lookup runs", true); }
        catch (Exception ex) { test("Player lookup runs", false); }

        // === Service: Team Overview ===
        try { SearchService.findTeamByName(data, p.getTeam().getTeamName()); test("Team overview runs", true); }
        catch (Exception ex) { test("Team overview runs", false); }

        // === Service: Hero Details ===
        try { SearchService.findHeroByName(data, h.getName()); test("Hero details runs", true); }
        catch (Exception ex) { test("Hero details runs", false); }

        // === Service: Equipment Ranking ===
        try { SearchService.showEquipmentRanking(data); test("Equipment ranking runs", true); }
        catch (Exception ex) { test("Equipment ranking runs", false); }

        // === Service: Leaderboard ===
        try { SearchService.showLeaderboard(data); test("Leaderboard runs", true); }
        catch (Exception ex) { test("Leaderboard runs", false); }

        // === Service: Match History ===
        try { SearchService.showMatchHistory(data, p.getTeam().getTeamName()); test("Match history runs", true); }
        catch (Exception ex) { test("Match history runs", false); }

        // === CRUD Operations ===
        try {
            DataManager.addPlayer(data, "TestPlayerX", "Gold", 50.0, 10);
            test("Add player CRUD", data.getPlayers().stream().anyMatch(pl -> "TestPlayerX".equals(pl.getUsername())));
            DataManager.removePlayer(data, "TestPlayerX");
            test("Remove player CRUD", data.getPlayers().stream().noneMatch(pl -> "TestPlayerX".equals(pl.getUsername())));
        } catch (Exception ex) { 
            test("CRUD operations", false); 
            test("CRUD operations2", false);
        }

        try {
            DataManager.addHero(data, "TestHeroZ", HeroRole.ASSASSIN, 1000, 200, 100);
            test("Add hero CRUD", data.getHeroes().stream().anyMatch(hr -> "TestHeroZ".equals(hr.getName())));
            DataManager.removeHero(data, "TestHeroZ");
            test("Remove hero CRUD", data.getHeroes().stream().noneMatch(hr -> "TestHeroZ".equals(hr.getName())));
        } catch (Exception ex) { 
            test("Hero CRUD add", false); 
            test("Hero CRUD remove", false);
        }

        try {
            DataManager.addTeam(data, "TestTeam99");
            test("Add team CRUD", data.getTeams().stream().anyMatch(t -> "TestTeam99".equals(t.getTeamName())));
            DataManager.removeTeam(data, "TestTeam99");
            test("Remove team CRUD", data.getTeams().stream().noneMatch(t -> "TestTeam99".equals(t.getTeamName())));
        } catch (Exception ex) {
            test("Team CRUD add", false);
            test("Team CRUD remove", false);
        }

        // === Identifiable Interface ===
        test("Hero is Identifiable", h instanceof Identifiable);
        test("Player is Identifiable", p instanceof Identifiable);
        test("Equipment is Identifiable", e instanceof Identifiable);

        // === File Persistence ===
        try {
            FilePersistence.saveData(data);
            GameData loaded = FilePersistence.loadData();
            test("Persistence save", true);
            test("Persistence load", loaded != null);
            test("Persistence integrity (players)", loaded != null && loaded.getPlayers().size() == data.getPlayers().size());
            test("Persistence integrity (heroes)", loaded != null && loaded.getHeroes().size() == data.getHeroes().size());
            test("Persistence integrity (equip)", loaded != null && loaded.getEquipments().size() == data.getEquipments().size());
            test("Persistence integrity (teams)", loaded != null && loaded.getTeams().size() == data.getTeams().size());
            test("Persistence integrity (matches)", loaded != null && loaded.getMatchRecords().size() == data.getMatchRecords().size());
        } catch (Exception ex) {
            test("Persistence save", false); test("Persistence load", false);
            test("Persistence integrity (players)", false); test("Persistence integrity (heroes)", false);
            test("Persistence integrity (equip)", false); test("Persistence integrity (teams)", false);
            test("Persistence integrity (matches)", false);
        }

        // === Admin/Player Role Check ===
        boolean hasAdmin = data.getAdmins().stream().anyMatch(a -> a.getRole() == Role.ADMIN);
        test("Admin role exists", hasAdmin);
        boolean hasPlayer = data.getPlayers().stream().anyMatch(pl -> pl.getRole() == Role.PLAYER);
        test("Player role exists", hasPlayer);

        // === Extra: Combat Simulator ===
        String h1 = data.getHeroes().get(0).getName();
        String h2 = data.getHeroes().get(1).getName();
        try { CombatSimulator.simulate(data, h1, h2); test("Combat simulator runs", true); }
        catch (Exception ex) { test("Combat simulator runs", false); }

        // === Extra: Recommendation Service ===
        try { 
            RecommendationService.recommendEquipmentForHero(data, h.getName()); 
            test("Equipment recommendation runs", true); 
        }
        catch (Exception ex) { test("Equipment recommendation runs", false); }
        try { 
            RecommendationService.recommendHeroesForPlayer(data, p.getUsername()); 
            test("Hero recommendation runs", true); 
        }
        catch (Exception ex) { test("Hero recommendation runs", false); }

        // === Summary ===
        System.out.println();
        System.out.println("========== Summary ==========");
        System.out.println("Passed: " + passed + "/" + (passed + failed));
        System.out.println("Failed: " + failed);
        System.out.println("Result: " + (failed == 0 ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
        System.exit(failed == 0 ? 0 : 1);
    }

    static void test(String name, boolean condition) {
        if (condition) { passed++; System.out.println("  PASS  " + name); }
        else { failed++; System.out.println("  FAIL  " + name); }
    }
}
