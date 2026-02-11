package com.intern.hub.starter.security.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@FieldDefaults(level = PRIVATE)
public abstract class AuditModel {

  Long createdAt;

  Long updatedAt;

  Long createdBy;

  Long updatedBy;

}
