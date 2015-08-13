package com.seoeun;

public class AvroRepoException extends Exception{

    public AvroRepoException() {
        super();
    }

    public AvroRepoException(String message) {
        super(message);
    }

    public AvroRepoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AvroRepoException(Throwable cause) {
        super(cause);
    }
}
