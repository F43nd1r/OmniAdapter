package com.faendir.omniadapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.faendir.omniadapter.model.Action;
import com.faendir.omniadapter.model.ChangeInformation;
import com.faendir.omniadapter.model.Component;
import com.faendir.omniadapter.model.Composite;
import com.faendir.omniadapter.model.DeepObservableList;
import com.faendir.omniadapter.model.SelectionMode;
import com.faendir.omniadapter.utils.TypedItemTouchHelperCallback;
import com.faendir.omniadapter.utils.Utils;

import org.apache.commons.lang3.event.EventListenerSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.faendir.omniadapter.model.Action.MOVE;
import static com.faendir.omniadapter.model.Action.REMOVE;

/**
 * Created on 07.08.2016.
 *
 * @author F43nd1r
 */

class OmniAdapterImpl<T extends Component> extends RecyclerView.Adapter<ComponentViewHolder<T>> implements OmniAdapter<T>, ComponentViewHolder.Listener<T>, DeepObservableList.Listener<T> {
    private final DeepObservableList<T> basis;
    private final Controller<T> controller;
    private final ItemTouchHelper touchHelper;
    private final Action.Click click;
    private final Action.LongClick longClick;
    private final Action.Swipe swipeToLeft;
    private final Action.Swipe swipeToRight;
    private final RecyclerView.LayoutManager layoutManager;
    @ColorInt
    private final int highlightColor;
    @ColorInt
    private final int selectionColor;
    private final Handler mainHandler;
    private final SelectionMode selectionMode;
    private final boolean deselectChildrenOnCollapse;
    private final SparseArray<String> undoActions;
    private final String undo;
    private final EventListenerSupport<SelectionListener<T>> selectionListener;
    private final EventListenerSupport<UndoListener<T>> undoListener;
    private final int insetDpPerLevel;
    private final boolean insetAsMargin;
    private final List<ChangeInformation<T>> dragChanges;
    private List<T> visible;
    private boolean bufferedUpdate;
    private boolean restoring;
    private boolean dragging;
    private RecyclerView recyclerView;
    @Nullable
    private Snackbar activeSnackbar;

    OmniAdapterImpl(Context context, DeepObservableList<T> basis, Controller<T> controller,
                    Action.Click click, Action.LongClick longClick, Action.Swipe swipeToLeft, Action.Swipe swipeToRight,
                    RecyclerView.LayoutManager layoutManager,
                    int highlightColor, int selectionColor, SelectionMode selectionMode,
                    int expandUntilLevelOnStartup, boolean deselectChildrenOnCollapse,
                    List<SelectionListener<T>> selectionListeners, SparseArray<String> undoActions, String undo, List<UndoListener<T>> undoListeners,
                    int insetDpPerLevel, boolean insetAsMargin) {
        this.click = click;
        this.longClick = longClick;
        this.swipeToLeft = swipeToLeft;
        this.swipeToRight = swipeToRight;
        this.layoutManager = layoutManager;
        this.highlightColor = highlightColor;
        this.selectionColor = selectionColor;
        this.selectionMode = selectionMode;
        this.deselectChildrenOnCollapse = deselectChildrenOnCollapse;
        this.undoActions = undoActions;
        this.undo = undo;
        selectionListener = Utils.createGenericEventListenerSupport(SelectionListener.class, selectionListeners);
        undoListener = Utils.createGenericEventListenerSupport(UndoListener.class, undoListeners);
        this.insetDpPerLevel = insetDpPerLevel;
        this.insetAsMargin = insetAsMargin;
        setHasStableIds(true);
        bufferedUpdate = false;
        this.basis = basis;
        this.controller = controller;
        dragChanges = new ArrayList<>();
        mainHandler = new Handler(context.getMainLooper());
        visible = this.basis.flatView();
        touchHelper = new ItemTouchHelper(new TouchCallback().asCallback());
        Utils.expandUntilLevel(this.basis, controller, expandUntilLevelOnStartup);
        this.basis.addListener(this);
    }

