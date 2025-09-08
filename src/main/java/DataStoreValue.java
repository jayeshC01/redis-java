import java.util.*;

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
    return isString() ? (String) value : null;
  }

  @SuppressWarnings("unchecked")
  public List<String> getAsList() {
    return isList() ? (List<String>) value : null;
  }

  public Long getAsLong() {
    return Long.parseLong(String.valueOf(value));
  }
}
