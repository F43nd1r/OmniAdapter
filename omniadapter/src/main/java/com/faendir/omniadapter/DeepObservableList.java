package com.faendir.omniadapter;

import android.os.Handler;
import android.os.Looper;

import org.apache.commons.lang3.event.EventListenerSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created on 08.08.2016.
 *
 * @author F43nd1r
 */

public class DeepObservableList<T extends Component> extends ArrayList<T> {
    private static DeepObservableList empty = null;

    static <E extends Component> DeepObservableList<E> emptyList() {
        if (empty == null) {
            empty = new DeepObservableList();
        }
        //noinspection unchecked
        return empty;
    }

    private transient final EventListenerSupport<Listener> listeners = EventListenerSupport.create(Listener.class);
    private transient final Handler handler = new Handler(Looper.getMainLooper());
    private transient boolean posted = false;
    private transient VisibleFilter filter = new NoFilter();

    public void addListener(Listener listener) {
        listeners.addListener(listener);
        for (Component component : this) {
            if (component instanceof Composite) {
                ((Composite) component).addListener(listener);
            }
        }
    }

    public void removeListener(Listener listener) {
        listeners.removeListener(listener);
        for (Component component : this) {
            if (component instanceof Composite) {
                ((Composite) component).removeListener(listener);
            }
        }
    }

    public void setFilter(VisibleFilter filter) {
        this.filter = filter;
    }

    public void visitDeep(ComponentVisitor<T> visitor) {
        visitDeep(visitor, 0);
    }

    private void visitDeep(ComponentVisitor<T> visitor, int level) {
        for (T component : this) {
            visitor.visit(component, level);
            if (component instanceof Composite && component.getState().isExpanded()) {
                //noinspection unchecked
                ((DeepObservableList<T>) component).visitDeep(visitor, level + 1);
            }
        }
    }

    List<T> flatView() {
        List<T> list = new ArrayList<>();
        for (T component : this) {
            if (filter.accept(component)) {
                list.add(component);
            }
            if (component instanceof Composite && component.getState().isExpanded()) {
                //noinspection unchecked
                list.addAll(((Composite<T>) component).flatView());
            }
        }
        return list;
    }

    private void notifyListeners() {
        if (!posted) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    posted = false;
                    listeners.fire().onListChanged();
                }
            });
            posted = true;
        }
    }

    @Override
    public void trimToSize() {
        super.trimToSize();
        notifyListeners();
    }

    @Override
    public T set(int index, T element) {
        T component = super.set(index, element);
        notifyListeners();
        return component;
    }

    @Override
    public boolean add(T component) {
        boolean result = super.add(component);
        notifyListeners();
        return result;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        notifyListeners();
    }

    @Override
    public T remove(int index) {
        T component = super.remove(index);
        notifyListeners();
        return component;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = super.remove(o);
        notifyListeners();
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = super.addAll(c);
        notifyListeners();
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean result = super.addAll(index, c);
        notifyListeners();
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        notifyListeners();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = super.removeAll(c);
        notifyListeners();
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean result = super.retainAll(c);
        notifyListeners();
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        notifyListeners();
    }

    public interface Listener {
        void onListChanged();
    }

    public interface VisibleFilter {
        boolean accept(Component component);
    }

    public interface ComponentVisitor<T> {
        void visit(T component, int level);
    }

    private static class NoFilter implements VisibleFilter {

        @Override
        public boolean accept(Component component) {
            return true;
        }
    }
}
