package models;

import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;

public class DataStoreValue {
  private Object value;
  long expiryTimeMillis;

  public DataStoreValue(Object value, long expiryTimeMillis) {
    this.value = value;
    this.expiryTimeMillis = expiryTimeMillis;
  }

  public DataStoreValue(Object value) {
    this.value = value;
    this.expiryTimeMillis = 0;
  }

  public boolean isExpired() {
    return expiryTimeMillis > 0 && System.currentTimeMillis() > expiryTimeMillis;
  }

  public void updateValue(Object updatedValue) {
    this.value = updatedValue;
  }

  public boolean isString() {
    return value instanceof String;
  }

  public boolean isList() {
    return value instanceof List;
  }

  public String getAsString() {
    if (!isString()) {
      throw new IllegalStateException(
          "WRONGTYPE Operation against a value of wrong type");
    }
    return (String) value;
  }

  @SuppressWarnings("unchecked")
  public List<String> getAsList() {
    if (!isList()) {
      throw new IllegalStateException(
          "WRONGTYPE Operation against a value of wrong type"
      );
    }
    return (List<String>) value;
  }

  public long getAsLong() {
    if (!(value instanceof String)) {
      throw new IllegalStateException(
          "WRONGTYPE Operation against a key holding the wrong kind of value"
      );
    }
    try {
      return Long.parseLong((String) value);
    } catch (NumberFormatException e) {
      throw new NumberFormatException("value is not an integer or out of range");
    }
  }

  public String getValueType() {
    if (value instanceof String || value instanceof Number) {
      return "string";
    } else if (value instanceof List) {
      return "list";
    } else if (value instanceof Set) {
      return "set";
    }
    return "undefined";
  }
}
