package model;

import java.util.Objects;

public class Hero implements Identifiable {
    private String id;
    private String name;
    private HeroRole heroRole;
    private java.util.List<String> skills;
    private int hp;
    private int atk;
    private int def;
    private java.util.List<Equipment> compatibleEquipments;

    public Hero() {
        this.skills = new java.util.ArrayList<>();
        this.compatibleEquipments = new java.util.ArrayList<>();
    }

    public Hero(String id, String name, HeroRole heroRole, int hp, int atk, int def) {
        this.id = id;
        this.name = name;
        this.heroRole = heroRole;
        this.skills = new java.util.ArrayList<>();
        this.compatibleEquipments = new java.util.ArrayList<>();
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
    public java.util.List<String> getSkills() { return skills; }
    public void setSkills(java.util.List<String> skills) { this.skills = skills; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }
    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }
    public java.util.List<Equipment> getCompatibleEquipments() { return compatibleEquipments; }
    public void setCompatibleEquipments(java.util.List<Equipment> compatibleEquipments) { this.compatibleEquipments = compatibleEquipments; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hero hero = (Hero) o;
        return Objects.equals(id, hero.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
