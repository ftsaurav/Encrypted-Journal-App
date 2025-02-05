package Model;

import com.google.firebase.Timestamp;

public class Journal {
    private String title;
    private String thought;
    private String imageUrl;
    private String userId;
    private com.google.firebase.Timestamp timeAdded;
    private String userName;
    private String document_id;

    public Journal() { //Must for firestore to work
    }

    public Journal(String title, String thought, String imageUrl, String userId, com.google.firebase.Timestamp timeAdded, String userName) {
        this.title = title;
        this.thought = thought;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.timeAdded = timeAdded;
        this.userName = userName;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
