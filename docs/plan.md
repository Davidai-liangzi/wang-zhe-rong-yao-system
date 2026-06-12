# plan.md - Honor of Kings Information Management System

## 1. Project Goal
Build a Java OOP-based Honor of Kings information management system that provides players and administrators with management and query functionality for heroes, equipment, and match data.

## 2. Requirement Analysis
The system must implement eight core functional modules: player lookup, team overview, hero details, equipment statistics, match history, leaderboard, data management, and authentication.

## 3. Java Concepts Used
The project uses inheritance (Person→Player/Admin), interface (Identifiable, implemented by all model classes), polymorphism (List<Identifiable> for generic queries), collections (ArrayList, List, Stream API), exception handling (try-with-resources), file I/O (JSON read/write with Persistable interface), enums (HeroRole, EquipmentType, Role), and abstract class (Person).

## 4. Class Design

### Enums
| Enum | Values |
|------|--------|
| `HeroRole` | WARRIOR, MAGE, ASSASSIN, TANK, MARKSMAN, SUPPORT |
| `EquipmentType` | ATTACK, DEFENSE, MAGIC, MOVEMENT, JUNGLE |
| `Role` | PLAYER, ADMIN |

### Interface
| Interface | Methods | Implemented By |
|-----------|---------|----------------|
| `Identifiable` | `+getId():String`, `+getName():String` | Person, Hero, Equipment, Team, MatchRecord |

### Core Classes
| Class | Type | Core Fields | Main Methods |
|-------|------|-------------|--------------|
| **Person** | abstract, implements Identifiable | id:String, username:String, password:String, role:Role | +getId(), +getName(), +getUsername(), +getRole() |
| **Player** | extends Person | heroPool:List<Hero>, rank:String, winRate:double, matchesPlayed:int, team:Team | +getHeroPool(), +getWinRate(), +getTeam() |
| **Admin** | extends Person | adminLevel:String | +getAdminLevel() (management logic in DataManager) |
| **Hero** | standalone, implements Identifiable | id:String, name:String, heroRole:HeroRole, skills:List<String>, hp:int, atk:int, def:int, compatibleEquipments:List<Equipment> | +getSkills(), +getHp(), +getCompatibleEquipments() |
| **Equipment** | standalone, implements Identifiable | id:String, name:String, type:EquipmentType, bonusAtk:int, bonusDef:int, bonusHp:int, price:int | +getBonusAtk(), +getType(), +getPrice() |
| **Team** | standalone, implements Identifiable | id:String, teamName:String, members:List<Player>, wins:int, losses:int | +getMembers(), +getWinRate(), +getId() |
| **MatchRecord** | standalone, implements Identifiable | id:String, teamA:Team, teamB:Team, scoreA:int, scoreB:int, matchDate:LocalDate | +getTeamA(), +getTeamB(), +getMatchDate() (win/loss logic in SearchService) |

### Class Relationships
- **Inheritance**: Player → Person, Admin → Person
- **Aggregation**: Player owns Hero list (heroPool, 1 player can have multiple heroes)
- **Aggregation**: Team contains Player list (members, 1 team has multiple players)
- **Association**: Player belongs to a Team
- **Association**: MatchRecord associates two Teams (teamA, teamB)

## 5. UML Draft

```
     <<interface>>
    ┌──────────────┐
    │ Identifiable │
    ├──────────────┤
    │ + getId()    │
    │ + getName()  │
    └──────┬───────┘
           │ implements
    ┌──────┼──────────────────────────────────┐
    ▼      ▼                                  │
┌─────────────────────┐                       │
│    <<abstract>>     │                       │
│       Person        │                       │
├─────────────────────┤                       │
│ - id: String        │                       │
│ - username: String  │                       │
│ - password: String  │                       │
│ - role: Role        │                       │
├─────────────────────┤                       │
│ + getId()           │                       │
│ + getName()         │                       │
│ + getUsername()     │                       │
│ + getRole()         │                       │
└─────────┬───────────┘                       │
     ┌────┴────┐                               │
     ▼         ▼                               │
┌──────────┐ ┌──────────┐                      │
│  Player  │ │  Admin   │                      │
├──────────┤ ├──────────┤                      │
│ -heroPool│ │ -adminLvl│                      │
│ -rank    │ ├──────────┤                      │
│ -winRate │ │+getAdmin │                      │
│ -matches │ │ Level()  │                      │
│ -team    │ └──────────┘                      │
├──────────┤                                   │
│+getHero  │                                   │
│ Pool()   │                                   │
│+getWin   │                                   │
│ Rate()   │                                   │
│+getTeam()│                                   │
└────┬─────┘                                   │
     │ owns 1..*                               │
     ▼                                         │
┌──────────┐    compatible 1..*   ┌──────────┐ │
│   Hero   │─────────────────────>│Equipment │◄┘
├──────────┤                      ├──────────┤
│ -id      │                      │ -id      │
│ -name    │                      │ -name    │
│ -heroRole│                      │ -type    │
│ -skills[]│                      │ -bonusAtk│
│ -hp/atk  │                      │ -bonusDef│
│  /def    │                      │ -bonusHp │
│ -compat  │                      │ -price   │
│  Eq[]    │                      ├──────────┤
├──────────┤                      │+getId()  │
│+getId()  │                      │+getName()│
│+getName()│                      │+getBonus │
│+getSkills│                      │ Atk()    │
│+getComp  │                      │+getType()│
│ Eq()     │                      │+getPrice │
└──────────┘                      └──────────┘

┌──────────┐    ┌────────────────────────────┐
│   Team   │    │        MatchRecord         │
├──────────┤    ├────────────────────────────┤
│ -id      │    │ -id                        │
│ -teamName│    │ -teamA: Team               │
│ -members │    │ -teamB: Team               │
│  []      │    │ -scoreA                    │
│ -wins    │    │ -scoreB                    │
│ -losses  │    │ -matchDate                 │
├──────────┤    ├────────────────────────────┤
│+getId()  │    │+getId() +getName()         │
│+getName()│    │+getTeamA/B()               │
│+getMembers│   │+getMatchDate()             │
│+getWin   │    │ (win/loss in SearchService) │
│ Rate()   │    └────────────────────────────┘
└──────────┘
   1 has many (members)
```
**UML Notes:**
- `<<interface>> Identifiable` provides unified getId()/getName() access for all entity classes
- Person (abstract), Hero, Equipment, Team, MatchRecord all implement Identifiable
- Hero and Equipment have bidirectional association: Hero.compatibleEquipments references multiple Equipment objects
- Player aggregates Hero (heroPool), Team aggregates Player (members)
- MatchRecord associates two Teams (teamA/teamB)
- Admin business logic implemented in service-layer DataManager

