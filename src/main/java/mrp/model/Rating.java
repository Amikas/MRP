package mrp.model;

import java.time.LocalDateTime;

public class Rating {
    private String id;
    private String mediaId;
    private String userId;
    private int score;
    private String comment;
    private boolean isCommentPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String username;
    private String mediaTitle;
    private int likeCount;
    private boolean userHasLiked; 

    public Rating() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getScore() { return score; }
    public void setScore(int score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }
        this.score = score;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isCommentPublic() { return isCommentPublic; }
    public void setCommentPublic(boolean commentPublic) { isCommentPublic = commentPublic; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMediaTitle() { return mediaTitle; }
    public void setMediaTitle(String mediaTitle) { this.mediaTitle = mediaTitle; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public boolean isUserHasLiked() { return userHasLiked; }
    public void setUserHasLiked(boolean userHasLiked) { this.userHasLiked = userHasLiked; }
}