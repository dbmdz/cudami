package de.digitalcollections.model.exception;

public record Problem(String type, String title, int status, String detail, String instance) {}
