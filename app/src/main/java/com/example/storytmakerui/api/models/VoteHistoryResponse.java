package com.example.storytmakerui.api.models;

public class VoteHistoryResponse {
    private int choiceId;
    private int chapterId;
    private int storyId;
    private String storyTitle;
    private String chapterTitle;
    private int selectedOption;
    private String voteText;
    private boolean isClosed;
    private Integer winningOption;
    private Integer option1Votes;
    private Integer option2Votes;
    private String votedAt;

    public VoteHistoryResponse() {}

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

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        this.selectedOption = selectedOption;
    }

    public String getVoteText() {
        return voteText;
    }

    public void setVoteText(String voteText) {
        this.voteText = voteText;
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

    public String getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(String votedAt) {
        this.votedAt = votedAt;
    }
}
