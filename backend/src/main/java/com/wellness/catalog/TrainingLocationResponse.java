package com.wellness.catalog;

public record TrainingLocationResponse(String code, String name) {

    public static TrainingLocationResponse from(TrainingLocation t) {
        return new TrainingLocationResponse(t.getCode(), t.getName());
    }
}
