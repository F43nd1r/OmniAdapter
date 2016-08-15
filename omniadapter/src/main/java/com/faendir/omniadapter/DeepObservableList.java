package com.faendir.omniadapter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.event.EventListenerSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created on 08.08.2016.
 *
 * @author F43nd1r
 */
public class DeepObservableList<T extends Component> implements List<T> {

    /**
     * copies this collection
     *
     * @param c   the collection to copy
     * @param <T> the type of the resulting list
     * @return a new DeepObservableList with the content of the collection
     */
    public static <T extends Component> DeepObservableList<T> copyOf(@NonNull Collection<? extends T> c) {
        return new DeepObservableList<>(c);
    }

    /**
     * wraps this list.
     * Note that the list must not be modified except through the returned DeepObservableList
     *
     * @param list the list to wrap
     * @param <T>  type of the resulting list
     * @return a new DeepObservableList wrapping this list
     */
    public static <T extends Component> DeepObservableList<T> wrap(@NonNull List<T> list) {
        return new DeepObservableList<>(list);
    }

    private final List<T> delegate;
    private transient final EventListenerSupport<Listener<T>> listeners = Utils.createGenericEventListenerSupport(Listener.class);
    private transient final ChildListener childListener = new ChildListener();
    private transient final List<ChangeInformation<T>> changeInfos = new ArrayList<>();
    private transient final Handler handler = new Handler(Looper.getMainLooper());
    private transient volatile boolean posted = false;
    private transient volatile boolean suppress = false;
    private transient VisibleFilter filter = new NoFilter();

    public DeepObservableList() {
        delegate = new ArrayList<>();
    }

    private DeepObservableList(@NonNull Collection<? extends T> c) {
        delegate = new ArrayList<>(c);
        afterAdd(c);
    }

    private DeepObservableList(@NonNull List<T> list) {
        delegate = list;
        afterAdd(list);
    }

    public void addListener(@NonNull Listener<? super T> listener) {
        //noinspection unchecked
        listeners.addListener((Listener<T>) listener);
    }

    public void removeListener(@NonNull Listener<? super T> listener) {
        //noinspection unchecked
        listeners.removeListener((Listener<T>) listener);
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
        suppress = true;
        for (T component : this) {
            if (component instanceof Composite) {
                //noinspection unchecked
                ((Composite<T>) component).getChildren().beginBatchedUpdates();
            }
        }

    }

    public void endBatchedUpdates() {
        for (T component : this) {
            if (component instanceof Composite) {
                //noinspection unchecked
                ((Composite<T>) component).getChildren().endBatchedUpdates();
            }
        }
        suppress = false;
        listChanged(true);
    }

    List<T> flatView() {
        List<T> list = new ArrayList<>();
        for (T component : this) {
            if (filter.accept(component)) {
                list.add(component);
            }
            if (component instanceof Composite && ((Composite) component).getState().isExpanded()) {
                //noinspection unchecked
                list.addAll(((Composite<T>) component).getChildren().flatView());
            }
        }
        return list;
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
            if (component instanceof Composite) {
                //noinspection unchecked
                ((Composite<T>) component).getChildren().removeListener(childListener);
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
        if (component instanceof Composite) {
            //noinspection unchecked
            ((Composite<T>) component).getChildren().addListener(childListener);
        }
        changeInfos.add(ChangeInformation.addInfo(component, this));
    }

    @Override
    public T set(int index, T element) {
        beforeRemove(delegate.get(index));
        T component = delegate.set(index, element);
        afterAdd(component);
        listChanged(suppress);
        return component;
    }

    @Override
    public boolean add(T component) {
        boolean result = delegate.add(component);
        afterAdd(component);
        listChanged(suppress);
        return result;
    }

    @Override
    public void add(int index, T element) {
        delegate.add(index, element);
        afterAdd(element);
        listChanged(suppress);
    }

    @Override
    public T remove(int index) {
        beforeRemove(delegate.get(index));
        T component = delegate.remove(index);
        listChanged(suppress);
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
        listChanged(suppress);
        return result;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        boolean result = delegate.addAll(c);
        afterAdd(c);
        listChanged(suppress);
        return result;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        boolean result = delegate.addAll(index, c);
        afterAdd(c);
        listChanged(suppress);
        return result;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        //noinspection unchecked
        beforeRemove((Collection<? extends T>) c);
        boolean result = delegate.removeAll(c);
        listChanged(suppress);
        return result;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        List<T> old = new ArrayList<>(this);
        //noinspection SuspiciousMethodCalls
        old.removeAll(c);
        beforeRemove(old);
        boolean result = delegate.retainAll(c);
        listChanged(suppress);
        return result;
    }

    @Override
    public void clear() {
        beforeRemove(this);
        delegate.clear();
        listChanged(suppress);
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
            listChanged(suppress);
        }
    }

    private static class NoFilter implements VisibleFilter {

        @Override
        public boolean accept(Component component) {
            return true;
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
            beforeRemove(current);
            delegate.set(t);
            current = t;
            afterAdd(current);
        }

        @Override
        public void add(T t) {
            delegate.add(t);
            current = t;
            afterAdd(current);
        }
    }

}
