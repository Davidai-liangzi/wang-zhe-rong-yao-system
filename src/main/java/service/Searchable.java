package service;

import model.GameData;

/**
 * Interface for all search/query operations.
 * All methods return formatted String results instead of printing directly.
 * Section 3.2 Required Java Concepts: at least one meaningful interface.
 */
public interface Searchable {
    String findPlayerByName(GameData data, String name);
    String findTeamByName(GameData data, String name);
    String findHeroByName(GameData data, String name);
    String showEquipmentRanking(GameData data);
    String showMatchHistory(GameData data, String input);
    String showLeaderboard(GameData data);
}
