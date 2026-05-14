package com.example.storytmakerui.api.models;

public class CreateChapterRequest {
    private String title;
    private String content;
    private Integer sequenceNumber;

    public CreateChapterRequest() {}

    public CreateChapterRequest(String title, String content, Integer sequenceNumber) {
        this.title = title;
        this.content = content;
        this.sequenceNumber = sequenceNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
