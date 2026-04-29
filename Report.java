package reports;

public class Report {
    public static final String PENDING     = "PENDING";
    public static final String ASSIGNED    = "ASSIGNED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String RESOLVED    = "RESOLVED";
    public static final String SUSPICIOUS  = "SUSPICIOUS";
    public static final String WORKER_COMPLAINT = "WORKER_COMPLAINT";
    public static final String CLOSED      = "CLOSED";

    public static final String[] CATEGORIES = {
            "Waste Management", "Water Pollution", "Air Pollution", "Illegal Dumping", "Noise Pollution", "Other"
    };

    private String id;
    private String userId;
    private double lat;
    private double lon;
    private String description;
    private String date;
    private String status;
    private String assignedWorkerId;
    private String category;
    private int upvotes;

    public Report(String id, String userId, double lat, double lon, String description, String date) {
        this.id = id;
        this.userId = userId;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
        this.date = date;
        this.status = PENDING;
        this.category = "Other";
        this.upvotes = 0;
    }

    public Report(String id, String userId, double lat, double lon, String description, String date, String category) {
        this(id, userId, lat, lon, description, date);
        this.category = category;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedWorkerId() { return assignedWorkerId; }
    public void setAssignedWorkerId(String workerId) { this.assignedWorkerId = workerId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int n) { this.upvotes = n; }
    public void addUpvote() { this.upvotes++; }

    @Override
    public String toString() {
        return "[" + id + "] " + category + " | " + status + " (" + date + ") - " + description + " [" + upvotes + " upvotes]";
    }
}
