package com.example.storytmakerui.api.models;

public class CreateChapterRequest {
    private String content;
    private Integer sequenceNumber;

    public CreateChapterRequest() {}

    public CreateChapterRequest(String content, Integer sequenceNumber) {
        this.content = content;
        this.sequenceNumber = sequenceNumber;
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
