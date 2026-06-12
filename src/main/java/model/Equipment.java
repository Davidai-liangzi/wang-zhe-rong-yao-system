package model;

import java.util.Objects;

public class Equipment implements Identifiable {
    private String id;
    private String name;
    private EquipmentType type;
    private int bonusAtk;
    private int bonusDef;
    private int bonusHp;
    private int price;

    public Equipment() {}

    public Equipment(String id, String name, EquipmentType type, int bonusAtk, int bonusDef, int bonusHp, int price) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.bonusAtk = bonusAtk;
        this.bonusDef = bonusDef;
        this.bonusHp = bonusHp;
        this.price = price;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }
    public int getBonusAtk() { return bonusAtk; }
    public void setBonusAtk(int bonusAtk) { this.bonusAtk = bonusAtk; }
    public int getBonusDef() { return bonusDef; }
    public void setBonusDef(int bonusDef) { this.bonusDef = bonusDef; }
    public int getBonusHp() { return bonusHp; }
    public void setBonusHp(int bonusHp) { this.bonusHp = bonusHp; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment eq = (Equipment) o;
        return Objects.equals(id, eq.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
