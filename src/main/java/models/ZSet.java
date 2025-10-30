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

  public List<String> range(int start, int stop) {
    List<String> out = new ArrayList<>();
    int size = items.size();
    if (size == 0) return out;

    int s = (start < 0) ? size + start : start;
    int e = (stop  < 0) ? size + stop  : stop;

    if (start < 0 && Math.abs(start) >= size) s = 0;
    if (stop  < 0 && Math.abs(stop)  >= size) e = 0;

    if (s < 0) s = 0;
    if (e < 0) e = 0;
    if (e >= size) e = size - 1;
    if (s >= size || s > e) return out;

    int i = 0;
    for (ZItem it : items) {
      if (i >= s && i <= e) out.add(it.member);
      if (i > e) break;
      i++;
    }
    return out;
  }

  public String scoreOf(String member) {
    ZItem it = index.get(member);
    if (it == null) return null;
    return Double.toString(it.score);
  }

  public boolean removeMember(String member) {
    ZItem it = index.remove(member);
    if (it == null) return false;
    items.remove(it);
    return true;
  }
}
