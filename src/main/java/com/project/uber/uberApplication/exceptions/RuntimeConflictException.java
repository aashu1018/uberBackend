package com.project.uber.uberApplication.exceptions;

public class RuntimeConflictException extends RuntimeException{

    public RuntimeConflictException(){}

    public RuntimeConflictException(String message){
        super(message);
    }
}
