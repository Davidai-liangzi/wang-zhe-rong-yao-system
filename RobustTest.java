import model.*;
import service.*;
import java.util.List;

/**
 * Robustness test �?exercises edge cases, bad data, boundary conditions.
 * Run: java -cp out;. RobustTest
 */
public class RobustTest {
    static int passed = 0, failed = 0;
    static final Searchable ss = new SearchService();

    public static void main(String[] args) {
        System.out.println("=== Robustness & Edge Case Testing ===\n");

        // ==== 1. Null data ====
        testCrash("null GameData �?findPlayerByName", () -> ss.findPlayerByName(null, "test"));
        testCrash("null GameData �?findTeamByName", () -> ss.findTeamByName(null, "test"));
        testCrash("null GameData �?findHeroByName", () -> ss.findHeroByName(null, "test"));
        testCrash("null GameData �?showEquipmentRanking", () -> ss.showEquipmentRanking(null));
        testCrash("null GameData �?showLeaderboard", () -> ss.showLeaderboard(null));
        testCrash("null GameData �?showMatchHistory", () -> ss.showMatchHistory(null, "test"));

        // ==== 2. Empty GameData (no data at all) ====
        GameData empty = new GameData();
        testCrash("empty GameData �?findPlayerByName", () -> ss.findPlayerByName(empty, "test"));
        testCrash("empty GameData �?findTeamByName", () -> ss.findTeamByName(empty, "test"));
        testCrash("empty GameData �?findHeroByName", () -> ss.findHeroByName(empty, "test"));
        testCrash("empty GameData �?showEquipmentRanking", () -> ss.showEquipmentRanking(empty));
        testCrash("empty GameData �?showLeaderboard", () -> ss.showLeaderboard(empty));
        testCrash("empty GameData �?showMatchHistory", () -> ss.showMatchHistory(empty, "test"));
        testCrash("empty GameData �?recommendEquipmentForHero", () -> RecommendationService.recommendEquipmentForHero(empty, "test"));
        testCrash("empty GameData �?recommendHeroesForPlayer", () -> RecommendationService.recommendHeroesForPlayer(empty, "test"));

        // ==== 3. Normal data �?search with null/empty strings ====
        GameData data = DataInitializer.initAll();

        testCrash("null search �?findPlayerByName(null)", () -> ss.findPlayerByName(data, null));
        testCrash("empty search �?findPlayerByName('')", () -> ss.findPlayerByName(data, ""));
        testCrash("null search �?findTeamByName(null)", () -> ss.findTeamByName(data, null));
        testCrash("empty search �?findTeamByName('')", () -> ss.findTeamByName(data, ""));
        testCrash("null search �?findHeroByName(null)", () -> ss.findHeroByName(data, null));
        testCrash("empty search �?findHeroByName('')", () -> ss.findHeroByName(data, ""));
        testCrash("null search �?showMatchHistory(null)", () -> ss.showMatchHistory(data, null));
        testCrash("empty search �?showMatchHistory('')", () -> ss.showMatchHistory(data, ""));

        // ==== 4. Non-existent names ====
        testCrash("non-existent player", () -> ss.findPlayerByName(data, "zzz_nobody_xyz"));
        testCrash("non-existent team", () -> ss.findTeamByName(data, "NoSuchTeam"));
        testCrash("non-existent hero", () -> ss.findHeroByName(data, "FakeHero123"));
        testCrash("non-existent in match history", () -> ss.showMatchHistory(data, "ghost_player"));

        // ==== 5. Combat with non-existent heroes ====
        testCrash("combat non-existent hero1", () -> CombatSimulator.simulate(data, "NoHero", "Lu Bu"));
        testCrash("combat non-existent hero2", () -> CombatSimulator.simulate(data, "Lu Bu", "NoHero"));
        testCrash("combat both non-existent", () -> CombatSimulator.simulate(data, "A", "B"));

        // ==== 6. Recommendation with non-existent ====
        testCrash("recommend equipment for unknown hero", () -> RecommendationService.recommendEquipmentForHero(data, "Nobody"));
        testCrash("recommend heroes for unknown player", () -> RecommendationService.recommendHeroesForPlayer(data, "unknown_user"));

        // ==== 7. CRUD with edge values ====
        testCrash("add player �?empty name", () -> DataManager.addPlayer(data, "", "King", 50, 10));
        testCrash("add player �?null rank", () -> DataManager.addPlayer(data, "nullRank", null, 50, 10));
        testCrash("add player �?negative winRate", () -> DataManager.addPlayer(data, "negWR", "Gold", -10.5, 5));
        testCrash("add player �?negative matches", () -> DataManager.addPlayer(data, "negMts", "Gold", 50, -100));
        testCrash("add player �?winRate > 100", () -> DataManager.addPlayer(data, "over100", "King", 150.0, 10));
        testCrash("remove non-existent player", () -> DataManager.removePlayer(data, "never_added_xyz"));
        testCrash("modify non-existent player", () -> DataManager.modifyPlayer(data, "ghost", "King", 50, 0));

        testCrash("add hero �?empty name", () -> DataManager.addHero(data, "", HeroRole.WARRIOR, 1, 1, 1));
        testCrash("add hero �?negative HP", () -> DataManager.addHero(data, "negHP", HeroRole.TANK, -100, 50, 50));
        testCrash("add hero �?zero stats", () -> DataManager.addHero(data, "zeroStat", HeroRole.MAGE, 0, 0, 0));
        testCrash("remove non-existent hero", () -> DataManager.removeHero(data, "NoSuchHero"));
        testCrash("modify non-existent hero", () -> DataManager.modifyHero(data, "ghost", HeroRole.TANK, 100, 100, 100));

        testCrash("add equipment �?empty name", () -> DataManager.addEquipment(data, "", EquipmentType.ATTACK, 1, 1, 1, 100));
        testCrash("add equipment �?negative price", () -> DataManager.addEquipment(data, "negPrice", EquipmentType.ATTACK, 10, 10, 10, -500));
        testCrash("add equipment �?negative stats", () -> DataManager.addEquipment(data, "negStats", EquipmentType.DEFENSE, -50, -50, -50, 100));
        testCrash("remove non-existent equipment", () -> DataManager.removeEquipment(data, "FakeEq"));

        testCrash("add team �?null name", () -> DataManager.addTeam(data, null));
        testCrash("remove non-existent team", () -> DataManager.removeTeam(data, "NoTeam"));
        testCrash("modify non-existent team", () -> DataManager.modifyTeam(data, "ghost", 0, 0));

        // ==== 8. Team with no matches (win rate = matches*0 = 0) ====
        Team zeroTeam = new Team("T99", "ZeroTeam", 0, 0);
        data.addTeam(zeroTeam);
        testCrash("team zero matches — team overview", () -> ss.findTeamByName(data, "ZeroTeam"));
        data.removeTeam(zeroTeam);

        // ==== 9. Add match record with null teams ====
        testCrash("match record �?null teamA", () -> {
            try { DataManager.addMatchRecord(data, null, data.getTeams().get(0), 1, 1, java.time.LocalDate.now()); }
            catch (Exception e) { /* expected to crash? */ throw e; }
        });

        // ==== 10. FilePersistence edge cases ====
        testCrash("save null data", () -> FilePersistence.saveData(null));

        // ==== 11. Duplicate IDs ====
        testCrash("duplicate ID add player", () -> {
            DataManager.addPlayer(data, "dup_test", "Gold", 50, 10);
            DataManager.addPlayer(data, "dup_test2", "Gold", 50, 10);
        });

        // ==== 12. Player with no team ====
        Player orphan = new Player("P99", "orphan_player", "pw", Role.PLAYER, "Gold", 50, 5, null);
        data.addPlayer(orphan);
        testCrash("player with null team", () -> ss.findPlayerByName(data, "orphan_player"));
        testCrash("match history for orphan player", () -> ss.showMatchHistory(data, "orphan_player"));
        data.removePlayer(orphan);

        // ==== 13. Hero with empty hero pool ====
        Player emptyHero = new Player("P88", "empty_hero", "pw", Role.PLAYER, "Gold", 50, 5, data.getTeams().get(0));
        data.addPlayer(emptyHero);
        testCrash("player with empty hero pool", () -> ss.findPlayerByName(data, "empty_hero"));
        data.removePlayer(emptyHero);

        // ==== 14. Empty compatible equipments ====
        Hero noEq = new Hero("H99", "NoEquipmentHero", HeroRole.WARRIOR, 100, 100, 100);
        data.addHero(noEq);
        testCrash("hero with no equipment", () -> ss.findHeroByName(data, "NoEquipmentHero"));
        data.removeHero(noEq);

        // ==== 15. Very long names ====
        String longName = "A".repeat(200);
        testCrash("very long player name search", () -> ss.findPlayerByName(data, longName));
        testCrash("very long team name search", () -> ss.findTeamByName(data, longName));
        testCrash("very long hero name search", () -> ss.findHeroByName(data, longName));

        // ==== Summary ====
        System.out.println();
        System.out.println("========== Robustness Summary ==========");
        System.out.println("Passed (no crash): " + passed + "/" + (passed + failed));
        System.out.println("Failed (crashed): " + failed);
        if (failed == 0) System.out.println("Result: ALL EDGE CASES HANDLED");
        else System.out.println("Result: " + failed + " CRASHES FOUND");
        System.exit(failed == 0 ? 0 : 1);
    }

    static void testCrash(String name, Runnable action) {
        try {
            action.run();
            passed++;
            System.out.println("  PASS  " + name);
        } catch (NullPointerException e) {
            failed++;
            System.out.println("  FAIL  " + name + " [NullPointerException] " + e.getMessage());
        } catch (Exception e) {
            failed++;
            System.out.println("  FAIL  " + name + " [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }
}
