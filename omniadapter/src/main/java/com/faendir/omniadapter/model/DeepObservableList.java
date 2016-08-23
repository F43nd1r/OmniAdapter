package com.faendir.omniadapter.model;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.omniadapter.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A collection which notifies listeners of deep changes
 *
 * @author F43nd1r
 * @since 8.8.2016
 * @param <T> Type of all items in this list (including sublists)
 */
public class DeepObservableList<T extends Component> implements List<T> {

    /**
     * copies this collection
     *
     * @param type type of all items in the collection and its children
     * @param c    the collection to copy
     * @param <T>  the type of the resulting list
     * @return a new DeepObservableList with the content of the collection
     */
    public static <T extends Component> DeepObservableList<T> copyOf(Class<T> type, @NonNull Collection<? extends T> c) {
        return new DeepObservableList<>(type, c);
    }

    /**
     * wraps this list.
     * Note that the list must not be modified except through the returned DeepObservableList
     *
     * @param type type of all items in the collection and its children
     * @param list the list to wrap
     * @param <T>  type of the resulting list
     * @return a new DeepObservableList wrapping this list
     */
    public static <T extends Component> DeepObservableList<T> wrap(Class<T> type, @NonNull List<T> list) {
        return new DeepObservableList<>(type, list);
    }

    private final List<T> delegate;
    private final Class<T> type;
    private transient final List<Listener<? super T>> listeners = new ArrayList<>();
    private transient final ChildListener childListener = new ChildListener();
    private transient final List<ChangeInformation<T>> changeInfos = new ArrayList<>();
    private transient final Handler handler = new Handler(Looper.getMainLooper());
    private transient volatile boolean posted = false;
    private transient volatile int batchCount = 0;
    private transient VisibleFilter filter = null;

    public DeepObservableList(Class<T> type) {
        this.type = type;
        delegate = new ArrayList<>();
    }

    private DeepObservableList(Class<T> type, @NonNull Collection<? extends T> c) {
        check(c);
        delegate = new ArrayList<>(c);
        this.type = type;
        afterAdd(c);
    }

    private DeepObservableList(Class<T> type, @NonNull List<T> list) {
        delegate = list;
        this.type = type;
        afterAdd(list);
    }

    public void addListener(@NonNull Listener<? super T> listener) {
        listeners.add(listener);
    }

    public void removeListener(@NonNull Listener<? super T> listener) {
        listeners.remove(listener);
    }

    public void setFilter(@NonNull VisibleFilter filter) {
        this.filter = filter;
    }

    public void visitDeep(@NonNull ComponentVisitor<T> visitor) {
        visitDeep(visitor, 0);
    }

    private void visitDeep(ComponentVisitor<T> visitor, int level) {
        for (T component : this) {
            visitor.visit(component, level);
            if (component instanceof Composite && ((Composite) component).getState().isExpanded()) {
                //noinspection unchecked
                (((Composite<T>) component).getChildren()).visitDeep(visitor, level + 1);
            }
        }
    }

    public void beginBatchedUpdates() {
        batchCount++;
        for (T component : this) {
            if (component instanceof Composite) {
                ((Composite) component).getChildren().beginBatchedUpdates();
            }
        }

    }

    public void endBatchedUpdates() {
        $endBatchedUpdates();
        listChanged(true);
    }

    private void $endBatchedUpdates() {
        if (batchCount > 0) {
            for (T component : this) {
                if (component instanceof Composite) {
                    ((Composite) component).getChildren().endBatchedUpdates();
                }
            }
            batchCount--;
        }
    }

    public List<T> flatView() {
        List<T> list = new ArrayList<>();
        for (T component : this) {
            if (filter == null || filter.accept(component)) {
                list.add(component);
            }
            if (component instanceof Composite && ((Composite) component).getState().isExpanded()) {
                //noinspection unchecked
                list.addAll(((Composite<T>) component).getChildren().flatView());
            }
        }
        return list;
    }

    private void listChanged() {
        listChanged(false);
    }


