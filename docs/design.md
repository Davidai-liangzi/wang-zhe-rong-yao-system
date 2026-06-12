# design.md - Design Document

## Architecture Overview

This project is a Honor of Kings information management system using a layered architecture:
- **model layer**: Data model classes (Person, Player, Admin, Hero, Equipment, Team, MatchRecord)
- **service layer**: Business logic classes (DataInitializer, DataManager, SearchService, FilePersistence, JsonPersistence, CombatSimulator, RecommendationService)
- **presentation layer**: Main.java console menu / GameGUI.java Swing interface

## Class Diagram

```
<<interface>>        <<abstract>>
  Identifiable          Person
  +getId():String       +getId():String
  +getName():String     +getName():String
         ^              +getUsername():String
         |              +getRole():Role
  ┌──────┼──────┐          ^
  │      │      │          |
  ▼      ▼      ▼    ┌────┴────┐
 Hero Equipment Team  Player   Admin
                         |
                    1    * (heroPool)
                    ┌───┴───┐
                    │  Hero  │
                    │  *     │ (compatibleEquipments)
                    │  ┌────┐│
                    └──┤ EQ ├┘
                       └────┘

  Team
  ├── 1..* Player (members)
  └── linked to MatchRecord (teamA/teamB)
```

## Interface Design

### Identifiable
```java
public interface Identifiable {
    String getId();
    String getName();
}
```
All core entity classes (Person and its subclasses, Hero, Equipment, Team, MatchRecord) implement this interface, providing unified ID and name access.

## Inheritance Hierarchy

```
Person (abstract, implements Identifiable)
  ├── Player (adds hero pool, rank, win rate, team association)
  └── Admin (adds admin level)
```

## Data Flow

```
Startup → Persistable.load() (JsonPersistence) → load GameData on success
                              └──→ DataInitializer.initAll() on failure
                                     ↓
                              Main.java menu loop
                              ┌──────────────────┐
                              │ SearchService     │ query/stats
                              │ DataManager       │ CRUD
                              └──────────────────┘
                                     ↓
Exit → Persistable.save() (JsonPersistence) → write to data.json
```

## Service Layer Architecture

### Responsibility Breakdown

| Class | Responsibility | Design Pattern |
|-------|---------------|----------------|
| `DataInitializer` | Hard-code initialization of all data (15 players, 15 heroes, 20 equipment, 3 teams, 10 matches), build complete GameData object graph | Factory (static factory method `initAll()`) |
| `SearchService` | All query functions: player lookup, team overview, hero details, equipment ranking, match history, leaderboard. Uses Stream API for filtering and sorting | Facade (unified query entry point) |
| `DataManager` | Admin CRUD operations: covers players, heroes, equipment, teams, match records. Operates on in-memory GameData, returns operation result messages | Command (each operation is an independent static method) |
| `FilePersistence` | JSON persistence facade: delegates to `Persistable` interface (JsonPersistence). `loadData()` reads from data.json, `saveData()` writes to data.json. Graceful degradation on error | Adapter (adapts object graph to JSON) |
| `CombatSimulator` | Turn-based battle: hero stats + equipment, damage formula, crit/dodge with equipment modifiers, full battle log | Strategy |
| `RecommendationService` | Equipment recommendation (role-adjusted scoring) + Hero recommendation (role gap analysis) | Strategy |

### Input/Output Contracts

| Method | Input | Output | Side Effects |
|--------|-------|--------|--------------|
| `SearchService.findPlayerByName` | GameData + player name | Print to console | None (read-only) |
| `SearchService.findTeamByName` | GameData + team name | Print to console | None (read-only) |
| `SearchService.findHeroByName` | GameData + hero name | Print to console | None (read-only) |
| `SearchService.showEquipmentRanking` | GameData | Print ranking table | None (read-only) |
| `SearchService.showLeaderboard` | GameData | Print leaderboard | None (read-only) |
| `SearchService.showMatchHistory` | GameData + player/team name | Print last 5 matches | None (read-only) |
| `DataManager.add*/remove*/modify*` | GameData + entity attributes | Print success/failure | Modifies GameData lists |
| `FilePersistence.loadData` | None (read file) | GameData or null | None |
| `FilePersistence.saveData` | GameData | None | Writes data.json file |

### Design Principles

1. **Read-Write Separation**: All SearchService methods never modify GameData; DataManager only modifies, never queries
2. **Static Methods Preferred**: All service methods are static, passing GameData as parameter to avoid global state
3. **Console Output as "Return"**: To simplify the assignment, query results are printf'd directly rather than returning DTO objects
4. **Orchestration in Main.java**: Menu loop, input parsing, permission checks all in Main.java; service layer does not handle I/O formatting

## Key Design Decisions

1. **Interface Introduction**: Added Identifiable interface to give all entity classes unified id/name access, supporting future generic queries
2. **Hero-Equipment Association**: Hero model directly linked to compatibleEquipments list, displayed in both player lookup and hero details
3. **Data Expansion**: 5 players per team, 3 heroes per player, meeting assignment minimum requirements with richer data
4. **ID System**: Unified P/H/E/T/M prefix + sequence number format, making management and referencing easier
5. **JSON Persistence**: JSON-based custom serialization approach, simple and reliable for this project's scale
