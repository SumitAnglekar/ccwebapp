package com.cloud.ccwebapp.recipe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  @JsonIgnore
  private String md5;

  @JsonIgnore
  private long contentLength;

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

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public void setContentLength(long contentLength) {
    this.contentLength = contentLength;
  }

  public String getMd5() {
    return md5;
  }

  public long getContentLength() {
    return contentLength;
  }

  @Override
  public String toString() {
    return "Image{" +
            "id=" + id +
            ", url='" + url + '\'' +
            '}';
  }
}
