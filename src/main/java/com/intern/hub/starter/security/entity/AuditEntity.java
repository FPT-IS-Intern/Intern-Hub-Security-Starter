package com.intern.hub.starter.security.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import static lombok.AccessLevel.PROTECTED;

@MappedSuperclass
@Getter
@Setter
@FieldDefaults(level = PROTECTED)
public abstract class AuditEntity {

  Long createdAt;

  Long updatedAt;

  @Version
  Integer version;

  @CreatedBy
  Long createdBy;

  @LastModifiedBy
  Long updatedBy;

  @PrePersist
  void onCreated() {
    long now = System.currentTimeMillis();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdated() {
    this.updatedAt = System.currentTimeMillis();
  }

}
