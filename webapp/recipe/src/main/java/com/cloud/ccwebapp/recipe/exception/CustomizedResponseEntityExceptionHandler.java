package com.cloud.ccwebapp.recipe.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UserAlreadyPresentException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleUserAlreadyPresentException(UserAlreadyPresentException ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RecipeNotFoundException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleRecipeNotFoundException(RecipeNotFoundException ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.NOT_FOUND.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    @ResponseBody
    public final ResponseEntity<Object> UserNotAuthorizedException(UserNotAuthorizedException ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.UNAUTHORIZED.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidInputException.class)
    @ResponseBody
    public final ResponseEntity<Object> InvalidInputException(InvalidInputException ex, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        Response exceptionResponse = new Response("Validation Failed",
                ex.getBindingResult().toString());
        return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
    }


    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Response exceptionResponse = new Response(HttpStatus.BAD_REQUEST.toString(), "Invalid Request Body");
        return new ResponseEntity(exceptionResponse.toString(), HttpStatus.BAD_REQUEST);
    }
}
