import model.*;
import service.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Automated verification test - exercises all core modules
 * with output-correctness assertions.
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

        // === Smoke Tests ===
        try { SearchService.findPlayerByName(data, "p2_jiucheng"); test("Player lookup runs", true); }
        catch (Exception ex) { test("Player lookup runs", false); }

        try { SearchService.findTeamByName(data, p.getTeam().getTeamName()); test("Team overview runs", true); }
        catch (Exception ex) { test("Team overview runs", false); }

        try { SearchService.findHeroByName(data, h.getName()); test("Hero details runs", true); }
        catch (Exception ex) { test("Hero details runs", false); }

        try { SearchService.showEquipmentRanking(data); test("Equipment ranking runs", true); }
        catch (Exception ex) { test("Equipment ranking runs", false); }

        try { SearchService.showLeaderboard(data); test("Leaderboard runs", true); }
        catch (Exception ex) { test("Leaderboard runs", false); }

        try { SearchService.showMatchHistory(data, p.getTeam().getTeamName()); test("Match history runs", true); }
        catch (Exception ex) { test("Match history runs", false); }

        // =============================================
        // Output-Correctness Verification Tests
        // =============================================
        System.out.println("\n=== Output Correctness Tests ===\n");

        runEquipScoreTests();
        runPlayerScoreTests();
        runRankMappingTests();
        runIsSuitableTests();
        runCombatDamageTests();
        runEquipScoreForHeroTests();
        runRecommendSortTests();

        // === CRUD Operations ===
        System.out.println();
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

        // === Extra: Combat Simulator (smoke) ===
        String h1 = data.getHeroes().get(0).getName();
        String h2 = data.getHeroes().get(1).getName();
        try { CombatSimulator.simulate(data, h1, h2); test("Combat simulator runs", true); }
        catch (Exception ex) { test("Combat simulator runs", false); }

        // === Extra: Recommendation Service (smoke) ===
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

    // ================================================================
    // equipScore: ATK*1.0 + DEF*0.8 + HP*0.6 - Price*0.001
    // ================================================================
    static void runEquipScoreTests() {
        System.out.println("--- equipScore ---");

        // ATK-only: 100*1.0 + 0 + 0 - 500*0.001 = 99.5
        Equipment atkItem = new Equipment("t1", "TestSword", EquipmentType.ATTACK, 100, 0, 0, 500);
        testEq("ATK-only equipment score = 99.5", SearchService.equipScore(atkItem), 99.5);

        // DEF-only: 0 + 100*0.8 + 0 - 300*0.001 = 79.7
        Equipment defItem = new Equipment("t2", "TestArmor", EquipmentType.DEFENSE, 0, 100, 0, 300);
        testEq("DEF-only equipment score = 79.7", SearchService.equipScore(defItem), 79.7);

        // HP-only: 0 + 0 + 200*0.6 - 100*0.001 = 119.9
        Equipment hpItem = new Equipment("t3", "TestBelt", EquipmentType.DEFENSE, 0, 0, 200, 100);
        testEq("HP-only equipment score = 119.9", SearchService.equipScore(hpItem), 119.9);

        // Expensive: ATK=50 Price=10000 -> 50 - 10 = 40.0
        Equipment expensive = new Equipment("t4", "Expensive", EquipmentType.ATTACK, 50, 0, 0, 10000);
        testEq("Expensive item score = 40.0", SearchService.equipScore(expensive), 40.0);

        // Ranking: hpItem(119.9) > atkItem(99.5) > defItem(79.7) > expensive(40.0)
        test("equipScore ranking: HP > ATK > DEF > Expensive",
                SearchService.equipScore(hpItem) > SearchService.equipScore(atkItem)
                && SearchService.equipScore(atkItem) > SearchService.equipScore(defItem)
                && SearchService.equipScore(defItem) > SearchService.equipScore(expensive));
    }

    // ================================================================
    // playerScore: WR*1.0 + rankToScore*5.0 + matches*0.01
    // ================================================================
    static void runPlayerScoreTests() {
        System.out.println("--- playerScore ---");

        // King(5), WR=60.0, matches=100 -> 60 + 25 + 1 = 86.0
        Player pKing = new Player("pk", "KingPlayer", "pw", Role.PLAYER, "King", 60.0, 100, null);
        testEq("playerScore King WR60 M100 = 86.0", SearchService.playerScore(pKing), 86.0);

        // Gold(1), WR=80.0, matches=10 -> 80 + 5 + 0.1 = 85.1
        Player pGold = new Player("pg", "GoldPlayer", "pw", Role.PLAYER, "Gold", 80.0, 10, null);
        testEq("playerScore Gold WR80 M10 = 85.1", SearchService.playerScore(pGold), 85.1);

        // King (86.0) > Gold (85.1) despite lower WR
        test("King edges Gold despite lower WR",
                SearchService.playerScore(pKing) > SearchService.playerScore(pGold));

        // Star(4), WR=50.0, matches=50 -> 50 + 20 + 0.5 = 70.5
        Player pStar = new Player("ps", "StarPlayer", "pw", Role.PLAYER, "Star", 50.0, 50, null);
        testEq("playerScore Star WR50 M50 = 70.5", SearchService.playerScore(pStar), 70.5);

        // Rank contributes 20pts (King 25 - Gold 5)
        double kingRankContrib = SearchService.rankToScore("King") * 5.0;
        double goldRankContrib = SearchService.rankToScore("Gold") * 5.0;
        test("Rank contributes 20pts King vs Gold", Math.abs(kingRankContrib - goldRankContrib - 20.0) < 0.01);
    }

    // ================================================================
    // rankToScore + scoreToRankName bidirectional mapping
    // ================================================================
    static void runRankMappingTests() {
        System.out.println("--- rankToScore / scoreToRankName ---");

        testEqI("rankToScore King=5", SearchService.rankToScore("King"), 5);
        testEqI("rankToScore Star=4", SearchService.rankToScore("Star"), 4);
        testEqI("rankToScore Diamond=3", SearchService.rankToScore("Diamond"), 3);
        testEqI("rankToScore Platinum=2", SearchService.rankToScore("Platinum"), 2);
        testEqI("rankToScore Gold=1", SearchService.rankToScore("Gold"), 1);
        testEqI("rankToScore null=1", SearchService.rankToScore(null), 1);
        testEqI("rankToScore unknown=1", SearchService.rankToScore("Bronze"), 1);

        test("scoreToRankName 5.0=King", "King".equals(SearchService.scoreToRankName(5.0)));
        test("scoreToRankName 4.5=King", "King".equals(SearchService.scoreToRankName(4.5)));
        test("scoreToRankName 4.0=Star", "Star".equals(SearchService.scoreToRankName(4.0)));
        test("scoreToRankName 3.5=Star", "Star".equals(SearchService.scoreToRankName(3.5)));
        test("scoreToRankName 3.0=Diamond", "Diamond".equals(SearchService.scoreToRankName(3.0)));
        test("scoreToRankName 2.5=Diamond", "Diamond".equals(SearchService.scoreToRankName(2.5)));
        test("scoreToRankName 2.0=Platinum", "Platinum".equals(SearchService.scoreToRankName(2.0)));
        test("scoreToRankName 1.5=Platinum", "Platinum".equals(SearchService.scoreToRankName(1.5)));
        test("scoreToRankName 1.0=Gold", "Gold".equals(SearchService.scoreToRankName(1.0)));
        test("scoreToRankName 0.0=Gold", "Gold".equals(SearchService.scoreToRankName(0.0)));
    }

    // ================================================================
    // isSuitable: role-to-equipment type matching
    // ================================================================
    static void runIsSuitableTests() {
        System.out.println("--- isSuitable ---");

        // WARRIOR
        test("WARRIOR suits ATTACK", SearchService.isSuitable(EquipmentType.ATTACK, HeroRole.WARRIOR));
        test("WARRIOR suits JUNGLE", SearchService.isSuitable(EquipmentType.JUNGLE, HeroRole.WARRIOR));
        test("WARRIOR suits MOVEMENT", SearchService.isSuitable(EquipmentType.MOVEMENT, HeroRole.WARRIOR));
        test("WARRIOR rejects DEFENSE", !SearchService.isSuitable(EquipmentType.DEFENSE, HeroRole.WARRIOR));
        test("WARRIOR rejects MAGIC", !SearchService.isSuitable(EquipmentType.MAGIC, HeroRole.WARRIOR));

        // TANK
        test("TANK suits DEFENSE", SearchService.isSuitable(EquipmentType.DEFENSE, HeroRole.TANK));
        test("TANK suits MOVEMENT", SearchService.isSuitable(EquipmentType.MOVEMENT, HeroRole.TANK));
        test("TANK rejects ATTACK", !SearchService.isSuitable(EquipmentType.ATTACK, HeroRole.TANK));
        test("TANK rejects JUNGLE", !SearchService.isSuitable(EquipmentType.JUNGLE, HeroRole.TANK));
        test("TANK rejects MAGIC", !SearchService.isSuitable(EquipmentType.MAGIC, HeroRole.TANK));

        // MAGE
        test("MAGE suits MAGIC", SearchService.isSuitable(EquipmentType.MAGIC, HeroRole.MAGE));
        test("MAGE suits MOVEMENT", SearchService.isSuitable(EquipmentType.MOVEMENT, HeroRole.MAGE));
        test("MAGE rejects ATTACK", !SearchService.isSuitable(EquipmentType.ATTACK, HeroRole.MAGE));
        test("MAGE rejects DEFENSE", !SearchService.isSuitable(EquipmentType.DEFENSE, HeroRole.MAGE));

        // SUPPORT
        test("SUPPORT suits DEFENSE", SearchService.isSuitable(EquipmentType.DEFENSE, HeroRole.SUPPORT));
        test("SUPPORT suits MAGIC", SearchService.isSuitable(EquipmentType.MAGIC, HeroRole.SUPPORT));
        test("SUPPORT rejects ATTACK", !SearchService.isSuitable(EquipmentType.ATTACK, HeroRole.SUPPORT));

        // MARKSMAN
        test("MARKSMAN suits ATTACK", SearchService.isSuitable(EquipmentType.ATTACK, HeroRole.MARKSMAN));
        test("MARKSMAN rejects DEFENSE", !SearchService.isSuitable(EquipmentType.DEFENSE, HeroRole.MARKSMAN));
    }

    // ================================================================
    // CombatSimulator.calculateDamage - deterministic with seeded Random
    // Formula: max(1, atk - def*0.6) +/- 5 random; crit 15%(+5% atk eq)*1.5; dodge 10%(+5% def eq)
    // ================================================================
    static void runCombatDamageTests() {
        System.out.println("--- CombatSimulator.calculateDamage ---");

        // Seed 42: n11=7 => +2; crit=63 (>=15 no); dodge=48 (>=10 no)
        // ATK=100 DEF=50 => base=max(1,100-30)=70; dmg=70+2=72
        Random rng42 = new Random(42);
        int d1 = CombatSimulator.calculateDamage(100, 50, false, false, rng42);
        test("Damage ATK100 DEF50 seed42 = 72", d1 == 72);

        // Seed 99: n11=3 => -2; crit=58 (>=15 no); dodge=29 (>=10 no)
        // ATK=80 DEF=100 => base=max(1,80-60)=20; dmg=20-2=18
        Random rng99 = new Random(99);
        int d2 = CombatSimulator.calculateDamage(80, 100, false, false, rng99);
        test("Damage ATK80 DEF100 seed99 = 18", d2 == 18);

        // Seed 1: n11=4 => -1; crit=88 (>=15 no); dodge=47 (>=10 no)
        // ATK=200 DEF=20 => base=max(1,200-12)=188; dmg=188-1=187
        Random rng1 = new Random(1);
        int d3 = CombatSimulator.calculateDamage(200, 20, false, false, rng1);
        test("Damage ATK200 DEF20 seed1 = 187", d3 == 187);

        // Seed 123: n11=10 => +5; crit=50 (>=20 no for atkEq); dodge=76 (>=15 no for defEq)
        // ATK=100 DEF=50 => base=70; dmg=70+5=75
        Random rng123 = new Random(123);
        int d5 = CombatSimulator.calculateDamage(100, 50, true, true, rng123);
        test("Damage ATK100 DEF50 withEq seed123 = 75", d5 == 75);

        // Seed 42 with atkEq => n11=7 (+2); crit=63 (>=20 no crit)
        Random rng42b = new Random(42);
        int d6 = CombatSimulator.calculateDamage(100, 50, true, false, rng42b);
        test("Damage ATK100 DEF50 atkEq seed42 = 72 (no crit)", d6 == 72);

        // Seed 522: n11=2 => -3; crit=1 (<15 **CRIT x1.5**); dodge=48 (>=10 no)
        // ATK=100 DEF=50 => base=70; dmg=(70-3)*1.5=100 (int cast)
        Random rng522 = new Random(522);
        int dCrit = CombatSimulator.calculateDamage(100, 50, false, false, rng522);
        test("Damage ATK100 DEF50 seed522 CRIT = 100", dCrit == 100);

        // Defense reduces damage
        Random rngA = new Random(0);
        int lowDef = CombatSimulator.calculateDamage(100, 20, false, false, rngA);
        Random rngB = new Random(0);
        int highDef = CombatSimulator.calculateDamage(100, 80, false, false, rngB);
        test("Higher defense means less damage", lowDef > highDef);

        // Floor: very high DEF vs low ATK still > 0
        Random rngFloor = new Random(0);
        int dFloor = CombatSimulator.calculateDamage(30, 200, false, false, rngFloor);
        test("Damage floor works (ATK30 DEF200 result > 0)", dFloor > 0);

        // Edge case: base DMG=0 before floor (ATK=1 DEF=2 -> 1-floor(1.2)=0)
        // Seed 123: n11=10 => +5. With base floor: 1+5=6. Without: 0+5=5
        // M14 removes Math.max(1,base) but NOT the final floor -> this test kills it
        Random rngETest = new Random(123);
        int dEdge = CombatSimulator.calculateDamage(1, 2, false, false, rngETest);
        test("Base floor edge case: ATK1 DEF2 seed123 = 6", dEdge == 6);
    }

    // ================================================================
    // equipScoreForHero - role-specific scoring weights
    // ================================================================
    static void runEquipScoreForHeroTests() {
        System.out.println("--- equipScoreForHero ---");

        Hero warrior = new Hero("t-h1", "TestWarrior", HeroRole.WARRIOR, 1000, 100, 80);
        Hero tank = new Hero("t-h2", "TestTank", HeroRole.TANK, 1200, 60, 120);
        Hero mage = new Hero("t-h3", "TestMage", HeroRole.MAGE, 900, 110, 50);
        Hero support = new Hero("t-h4", "TestSupport", HeroRole.SUPPORT, 1100, 40, 100);

        Equipment atkEq = new Equipment("te1", "ATK", EquipmentType.ATTACK, 50, 0, 0, 0);
        Equipment defEq = new Equipment("te2", "DEF", EquipmentType.DEFENSE, 0, 50, 0, 0);

        // WARRIOR: atkW=1.5 => atkEq=50*1.5=75.0; defEq=50*0.5=25.0
        testEq("WARRIOR ATK item scores 75.0",
                RecommendationService.equipScoreForHero(atkEq, warrior), 75.0);
        testEq("WARRIOR DEF item scores 25.0",
                RecommendationService.equipScoreForHero(defEq, warrior), 25.0);
        test("WARRIOR ranks ATK > DEF",
                RecommendationService.equipScoreForHero(atkEq, warrior) > RecommendationService.equipScoreForHero(defEq, warrior));

        // TANK: defW=1.5 => defEq=50*1.5=75.0; atkW=0.3 => atkEq=50*0.3=15.0
        testEq("TANK DEF item scores 75.0",
                RecommendationService.equipScoreForHero(defEq, tank), 75.0);
        testEq("TANK ATK item scores 15.0",
                RecommendationService.equipScoreForHero(atkEq, tank), 15.0);
        test("TANK ranks DEF > ATK",
                RecommendationService.equipScoreForHero(defEq, tank) > RecommendationService.equipScoreForHero(atkEq, tank));

        // MAGE: atkW=1.4 => atkEq=50*1.4=70.0
        testEq("MAGE ATK item scores 70.0",
                RecommendationService.equipScoreForHero(atkEq, mage), 70.0);
        test("MAGE ranks ATK > DEF",
                RecommendationService.equipScoreForHero(atkEq, mage) > RecommendationService.equipScoreForHero(defEq, mage));

        // SUPPORT: atkW=0.2, defW=1.2 => atkEq=10.0, defEq=60.0
        testEq("SUPPORT ATK item scores 10.0",
                RecommendationService.equipScoreForHero(atkEq, support), 10.0);
        testEq("SUPPORT DEF item scores 60.0",
                RecommendationService.equipScoreForHero(defEq, support), 60.0);
        test("SUPPORT ranks DEF > ATK",
                RecommendationService.equipScoreForHero(defEq, support) > RecommendationService.equipScoreForHero(atkEq, support));

        // Price penalty
        Equipment priced = new Equipment("te3", "Priced", EquipmentType.ATTACK, 100, 0, 0, 10000);
        Equipment freeItem = new Equipment("te4", "Free", EquipmentType.ATTACK, 100, 0, 0, 0);
        test("Price penalty reduces score",
                RecommendationService.equipScoreForHero(freeItem, warrior) > RecommendationService.equipScoreForHero(priced, warrior));
    }

    // ================================================================
    // Recommendation sort - descending by HP+ATK+DEF
    // ================================================================
    static void runRecommendSortTests() {
        System.out.println("--- Recommendation sort order ---");

        // Verify RecommendationService sort via getRecommendedHeroesForPlayer
        // Create a player owning only WARRIOR roles, so all other roles are gaps
        Hero strong = new Hero("rs1", "Strong", HeroRole.TANK, 5000, 300, 200);   // total 5500
        Hero medium = new Hero("rs2", "Medium", HeroRole.MAGE, 3000, 200, 100);    // total 3300
        Hero weak = new Hero("rs3", "Weak", HeroRole.SUPPORT, 1000, 50, 50);        // total 1100
        Hero ownedHero = new Hero("rs-own", "OwnedHero", HeroRole.WARRIOR, 1000, 100, 100);
        Player testP = new Player("rs-p", "SortPlayer", "pw", Role.PLAYER, "Gold", 50.0, 5, null);
        testP.getHeroPool().add(ownedHero);

        GameData miniData = new GameData();
        miniData.getPlayers().add(testP);
        miniData.getHeroes().add(strong);   // total 5500
        miniData.getHeroes().add(medium);   // total 3300
        miniData.getHeroes().add(weak);     // total 1100
        miniData.getHeroes().add(ownedHero); // already owned, should be excluded

        List<Hero> sorted = RecommendationService.getRecommendedHeroesForPlayer(miniData, "SortPlayer");
        test("Recommend sort: strong first (via service)",
                sorted.size() >= 3 && sorted.get(0).getName().equals("Strong"));
        test("Recommend sort: medium second (via service)",
                sorted.size() >= 3 && sorted.get(1).getName().equals("Medium"));
        test("Recommend sort: weak last (via service)",
                sorted.size() >= 3 && sorted.get(2).getName().equals("Weak"));
    }

    // ================================================================
    // Helpers
    // ================================================================
    static void test(String name, boolean condition) {
        if (condition) { passed++; System.out.println("  PASS  " + name); }
        else { failed++; System.out.println("  FAIL  " + name); }
    }

    static void testEq(String name, double actual, double expected) {
        boolean ok = Math.abs(actual - expected) < 0.01;
        if (ok) { passed++; System.out.println("  PASS  " + name); }
        else { failed++; System.out.println("  FAIL  " + name + " [expected=" + expected + " actual=" + actual + "]"); }
    }

    static void testEqI(String name, int actual, int expected) {
        if (actual == expected) { passed++; System.out.println("  PASS  " + name); }
        else { failed++; System.out.println("  FAIL  " + name + " [expected=" + expected + " actual=" + actual + "]"); }
    }
}
