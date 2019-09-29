package com.cloud.ccwebapp.recipe.exception;

public class Response {


    private String error;
    private String details;

    public Response(String error, String details) {
            super();
            this.error = error;
            this.details = details;
    }

    public String getError() {
        return error;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "{\n" +
                "\t\"error\":\"" + error + "\",\n" +
                "\t\"details\":\"" + details + "\"\n" +
                '}';
    }
}