## 6. Data Design
Initial dataset contains 15 players (5 per team), 15 heroes (each linked to 2-4 compatible equipment items), 20 equipment items, 3 teams, and 10 match records. Data is persisted via JSON to data.json file. On startup, loads from file first; falls back to DataInitializer hard-coded data on failure.

## 7. AI Usage Plan

Three AI agent roles will be used throughout development. The same underlying model (Deepseek-V4-Pro via Trae IDE) will be assigned different roles through system prompts, allowing each role to focus on its specific responsibility.

| Agent Role | Allowed To Help With | NOT Allowed To | How I Will Verify |
|------------|---------------------|----------------|-------------------|
| **Architect Agent** | Class structure design, UML suggestions, module planning, interface decisions, design pattern selection, service layer architecture | Writing full class implementations without my understanding | I will manually draw UML diagrams based on AI suggestions, and write class responsibilities in my own words in plan.md |
| **Implementation Agent** | Specific method implementations, collection operations, Stream API usage, file I/O code, exception handling blocks | Implementing entire modules without incremental review | Compile after every generated class with `javac`, run the method in isolation, verify edge cases manually |
| **Testing/Reviewer Agent** | Bug finding, code review, test case generation, edge case identification, mutation testing design | Fabricating test results or passing buggy code as correct | Run all tests myself, record actual output manually, fix bugs myself rather than asking AI to rewrite |

**Why three roles instead of one**: Using a single "code everything" prompt would produce unverified, monolithic code. Separating roles forces me to (a) understand the design before implementing, (b) verify each piece before integrating, and (c) critically review after implementation — mirroring real software engineering workflows.

## 8. Prompt Strategy

Each prompt will follow a structured format to maximize AI output quality and traceability:

**Prompt Structure**:
1. **Agent Role Declaration**: Begin with "You are a [Java Architect / Implementation / Testing] Agent" to set context
2. **Task Scope**: Specify exactly which class, method, or module — never "write the whole project"
3. **Input Context**: Provide existing class signatures, field names, and method contracts so AI doesn't guess
4. **Expected Output**: State the format — "Java code only", "text explanation", "UML diagram", or "test cases"
5. **Constraints**: List what NOT to do — e.g., "Do not rewrite unrelated code", "Do not change existing class names"

**Example of Strong vs Weak Prompts** (per Appendix B of requirement):

| Quality | Weak Prompt | Strong Prompt |
|---------|-------------|---------------|
| Implementation | "Code this." | "Implement only the player lookup method using the existing Player, Hero, and Team classes. Explain assumptions and edge cases. Do not rewrite unrelated code." |
| Debugging | "Fix my code." | "The following method crashes when searching an unknown hero. Identify the cause, explain it, and suggest a minimal fix. Do not rewrite unrelated code." |
| Review | "Is this good?" | "Review this Java class for OOP design, encapsulation, collection usage, and potential null pointer bugs. Give specific comments." |

**Verification Checklist for Every AI Output**:
- Does the code compile with `javac`?
- Does the code match the existing class structure (field names, method signatures)?
- Are edge cases handled (null, empty, not found)?
- Can I explain every line of the generated code?

**Prompt Recording**: Every prompt will be recorded in `ai/prompts.md` with date/time, model, agent role, the actual prompt text, a summary of AI's response, my decision (accepted/modified/rejected), and the related Git commit hash — meeting the full requirements of Section 6.2.

## 9. Development Timeline

