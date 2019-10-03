package com.cloud.ccwebapp.recipe.exception;

import java.util.Date;

public class Response {


    private String error;
    private String details;
    private Date timestamp;

    public Response(String error, String details) {
        super();
        this.error = error;
        this.details = details;
        this.timestamp = new Date();
    }

    public String getError() {
        return error;
    }

    public String getDetails() {
        return details;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "{\n" +
                "\t\"error\":\"" + error + "\",\n" +
                "\t\"details\":\"" + details + "\",\n" +
                "\t\"timestamp\":\"" + timestamp + "\"\n" +
                '}';
    }
}

