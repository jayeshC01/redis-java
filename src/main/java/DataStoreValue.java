public class DataStoreValue {
  private String value;
  long expiryTimeMillis;

  public DataStoreValue(String value, long expiryTimeMillis) {
    this.value = value;
    this.expiryTimeMillis = expiryTimeMillis;
  }

  public boolean isExpired() {
    return expiryTimeMillis > 0 && System.currentTimeMillis() > expiryTimeMillis;
  }

  public String getValue() {
    return value;
  }
}
