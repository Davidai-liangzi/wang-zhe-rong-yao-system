package service;

import model.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataInitializer {

    public static GameData initAll() {
        GameData data = new GameData();

        // ========== 20 Equipment ==========
        List<Equipment> eqList = new ArrayList<>();
        eqList.add(new Equipment("E01", "Armor Breaker",    EquipmentType.ATTACK,  180, 0,   0,   2950));
        eqList.add(new Equipment("E02", "Infinity Blade",   EquipmentType.ATTACK,  130, 0,   0,   2140));
        eqList.add(new Equipment("E03", "Shadow Axe",       EquipmentType.ATTACK,  85,  0,   500, 2090));
        eqList.add(new Equipment("E04", "Master's Power",   EquipmentType.ATTACK,  80,  0,   500, 2100));
        eqList.add(new Equipment("E05", "Blood Weeper",     EquipmentType.ATTACK,  100, 0,   0,   1740));
        eqList.add(new Equipment("E06", "Thorn Armor",      EquipmentType.DEFENSE, 30,  360, 0,   1910));
        eqList.add(new Equipment("E07", "Ominous Omen",     EquipmentType.DEFENSE, 0,   270, 1200,2180));
        eqList.add(new Equipment("E08", "Red Lotus Cloak",  EquipmentType.DEFENSE, 0,   240, 1000,1830));
        eqList.add(new Equipment("E09", "Frozen Storm",     EquipmentType.DEFENSE, 0,   360, 0,   2100));
        eqList.add(new Equipment("E10", "Witch's Cloak",    EquipmentType.DEFENSE, 0,   200, 1000,2080));
        eqList.add(new Equipment("E11", "Echo Staff",       EquipmentType.MAGIC,   240, 0,   0,   2100));
        eqList.add(new Equipment("E12", "Scholar's Wrath",  EquipmentType.MAGIC,   240, 0,   0,   2300));
        eqList.add(new Equipment("E13", "Void Staff",       EquipmentType.MAGIC,   180, 0,   500, 2110));
        eqList.add(new Equipment("E14", "Sage's Tome",      EquipmentType.MAGIC,   400, 0,   0,   2990));
        eqList.add(new Equipment("E15", "Swift Boots",      EquipmentType.MOVEMENT,0,   0,   0,   530));
        eqList.add(new Equipment("E16", "Calm Boots",       EquipmentType.MOVEMENT,0,   0,   0,   710));
        eqList.add(new Equipment("E17", "Resist Boots",     EquipmentType.MOVEMENT,0,   110, 0,   710));
        eqList.add(new Equipment("E18", "Pursuit Blade",    EquipmentType.JUNGLE,  40,  0,   0,   750));
        eqList.add(new Equipment("E19", "Guerrilla Saber",  EquipmentType.JUNGLE,  40,  0,   0,   750));
        eqList.add(new Equipment("E20", "Giant's Grip",     EquipmentType.JUNGLE,  0,   0,   500, 1500));
        for (Equipment e : eqList) data.addEquipment(e);

        // ========== 15 Heroes (with skills + compatible equipment) ==========
        List<Hero> heroList = new ArrayList<>();
        Hero h = new Hero("H01", "Lu Bu", HeroRole.WARRIOR, 3500, 180, 100);
        h.getSkills().addAll(List.of("Sky Piercer","Wolf Grip","Demon Descent"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(0), eqList.get(1), eqList.get(4))); heroList.add(h);
        h = new Hero("H02", "Guan Yu", HeroRole.WARRIOR, 3400, 175, 95);
        h.getSkills().addAll(List.of("Lone Blade","Green Dragon","Blade Cavalry"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(0), eqList.get(2), eqList.get(4))); heroList.add(h);
        h = new Hero("H03", "Kai",   HeroRole.WARRIOR, 3300, 170, 90);
        h.getSkills().addAll(List.of("Spinning Blade","Blade Storm","Undying Body"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(1), eqList.get(3), eqList.get(4))); heroList.add(h);

        h = new Hero("H04", "Diao Chan", HeroRole.MAGE, 3100, 120, 80);
        h.getSkills().addAll(List.of("Falling Petals","Heart Lock","Blooming Grace"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(10), eqList.get(11), eqList.get(13))); heroList.add(h);
        h = new Hero("H05", "Angela", HeroRole.MAGE, 3000, 130, 75);
        h.getSkills().addAll(List.of("Fireball","Chaos Seed","Blazing Radiance"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(11), eqList.get(12), eqList.get(13))); heroList.add(h);
        h = new Hero("H06", "Ying Zheng", HeroRole.MAGE, 3050, 125, 78);
        h.getSkills().addAll(List.of("King's Punishment","King's Guard","Supreme Rule"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(10), eqList.get(11), eqList.get(12))); heroList.add(h);

        h = new Hero("H07", "Li Bai",   HeroRole.ASSASSIN, 3200, 200, 70);
        h.getSkills().addAll(List.of("Drinking Song","Divine Brush","Lotus Sword"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(0), eqList.get(1), eqList.get(17), eqList.get(18))); heroList.add(h);
        h = new Hero("H08", "Prince of Lanling", HeroRole.ASSASSIN, 3150, 195, 65);
        h.getSkills().addAll(List.of("Secret Art: Mastery","Secret Art: Clone","Secret Art: Stealth"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(1), eqList.get(2), eqList.get(17), eqList.get(18))); heroList.add(h);

        h = new Hero("H09", "Zhang Fei", HeroRole.TANK, 5000, 100, 150);
        h.getSkills().addAll(List.of("Cage","Guardian","Beast Rage"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(5), eqList.get(6), eqList.get(7), eqList.get(8))); heroList.add(h);
        h = new Hero("H10", "Lian Po", HeroRole.TANK, 4800, 95, 160);
        h.getSkills().addAll(List.of("Bold Charge","Burst","Justice Fist"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(6), eqList.get(7), eqList.get(9), eqList.get(8))); heroList.add(h);

        h = new Hero("H11", "Hou Yi",     HeroRole.MARKSMAN, 3000, 185, 75);
        h.getSkills().addAll(List.of("Scorching Wind","Arrow Rain","Punishing Shot"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(0), eqList.get(1), eqList.get(2))); heroList.add(h);
        h = new Hero("H12", "Luban No.7", HeroRole.MARKSMAN, 2900, 190, 70);
        h.getSkills().addAll(List.of("Blowfish Grenade","Shark Cannon","Air Support"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(1), eqList.get(3), eqList.get(4))); heroList.add(h);
        h = new Hero("H13", "Sun Shangxiang",   HeroRole.MARKSMAN, 2950, 188, 72);
        h.getSkills().addAll(List.of("Rolling Assault","Lotus Bomb","Ultimate Ballista"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(0), eqList.get(2), eqList.get(4))); heroList.add(h);

        h = new Hero("H14", "Cai Wenji", HeroRole.SUPPORT, 3300, 60, 90);
        h.getSkills().addAll(List.of("Pure Thoughts","Flute Melody","Worry-Free Song"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(6), eqList.get(9), eqList.get(15))); heroList.add(h);
        h = new Hero("H15", "Sun Bin",   HeroRole.SUPPORT, 3200, 55, 85);
        h.getSkills().addAll(List.of("Time Bomb","Time Wave","Time Passage"));
        h.getCompatibleEquipments().addAll(List.of(eqList.get(7), eqList.get(10), eqList.get(15))); heroList.add(h);
        for (Hero hero : heroList) data.addHero(hero);

        // ========== 3 Teams ==========
        Team teamAG = new Team("T01", "AG Super Play", 28, 10);
        Team teamQG = new Team("T02", "QGhappy",  25, 12);
        Team teamES = new Team("T03", "eStarPro",  22, 15);
        data.addTeam(teamAG);
        data.addTeam(teamQG);
        data.addTeam(teamES);

        // ========== 15 Players (5 per team, 3 heroes each) ==========
        Player p = new Player("P01", "p1_fly",  "123", Role.PLAYER, "King", 72.5, 120, teamAG);
        p.getHeroPool().addAll(List.of(heroList.get(0), heroList.get(3), heroList.get(6))); data.addPlayer(p);
        p = new Player("P02", "p2_jiucheng", "123", Role.PLAYER, "King", 68.3, 95,  teamAG);
        p.getHeroPool().addAll(List.of(heroList.get(3), heroList.get(4), heroList.get(1))); data.addPlayer(p);
        p = new Player("P03", "p3_yinuo", "123", Role.PLAYER, "Star", 65.0, 85,  teamAG);
        p.getHeroPool().addAll(List.of(heroList.get(10), heroList.get(11), heroList.get(6))); data.addPlayer(p);
        p = new Player("P04", "p10_menglei","123", Role.PLAYER, "King", 80.0, 150, teamAG);
        p.getHeroPool().addAll(List.of(heroList.get(6), heroList.get(7), heroList.get(1))); data.addPlayer(p);
        p = new Player("P05", "p11_nuanyang","123", Role.PLAYER, "Diamond", 57.2, 62,  teamAG);
        p.getHeroPool().addAll(List.of(heroList.get(0), heroList.get(7), heroList.get(2))); data.addPlayer(p);

        p = new Player("P06", "p4_hurt",  "123", Role.PLAYER, "King", 70.1, 105, teamQG);
        p.getHeroPool().addAll(List.of(heroList.get(10), heroList.get(11), heroList.get(12))); data.addPlayer(p);
        p = new Player("P07", "p5_cat",   "123", Role.PLAYER, "Star", 62.8, 78,  teamQG);
        p.getHeroPool().addAll(List.of(heroList.get(3), heroList.get(5), heroList.get(4))); data.addPlayer(p);
        p = new Player("P08", "p6_gemini","123", Role.PLAYER, "Diamond", 58.0, 60,  teamQG);
        p.getHeroPool().addAll(List.of(heroList.get(6), heroList.get(8), heroList.get(2))); data.addPlayer(p);
        p = new Player("P09", "p12_citong","123", Role.PLAYER, "Star", 63.5, 72,  teamQG);
        p.getHeroPool().addAll(List.of(heroList.get(11), heroList.get(13), heroList.get(0))); data.addPlayer(p);
        p = new Player("P10", "p13_feiniu","123", Role.PLAYER, "King", 74.0, 110, teamQG);
        p.getHeroPool().addAll(List.of(heroList.get(0), heroList.get(2), heroList.get(9))); data.addPlayer(p);

        p = new Player("P11", "p7_nuoyan", "123", Role.PLAYER, "King", 75.2, 130, teamES);
        p.getHeroPool().addAll(List.of(heroList.get(6), heroList.get(13), heroList.get(1))); data.addPlayer(p);
        p = new Player("P12", "p8_huahai",  "123", Role.PLAYER, "Star", 64.5, 88,  teamES);
        p.getHeroPool().addAll(List.of(heroList.get(11), heroList.get(10), heroList.get(7))); data.addPlayer(p);
        p = new Player("P13", "p9_wuhen",  "123", Role.PLAYER, "Diamond", 55.3, 55,  teamES);
        p.getHeroPool().addAll(List.of(heroList.get(2), heroList.get(8), heroList.get(13))); data.addPlayer(p);
        p = new Player("P14", "p14_zongqing","123", Role.PLAYER, "Star", 61.7, 70,  teamES);
        p.getHeroPool().addAll(List.of(heroList.get(5), heroList.get(14), heroList.get(4))); data.addPlayer(p);
        p = new Player("P15", "p15_juzi","123", Role.PLAYER, "Diamond", 56.8, 50,  teamES);
        p.getHeroPool().addAll(List.of(heroList.get(9), heroList.get(12), heroList.get(8))); data.addPlayer(p);

        // Add players to team members
        List<Player> playerList = data.getPlayers();
        for (int i = 0; i < 5; i++) teamAG.getMembers().add(playerList.get(i));
        for (int i = 5; i < 10; i++) teamQG.getMembers().add(playerList.get(i));
        for (int i = 10; i < 15; i++) teamES.getMembers().add(playerList.get(i));

        // ========== 2 Admins ==========
        data.addAdmin(new Admin("A01", "admin1", "admin", Role.ADMIN, "super"));
        data.addAdmin(new Admin("A02", "admin2", "admin", Role.ADMIN, "normal"));

        // ========== 10 Match Records ==========
        data.addMatchRecord(new MatchRecord("M01", teamAG, teamQG, 3, 1, LocalDate.of(2026,5,10)));
        data.addMatchRecord(new MatchRecord("M02", teamAG, teamES, 2, 3, LocalDate.of(2026,5,12)));
        data.addMatchRecord(new MatchRecord("M03", teamQG, teamES, 2, 2, LocalDate.of(2026,5,15)));
        data.addMatchRecord(new MatchRecord("M04", teamAG, teamQG, 3, 0, LocalDate.of(2026,5,18)));
        data.addMatchRecord(new MatchRecord("M05", teamES, teamAG, 1, 3, LocalDate.of(2026,5,20)));
        data.addMatchRecord(new MatchRecord("M06", teamQG, teamAG, 2, 3, LocalDate.of(2026,5,22)));
        data.addMatchRecord(new MatchRecord("M07", teamES, teamQG, 3, 1, LocalDate.of(2026,5,25)));
        data.addMatchRecord(new MatchRecord("M08", teamAG, teamES, 3, 2, LocalDate.of(2026,5,28)));
        data.addMatchRecord(new MatchRecord("M09", teamQG, teamES, 1, 3, LocalDate.of(2026,6,1)));
        data.addMatchRecord(new MatchRecord("M10", teamAG, teamQG, 2, 2, LocalDate.of(2026,6,5)));

        return data;
    }
}
