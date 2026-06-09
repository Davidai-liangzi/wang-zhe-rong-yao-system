package service;

import model.GameData;
import java.io.*;

/**
 * File persistence: load data.ser on startup, save on exit
 */
public class FilePersistence {

    private static final String FILE_PATH = "data.ser";

    /**
     * Load GameData from file, return null if file doesn't exist or is corrupted
     */
    public static GameData loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("[Persistence] No save file found. Using initial data.");
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            GameData data = (GameData) ois.readObject();
            System.out.println("[Persistence] Data loaded successfully.");
            return data;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[Persistence] Load failed (" + e.getMessage() + "), using initial data.");
            return null;
        }
    }

    /**
     * Save GameData to file
     */
    public static void saveData(GameData data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(data);
            System.out.println("[Persistence] Data saved to data.ser");
        } catch (IOException e) {
            System.out.println("[Persistence] Save failed: " + e.getMessage());
        }
    }
}
