package model;

import java.io.Serializable;

public abstract class Person implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String username;
    private String password;
    private Role role;

    public Person() {}

    public Person(String id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    @Override
    public String getName() { return username; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