    @Override
    public ComponentViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        ComponentViewHolder<T> componentViewHolder = new ComponentViewHolder<>(controller.createView(parent, viewType), viewType, highlightColor, selectionColor, insetDpPerLevel, insetAsMargin);
        componentViewHolder.setListener(this);
        return componentViewHolder;
    }

    @Override
    public void onBindViewHolder(ComponentViewHolder<T> holder, int position) {
        T component = visible.get(position);
        controller.bindView(holder.getView(), component, holder.getLevel());
        holder.setComponent(visible.get(position));
    }

    @Override
    public void onViewRecycled(ComponentViewHolder<T> holder) {
        holder.detach();
    }

    @Override
    public int getItemCount() {
        return visible.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Utils.findLevel(basis, visible.get(position));
    }

    @Override
    public long getItemId(int position) {
        return visible.get(position).hashCode();
    }

    @Override
    public void onClick(ComponentViewHolder<T> viewHolder) {
        executeAction(click, viewHolder);
    }

    @Override
    public boolean onLongClick(ComponentViewHolder<T> viewHolder) {
        return executeAction(longClick, viewHolder);
    }

    @Override
    public void onExpansionToggled(ComponentViewHolder<T> viewHolder) {
        update();
    }

    @Override
    public void onSelectionToggled(ComponentViewHolder<T> viewHolder) {
        List<T> selection = getSelection();
        if (selection.size() > 0) {
            selectionListener.fire().onSelectionChanged(selection);
        } else {
            selectionListener.fire().onSelectionCleared();
        }
    }

    void attach(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        touchHelper.attachToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                commitPendingUndoIfAny();
            }
        });
    }

    @Override
    public void clearSelection() {
        Utils.clearSelection(basis);
        selectionListener.fire().onSelectionCleared();
    }

    @NonNull
    @Override
    public List<T> getSelection() {
        final List<T> list = new ArrayList<>();
        basis.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                if (component.getState().isSelected()) {
                    list.add(component);
                }
            }
        }, true);
        return list;
    }

    @NonNull
    @Override
    public List<T> getSelectionByLevel(@IntRange(from = 0) final int l) {
        final List<T> list = new ArrayList<>();
        basis.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                if (l == level && component.getState().isSelected()) {
                    list.add(component);
                }
            }
        }, true);
        return list;
    }

    @NonNull
    @Override
    public <E extends T> List<E> getSelectionByType(final Class<E> type) {
        final List<E> list = new ArrayList<>();
        basis.visitDeep(new DeepObservableList.ComponentVisitor<T>() {
            @Override
            public void visit(T component, int level) {
                if (type.isAssignableFrom(component.getClass()) && component.getState().isSelected()) {
                    list.add(type.cast(component));
                }
            }
        }, true);
        return list;
    }

    @Override
    public void notifyItemUpdated(T component) {
        int index = visible.indexOf(component);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    private boolean executeAction(final Action.BaseAction action, final ComponentViewHolder<T> viewHolder) {
        final T component = viewHolder.getComponent();
        final int actionId = action.resolve(component, viewHolder.getLevel());
        if (actionId == Action.NONE || !component.getState().isEnabled()) {
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!action.getListener().allowTrigger(component, actionId)) {
                    return;
                }
                commitPendingUndoIfAny();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean actionSuccess = true;
                        switch (actionId) {
                            case Action.SELECT:
                                actionSuccess = toggleSelection(viewHolder.getComponent());
                                break;
                            case Action.EXPAND:
                                actionSuccess = toggleExpansion(viewHolder.getComponent());
                                break;
                            case REMOVE:
                                Utils.findParent(basis, component).remove(component);
                                break;
                            case Action.DRAG:
                                actionSuccess = false;
                                break;
                        }
                        if (actionSuccess) {
                            action.getListener().onTrigger(component, actionId);
                        }
                    }
                });
            }
        }).start();
        return true;
    }

    private void update() {
        if (!bufferedUpdate) {
            bufferedUpdate = true;
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    bufferedUpdate = false;
                    final List<T> old = visible;
                    List<T> current = basis.flatView();
                    Iterator<T> iterator = current.iterator();
                    List<T> possibleRemoved = new ArrayList<>();
                    List<T> possibleAdded = new ArrayList<>();
                    List<T> possibleMoved = new ArrayList<>();
                    for (T oldComponent : old) {
                        T currentComponent = iterator.hasNext() ? iterator.next() : null;
                        if (oldComponent != currentComponent) {
                            if (possibleAdded.remove(oldComponent)) {
                                possibleMoved.add(oldComponent);
                            } else {
                                possibleRemoved.add(oldComponent);
                            }
                            if (currentComponent != null) {
                                if (possibleRemoved.remove(currentComponent)) {
                                    possibleMoved.add(currentComponent);
                                } else {
                                    possibleAdded.add(currentComponent);
                                }
                            }
                        }
                    }
                    while (iterator.hasNext()) {
                        possibleAdded.add(iterator.next());
                    }
                    visible = current;
                    for (T component : possibleRemoved) {
                        int index = old.indexOf(component);
                        notifyItemRemoved(index);
                        old.remove(index);
                    }
                    for (T component : possibleAdded) {
                        int index = current.indexOf(component);
                        notifyItemInserted(index);
                        old.add(index, component);
                    }
                    for (T component : possibleMoved) {
                        int from = old.indexOf(component);
                        int to = current.indexOf(component);
                        if (from != to) {
                            notifyItemMoved(from, to);
                            old.add(to, old.remove(from));
                        }
                    }
                }
            });
        }
    }

    private boolean toggleSelection(T component) {
        if (controller.isSelectable(component)) {
            boolean select = !component.getState().isSelected();
            if (select && selectionMode == SelectionMode.SINGLE) {
                clearSelection();
            }
            component.getState().setSelected(select);
            return true;
        }
        return false;
    }

    private boolean toggleExpansion(T component) {
        if (Utils.checkIsExpandable(controller, component)) {
            Composite.ExpandableState state = ((Composite) component).getState();
            state.setExpanded(!state.isExpanded());
            if (deselectChildrenOnCollapse && !state.isExpanded()) {
                Utils.clearSelection(((Composite) component).getChildren());
            }
            return true;
        }
        return false;
    }

    @Override
    public void commitPendingUndoIfAny() {
        if (activeSnackbar != null) {
            activeSnackbar.dismiss();
        }
    }

    @Override
    public void onListChanged(List<ChangeInformation<T>> changeInfo) {
        if (!dragging) {
            if (!restoring) {
                List<ChangeInformation.Add<T>> additions = new ArrayList<>();
                List<ChangeInformation.Remove<T>> removals = new ArrayList<>();
                List<ChangeInformation.Move<T>> moves = new ArrayList<>();
                for (ChangeInformation<T> change : changeInfo) {
                    if (change instanceof ChangeInformation.Add) {
                        additions.add((ChangeInformation.Add<T>) change);

                    } else if (change instanceof ChangeInformation.Remove) {
                        removals.add((ChangeInformation.Remove<T>) change);

                    } else if (change instanceof ChangeInformation.Move) {
                        moves.add((ChangeInformation.Move<T>) change);

                    }
                }
                final boolean remove = undoActions.get(REMOVE) != null && !removals.isEmpty() && additions.isEmpty();
                final boolean move = undoActions.get(MOVE) != null && removals.isEmpty() && additions.isEmpty() && !moves.isEmpty();
                if (remove || move) {
                    SnackbarListener listener = new SnackbarListener(remove ? removals : moves);
                    activeSnackbar = Snackbar.make(recyclerView, undoActions.get(remove ? REMOVE : MOVE), Snackbar.LENGTH_INDEFINITE)
                            .setAction(undo, listener).setCallback(listener);
                    activeSnackbar.show();
                }
            }
        } else {
            dragChanges.addAll(changeInfo);
        }
        update();
    }

    @SafeVarargs
    public final void notifyItemsUpdated(T... components) {
        for (T component : components) {
            notifyItemUpdated(component);
        }
    }

    @Override
    public void notifyItemsUpdated(Collection<? extends T> components) {
        for (T component : components) {
            notifyItemUpdated(component);
        }
    }

    @Override
    public void notifyDataSetUpdated() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public List<T> getVisible() {
        return new ArrayList<>(visible);
    }

    @NonNull
    @Override
    public List<T> getVisibleByLevel(@IntRange(from = 0) int level) {
        List<T> list = new ArrayList<>();
        for (T component : visible) {
            if (Utils.findLevel(basis, component) == level) {
                list.add(component);
            }
        }
        return list;
    }

    @NonNull
    @Override
    public List<T> getVisibleByParent(T parent) {
        List<T> list = new ArrayList<>();
        if (parent instanceof Composite)
            //noinspection unchecked
            for (T component : ((Composite<T>) parent).getChildren()) {
                if (visible.contains(component)) {
                    list.add(component);
                }
            }
        return list;
    }

    @NonNull
    @Override
    public <E extends T> List<E> getVisibleByType(Class<E> type) {
        List<E> list = new ArrayList<>();
        for (T component : visible) {
            if (type.isAssignableFrom(component.getClass())) {
                list.add(type.cast(component));
            }
        }
        return list;
    }

    @Override
    public int getVisibleCount() {
        return visible.size();
    }

    @Override
    public boolean isVisible(T component) {
        return visible.contains(component);
    }

    private class TouchCallback extends TypedItemTouchHelperCallback<ComponentViewHolder<T>> {

        @Override
        public int getDragDirs(RecyclerView recyclerView, ComponentViewHolder<T> viewHolder) {
            ComponentViewHolder holder = (ComponentViewHolder) viewHolder;
            return holder.getComponent().getState().isEnabled()
                    && longClick.resolve(holder.getComponent(), holder.getLevel()) == Action.DRAG
                    ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : 0;
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, ComponentViewHolder<T> viewHolder) {
            ComponentViewHolder holder = (ComponentViewHolder) viewHolder;
            return holder.getComponent().getState().isEnabled()
                    ? (swipeToLeft.resolve(holder.getComponent(), holder.getLevel()) != Action.NONE ? ItemTouchHelper.LEFT : 0)
                    | (swipeToRight.resolve(holder.getComponent(), holder.getLevel()) != Action.NONE ? ItemTouchHelper.RIGHT : 0) : 0;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, ComponentViewHolder<T> fromHolder, ComponentViewHolder<T> toHolder) {
            dragging = true;
            T from = fromHolder.getComponent();
            T to = toHolder.getComponent();
            DeepObservableList<T> toList;
            int index;
            if (fromHolder.getLevel() == toHolder.getLevel()) {
                toList = Utils.findParent(basis, to);
                index = toList.indexOf(to);
            } else if (fromHolder.getLevel() == toHolder.getLevel() + 1 && to instanceof Composite) {
                //noinspection unchecked
                toList = ((Composite<T>) to).getChildren();
                if (!((Composite) to).getState().isExpanded()) toggleExpansion(to);
                index = toList.size();
            } else {
                return false;
            }
            DeepObservableList<T> fromList = Utils.findParent(basis, from);
            if (fromList.equals(toList) && fromList.indexOf(from) < index) {
                index--;
            }
            if (!controller.shouldMove(from, fromList, fromList.indexOf(from), toList, toList.indexOf(to))) {
                return false;
            }
            fromList.remove(from);
            toList.add(index, from);
            return true;
        }

        @Override
        public void clearView(RecyclerView recyclerView, ComponentViewHolder<T> viewHolder) {
            super.clearView(recyclerView, viewHolder);
            dragging = false;
            onListChanged(new ArrayList<>(Utils.compileChanges(dragChanges)));
            dragChanges.clear();
        }

        @Override
        public RecyclerView.ViewHolder chooseDropTarget(ComponentViewHolder<T> selected, List<ComponentViewHolder<T>> dropTargets, int curX, int curY) {
            for (Iterator<ComponentViewHolder<T>> iterator = dropTargets.iterator(); iterator.hasNext(); ) {
                ComponentViewHolder<T> holder = iterator.next();
                if (holder.getLevel() > selected.getLevel() || holder.getLevel() + 1 < selected.getLevel()) {
                    iterator.remove();
                }
            }
            return super.chooseDropTarget(selected, dropTargets, curX, curY);
        }

        @Override
        public void onSwiped(ComponentViewHolder<T> holder, int direction) {
            Action.BaseAction action = direction == ItemTouchHelper.LEFT ? swipeToLeft : swipeToRight;
            if (action.resolve(holder.getComponent(), holder.getLevel()) != REMOVE) {
                notifyItemUpdated(holder.getComponent());
            }
            if (controller.shouldSwipe(holder.getComponent(), direction)) {
                executeAction(action, holder);
            }
        }
    }

    private class SnackbarListener extends Snackbar.Callback implements View.OnClickListener {
        private final List<? extends ChangeInformation<T>> changes;
        private boolean reverted;

        private SnackbarListener(List<? extends ChangeInformation<T>> changes) {
            this.changes = changes;
            reverted = false;
        }


        @Override
        public void onClick(View view) {
            restoring = true;
            basis.beginBatchedUpdates();
            for (ChangeInformation<? extends T> change : changes) {
                if (change instanceof ChangeInformation.IAdd) {
                    //noinspection unchecked (type of change always equals the type of the interface)
                    ((ChangeInformation.IAdd<T>) change).getNewParent().remove(change.getComponent());
                }
                if (change instanceof ChangeInformation.IRemove) {
                    //noinspection unchecked (type of change always equals the type of the interface)
                    ChangeInformation.IRemove<T> remove = (ChangeInformation.IRemove<T>) change;
                    remove.getFormerParent().add(remove.getFormerPosition(), change.getComponent());
                }
            }
            basis.endBatchedUpdates();
            restoring = false;
            activeSnackbar = null;
            undoListener.fire().onActionReverted(changes);
            reverted = true;
        }

        @Override
        public void onDismissed(Snackbar snackbar, int event) {
            super.onDismissed(snackbar, event);
            if (activeSnackbar == snackbar) {
                activeSnackbar = null;
            }
            if (!reverted) {
                undoListener.fire().onActionPersisted(changes);
            }
        }
    }

}
