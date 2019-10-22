package com.cloud.ccwebapp.recipe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImageAlreadyExistsException extends RuntimeException {
  public ImageAlreadyExistsException(String message){
    super(message);
  }
}