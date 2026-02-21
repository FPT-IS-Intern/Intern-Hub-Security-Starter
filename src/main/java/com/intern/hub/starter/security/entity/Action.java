package com.intern.hub.starter.security.entity;

public enum Action {

  CREATE("create"),
  READ("read"),
  UPDATE("update"),
  DELETE("delete"),
  REVIEW("review");

  public final String value;

  Action(String value) {
    this.value = value;
  }

}