    private void listChanged(boolean direct) {
        if (batchCount == 0) {
            if (direct) {
                notifyListeners(null);
            } else if (!posted) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyListeners(null);
                        posted = false;
                    }
                });
                posted = true;
            }
        }
    }

    private void notifyListeners(@Nullable Listener<? super T> until) {
        ArrayList<ChangeInformation<T>> changes = new ArrayList<>(Utils.compileChanges(changeInfos));
        changeInfos.clear();
        if (!changes.isEmpty()) {
            for (Listener<? super T> listener : listeners) {
                if (listener == until) {
                    break;
                }
                notifyListener(listener, changes);
                if (changes.size() > 0) {
                    changes.addAll(changeInfos);
                    changes = new ArrayList<>(Utils.compileChanges(changes));
                    notifyListeners(listener);
                }
            }
        }
    }

    private <E extends Component> void notifyListener(Listener<E> listener, List<ChangeInformation<T>> changes) {
        beginBatchedUpdates();
        List<ChangeInformation<E>> list = new ArrayList<>();
        for (ChangeInformation<T> change : changes) {
            //noinspection unchecked (E is super T and only output on ChangeInformation)
            list.add((ChangeInformation<E>) change);
        }
        listener.onListChanged(list);
        $endBatchedUpdates();
    }

    private void check(Collection<? extends T> components){
        for (T component : components){
            check(component);
        }
    }

    private void check(T component) {
        if (!type.isAssignableFrom(component.getClass()) || component instanceof Composite && !type.isAssignableFrom(((Composite) component).getChildren().type)) {
            throw new IllegalArgumentException("All items in this list must be deeply of type " + type.getName());
        }
    }

    private void beforeRemove(Collection<? extends T> components) {
        for (T c : components) {
            beforeRemove(c);
        }
    }

    private void beforeRemove(T component) {
        if (contains(component)) {
            if (component instanceof Composite) {
                //noinspection unchecked
                ((Composite<T>) component).getChildren().removeListener(childListener);
            }
            changeInfos.add(new ChangeInformation.Remove<>(component, this, indexOf(component)));
        }
    }

    private void afterAdd(Collection<? extends T> components) {
        for (T c : components) {
            afterAdd(c);
        }
    }

    private void afterAdd(T component) {
        if (component instanceof Composite) {
            //noinspection unchecked
            ((Composite<T>) component).getChildren().addListener(childListener);
        }
        changeInfos.add(new ChangeInformation.Add<>(component, this));
    }

    @Override
    public T set(int index, T element) {
        check(element);
        beforeRemove(delegate.get(index));
        T component = delegate.set(index, element);
        afterAdd(component);
        listChanged();
        return component;
    }

    @Override
    public boolean add(T component) {
        check(component);
        boolean result = delegate.add(component);
        afterAdd(component);
        listChanged();
        return result;
    }

    @Override
    public void add(int index, T element) {
        check(element);
        delegate.add(index, element);
        afterAdd(element);
        listChanged();
    }

    @Override
    public T remove(int index) {
        beforeRemove(delegate.get(index));
        T component = delegate.remove(index);
        listChanged();
        return component;
    }

    @Override
    public boolean remove(Object o) {
        try {
            //noinspection unchecked
            return remove((T) o);
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public boolean remove(T o) {
        beforeRemove(o);
        boolean result = delegate.remove(o);
        listChanged();
        return result;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        check(c);
        boolean result = delegate.addAll(c);
        afterAdd(c);
        listChanged();
        return result;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        check(c);
        boolean result = delegate.addAll(index, c);
        afterAdd(c);
        listChanged();
        return result;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        try {
            //noinspection unchecked
            beforeRemove((Collection<? extends T>) c);
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException(e);
        }
        boolean result = delegate.removeAll(c);
        listChanged();
        return result;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        List<T> old = new ArrayList<>(this);
        //noinspection SuspiciousMethodCalls
        old.removeAll(c);
        beforeRemove(old);
        boolean result = delegate.retainAll(c);
        listChanged();
        return result;
    }

    @Override
    public void clear() {
        beforeRemove(this);
        delegate.clear();
        listChanged();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new IteratorImpl();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] t1s) {
        //noinspection SuspiciousToArrayCall
        return delegate.toArray(t1s);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return delegate.containsAll(collection);
    }

    @Override
    public T get(int i) {
        return delegate.get(i);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return new IteratorImpl();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int i) {
        return new IteratorImpl(i);
    }

    @NonNull
    @Override
    public List<T> subList(int i, int i1) {
        return delegate.subList(i, i1);
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
            listChanged();
        }
    }

    private class IteratorImpl implements ListIterator<T> {
        private final ListIterator<T> delegate;
        private T current;

        private IteratorImpl() {
            delegate = DeepObservableList.this.delegate.listIterator();
        }

        private IteratorImpl(int index) {
            delegate = DeepObservableList.this.delegate.listIterator(index);
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public T next() {
            current = delegate.next();
            return current;
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public T previous() {
            current = delegate.previous();
            return current;
        }

        @Override
        public int nextIndex() {
            return delegate.nextIndex();
        }

        @Override
        public int previousIndex() {
            return delegate.previousIndex();
        }

        @Override
        public void remove() {
            beforeRemove(current);
            delegate.remove();
        }

        @Override
        public void set(T t) {
            check(t);
            beforeRemove(current);
            delegate.set(t);
            current = t;
            afterAdd(current);
        }

        @Override
        public void add(T t) {
            check(t);
            delegate.add(t);
            current = t;
            afterAdd(current);
        }
    }

}
