package com.cloud.ccwebapp.recipe.model;

import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

public class Image {

  @ReadOnlyProperty
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String url;

  public Image() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
