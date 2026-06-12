package service;

import model.GameData;

/**
 * File persistence facade — delegates to JsonPersistence.
 * Maintains backward-compatible static API for existing tests.
 * Section 10.4 Extra Credit: JSON persistence replaced Java serialization.
 */
public class FilePersistence {

    private static final Persistable persistence = new JsonPersistence();

    public static GameData loadData() {
        return persistence.load();
    }

    public static void saveData(GameData data) {
        persistence.save(data);
    }
}
