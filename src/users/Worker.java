package users;

public class Worker {
    private String id;
    private String name;
    private String password;
    private int assignedCount;
    private int resolvedCount;
    private int complaintCount;

    public Worker(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.assignedCount = 0;
        this.resolvedCount = 0;
        this.complaintCount = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public int getAssignedCount() { return assignedCount; }
    public void setAssignedCount(int c) { this.assignedCount = c; }
    public int getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(int c) { this.resolvedCount = c; }
    public int getComplaintCount() { return complaintCount; }
    public void setComplaintCount(int c) { this.complaintCount = c; }

    public void incrementAssignedCount() { this.assignedCount++; }
    public void decrementAssignedCount() { if (this.assignedCount > 0) this.assignedCount--; }
    public void incrementResolvedCount() { this.resolvedCount++; }
    public void incrementComplaintCount() { this.complaintCount++; }

    @Override
    public String toString() {
        return name + " (" + id + ") | Resolves: " + resolvedCount;
    }
}
