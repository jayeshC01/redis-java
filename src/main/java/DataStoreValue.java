public class DataStoreValue {
  private String value;
  long expiryTimeMillis;

  public DataStoreValue(String value, long expiryTimeMillis) {
    this.value = value;
    this.expiryTimeMillis = expiryTimeMillis;
  }

  public DataStoreValue(String value) {
    this.value = value;
    this.expiryTimeMillis = 0;
  }

  public DataStoreValue(long value) {
    this.value = String.valueOf(value);
    this.expiryTimeMillis = 0;
  }

  public boolean isExpired() {
    return expiryTimeMillis > 0 && System.currentTimeMillis() > expiryTimeMillis;
  }

  public String getValue() {
    return value;
  }

  public void updateValue(String updatedValue) {
    this.value = updatedValue;
  }
}
