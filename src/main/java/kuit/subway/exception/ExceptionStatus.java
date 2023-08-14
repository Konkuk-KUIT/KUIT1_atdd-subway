package kuit.subway.exception;

import org.springframework.http.HttpStatus;


public interface ExceptionStatus {

    HttpStatus getHttpStatus();

    int getCode();

    String getMessage();
}
