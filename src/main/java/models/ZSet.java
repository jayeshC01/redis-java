package models;

import java.util.*;

public class ZSet {
  public static final class ZItem {
    public final String member;
    public final double score;
    public ZItem(String member, double score) {
      this.member = member;
      this.score = score;
    }
  }

  private final TreeSet<ZItem> items = new TreeSet<>(
      Comparator.comparingDouble((ZItem z) -> z.score).thenComparing(z -> z.member)
  );
  private final HashMap<String, ZItem> index = new HashMap<>();

  public void insert(double score, String member) {
    ZItem it = new ZItem(member, score);
    items.add(it);
    index.put(member, it);
  }

  public boolean containsMember(String member) { return index.containsKey(member); }

  public int size() { return items.size(); }

  public boolean addOrUpdate(double score, String member) {
    ZItem existing = index.get(member);
    if (existing != null) {
      items.remove(existing);
      ZItem updated = new ZItem(member, score);
      items.add(updated);
      index.put(member, updated);
      return false;
    } else {
      ZItem it = new ZItem(member, score);
      items.add(it);
      index.put(member, it);
      return true;
    }
  }

}
