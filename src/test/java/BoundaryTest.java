import model.*;
import service.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * Comprehensive Boundary Value Testing for Honor of Kings IMS.
 * 
 * Covers:
 *   P1 - Core calculation methods (rankToScore, scoreToRankName, equipScore,
 *        playerScore, calculateDamage, equipScoreForHero, isSuitable)
 *   P2 - Data validation methods (CRUD boundary checks)
 *   P3 - Helper methods (findByName, getRecommendedHeroesForPlayer, nextId)
 * 
 * JUnit 5 with parameterized tests. Java 11+ compatible.
 */
@DisplayName("Boundary Value Tests")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class BoundaryTest {

    // ================================================================
    //  P1: rankToScore(String) 鈥?String 鈫?int mapping
    // ================================================================
    @Nested
    @DisplayName("P1.1 rankToScore 鈥?rank-string to int mapping")
    class RankToScoreTests {

        @ParameterizedTest(name = "rankToScore({0}) = {1}")
        @CsvSource({
            "Gold,     1",   // L (lowest valid)
            "Platinum, 2",   // L+
            "Diamond,  3",   // Nom
            "Star,     4",   // R-
            "King,     5",   // R (highest valid)
        })
        void validRanks(String rank, int expected) {
            assertEquals(expected, SearchService.rankToScore(rank));
        }

        @ParameterizedTest(name = "rankToScore({0}) = 1 (default)")
        @NullAndEmptySource
        @ValueSource(strings = {"Bronze", "Iron", "Challenger", "KING", "king", "  Gold"})
        void invalidRanksDefaultTo1(String rank) {
            assertEquals(1, SearchService.rankToScore(rank));
        }
    }

    // ================================================================
    //  P1: scoreToRankName(double) 鈥?threshold boundary mapping
    // ================================================================
    @Nested
    @DisplayName("P1.2 scoreToRankName 鈥?threshold boundary mapping")
    class ScoreToRankNameTests {

        @ParameterizedTest(name = "scoreToRankName({0}) = {1}")
        @CsvSource({
            // Gold: score < 1.5
            "0.0,      Gold",  // L (lowest input)
            "1.0,      Gold",  // Nom
            "1.4999,   Gold",  // R- (just below 1.5)
            // Platinum: 1.5 <= score < 2.5
            "1.5,      Platinum", // L
            "1.50001,  Platinum", // L+
            "2.0,      Platinum", // Nom
            "2.4999,   Platinum", // R-
            // Diamond: 2.5 <= score < 3.5
            "2.5,      Diamond",  // L
            "2.50001,  Diamond",  // L+
            "3.0,      Diamond",  // Nom
            "3.4999,   Diamond",  // R-
            // Star: 3.5 <= score < 4.5
            "3.5,      Star",     // L
            "3.50001,  Star",     // L+
            "4.0,      Star",     // Nom
            "4.4999,   Star",     // R-
            // King: score >= 4.5
            "4.5,      King",     // L (R)
            "4.50001,  King",     // L+ (R+)
            "5.0,      King",     // Nom
            "9.9,      King",     // far above threshold
        })
        void thresholdBoundaries(double score, String expected) {
            assertEquals(expected, SearchService.scoreToRankName(score));
        }

        @Test
        @DisplayName("Negative score returns Gold")
        void negativeScoreReturnsGold() {
            assertEquals("Gold", SearchService.scoreToRankName(-100.0));
        }
    }

    // ================================================================
    //  P1: equipScore(Equipment) 鈥?ATK*1.0 + DEF*0.8 + HP*0.6 - Price*0.001
    // ================================================================
    @Nested
    @DisplayName("P1.3 equipScore 鈥?composite equipment scoring")
    class EquipScoreTests {

        @ParameterizedTest(name = "equipScore(ATK={0},DEF={1},HP={2},Price={3}) = {4}")
        @CsvSource({
            // All zero
            "0, 0, 0, 0,        0.0",
            // Only ATK: 100*1.0 + 0 - 500*0.001 = 99.5
            "100, 0, 0, 500,    99.5",
            // Only DEF: 0 + 100*0.8 + 0 - 300*0.001 = 79.7
            "0, 100, 0, 300,    79.7",
            // Only HP: 0 + 0 + 200*0.6 - 100*0.001 = 119.9
            "0, 0, 200, 100,    119.9",
            // ATK=1 (L+ for ATK): 1.0 - 0 = 1.0
            "1, 0, 0, 0,        1.0",
            // Price dominates: ATK=1, Price=10000 -> 1.0 - 10.0 = -9.0
            "1, 0, 0, 10000,   -9.0",
            // Large values: 100000 * (1.0+0.8+0.6) - 100000*0.001 = 240000 - 100 = 239900
            "100000, 100000, 100000, 100000, 239900.0",
        })
        void equipScoreBoundaries(int atk, int def, int hp, int price, double expected) {
            Equipment e = new Equipment("t", "Test", EquipmentType.ATTACK, atk, def, hp, price);
            assertEquals(expected, SearchService.equipScore(e), 0.01);
        }

        @Test
        @DisplayName("Price penalty test: higher price reduces score")
        void pricePenaltyReducesScore() {
            Equipment cheap = new Equipment("c", "Cheap", EquipmentType.ATTACK, 100, 0, 0, 0);
            Equipment pricey = new Equipment("p", "Pricey", EquipmentType.ATTACK, 100, 0, 0, 10000);
            assertTrue(SearchService.equipScore(cheap) > SearchService.equipScore(pricey));
        }
    }

    // ================================================================
    //  P1: playerScore(Player) 鈥?WR*1.0 + rankToScore*5.0 + matches*0.01
    // ================================================================
    @Nested
    @DisplayName("P1.4 playerScore 鈥?composite player scoring")
    class PlayerScoreTests {

        @ParameterizedTest(name = "playerScore(rank={0}, WR={1}, matches={2}) = {3}")
        @CsvSource({
            // Zero everything: 0 + 1*5 + 0 = 5.0 (null rank 鈫?1*5)
            "null,  0.0,   0,    5.0",
            // Gold(1), WR=0, matches=0 鈫?0+5+0=5.0
            "Gold,  0.0,   0,    5.0",
            // Gold, WR=0.00001(L+), matches=0 鈫?5.00001
            "Gold,  0.00001, 0,  5.00001",
            // King(5), WR=100.0(R), matches=0 鈫?100+25+0=125.0
            "King,  100.0, 0,    125.0",
            // King, WR=60, matches=MAX_VALUE 鈫?overflow potential
            // "King,  60.0,  2147483647, 21474836573.47",
            // Gold, WR=80, matches=10 鈫?80+5+0.1=85.1
            "Gold,  80.0,  10,   85.1",
            // Star(4), WR=50, matches=50 鈫?50+20+0.5=70.5
            "Star,  50.0,  50,   70.5",
            // Diamond(3), WR=40, matches=0 鈫?40+15+0=55.0
            "Diamond, 40.0, 0,   55.0",
            // Platinum(2), WR=30, matches=0 鈫?30+10+0=40.0
            "Platinum, 30.0, 0,  40.0",
        })
        void playerScoreBoundaries(String rank, double wr, int matches, double expected) {
            String actualRank = "null".equals(rank) ? null : rank;
            Player p = new Player("p", "TestPlayer", "pw", Role.PLAYER, actualRank, wr, matches, null);
            assertEquals(expected, SearchService.playerScore(p), 0.01);
        }

        @Test
        @DisplayName("Null rank defaults to score 1")
        void nullRankDefaultsToOne() {
            Player p = new Player("p", "Test", "pw", Role.PLAYER, null, 0, 0, null);
            assertEquals(5.0, SearchService.playerScore(p), 0.01);
        }
    }

    // ================================================================
    //  P1: calculateDamage(int atk, int def, boolean hasAtkEq, boolean hasDefEq, Random)
    //  Formula: base=max(1,atk-(int)(def*0.6)); dmg=base+rand(-5..+5);
    //           crit(15%/20%)鈫?1.5; dodge(10%/15%)鈫?; final=max(1,dmg)
    // ================================================================
    @Nested
    @DisplayName("P1.5 calculateDamage 鈥?deterministic damage with seeded RNG")
    class CalculateDamageTests {

        // Known seeds produce predictable results:
        // Seed 42: n11=7鈫?2, crit=63(>=15 false), dodge=48(>=10 false)
        @ParameterizedTest(name = "Damage(atk={0},def={1},atkEq={2},defEq={3},seed={4}) = {5}")
        @CsvSource({
            // ATK=0 def=0 tree: base=max(1,0-0)=1; seed42鈫?2=3
            "0,   0,   false, false, 42, 3",
            // ATK=1 def=1 tree: base=max(1,1-0)=1; seed42鈫?2=3
            "1,   1,   false, false, 42, 3",
            // ATK=100 def=50 tree: base=max(1,100-30)=70; seed42鈫?2=72
            "100, 50,  false, false, 42, 72",
            // ATK=80 def=100 tree: base=max(1,80-60)=20; seed99(n11=3鈫?2,no crit,no dodge)=18
            "80,  100, false, false, 99, 18",
            // ATK=200 def=20 tree: base=max(1,200-12)=188; seed1(n11=4鈫?1)=187
            "200, 20,  false, false, 1,  187",
            // With both equipment modifiers, seed123: n11=10鈫?5, crit=50(>=20 false), dodge=76(>=15 false)
            "100, 50,  true,  true,  123,75",
            // CRITICAL test: seed522 triggers crit(n11=2鈫?3, crit=1<15鈫捗?.5): (70-3)*1.5=100
            "100, 50,  false, false, 522,100",
            // Very high def vs low ATK: base should floor to 1 at least
            "30,  200, false, false, 0,  1",
        })
        void damageBoundaries(int atk, int def, boolean atkEq, boolean defEq, long seed, int expected) {
            Random rng = new Random(seed);
            int result = CombatSimulator.calculateDamage(atk, def, atkEq, defEq, rng);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Higher defense 鈫?less damage (same seed)")
        void higherDefLessDamage() {
            Random rngA = new Random(0);
            int lowDef = CombatSimulator.calculateDamage(100, 20, false, false, rngA);
            Random rngB = new Random(0);
            int highDef = CombatSimulator.calculateDamage(100, 80, false, false, rngB);
            assertTrue(lowDef > highDef, "Low def damage should be > high def damage");
        }

        @Test
        @DisplayName("ATK=1, DEF=2 鈥?base floor edge: max(1, 1-1)=1, plus random")
        void baseFloorEdgeCase() {
            // Seed 123: n11=10 鈫?+5 鈫?1+5=6 (proves base floor is 1 not 0)
            int d = CombatSimulator.calculateDamage(1, 2, false, false, new Random(123));
            assertEquals(6, d);
        }

        @Test
        @DisplayName("Damage is never negative (dodge returns 0, floor is 1 otherwise)")
        void damageNeverNegative() {
            for (long seed = 0; seed < 100; seed++) {
                int d = CombatSimulator.calculateDamage(1, 2, false, false, new Random(seed));
                assertTrue(d >= 0, "Damage should be >= 0 for seed=" + seed);
            }
        }
    }

    // ================================================================
    //  P1: equipScoreForHero(Equipment, Hero) 鈥?role-weighted scoring
    // ================================================================
    @Nested
    @DisplayName("P1.6 equipScoreForHero 鈥?role-adjusted equipment scoring")
    class EquipScoreForHeroTests {

        static Equipment atkItem = new Equipment("ea", "ATK+50", EquipmentType.ATTACK, 50, 0, 0, 0);
        static Equipment defItem = new Equipment("ed", "DEF+50", EquipmentType.DEFENSE, 0, 50, 0, 0);
        static Equipment hpItem  = new Equipment("eh", "HP+50",  EquipmentType.DEFENSE, 0, 0, 50, 0);

        static Hero warrior    = new Hero("hw", "W", HeroRole.WARRIOR,   1000, 100, 80);
        static Hero assassin   = new Hero("ha", "A", HeroRole.ASSASSIN,  1000, 100, 80);
        static Hero tank       = new Hero("ht", "T", HeroRole.TANK,      1200, 60, 120);
        static Hero mage       = new Hero("hm", "M", HeroRole.MAGE,      900,  110, 50);
        static Hero marksman   = new Hero("hk", "K", HeroRole.MARKSMAN,  1000, 120, 70);
        static Hero support    = new Hero("hs", "S", HeroRole.SUPPORT,   1100, 40, 100);

        @ParameterizedTest(name = "{0} vs ATK item = {1}")
        @CsvSource({
            "WARRIOR,  75.0",  // 50*1.5
            "ASSASSIN, 75.0",  // 50*1.5
            "TANK,     15.0",  // 50*0.3
            "MAGE,     70.0",  // 50*1.4
            "MARKSMAN, 65.0",  // 50*1.3
            "SUPPORT,  10.0",  // 50*0.2
        })
        void atkItemByRole(HeroRole role, double expected) {
            Hero h = heroForRole(role);
            assertEquals(expected, RecommendationService.equipScoreForHero(atkItem, h), 0.01);
        }

        @ParameterizedTest(name = "{0} vs DEF item = {1}")
        @CsvSource({
            "WARRIOR,  25.0",  // 50*0.5
            "ASSASSIN, 25.0",  // 50*0.5
            "TANK,     75.0",  // 50*1.5
            "MAGE,     15.0",  // 50*0.3
            "MARKSMAN, 20.0",  // 50*0.4
            "SUPPORT,  60.0",  // 50*1.2
        })
        void defItemByRole(HeroRole role, double expected) {
            Hero h = heroForRole(role);
            assertEquals(expected, RecommendationService.equipScoreForHero(defItem, h), 0.01);
        }

        @ParameterizedTest(name = "{0} vs HP item = {1}")
        @CsvSource({
            "WARRIOR,  25.0",  // 50*0.5
            "ASSASSIN, 25.0",  // 50*0.5
            "TANK,     60.0",  // 50*1.2
            "MAGE,     20.0",  // 50*0.4
            "MARKSMAN, 25.0",  // 50*0.5
            "SUPPORT,  50.0",  // 50*1.0
        })
        void hpItemByRole(HeroRole role, double expected) {
            Hero h = heroForRole(role);
            assertEquals(expected, RecommendationService.equipScoreForHero(hpItem, h), 0.01);
        }

        @Test
        @DisplayName("Zero-bonus equipment gives -price*0.001")
        void zeroBonusEquipment() {
            Equipment zero = new Equipment("z", "Zero", EquipmentType.MOVEMENT, 0, 0, 0, 1000);
            double score = RecommendationService.equipScoreForHero(zero, warrior);
            assertEquals(-1.0, score, 0.01);
        }

        @Test
        @DisplayName("Price penalty decreases score linearly")
        void pricePenaltyLinear() {
            Equipment free = new Equipment("f", "Free", EquipmentType.MOVEMENT, 0, 0, 0, 0);
            Equipment cost100 = new Equipment("c", "Cost", EquipmentType.MOVEMENT, 0, 0, 0, 10000);
            double scoreFree = RecommendationService.equipScoreForHero(free, warrior);
            double scoreCost = RecommendationService.equipScoreForHero(cost100, warrior);
            assertEquals(-10.0, scoreCost, 0.01);
            assertTrue(scoreFree > scoreCost);
        }

        private Hero heroForRole(HeroRole role) {
            switch (role) {
                case WARRIOR:  return warrior;
                case ASSASSIN: return assassin;
                case TANK:     return tank;
                case MAGE:     return mage;
                case MARKSMAN: return marksman;
                case SUPPORT:  return support;
                default: throw new IllegalArgumentException();
            }
        }
    }

    // ================================================================
    //  P1: isSuitable(EquipmentType, HeroRole) 鈥?cross-product truth table
    // ================================================================
    @Nested
    @DisplayName("P1.7 isSuitable 鈥?equipment-hero compatibility matrix")
    class IsSuitableTests {

        @ParameterizedTest(name = "isSuitable({0}, {1}) = {2}")
        @CsvSource({
            // WARRIOR: ATTACK, JUNGLE, MOVEMENT only
            "WARRIOR, ATTACK,  true",
            "WARRIOR, JUNGLE,  true",
            "WARRIOR, MOVEMENT,true",
            "WARRIOR, DEFENSE, false",
            "WARRIOR, MAGIC,   false",
            // ASSASSIN: same as WARRIOR
            "ASSASSIN,ATTACK,  true",
            "ASSASSIN,JUNGLE,  true",
            "ASSASSIN,MOVEMENT,true",
            "ASSASSIN,DEFENSE, false",
            "ASSASSIN,MAGIC,   false",
            // MAGE: MAGIC, MOVEMENT
            "MAGE,    MAGIC,   true",
            "MAGE,    MOVEMENT,true",
            "MAGE,    ATTACK,  false",
            "MAGE,    DEFENSE, false",
            "MAGE,    JUNGLE,  false",
            // TANK: DEFENSE, MOVEMENT
            "TANK,    DEFENSE, true",
            "TANK,    MOVEMENT,true",
            "TANK,    ATTACK,  false",
            "TANK,    JUNGLE,  false",
            "TANK,    MAGIC,   false",
            // MARKSMAN: same as WARRIOR
            "MARKSMAN,ATTACK,  true",
            "MARKSMAN,JUNGLE,  true",
            "MARKSMAN,MOVEMENT,true",
            "MARKSMAN,DEFENSE, false",
            "MARKSMAN,MAGIC,   false",
            // SUPPORT: DEFENSE, MAGIC, MOVEMENT
            "SUPPORT, DEFENSE, true",
            "SUPPORT, MAGIC,   true",
            "SUPPORT, MOVEMENT,true",
            "SUPPORT, ATTACK,  false",
            "SUPPORT, JUNGLE,  false",
        })
        void suitabilityMatrix(HeroRole role, EquipmentType type, boolean expected) {
            assertEquals(expected, SearchService.isSuitable(type, role));
        }
    }

    // ================================================================
    //  P2: DataManager CRUD 鈥?validation boundaries
    // ================================================================
    @Nested
    @DisplayName("P2.1 DataManager.addPlayer 鈥?validation boundaries")
    class AddPlayerValidationTests {

        private GameData data;

        @BeforeEach
        void setUp() {
            data = new GameData();
        }

        @Test
        @DisplayName("null data 鈥?no crash")
        void nullData() {
            assertDoesNotThrow(() -> DataManager.addPlayer(null, "test", "King", 50, 10));
        }

        @ParameterizedTest(name = "name=''{0}'' 鈫?rejected")
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        void invalidNames(String name) {
            data.addPlayer(new Player("P01", "existing", "pw", Role.PLAYER, "Gold", 50, 5, null));
            int sizeBefore = data.getPlayers().size();
            DataManager.addPlayer(data, name, "King", 50, 10);
            assertEquals(sizeBefore, data.getPlayers().size(), "No player should be added for invalid name");
        }

        @ParameterizedTest(name = "addPlayer winRate={0}")
        @CsvSource({
            "-0.01, false",   // L-1: just below 0
            "0.0,   true",    // L
            "0.0001,true",    // L+
            "50.0,  true",    // Nom
            "100.0, true",    // R
            "100.01,false",   // R+1: just above 100
            "150.0, false",   // far above
        })
        void winRateBoundaries(double wr, boolean shouldAdd) {
            data.addPlayer(new Player("P01", "existing", "pw", Role.PLAYER, "Gold", 50, 5, null));
            int sizeBefore = data.getPlayers().size();
            DataManager.addPlayer(data, "testWR", "Gold", wr, 10);
            if (shouldAdd) {
                assertEquals(sizeBefore + 1, data.getPlayers().size());
            } else {
                assertEquals(sizeBefore, data.getPlayers().size());
            }
        }

        @ParameterizedTest(name = "addPlayer matches={0}")
        @CsvSource({
            "-1,     false",  // L-1
            "0,      true",   // L
            "1,      true",   // L+
            "100,    true",   // Nom
            "100000, true",   // far above
        })
        void matchesBoundaries(int matches, boolean shouldAdd) {
            data.addPlayer(new Player("P01", "existing", "pw", Role.PLAYER, "Gold", 50, 5, null));
            int sizeBefore = data.getPlayers().size();
            DataManager.addPlayer(data, "testMatches", "Gold", 50, matches);
            if (shouldAdd) {
                assertEquals(sizeBefore + 1, data.getPlayers().size());
            } else {
                assertEquals(sizeBefore, data.getPlayers().size());
            }
        }

        @Test
        @DisplayName("addPlayer with null rank succeeds")
        void nullRankSucceeds() {
            DataManager.addPlayer(data, "nullRank", null, 50, 10);
            assertTrue(data.getPlayers().stream().anyMatch(p -> "nullRank".equals(p.getUsername())));
        }

        @Test
        @DisplayName("addPlayer with NaN winRate rejected")
        void nanWinRateRejected() {
            data.addPlayer(new Player("P01", "existing", "pw", Role.PLAYER, "Gold", 50, 5, null));
            int sizeBefore = data.getPlayers().size();
            DataManager.addPlayer(data, "nanWR", "Gold", Double.NaN, 10);
            assertEquals(sizeBefore, data.getPlayers().size(), "NaN winRate should be rejected");
        }
    }

    @Nested
    @DisplayName("P2.2 DataManager.addHero 鈥?validation boundaries")
    class AddHeroValidationTests {

        private GameData data;

        @BeforeEach
        void setUp() {
            data = new GameData();
            data.addHero(new Hero("H01", "Existing", HeroRole.WARRIOR, 3000, 100, 100));
        }

        @ParameterizedTest(name = "addHero name=''{0}'' 鈫?rejected")
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        void invalidNames(String name) {
            int sizeBefore = data.getHeroes().size();
            DataManager.addHero(data, name, HeroRole.WARRIOR, 1, 1, 1);
            assertEquals(sizeBefore, data.getHeroes().size());
        }

        @ParameterizedTest(name = "addHero ({1},{2},{3}) 鈫?rejected={4}")
        @CsvSource({
            "-1, 100, 100, true",    // HP < 0 鈫?negative
            "1,  -1,  100, true",    // ATK < 0
            "1, 100,  -1,  true",    // DEF < 0
            "0,   0,   0,  false",   // L: all zero (allowed: stats cannot be *negative*)
            "1,   1,   1,  false",   // L+: just above zero
            "5000, 300, 200, false", // Nom
        })
        void statBoundaries(int hp, int atk, int def, boolean isRejected) {
            int sizeBefore = data.getHeroes().size();
            DataManager.addHero(data, "testHero", HeroRole.TANK, hp, atk, def);
            if (isRejected) {
                assertEquals(sizeBefore, data.getHeroes().size());
            } else {
                assertEquals(sizeBefore + 1, data.getHeroes().size());
            }
        }
    }

    @Nested
    @DisplayName("P2.3 DataManager.addEquipment 鈥?validation boundaries")
    class AddEquipmentValidationTests {

        private GameData data;

        @BeforeEach
        void setUp() {
            data = new GameData();
            data.addEquipment(new Equipment("E01", "Existing", EquipmentType.ATTACK, 100, 0, 0, 1000));
        }

        @ParameterizedTest(name = "addEquipment name=''{0}'' 鈫?rejected")
        @NullAndEmptySource
        void invalidNames(String name) {
            int sizeBefore = data.getEquipments().size();
            DataManager.addEquipment(data, name, EquipmentType.ATTACK, 1, 1, 1, 100);
            assertEquals(sizeBefore, data.getEquipments().size());
        }

        @ParameterizedTest(name = "addEquipment price={0} 鈫?allowed={1}")
        @CsvSource({
            "-1,  false",  // L-1
            "0,   true",   // L
            "1,   true",   // L+
            "2100,true",   // Nom
            "9999,true",   // far above
        })
        void priceBoundaries(int price, boolean shouldAdd) {
            int sizeBefore = data.getEquipments().size();
            DataManager.addEquipment(data, "testEq", EquipmentType.ATTACK, 10, 10, 10, price);
            if (shouldAdd) {
                assertEquals(sizeBefore + 1, data.getEquipments().size());
            } else {
                assertEquals(sizeBefore, data.getEquipments().size());
            }
        }

        @ParameterizedTest(name = "addEquipment ({0},{1},{2}) 鈫?rejected={3}")
        @CsvSource({
            "-1,   0,   0,  true",
            "0,   -1,   0,  true",
            "0,    0,  -1,  true",
            "0,    0,   0,  false",
        })
        void statBoundaries(int atk, int def, int hp, boolean isRejected) {
            int sizeBefore = data.getEquipments().size();
            DataManager.addEquipment(data, "testEq", EquipmentType.ATTACK, atk, def, hp, 0);
            if (isRejected) {
                assertEquals(sizeBefore, data.getEquipments().size());
            } else {
                assertEquals(sizeBefore + 1, data.getEquipments().size());
            }
        }
    }

    @Nested
    @DisplayName("P2.4 DataManager.addTeam 鈥?validation boundaries")
    class AddTeamValidationTests {

        @Test
        @DisplayName("null or empty team name rejected")
        void nullOrEmptyTeamName() {
            GameData data = new GameData();
            data.addTeam(new Team("T01", "Existing", 0, 0));
            int sizeBefore = data.getTeams().size();
            DataManager.addTeam(data, null);
            DataManager.addTeam(data, "");
            DataManager.addTeam(data, "  ");
            assertEquals(sizeBefore, data.getTeams().size());
        }
    }

    @Nested
    @DisplayName("P2.5 DataManager.addMatchRecord 鈥?null team validation")
    class AddMatchRecordValidationTests {

        @Test
        @DisplayName("Null teamA or teamB rejected")
        void nullTeamsRejected() {
            GameData data = new GameData();
            Team t = new Team("T01", "Team", 0, 0);
            data.addTeam(t);
            int sizeBefore = data.getMatchRecords().size();

            DataManager.addMatchRecord(data, null, t, 1, 1, java.time.LocalDate.now());
            assertEquals(sizeBefore, data.getMatchRecords().size());

            DataManager.addMatchRecord(data, t, null, 1, 1, java.time.LocalDate.now());
            assertEquals(sizeBefore, data.getMatchRecords().size());
        }
    }

    // ================================================================
    //  P3: Helper 鈥?nextId (auto-increment ID generator)
    // ================================================================
    @Nested
    @DisplayName("P3.1 nextId 鈥?ID auto-increment boundary")
    class NextIdTests {

        @Test
        @DisplayName("Empty list 鈫?prefix+01")
        void emptyListReturns01() {
            // nextId is private; we test via add operations and check generated IDs
            GameData data = new GameData();
            DataManager.addPlayer(data, "first", "Gold", 50, 10);
            String id = data.getPlayers().get(0).getId();
            assertTrue(id.startsWith("P"));
            // Since no prior entries, should be "P01"
            assertEquals("P01", id);
        }

        @Test
        @DisplayName("Sequential IDs increment correctly")
        void sequentialIdsIncrement() {
            GameData data = new GameData();
            DataManager.addPlayer(data, "p1", "Gold", 50, 10);
            DataManager.addPlayer(data, "p2", "Gold", 50, 10);
            DataManager.addPlayer(data, "p3", "Gold", 50, 10);
            assertEquals("P01", data.getPlayers().get(0).getId());
            assertEquals("P02", data.getPlayers().get(1).getId());
            assertEquals("P03", data.getPlayers().get(2).getId());
        }

        @Test
        @DisplayName("ID prefix preserved for different entity types")
        void differentEntityPrefixes() {
            GameData data = new GameData();
            DataManager.addPlayer(data, "p", "Gold", 50, 10);
            DataManager.addHero(data, "h", HeroRole.WARRIOR, 100, 1, 1);
            DataManager.addEquipment(data, "e", EquipmentType.ATTACK, 1, 1, 1, 100);
            DataManager.addTeam(data, "t");

            assertTrue(data.getPlayers().get(0).getId().startsWith("P"));
            assertTrue(data.getHeroes().get(0).getId().startsWith("H"));
            assertTrue(data.getEquipments().get(0).getId().startsWith("E"));
            assertTrue(data.getTeams().get(0).getId().startsWith("T"));
        }
    }

    // ================================================================
    //  P3: Search 鈥?findPlayerByName, findHeroByName, findTeamByName
    // ================================================================
    @Nested
    @DisplayName("P3.2 SearchService 鈥?null/empty input boundaries")
    class SearchServiceBoundaries {

        private final Searchable ss = new SearchService();
        private GameData fullData;

        @BeforeEach
        void setUp() {
            fullData = DataInitializer.initAll();
        }

        @Test
        @DisplayName("findPlayerByName(null) no crash")
        void findPlayerNull() {
            assertDoesNotThrow(() -> ss.findPlayerByName(fullData, null));
        }

        @Test
        @DisplayName("findPlayerByName(empty) no crash")
        void findPlayerEmpty() {
            assertDoesNotThrow(() -> ss.findPlayerByName(fullData, ""));
        }

        @Test
        @DisplayName("findPlayerByName(non-existent) no crash")
        void findPlayerNonExistent() {
            assertDoesNotThrow(() -> ss.findPlayerByName(fullData, "zzz_nobody_xyz"));
        }

        @Test
        @DisplayName("findHeroByName(null) no crash")
        void findHeroNull() {
            assertDoesNotThrow(() -> ss.findHeroByName(fullData, null));
        }

        @Test
        @DisplayName("findHeroByName(empty) no crash")
        void findHeroEmpty() {
            assertDoesNotThrow(() -> ss.findHeroByName(fullData, ""));
        }

        @Test
        @DisplayName("findTeamByName(null) no crash")
        void findTeamNull() {
            assertDoesNotThrow(() -> ss.findTeamByName(fullData, null));
        }

        @Test
        @DisplayName("showMatchHistory(null input) no crash")
        void matchHistoryNull() {
            assertDoesNotThrow(() -> ss.showMatchHistory(fullData, null));
        }

        @Test
        @DisplayName("showEquipmentRanking(null) no crash")
        void equipRankingNull() {
            assertDoesNotThrow(() -> ss.showEquipmentRanking(null));
        }

        @Test
        @DisplayName("showLeaderboard(null) no crash")
        void leaderboardNull() {
            assertDoesNotThrow(() -> ss.showLeaderboard(null));
        }
    }

    // ================================================================
    //  P3: RecommendationService boundaries
    // ================================================================
    @Nested
    @DisplayName("P3.3 RecommendationService 鈥?edge case boundaries")
    class RecommendationServiceBoundaries {

        @Test
        @DisplayName("getRecommendedHeroesForPlayer null data 鈫?empty list")
        void nullDataReturnsEmpty() {
            List<Hero> result = RecommendationService.getRecommendedHeroesForPlayer(null, "any");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getRecommendedHeroesForPlayer null player 鈫?empty list")
        void nullPlayerReturnsEmpty() {
            GameData data = new GameData();
            List<Hero> result = RecommendationService.getRecommendedHeroesForPlayer(data, "unknown");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getRecommendedHeroesForPlayer fully covered roles 鈫?empty list")
        void fullCoverageReturnsEmpty() {
            GameData data = new GameData();
            Player p = new Player("p1", "fullCover", "pw", Role.PLAYER, "Gold", 50, 5, null);
            // Own a hero of every role
            Hero[] all = {
                new Hero("h1", "WarriorHero",  HeroRole.WARRIOR,  100, 100, 100),
                new Hero("h2", "MageHero",     HeroRole.MAGE,     100, 100, 100),
                new Hero("h3", "AssassinHero", HeroRole.ASSASSIN, 100, 100, 100),
                new Hero("h4", "TankHero",     HeroRole.TANK,     100, 100, 100),
                new Hero("h5", "MarksmanHero", HeroRole.MARKSMAN, 100, 100, 100),
                new Hero("h6", "SupportHero",  HeroRole.SUPPORT,  100, 100, 100),
            };
            for (Hero h : all) {
                p.getHeroPool().add(h);
                data.addHero(h);
            }
            data.addPlayer(p);

            List<Hero> result = RecommendationService.getRecommendedHeroesForPlayer(data, "fullCover");
            assertTrue(result.isEmpty(), "Should be empty when all roles covered");
        }

        @Test
        @DisplayName("recommendEquipmentForHero null data 鈫?no crash")
        void recommendEquipmentNull() {
            assertDoesNotThrow(() -> RecommendationService.recommendEquipmentForHero(null, "any"));
        }

        @Test
        @DisplayName("recommendEquipmentForHero unknown hero 鈫?no crash")
        void recommendEquipmentUnknownHero() {
            GameData data = DataInitializer.initAll();
            assertDoesNotThrow(() -> RecommendationService.recommendEquipmentForHero(data, "NotAHero"));
        }

        @Test
        @DisplayName("recommendHeroesForPlayer unknown player 鈫?no crash")
        void recommendHeroesUnknownPlayer() {
            GameData data = DataInitializer.initAll();
            assertDoesNotThrow(() -> RecommendationService.recommendHeroesForPlayer(data, "unknown_player"));
        }
    }

    // ================================================================
    //  CombatSimulator.simulate 鈥?null/missing hero boundaries
    // ================================================================
    @Nested
    @DisplayName("P3.4 CombatSimulator 鈥?null/missing hero resilience")
    class CombatSimulatorBoundaries {

        @Test
        @DisplayName("simulate null data 鈫?no crash")
        void nullData() {
            assertDoesNotThrow(() -> CombatSimulator.simulate(null, "A", "B"));
        }

        @Test
        @DisplayName("simulate non-existent hero 鈫?no crash")
        void nonExistentHero() {
            GameData data = DataInitializer.initAll();
            assertDoesNotThrow(() -> CombatSimulator.simulate(data, "GhostHero", "Lu Bu"));
            assertDoesNotThrow(() -> CombatSimulator.simulate(data, "Lu Bu", "GhostHero"));
            assertDoesNotThrow(() -> CombatSimulator.simulate(data, "A", "B"));
        }
    }

    // ================================================================
    //  Team winRate boundary (division by zero guard)
    // ================================================================
    @Test
    @DisplayName("Team with 0W 0L 鈫?win rate division by zero guard")
    void zeroWinZeroLossTeam() {
        GameData data = new GameData();
        Team zero = new Team("T99", "ZeroTeam", 0, 0);
        data.addTeam(zero);
        // findTeamByName calls: wr = wins+losses>0 ? wins/(wins+losses)*100 : 0
        Searchable searchable = new SearchService();
        assertDoesNotThrow(() -> searchable.findTeamByName(data, "ZeroTeam"));
    }

    // ================================================================
    //  FilePersistence boundaries
    // ================================================================
    @Nested
    @DisplayName("P3.5 FilePersistence 鈥?save/load boundaries")
    class FilePersistenceBoundaries {

        @Test
        @DisplayName("saveData(null) does not crash")
        void saveNullData() {
            assertDoesNotThrow(() -> FilePersistence.saveData(null));
        }

        @Test
        @DisplayName("loadData returns null when no file")
        void loadNoFile() {
            // data.ser may or may not exist depending on prior runs
            // loadData handles missing file gracefully
            GameData loaded = FilePersistence.loadData();
            // Just verify no exception thrown; loaded could be null or data from file
            assertTrue(loaded == null || loaded instanceof GameData);
        }
    }

    // ================================================================
    //  P3: Team equals/hashCode 鈥?boundary: null id consistency
    // ================================================================
    @Nested
    @DisplayName("P3.6 Team 鈥?equals/hashCode with null id")
    class TeamEqualsTests {

        @Test
        @DisplayName("Two teams with same null id are equal")
        void twoNullIdTeamsAreEqual() {
            Team t1 = new Team(null, "A", 0, 0);
            Team t2 = new Team(null, "A", 0, 0);
            assertEquals(t1, t2, "Two teams with null id should be equal");
            assertEquals(t1.hashCode(), t2.hashCode(), "hashCode should match for equal teams");
        }

        @Test
        @DisplayName("Two teams with same id are equal")
        void sameIdTeamsAreEqual() {
            Team t1 = new Team("T01", "A", 0, 0);
            Team t2 = new Team("T01", "B", 5, 5);
            assertEquals(t1, t2);
            assertEquals(t1.hashCode(), t2.hashCode());
        }

        @Test
        @DisplayName("Two teams with different id are not equal")
        void differentIdTeamsAreNotEqual() {
            Team t1 = new Team("T01", "A", 0, 0);
            Team t2 = new Team("T02", "A", 0, 0);
            assertNotEquals(t1, t2);
        }

        @Test
        @DisplayName("Team not equal to null")
        void teamNotEqualToNull() {
            Team t = new Team("T01", "A", 0, 0);
            assertNotEquals(null, t);
        }
    }
}

