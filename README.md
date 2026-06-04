# AI-Assisted Honor of Kings Information Management System

## 1. Project Overview
A Java OOP-based Honor of Kings information management system supporting player lookup, team overview, hero details, equipment statistics, match history, leaderboard, data management, and authentication. Developed with AI assistance, with all AI usage and decisions strictly documented.

## 2. How to Run

Compile:
```bash
javac -encoding UTF-8 -d out -sourcepath src src/Main.java
```

Run (console):
```bash
java -cp out Main
```
Select mode 1 for console interface.

Run (GUI):
```bash
java -cp out Main
```
Select mode 2 to launch Swing GUI.

## 3. Default Login Accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | admin1 | admin |
| Admin | admin2 | admin |
| Player | p1_fly ~ p15_orange | 123 |

## 4. Implemented Features

| Module | Function | Permission |
|--------|----------|------------|
| Player Lookup | Search by username, displays rank/win rate/team/hero pool (with skills and equipment) | All |
| Team Overview | Search by team name, displays record/win rate/members/average rank/top player | All |
| Hero Details | Search by hero name, displays attributes/skills/compatible equipment/recommended equipment/owners | All |
| Equipment Stats | Display all equipment ranked by composite score | All |
| Match History | Query last 5 matches by player or team | All |
| Leaderboard | Rank all players by composite score | All |
| Data Management | Add/delete/modify players/heroes/equipment/teams/match records | Admin |
| Authentication | Admin/Player dual-role login with permissions | All |
| File Persistence | Load data.ser on startup, auto-save on exit (Java serialization) | Auto |
| Interface | Identifiable interface implemented by all model classes | — |
| Combat Simulation | Turn-based battle with crit/dodge/equipment bonuses | GUI+Console |
| Recommendation Engine | Equipment recommendation (role-adjusted weights) + Hero recommendation (role gap analysis) | GUI |
| GUI | Swing graphical interface with 7 tabs | Mode 2 |

## 5. Java Concepts Used

| Concept | Usage |
|---------|-------|
| Inheritance | Person → Player, Person → Admin |
| Interface | Identifiable (Person/Hero/Equipment/Team/MatchRecord implement it) |
| Polymorphism | List references storing subclass objects, interface references |
| Encapsulation | All fields private + getters/setters |
| Collections | ArrayList, List, Stream API |
| Exception Handling | try-with-resources, file I/O exception catching |
| File I/O | ObjectOutputStream/ObjectInputStream serialization |
| Enums | HeroRole, EquipmentType, Role |
| Abstract Class | Person (abstract base class) |

## 6. AI Usage Summary

- **AI Tool**: Trae IDE (AI Coding Assistant)
- **Model**: Deepseek-V4-Pro
- **Prompt Records**: 19 entries (see ai/prompts.md)
- **Agent Roles**: Architect (3), Implementation (10), Testing/Reviewer (6)
- **Adoption Strategy**: AI-generated code verified manually, edges adjusted, data tuned; all code underwent human review

## 7. Testing Summary

- **Test File**: docs/test-cases.md
- **Test Count**: 23 cases
- **Pass Rate**: 100%
- **Coverage**: Player lookup(4), Team overview(2), Hero details(3), Equipment stats(1), Leaderboard(1), Match history(2), CRUD(7), File persistence(1), Permissions(1), Edge input(1), Data integrity(1)
- **Automated Verification**: FullTest.java — 55/55 assertions passed

## 8. Known Limitations

1. Login system has no password verification (role selection only)
2. Admin modify does not support editing hero skills or recommended equipment
3. Match history participating heroes shown as first hero in pool (not actual pick)
4. Serialized saves not backward-compatible after class structure changes
5. Console display may have Chinese rendering issues in non-UTF-8 terminals
