package com.cloud.ccwebapp.recipe.model;

import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Image {

  @ReadOnlyProperty
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String url;

  @Transient
  private MultipartFile file;

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

  @Override
  public String toString() {
    return "Image{" +
            "id=" + id +
            ", url='" + url + '\'' +
            '}';
  }
}
