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
}
