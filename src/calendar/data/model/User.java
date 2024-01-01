package calendar.data.model;

public class User {
    private int id;
    private String uid;
    private String userName;
    private String email;
    private String password;
    private int notificationChannel;
    private int familyId;

    private boolean available;

    public User() {
        this.id = -1;
    }

    public User(int id, String userName, String email) {
        this(id, "", userName, email, "", 0, -1);
    }

    public User(int id, String uid, String userName, String email, String password) {
        this(id, uid, userName, email, password, 0, -1);
    }

    public User(String uid, String userName, String email, String password, int notificationChannel) {
        // user for insert. id is automatically generated.
        this(-1, uid, userName, email, password, notificationChannel, -1);
    }

    public User(int id, String uid, String userName, String email, String password, int notificationChannel, int familyId) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.notificationChannel = notificationChannel;
        this.familyId = familyId;
    }

    public int getUserId() {
        return id;
    }

    public void setUserId(int id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNotificationChannel() {
        return notificationChannel;
    }

    public void setNotificationChannel(int notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    public int getFamilyId() {
        return familyId;
    }
    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }

    // toString() method to represent User object as a string
    @Override
    public String toString() {
        return "User {" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", notificationChannel='" + notificationChannel + '\'' +
                '}';
    }
}
