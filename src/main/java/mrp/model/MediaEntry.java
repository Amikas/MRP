package mrp.model;

public class MediaEntry {
    private String id;
    private String title;
    private String description;
    private String mediaType; // "movie", "series", "game"
    private int releaseYear;
    private String genres;
    private int ageRestriction;
    private String creatorId;

    public MediaEntry() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public int getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(int ageRestriction) { this.ageRestriction = ageRestriction; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
}