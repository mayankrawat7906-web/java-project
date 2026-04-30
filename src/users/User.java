package users;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private int points;
    private boolean blocked;
    private int penalties;

    public User(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.points = 0;
        this.blocked = false;
        this.penalties = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public void addPoints(int p) { this.points += p; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public int getPenalties() { return penalties; }
    public void setPenalties(int p) { this.penalties = p; }
    public void incrementPenalties() { this.penalties++; }

    @Override
    public String toString() {
        return name + " (" + id + ") | Points: " + points;
    }
}
