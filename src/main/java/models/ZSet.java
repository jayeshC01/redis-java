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

  public Integer rankOf(String member) {
    if (!index.containsKey(member)) return null;
    int i = 0;
    for (ZItem it : items) {
      if (it.member.equals(member)) return i;
      i++;
    }
    return null;
  }

  public java.util.List<String> range(int start, int stop) {
    java.util.List<String> out = new java.util.ArrayList<>();
    int size = items.size();

    if (size == 0 || start >= size || start > stop) {
      return out;
    }

    if (stop >= size) {
      stop = size - 1;
    }

    int i = 0;
    for (ZItem it : items) {
      if (i >= start && i <= stop) {
        out.add(it.member);
      }
      if (i > stop) break;
      i++;
    }
    return out;
  }
}
