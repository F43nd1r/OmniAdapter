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

    static <E extends Component> DeepObservableList<E> emptyList() {
        return new DeepObservableList<>();
    }

    private transient final EventListenerSupport<Listener<T>> listeners = Utils.createGenericEventListenerSupport(Listener.class);
    private transient final ChildListener childListener = new ChildListener();
    private transient final List<ChangeInformation<T>> changeInfos = new ArrayList<>();
    private transient final Handler handler = new Handler(Looper.getMainLooper());
    private transient volatile boolean posted = false;
    private transient volatile boolean suppress = false;
    private transient VisibleFilter filter = new NoFilter();

    public DeepObservableList(int initialCapacity) {
        super(initialCapacity);
    }

    public DeepObservableList() {
    }

    public DeepObservableList(Collection<? extends T> c) {
        super(c);
        afterAdd(c);
    }

    public void addListener(Listener<T> listener) {
        listeners.addListener(listener);
        for (Component component : this) {
            if (component instanceof Composite) {
                //noinspection unchecked
                ((Composite<T>) component).addListener(listener);
            }
        }
    }

    public void removeListener(Listener<T> listener) {
        listeners.removeListener(listener);
        for (Component component : this) {
            if (component instanceof Composite) {
                //noinspection unchecked
                ((Composite<T>) component).removeListener(listener);
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

    public void beginBatchedUpdates() {
        suppress = true;
        for (T component : this) {
            if (component instanceof DeepObservableList) {
                //noinspection unchecked
                ((DeepObservableList<T>) component).beginBatchedUpdates();
            }
        }

    }

    public void endBatchedUpdates() {
        for (T component : this) {
            if (component instanceof DeepObservableList) {
                //noinspection unchecked
                ((DeepObservableList<T>) component).endBatchedUpdates();
            }
        }
        suppress = false;
        listChanged(true);
    }


    private void listChanged(boolean direct) {
        if (!suppress && !posted) {
            if (direct) {
                notifyListeners();
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyListeners();
                        posted = false;
                    }
                });
                posted = true;
            }
        }
    }

    private void notifyListeners() {
        ArrayList<ChangeInformation<T>> changes = new ArrayList<>(Utils.compileChanges(changeInfos));
        changeInfos.clear();
        if (!changes.isEmpty()) {
            listeners.fire().onListChanged(changes);
        }
    }

    private void beforeRemove(Collection<? extends T> components) {
        for (T c : components) {
            beforeRemove(c);
        }
    }

    private void beforeRemove(T component) {
        if (contains(component)) {
            if (component instanceof DeepObservableList) {
                //noinspection unchecked
                ((DeepObservableList<T>) component).removeListener(childListener);
            }
            changeInfos.add(ChangeInformation.removeInfo(component, this, indexOf(component)));
        }
    }

    private void afterAdd(Collection<? extends T> components) {
        for (T c : components) {
            afterAdd(c);
        }
    }

    private void afterAdd(T component) {
        if (component instanceof DeepObservableList) {
            //noinspection unchecked
            ((DeepObservableList<T>) component).addListener(childListener);
        }
        changeInfos.add(ChangeInformation.addInfo(component, this));
    }

    @Override
    public T set(int index, T element) {
        beforeRemove(super.get(index));
        T component = super.set(index, element);
        afterAdd(component);
        listChanged(suppress);
        return component;
    }

    @Override
    public boolean add(T component) {
        boolean result = super.add(component);
        afterAdd(component);
        listChanged(suppress);
        return result;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        afterAdd(element);
        listChanged(suppress);
    }

    @Override
    public T remove(int index) {
        beforeRemove(super.get(index));
        T component = super.remove(index);
        listChanged(suppress);
        return component;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(T o) {
        beforeRemove(o);
        boolean result = super.remove(o);
        listChanged(suppress);
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = super.addAll(c);
        afterAdd(c);
        listChanged(suppress);
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean result = super.addAll(index, c);
        afterAdd(c);
        listChanged(suppress);
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            beforeRemove(super.get(i));
        }
        super.removeRange(fromIndex, toIndex);
        listChanged(suppress);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        //noinspection unchecked
        beforeRemove((Collection<? extends T>) c);
        boolean result = super.removeAll(c);
        listChanged(suppress);
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<T> old = new ArrayList<>(this);
        //noinspection SuspiciousMethodCalls
        old.removeAll(c);
        beforeRemove(old);
        boolean result = super.retainAll(c);
        listChanged(suppress);
        return result;
    }

    @Override
    public void clear() {
        beforeRemove(this);
        super.clear();
        listChanged(suppress);
    }

    public interface Listener<T extends Component> {
        void onListChanged(List<ChangeInformation<T>> changeInfo);
    }

    public interface VisibleFilter {
        boolean accept(Component component);
    }

    public interface ComponentVisitor<T> {
        void visit(T component, int level);
    }

    private class ChildListener implements Listener<T> {
        @Override
        public void onListChanged(List<ChangeInformation<T>> changeInfo) {
            changeInfos.addAll(changeInfo);
            listChanged(suppress);
        }
    }

    private static class NoFilter implements VisibleFilter {

        @Override
        public boolean accept(Component component) {
            return true;
        }
    }

}
