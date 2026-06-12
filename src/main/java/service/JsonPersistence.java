package service;

import model.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * JSON persistence implementing Persistable interface.
 * Replaces Java serialization with human-readable JSON files.
 * Uses ID-based references to avoid circular dependency issues.
 * Section 10.4 Extra Credit 鈥?Data Persistence in JSON format.
 */
public class JsonPersistence implements Persistable {

    private static final String FILE_PATH = "data.json";

    @Override
    public GameData load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("[Persistence] No save file found. Using initial data.");
            return null;
        }
        try {
            String text = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            return parseGameData(text);
        } catch (IOException e) {
            System.out.println("[Persistence] Load failed (" + e.getMessage() + "), using initial data.");
            return null;
        }
    }

    @Override
    public void save(GameData data) {
        if (data == null) {
            System.out.println("[Persistence] Save failed: null data.");
            return;
        }
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(FILE_PATH), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.write(toJson(data));
            System.out.println("[Persistence] Data saved to " + FILE_PATH);
        } catch (IOException e) {
            System.out.println("[Persistence] Save failed: " + e.getMessage());
        }
    }

    // ======================
    // JSON Serialization
    // ======================

    private String toJson(GameData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"players\": [\n");
        List<Player> players = data.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            sb.append(playerToJson(players.get(i)));
            if (i < players.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"admins\": [\n");
        List<Admin> admins = data.getAdmins();
        for (int i = 0; i < admins.size(); i++) {
            sb.append(adminToJson(admins.get(i)));
            if (i < admins.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"heroes\": [\n");
        List<Hero> heroes = data.getHeroes();
        for (int i = 0; i < heroes.size(); i++) {
            sb.append(heroToJson(heroes.get(i)));
            if (i < heroes.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"equipments\": [\n");
        List<Equipment> equips = data.getEquipments();
        for (int i = 0; i < equips.size(); i++) {
            sb.append(equipmentToJson(equips.get(i)));
            if (i < equips.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"teams\": [\n");
        List<Team> teams = data.getTeams();
        for (int i = 0; i < teams.size(); i++) {
            sb.append(teamToJson(teams.get(i)));
            if (i < teams.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"matchRecords\": [\n");
        List<MatchRecord> records = data.getMatchRecords();
        for (int i = 0; i < records.size(); i++) {
            sb.append(matchToJson(records.get(i)));
            if (i < records.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String playerToJson(Player p) {
        return String.format(
            "    {\"id\":%s,\"username\":%s,\"password\":%s,\"role\":%s,\"rank\":%s,\"winRate\":%.1f,\"matchesPlayed\":%d,\"teamId\":%s,\"heroIds\":%s}",
            quote(p.getId()), quote(p.getUsername()), quote(p.getPassword()),
            quote(p.getRole().name()), quote(p.getRank()),
            p.getWinRate(), p.getMatchesPlayed(),
            p.getTeam() != null ? quote(p.getTeam().getId()) : "null",
            stringListToJson(p.getHeroPool())
        );
    }

    private String adminToJson(Admin a) {
        return String.format(
            "    {\"id\":%s,\"username\":%s,\"password\":%s,\"role\":%s,\"adminLevel\":%s}",
            quote(a.getId()), quote(a.getUsername()), quote(a.getPassword()),
            quote(a.getRole().name()), quote(a.getAdminLevel())
        );
    }

    private String heroToJson(Hero h) {
        return String.format(
            "    {\"id\":%s,\"name\":%s,\"heroRole\":%s,\"hp\":%d,\"atk\":%d,\"def\":%d,\"skills\":%s,\"equipmentIds\":%s}",
            quote(h.getId()), quote(h.getName()), quote(h.getHeroRole().name()),
            h.getHp(), h.getAtk(), h.getDef(),
            strListToJson(h.getSkills()),
            stringListToJson(h.getCompatibleEquipments())
        );
    }

    private String equipmentToJson(Equipment e) {
        return String.format(
            "    {\"id\":%s,\"name\":%s,\"type\":%s,\"bonusAtk\":%d,\"bonusDef\":%d,\"bonusHp\":%d,\"price\":%d}",
            quote(e.getId()), quote(e.getName()), quote(e.getType().name()),
            e.getBonusAtk(), e.getBonusDef(), e.getBonusHp(), e.getPrice()
        );
    }

    private String teamToJson(Team t) {
        List<String> memberIds = new ArrayList<>();
        for (Player m : t.getMembers()) memberIds.add(m.getId());
        return String.format(
            "    {\"id\":%s,\"teamName\":%s,\"wins\":%d,\"losses\":%d,\"memberIds\":%s}",
            quote(t.getId()), quote(t.getTeamName()),
            t.getWins(), t.getLosses(),
            strListToJson(memberIds)
        );
    }

    private String matchToJson(MatchRecord m) {
        return String.format(
            "    {\"id\":%s,\"teamAId\":%s,\"teamBId\":%s,\"scoreA\":%d,\"scoreB\":%d,\"matchDate\":%s}",
            quote(m.getId()),
            quote(m.getTeamA().getId()), quote(m.getTeamB().getId()),
            m.getScoreA(), m.getScoreB(),
            quote(m.getMatchDate().toString())
        );
    }

    private String strListToJson(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(quote(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String stringListToJson(List<? extends Identifiable> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(quote(list.get(i).getId()));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String quote(String s) {
        if (s == null) return "null";
        return "\"" + escapeJson(s) + "\"";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // ======================
    // JSON Deserialization
    // ======================

    private GameData parseGameData(String text) {
        GameData data = new GameData();

        Map<String, Equipment> equipMap = new LinkedHashMap<>();
        Map<String, Hero> heroMap = new LinkedHashMap<>();
        Map<String, Team> teamMap = new LinkedHashMap<>();
        Map<String, Player> playerMap = new LinkedHashMap<>();

        // Parse equipments first (no dependencies)
        String equipSection = extractSection(text, "equipments");
        List<Map<String, String>> equipObjs = parseArray(equipSection);
        for (Map<String, String> obj : equipObjs) {
            Equipment e = new Equipment(
                obj.get("id"), obj.get("name"),
                EquipmentType.valueOf(obj.get("type")),
                parseInt(obj.get("bonusAtk")), parseInt(obj.get("bonusDef")),
                parseInt(obj.get("bonusHp")), parseInt(obj.get("price"))
            );
            data.addEquipment(e);
            equipMap.put(e.getId(), e);
        }

        // Parse heroes
        String heroSection = extractSection(text, "heroes");
        List<Map<String, String>> heroObjs = parseArray(heroSection);
        for (Map<String, String> obj : heroObjs) {
            Hero h = new Hero(
                obj.get("id"), obj.get("name"),
                HeroRole.valueOf(obj.get("heroRole")),
                parseInt(obj.get("hp")), parseInt(obj.get("atk")), parseInt(obj.get("def"))
            );
            // Parse skills
            h.setSkills(parseStringArray(obj.get("skills")));
            // Link equipment
            List<String> eqIds = parseStringArray(obj.get("equipmentIds"));
            for (String eqId : eqIds) {
                Equipment eq = equipMap.get(eqId);
                if (eq != null) h.getCompatibleEquipments().add(eq);
            }
            data.addHero(h);
            heroMap.put(h.getId(), h);
        }

        // Parse teams (before players for member linking)
        String teamSection = extractSection(text, "teams");
        List<Map<String, String>> teamObjs = parseArray(teamSection);
        for (Map<String, String> obj : teamObjs) {
            Team t = new Team(
                obj.get("id"), obj.get("teamName"),
                parseInt(obj.get("wins")), parseInt(obj.get("losses"))
            );
            data.addTeam(t);
            teamMap.put(t.getId(), t);
        }

        // Parse players
        String playerSection = extractSection(text, "players");
        List<Map<String, String>> playerObjs = parseArray(playerSection);
        for (Map<String, String> obj : playerObjs) {
            Team team = teamMap.get(obj.get("teamId"));
            Player p = new Player(
                obj.get("id"), obj.get("username"), obj.get("password"),
                Role.valueOf(obj.get("role")), obj.get("rank"),
                parseDouble(obj.get("winRate")), parseInt(obj.get("matchesPlayed")),
                team
            );
            // Link heroes
            List<String> heroIds = parseStringArray(obj.get("heroIds"));
            for (String hid : heroIds) {
                Hero h = heroMap.get(hid);
                if (h != null) p.getHeroPool().add(h);
            }
            data.addPlayer(p);
            playerMap.put(p.getId(), p);
        }

        // Link team members
        for (int i = 0; i < teamObjs.size(); i++) {
            Map<String, String> obj = teamObjs.get(i);
            Team t = data.getTeams().get(i);
            List<String> memberIds = parseStringArray(obj.get("memberIds"));
            for (String mid : memberIds) {
                Player player = playerMap.get(mid);
                if (player != null) t.getMembers().add(player);
            }
        }

        // Parse admins
        String adminSection = extractSection(text, "admins");
        List<Map<String, String>> adminObjs = parseArray(adminSection);
        for (Map<String, String> obj : adminObjs) {
            Admin a = new Admin(
                obj.get("id"), obj.get("username"), obj.get("password"),
                Role.valueOf(obj.get("role")), obj.get("adminLevel")
            );
            data.addAdmin(a);
        }

        // Parse match records
        String matchSection = extractSection(text, "matchRecords");
        List<Map<String, String>> matchObjs = parseArray(matchSection);
        for (Map<String, String> obj : matchObjs) {
            Team ta = teamMap.get(obj.get("teamAId"));
            Team tb = teamMap.get(obj.get("teamBId"));
            if (ta != null && tb != null) {
                MatchRecord m = new MatchRecord(
                    obj.get("id"), ta, tb,
                    parseInt(obj.get("scoreA")), parseInt(obj.get("scoreB")),
                    LocalDate.parse(obj.get("matchDate"))
                );
                data.addMatchRecord(m);
            }
        }

        System.out.println("[Persistence] Data loaded successfully.");
        return data;
    }

    // ======================
    // JSON Parser Helpers
    // ======================

    private String extractSection(String json, String key) {
        String search = "\"" + key + "\"";
        int start = json.indexOf(search);
        if (start < 0) return "[]";
        start = json.indexOf('[', start);
        int bracketCount = 0;
        int end = start;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '[') bracketCount++;
            if (json.charAt(i) == ']') {
                bracketCount--;
                if (bracketCount == 0) { end = i + 1; break; }
            }
        }
        return json.substring(start, end);
    }

    private List<Map<String, String>> parseArray(String arrText) {
        List<Map<String, String>> result = new ArrayList<>();
        arrText = arrText.trim();
        if (!arrText.startsWith("[")) return result;
        // Remove outer brackets
        arrText = arrText.substring(1, arrText.length() - 1).trim();
        if (arrText.isEmpty()) return result;

        // Split by top-level objects
        int depth = 0;
        boolean inString = false;
        int objStart = -1;
        for (int i = 0; i < arrText.length(); i++) {
            char c = arrText.charAt(i);
            if (c == '"' && (i == 0 || arrText.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) continue;
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            }
            if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    result.add(parseObject(arrText.substring(objStart, i + 1)));
                    objStart = -1;
                }
            }
        }
        return result;
    }

    private Map<String, String> parseObject(String objText) {
        Map<String, String> result = new LinkedHashMap<>();
        objText = objText.trim();
        if (!objText.startsWith("{") || !objText.endsWith("}")) return result;
        objText = objText.substring(1, objText.length() - 1).trim();

        int i = 0;
        while (i < objText.length()) {
            // Skip whitespace and commas
            while (i < objText.length() && (objText.charAt(i) == ' ' || objText.charAt(i) == ',')) i++;
            if (i >= objText.length()) break;
            // Expect key
            if (objText.charAt(i) != '"') break;
            i++;
            StringBuilder key = new StringBuilder();
            while (i < objText.length()) {
                char c = objText.charAt(i);
                if (c == '\\') { i++; if (i < objText.length()) { key.append(objText.charAt(i)); i++; } continue; }
                if (c == '"') { i++; break; }
                key.append(c);
                i++;
            }
            // Skip colon
            while (i < objText.length() && (objText.charAt(i) == ' ' || objText.charAt(i) == ':')) i++;
            // Parse value
            String value = parseJsonValue(objText, i);
            result.put(key.toString(), value);
            if (value.equals("null")) {
                i += 4;
            } else if (value.isEmpty()) {
                break;
            } else {
                i += countValueChars(objText, i);
            }
        }
        return result;
    }

    private String parseJsonValue(String text, int start) {
        if (start >= text.length()) return "";
        char c = text.charAt(start);
        if (c == '"') {
            // String value
            StringBuilder sb = new StringBuilder();
            int i = start + 1;
            while (i < text.length()) {
                char ch = text.charAt(i);
                if (ch == '\\') { i++; if (i < text.length()) { sb.append(text.charAt(i)); i++; } continue; }
                if (ch == '"') break;
                sb.append(ch);
                i++;
            }
            return sb.toString();
        } else if (c == '[') {
            // Array value - return raw text between [ and ]
            int depth = 1;
            int i = start + 1;
            while (i < text.length() && depth > 0) {
                if (text.charAt(i) == '[') depth++;
                if (text.charAt(i) == ']') depth--;
                i++;
            }
            return text.substring(start, i);
        } else if (c == '{') {
            int depth = 1;
            int i = start + 1;
            boolean inStr = false;
            while (i < text.length() && depth > 0) {
                char ch = text.charAt(i);
                if (ch == '"' && (i == 0 || text.charAt(i - 1) != '\\')) inStr = !inStr;
                if (!inStr) {
                    if (ch == '{') depth++;
                    if (ch == '}') depth--;
                }
                i++;
            }
            return text.substring(start, i);
        } else {
            // Number or null
            StringBuilder sb = new StringBuilder();
            int i = start;
            while (i < text.length() && text.charAt(i) != ',' && text.charAt(i) != '}' && text.charAt(i) != ']') {
                sb.append(text.charAt(i));
                i++;
            }
            return sb.toString().trim();
        }
    }

    private int countValueChars(String text, int start) {
        if (start >= text.length()) return 0;
        char c = text.charAt(start);
        if (c == '"') {
            int count = 1; // opening quote
            int i = start + 1;
            while (i < text.length()) {
                if (text.charAt(i) == '\\') { count += 2; i += 2; continue; }
                count++;
                if (text.charAt(i) == '"') break;
                i++;
            }
            return count;
        } else if (c == '[' || c == '{') {
            int depth = 1;
            int i = start + 1;
            boolean inStr = false;
            while (i < text.length() && depth > 0) {
                char ch = text.charAt(i);
                if (ch == '"' && (i == 0 || text.charAt(i - 1) != '\\')) inStr = !inStr;
                if (!inStr) {
                    if (ch == '[' || ch == '{') depth++;
                    if (ch == ']' || ch == '}') depth--;
                }
                i++;
            }
            return i - start;
        } else {
            int i = start;
            while (i < text.length() && text.charAt(i) != ',' && text.charAt(i) != '}' && text.charAt(i) != ']') i++;
            return i - start;
        }
    }

    private List<String> parseStringArray(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isEmpty() || raw.equals("null") || !raw.startsWith("[")) return result;
        // Simple: split by commas, strip quotes and spaces
        raw = raw.substring(1, raw.length() - 1).trim();
        if (raw.isEmpty()) return result;
        String[] parts = raw.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("\"") && part.endsWith("\"")) {
                part = part.substring(1, part.length() - 1);
            }
            if (!part.isEmpty()) result.add(part);
        }
        return result;
    }

    private int parseInt(String s) {
        if (s == null || s.isEmpty()) return 0;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    private double parseDouble(String s) {
        if (s == null || s.isEmpty()) return 0;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return 0; }
    }
}

