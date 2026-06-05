package model;

public class Admin extends Person {
    private String adminLevel;

    public Admin() {}

    public Admin(String id, String username, String password, Role role, String adminLevel) {
        super(id, username, password, role);
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
}
