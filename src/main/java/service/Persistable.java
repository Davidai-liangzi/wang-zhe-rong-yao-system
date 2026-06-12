package service;

import model.GameData;

/**
 * Interface for data persistence (Section 10.4 Extra Credit).
 * Replaces Java serialization with structured data loading/saving.
 */
public interface Persistable {
    GameData load();
    void save(GameData data);
}
