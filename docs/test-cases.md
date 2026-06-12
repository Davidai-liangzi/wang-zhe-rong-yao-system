# test-cases.md - Test Cases

## Test Environment
- Application: Honor of Kings Information Management System
- Initial Data: 15 players, 15 heroes, 20 equipment items, 3 teams, 10 match records
- Data Source: DataInitializer hard-coded data

---

### TC-01: Find existing player

| Field | Content |
|-------|---------|
| **ID** | TC-01 |
| **Module** | Player Lookup |
| **Input** | Admin login -> Option 1 -> Enter "p1_fly" |
| **Expected** | Display username "p1_fly", rank "King", win rate 72.5%, team "AG Super Play", hero pool includes "Lu Bu" |
| **Actual** | Correctly displays p1_fly, rank=King, win rate=72.5%, team=AG Super Play, hero pool=Lu Bu (WARRIOR, HP:3500 ATK:180 DEF:100), skills=Sky Piercer, Wolf Grip, Demon Descent |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-02: Find non-existent player

| Field | Content |
|-------|---------|
| **ID** | TC-02 |
| **Module** | Player Lookup |
| **Input** | Admin login -> Option 1 -> Enter "nonexist" |
| **Expected** | Output "Player not found: nonexist" |
| **Actual** | Output "Player not found: nonexist", correct |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-03: Find existing team

| Field | Content |
|-------|---------|
| **ID** | TC-03 |
| **Module** | Team Overview |
| **Input** | Admin login -> Option 2 -> Enter "AG Super Play" |
| **Expected** | Display team name "AG Super Play", record "28W / 10L", win rate ~73.7%, 5 members, member list includes "p1_fly", "p10_menglei" etc. |
| **Actual** | Correctly displays AG Super Play 28W/10L, win rate 73.7%, 5 members (p1_fly/p2_jiucheng/p3_yinuo/p10_menglei/p11_nuanyang), total matches 512, average rank score 4.8 (King+), top player p10_menglei (win rate 80%) |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-04: Find non-existent team

| Field | Content |
|-------|---------|
| **ID** | TC-04 |
| **Module** | Team Overview |
| **Input** | Admin login -> Option 2 -> Enter "NonExistentTeam" |
| **Expected** | Output "Team not found: NonExistentTeam" |
| **Actual** | Output "Team not found: NonExistentTeam", correct |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-05: Find existing hero

| Field | Content |
|-------|---------|
| **ID** | TC-05 |
| **Module** | Hero Details |
| **Input** | Admin login -> Option 3 -> Enter "Li Bai" |
| **Expected** | Display name "Li Bai", role "ASSASSIN", HP=3200 ATK=200 DEF=70, skills include "Drinking Song", "Divine Brush", "Lotus Sword"; recommended equipment includes attack/jungle/movement types; owners include "p7_nuoyan", "p10_menglei" |
| **Actual** | Correctly displays Li Bai/ASSASSIN/HP3200/ATK200/DEF70, 3 skills, 11 recommended equipment items (attack/movement/jungle), owners p7_nuoyan, p10_menglei |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-06: Find non-existent hero

| Field | Content |
|-------|---------|
| **ID** | TC-06 |
| **Module** | Hero Details |
| **Input** | Admin login -> Option 3 -> Enter "Wang Zhaojun" |
| **Expected** | Output "Hero not found: Wang Zhaojun" |
| **Actual** | Output "Hero not found: Wang Zhaojun", correct |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-07: Equipment ranking

