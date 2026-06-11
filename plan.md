# plan.md - Honor of Kings Information Management System

## 1. Project Goal
Build a Java OOP-based Honor of Kings information management system that provides players and administrators with management and query functionality for heroes, equipment, and match data.

## 2. Requirement Analysis
The system must implement eight core functional modules: player lookup, team overview, hero details, equipment statistics, match history, leaderboard, data management, and authentication.

## 3. Java Concepts Used
The project uses inheritance (Person→Player/Admin), interface (Identifiable, implemented by all model classes), polymorphism (List<Identifiable> for generic queries), collections (ArrayList, List, Stream API), exception handling (try-with-resources), file I/O (ObjectOutputStream/ObjectInputStream serialization), enums (HeroRole, EquipmentType, Role), and abstract class (Person).

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
Initial dataset contains 15 players (5 per team), 15 heroes (each linked to 2-4 compatible equipment items), 20 equipment items, 3 teams, and 10 match records. Data is persisted via Java serialization (ObjectOutputStream) to data.ser file. On startup, loads from file first; falls back to DataInitializer hard-coded data on failure.

## 7. AI Usage Plan
Use Architect Agent for class design, Implementation Agent for specific methods, Testing Agent for bug finding.

## 8. Prompt Strategy
Each prompt will specify the Agent role, task scope, and expected output format, aiming to make AI output verifiable and traceable.

## 9. Development Timeline
The project progresses through 8 stages, from requirements analysis to final documentation submission (estimated 1 week).

## 10. Testing Plan
Manual test cases written for each core function, recorded in docs/test-cases.md. Executed 23 manual test cases covering all 8 functional modules plus permission control and edge input, 100% pass rate. Full automated verification (43 tests) covering core modules, CRUD, persistence, Identifiable interface, combat simulator, and recommendation engine — all passed 43/43.

## 11. Risk Analysis
Key risks include uncontrollable AI-generated code quality and debugging time exceeding expectations (especially AI hallucinations). These will be mitigated through incremental verification and iterative fixes.

## 12. Final Reflection Placeholder
See ai/reflection.md

## 13. Extra Credit Features
| Feature | Implementation | Description |
|---------|---------------|-------------|
| 10.1 Combat Simulation | `CombatSimulator.java` | Turn-based combat: damage formula max(1, ATK - DEF*0.6) ±5, 15% crit (×1.5), 10% dodge, equipment bonuses affect probabilities, full battle log |
| 10.2 Recommendation Engine | `RecommendationService.java` | Equipment recommendation: role-adjusted scoring weights (Warrior ATK×1.5/Tank DEF×1.5); Hero recommendation: role gap analysis to suggest missing-role heroes |
| 10.3 GUI | `GameGUI.java` | Swing JTabbedPane, 7 tabs covering all features (player/team/hero/equipment/leaderboard/combat/recommend), System.out redirected to JTextArea |
| 10.4 Data Persistence | `FilePersistence.java` | Java serialization to data.ser, auto-load on startup |
