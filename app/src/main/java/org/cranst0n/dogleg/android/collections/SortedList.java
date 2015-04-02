package org.cranst0n.dogleg.android.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SortedList<E extends Comparable> extends ArrayList<E> {

  @Override
  public boolean add(E object) {
    boolean res = super.add(object);
    Collections.sort(this);
    return res;
  }

  @Override
  public boolean addAll(Collection<? extends E> collection) {
    boolean res = super.addAll(collection);
    Collections.sort(this);
    return res;
  }

  @Override
  public void add(int index, E object) {
    super.add(index, object);
    Collections.sort(this);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> collection) {
    boolean res = super.addAll(index, collection);
    Collections.sort(this);
    return res;
  }
}