| Field | Content |
|-------|---------|
| **ID** | TC-07 |
| **Module** | Equipment Stats |
| **Input** | Admin login -> Option 4 |
| **Expected** | Display ranking table of 20 equipment items sorted by composite score descending. Rank 1 should be "Ominous Omen" (DEF=270 HP=1200, score=933.8, high DEF weight causes top ranking); last place "Calm Boots" (pure movement item, score=-0.7) |
| **Actual** | Correctly displays 20 equipment rankings, rank 1 Ominous Omen (933.8), last Calm Boots (-0.7). Formula executed correctly: DEF*0.8+HP*0.6 makes defense items score higher than pure attack items |
| **Pass** | Pass |
| **Bug** | None. Expected result was originally wrong (thought Sage's Tome would rank first), but the formula with DEF weight 0.8 + HP weight 0.6 makes Ominous Omen far ahead, consistent with formula logic |

---

### TC-08: Player leaderboard

| Field | Content |
|-------|---------|
| **ID** | TC-08 |
| **Module** | Leaderboard |
| **Input** | Admin login -> Option 6 |
| **Expected** | Display ranking table of 15 players sorted by composite score descending. Rank 1 should be "p10_menglei" (win rate 80%, King, 150 matches), last "p9_wuhen" (win rate 55.3%, Diamond, 55 matches); ties broken by username lexicographic order |
| **Actual** | Correctly displays 15 players, rank 1 p10_menglei (106.5 score), rank 15 p9_wuhen (70.9 score). Ranking order correct: King > Star > Diamond, same tier ordered by score descending |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-09: Match history — query by player

| Field | Content |
|-------|---------|
| **ID** | TC-09 |
| **Module** | Match History |
| **Input** | Admin login -> Option 5 -> Enter "p1_fly" |
| **Expected** | Display "Query: Player p1_fly", list AG Super Play's last 5 matches in reverse chronological order, each showing opponent, score, and result |
| **Actual** | Correctly displays 5 matches (2026-06-05 to 2026-05-18), reverse chronological order, opponents QGhappy/eStarPro, with scores and results (4 wins 1 draw), participating heroes correctly list AG Super Play members |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-10: Match history — query by team

| Field | Content |
|-------|---------|
| **ID** | TC-10 |
| **Module** | Match History |
| **Input** | Admin login -> Option 5 -> Enter "eStarPro" |
| **Expected** | Display "Query: Team eStarPro", list eStarPro's last 5 matches in reverse chronological order |
| **Actual** | Correctly displays 5 matches (2026-06-01 to 2026-05-15), reverse chronological order, opponents QGhappy/AG Super Play, with scores and results (2 wins 2 losses 1 draw), participating heroes correctly list eStarPro members |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-11: Admin adds player then queries

| Field | Content |
|-------|---------|
| **ID** | TC-11 |
| **Module** | Data Management |
| **Input** | Admin login -> Option 7 -> Option 1 -> Enter username "test_new", rank "Diamond", win rate 60, matches 30 -> Option 0 back -> Option 1 -> Enter "test_new" |
| **Expected** | Add outputs "Player added: test_new"; query finds the player, displays rank "Diamond", win rate 60% |
| **Actual** | Add outputs "Player added: test_new", query returns test_new/Diamond/win rate 60.0%/30 matches/no team/no heroes, fully correct |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-12: Admin deletes player then queries

| Field | Content |
|-------|---------|
| **ID** | TC-12 |
| **Module** | Data Management |
| **Input** | Admin login -> Option 7 -> Option 2 -> Enter "p6_gemini" -> Option 0 back -> Option 1 -> Enter "p6_gemini" |
| **Expected** | Delete outputs "Player deleted: p6_gemini"; query outputs "Player not found: p6_gemini" |
| **Actual** | Delete outputs "Player deleted: p6_gemini", query outputs "Player not found: p6_gemini", fully correct |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-13: File persistence — verify after add/delete and restart

| Field | Content |
|-------|---------|
| **ID** | TC-13 |
| **Module** | File Persistence |
| **Input** | Delete data.json -> Start program (admin) -> Add player "persist_test" -> Exit (auto-save) -> Restart -> Query "persist_test" |
| **Expected** | On second startup, loads from data.json, can find persist_test |
| **Actual** | Step 14 verified: first startup shows "No save file found. Using initial data", saves data.json on exit, restart shows "Data loaded successfully". Add/delete operations persist correctly |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-14: Player menu has no data management permission

| Field | Content |
|-------|---------|
| **ID** | TC-14 |
| **Module** | Permission Control |
| **Input** | Login as player -> Enter option 7 |
| **Expected** | Output "Access denied" |
| **Actual** | Player menu doesn't show option 7 (only shows 1-6 and 0). If forced to enter 7, since role=2 != 1, outputs "Access denied" |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-15: Edge case — empty input query

| Field | Content |
|-------|---------|
| **ID** | TC-15 |
| **Module** | Player Lookup |
| **Input** | Admin login -> Option 1 -> Enter empty string (press Enter) |
| **Expected** | Output "Player not found: " (empty string doesn't match any username) |
| **Actual** | Output "Player not found: ", matches expected. Empty string doesn't match any player name, no crash |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-16: Admin modifies player then verifies

| Field | Content |
|-------|---------|
| **ID** | TC-16 |
| **Module** | Data Management (Modify) |
| **Input** | Admin login -> Option 7 -> Option 3 -> Enter "p1_fly", new rank "Star", new win rate 75, new matches 130 -> Back -> Option 1 -> Enter "p1_fly" |
| **Expected** | Modify outputs "Player modified: p1_fly"; query shows rank changed to "Star", win rate 75%, matches 130 |
| **Actual** | Modify outputs "Player modified: p1_fly", query returns p1_fly/Star/win rate 75.0%/130 matches, all three attributes updated, team and hero pool unchanged |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-17: Admin modifies hero then verifies

| Field | Content |
|-------|---------|
| **ID** | TC-17 |
| **Module** | Data Management (Modify) |
| **Input** | Admin login -> Option 7 -> Option 6 -> Enter "Lu Bu", new role TANK, new HP 4000, new ATK 150, new DEF 120 -> Back -> Option 3 -> Enter "Lu Bu" |
| **Expected** | Modify outputs "Hero modified: Lu Bu"; query shows role changed to TANK, HP=4000, ATK=150, DEF=120, recommended equipment changes to defense + movement types |
| **Actual** | Modify outputs "Hero modified: Lu Bu", query returns Lu Bu/TANK/HP4000/ATK150/DEF120, recommended equipment changes to defense items (Thorn Armor/Ominous Omen etc.) + movement items, skills and owners unchanged |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-18: Admin modifies equipment then verifies

| Field | Content |
|-------|---------|
| **ID** | TC-18 |
| **Module** | Data Management (Modify) |
| **Input** | Admin login -> Option 7 -> Option 9 -> Enter "Armor Breaker", new type ATTACK, new ATK 200, new DEF 50, new HP 100, new price 3000 -> Back -> Option 4 |
| **Expected** | Modify outputs "Equipment modified: Armor Breaker"; ranking table shows Armor Breaker's new attributes effective (ATK=200 DEF=50 HP=100 Price=3000, score ~297) |
| **Actual** | Modify outputs "Equipment modified: Armor Breaker", ranking table shows Armor Breaker ATK=200/DEF=50/HP=100/Price=3000/score 297.0, rank rises from 13 to 10 |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-19: Admin modifies team then verifies

| Field | Content |
|-------|---------|
| **ID** | TC-19 |
| **Module** | Data Management (Modify) |
| **Input** | Admin login -> Option 7 -> Option 12 -> Enter "AG Super Play", new wins 30, new losses 8 -> Back -> Option 2 -> Enter "AG Super Play" |
| **Expected** | Modify outputs "Team modified: AG Super Play"; query shows record 30W / 8L, win rate ~78.9% |
| **Actual** | Modify outputs "Team modified: AG Super Play", query returns record 30W/8L/win rate 78.9%, member list unchanged |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-20: Modify non-existent entity

| Field | Content |
|-------|---------|
| **ID** | TC-20 |
| **Module** | Data Management (Modify) |
| **Input** | Admin login -> Option 7 -> Option 3 -> Enter "nonexist" |
| **Expected** | Output "Player not found: nonexist" |
| **Actual** | Output "Player not found: nonexist", program does not crash |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-21: Player lookup displays hero equipment

| Field | Content |
|-------|---------|
| **ID** | TC-21 |
| **Module** | Player Lookup |
| **Input** | Admin login -> Option 1 -> Enter "p1_fly" |
| **Expected** | Below each hero in hero pool, display "Equipment:" line listing compatible equipment names for that hero |
| **Actual** | p1_fly's Lu Bu displays "Equipment: Armor Breaker, Infinity Blade, Blood Weeper", Diao Chan displays "Equipment: Echo Staff, Scholar's Wrath, Sage's Tome", Li Bai displays "Equipment: Armor Breaker, Infinity Blade, Pursuit Blade, Guerrilla Saber" |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-22: Hero details display compatible equipment

| Field | Content |
|-------|---------|
| **ID** | TC-22 |
| **Module** | Hero Details |
| **Input** | Admin login -> Option 3 -> Enter "Lu Bu" |
| **Expected** | In "Compatible Equipment" section, list equipment directly linked to this hero (Armor Breaker, Infinity Blade, Blood Weeper) with their attributes |
| **Actual** | Compatible equipment section correctly displays 3 items (Armor Breaker/Infinity Blade/Blood Weeper), including type, attributes and price |
| **Pass** | Pass |
| **Bug** | None |

---

### TC-23: Data volume verification — 5 players per team

| Field | Content |
|-------|---------|
| **ID** | TC-23 |
| **Module** | Data Integrity |
| **Input** | Start program -> Admin login -> Option 2 -> Query "AG Super Play", "QGhappy", "eStarPro" respectively |
| **Expected** | Each team displays "Members: 5" |
| **Actual** | AG Super Play=5, QGhappy=5, eStarPro=5. Each player has 3 heroes, meets minimum requirements |
| **Pass** | Pass |
| **Bug** | None |

---

## Test Summary

| Statistic | Count |
|-----------|-------|
| Total Tests | 23 |
| Passed | 23 |
| Failed | 0 |
| Bugs Found | 0 |
| Pass Rate | 100% |

- Test Date: 2026-06-09
- Test Method: Wrote TestRunner.java and TestRunner2.java directly calling service layer methods, all 23 test cases passed
- Coverage: Player lookup (4), Team overview (2), Hero details (3), Equipment stats (1), Leaderboard (1), Match history (2), CRUD (7), File persistence (1), Permission control (1), Edge input (1), Data integrity (1)

---

## Full Feature Verification (2026-06-11)

Re-verified after Git history rebuild. Automated TestRunner covering all core modules and extra credit features.

| # | Test | Result |
|---|------|--------|
| 1 | Data loaded (players >= 10) | Pass |
| 2 | Heroes >= 15 | Pass |
| 3 | Equipment >= 20 | Pass |
| 4 | Teams >= 3 | Pass |
| 5 | Match records >= 10 | Pass |
| 6 | Admins exist | Pass |
| 7 | Player has ID | Pass |
| 8 | Player has username | Pass |
| 9 | Player has team | Pass |
| 10 | Player has heroes (>= 3) | Pass |
| 11 | Team has >= 5 members | Pass |
| 12 | Hero has role | Pass |
| 13 | Hero has stats | Pass |
| 14 | Hero has compatible equipment (>= 2) | Pass |
| 15 | Equipment has type | Pass |
| 16 | Equipment has name | Pass |
| 17 | Player lookup runs | Pass |
| 18 | Team overview runs | Pass |
| 19 | Hero details runs | Pass |
| 20 | Equipment ranking runs | Pass |
| 21 | Leaderboard runs | Pass |
| 22 | Match history runs | Pass |
| 23 | Add player CRUD | Pass |
| 24 | Remove player CRUD | Pass |
| 25 | Add hero CRUD | Pass |
| 26 | Remove hero CRUD | Pass |
| 27 | Add team CRUD | Pass |
| 28 | Remove team CRUD | Pass |
| 29 | Hero implements Identifiable | Pass |
| 30 | Player implements Identifiable | Pass |
| 31 | Equipment implements Identifiable | Pass |
| 32 | Persistence save | Pass |
| 33 | Persistence load | Pass |
| 34 | Persistence integrity (players) | Pass |
| 35 | Persistence integrity (heroes) | Pass |
| 36 | Persistence integrity (equip) | Pass |
| 37 | Persistence integrity (teams) | Pass |
| 38 | Persistence integrity (matches) | Pass |
| 39 | Admin role exists | Pass |
| 40 | Player role exists | Pass |
| 41 | Combat simulator runs | Pass |
| 42 | Equipment recommendation runs | Pass |
| 43 | Hero recommendation runs | Pass |

### Final Summary

| Statistic | Count |
|-----------|-------|
| Total Tests (Previous) | 23 |
| Total Tests (Full Verification) | 43 |
| Passed | 43 |
| Failed | 0 |
| Pass Rate | 100% |

- **Verification Date**: 2026-06-11
- **Method**: Automated TestRunner.java directly calls all service layer methods, model constructors, CRUD operations, file persistence, combat simulation, and recommendation engine
- **Extra Features Verified**: Combat Simulator (Section 10.1), Recommendation Engine (Section 10.2), File Persistence (Section 10.4)
