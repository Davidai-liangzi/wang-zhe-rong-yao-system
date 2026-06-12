package model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Hero implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private HeroRole heroRole;
    private List<String> skills;
    private int hp;
    private int atk;
    private int def;
    private List<Equipment> compatibleEquipments;

    public Hero() {
        this.skills = new ArrayList<>();
        this.compatibleEquipments = new ArrayList<>();
    }

    public Hero(String id, String name, HeroRole heroRole, int hp, int atk, int def) {
        this.id = id;
        this.name = name;
        this.heroRole = heroRole;
        this.skills = new ArrayList<>();
        this.compatibleEquipments = new ArrayList<>();
        this.hp = hp;
        this.atk = atk;
        this.def = def;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public HeroRole getHeroRole() { return heroRole; }
    public void setHeroRole(HeroRole heroRole) { this.heroRole = heroRole; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }
    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }
    public List<Equipment> getCompatibleEquipments() { return compatibleEquipments; }
    public void setCompatibleEquipments(List<Equipment> compatibleEquipments) { this.compatibleEquipments = compatibleEquipments; }
}