Following the 8-stage workflow recommended in the coursework specification (Section 8.1):

| Stage | Tasks | Key Deliverables | Status |
|-------|-------|-----------------|--------|
| **Stage 1** | Read requirements, create Git repository, write first plan.md | Initial plan.md, project structure | Completed |
| **Stage 2** | Ask Architect Agent for class structure feedback; revise class design manually based on UML suggestions | UML class diagram, revised plan.md Section 4-5 | Completed |
| **Stage 3** | Implement model classes (Person, Player, Admin, Hero, Equipment, Team, MatchRecord), enums, Identifiable interface, and hard-coded initial data | All model classes + DataInitializer + GameData container | Completed |
| **Stage 4** | Implement console menu system and all search/query features (player lookup, team overview, hero details, equipment stats, match history, leaderboard) | SearchService.java, Main.java menu loop | Completed |
| **Stage 5** | Implement authentication (Admin/Player dual-role login with username+password, 3-attempt limit) and permission-based access control | Authentication in Main.java, permission checks | Completed |
| **Stage 6** | Implement file persistence (JSON) and ranking/leaderboard functions | JsonPersistence.java, data.json auto-save/load | Completed |
| **Stage 7** | Use Testing/Reviewer Agent to find bugs; write manual test cases; execute all tests; fix bugs and record decisions | test-cases.md (23 cases), TestRunner.java, BoundaryTest.java | Completed |
| **Stage 8** | Finish documentation (design.md, README.md), complete reflection.md, export Git history, conduct mutation testing (20 mutants), final compilation verification | All documentation files, mutation_result.txt, git-history.txt | Completed |

**Stage 7 detail — Testing workflow**:
1. Write 15 manual test cases covering all 8 functional modules
2. Execute tests via console interaction, record actual vs expected output
3. Write TestRunner.java for automated verification (113 assertions, 100% pass)
4. Write BoundaryTest.java with JUnit5 parameterized tests (180 boundary tests)
5. Execute mutation testing with 20 mutants — all 20 killed, proving test suite effectiveness
6. Fix any bugs found (e.g., Scanner newline issue, getName() missing in Person)

## 10. Testing Plan
Manual test cases written for each core function, recorded in docs/test-cases.md. Executed 23 manual test cases covering all 8 functional modules plus permission control and edge input, 100% pass rate. Full automated verification (43 tests) covering core modules, CRUD, persistence, Identifiable interface, combat simulator, and recommendation engine — all passed 43/43.

## 11. Risk Analysis

| Risk | Severity | Likelihood | Mitigation Strategy |
|------|----------|------------|---------------------|
| **AI generates code that compiles but is logically wrong** | High | Medium | Incremental compilation and manual testing after every AI response; never accept a large batch of code at once; verify edge cases independently |
| **AI hallucinates non-existent methods or APIs** | Medium | Medium | Immediately compile with `javac` after generation; if error, ask AI to fix or fix manually; always cross-reference with official Java documentation |
| **Scanner input buffer issues (newline left by nextInt)** | Medium | High | Add `sc.nextLine()` after every `nextInt()`/`nextDouble()` call; wrap input in try-catch for type mismatches |
| **Serialization backward-compatibility broken after class changes** | Low | Medium | Delete `data.json` when class structure changes; fall back to DataInitializer on deserialization failure (already implemented in JsonPersistence) |
| **Debugging AI-generated code takes longer than expected** | Medium | Medium | Time-box each debugging session to 30 minutes; if unresolved, commit the broken version with [Fix] prefix, switch to another feature, and return with fresh perspective |
| **Git history becomes messy with AI-generated commits mixed with human changes** | Low | Low | Strict commit message prefix discipline: [Human], [AI-Architect], [AI-Implementation], [AI-Review], [Test], [Fix], [Docs]; commit after each meaningful change, not in bulk |
| **Chinese character encoding issues in console output** | Low | Medium | Compile with `-encoding UTF-8`; document workaround in README; consider redirecting output to file for verification |
| **Over-reliance on AI leads to shallow understanding** | High | Medium | For every AI-generated class, write its responsibility and relationship in my own words in plan.md/design.md; ensure I can explain every class and method to the marker

## 12. Final Reflection Placeholder
See ai/reflection.md

## 13. Extra Credit Features
| Feature | Implementation | Description |
|---------|---------------|-------------|
| 10.1 Combat Simulation | `CombatSimulator.java` | Turn-based combat: damage formula max(1, ATK - DEF*0.6) ±5, 15% crit (×1.5), 10% dodge, equipment bonuses affect probabilities, full battle log |
| 10.2 Recommendation Engine | `RecommendationService.java` | Equipment recommendation: role-adjusted scoring weights (Warrior ATK×1.5/Tank DEF×1.5); Hero recommendation: role gap analysis to suggest missing-role heroes |
| 10.3 GUI | `GameGUI.java` | Swing JTabbedPane, 7 tabs covering all features (player/team/hero/equipment/leaderboard/combat/recommend), System.out redirected to JTextArea |
| 10.4 Data Persistence | `JsonPersistence.java` | JSON persistence to data.json, auto-load on startup |
