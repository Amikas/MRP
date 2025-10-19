package mrp.model;

public class Rating {
    private String id;
    private String mediaId;
    private String userId;
    private int score;
    private String comment;

    public Rating() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
