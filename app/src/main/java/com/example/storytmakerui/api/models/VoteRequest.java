package com.example.storytmakerui.api.models;

public class VoteRequest {
    private int option;

    public VoteRequest() {}

    public VoteRequest(int option) {
        this.option = option;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }
}
