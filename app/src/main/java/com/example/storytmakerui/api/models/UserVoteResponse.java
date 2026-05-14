package com.example.storytmakerui.api.models;

public class UserVoteResponse {
    private int id;
    private int choiceId;
    private int chapterId;
    private String chapterTitle;
    private int storyId;
    private String storyTitle;
    private int votedOption;
    private String option1Text;
    private String option2Text;
    private Integer option1Votes;
    private Integer option2Votes;
    private boolean isClosed;
    private Integer winningOption;
    private String votedAt;

    public UserVoteResponse() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(int choiceId) {
        this.choiceId = choiceId;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public int getStoryId() {
        return storyId;
    }

    public void setStoryId(int storyId) {
        this.storyId = storyId;
    }

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public int getVotedOption() {
        return votedOption;
    }

    public void setVotedOption(int votedOption) {
        this.votedOption = votedOption;
    }

    public String getOption1Text() {
        return option1Text;
    }

    public void setOption1Text(String option1Text) {
        this.option1Text = option1Text;
    }

    public String getOption2Text() {
        return option2Text;
    }

    public void setOption2Text(String option2Text) {
        this.option2Text = option2Text;
    }

    public Integer getOption1Votes() {
        return option1Votes;
    }

    public void setOption1Votes(Integer option1Votes) {
        this.option1Votes = option1Votes;
    }

    public Integer getOption2Votes() {
        return option2Votes;
    }

    public void setOption2Votes(Integer option2Votes) {
        this.option2Votes = option2Votes;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public Integer getWinningOption() {
        return winningOption;
    }

    public void setWinningOption(Integer winningOption) {
        this.winningOption = winningOption;
    }

    public String getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(String votedAt) {
        this.votedAt = votedAt;
    }
}
