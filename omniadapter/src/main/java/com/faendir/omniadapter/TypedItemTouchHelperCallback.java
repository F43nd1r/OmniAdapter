package com.faendir.omniadapter;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;

import java.util.List;

/**
 * Can be extended instead of {@link SimpleCallback} if the type of the ViewHolder is always the same.
 * It performs the necessary casts.
 *
 * @author F43nd1r
 */
@SuppressWarnings({"unchecked", "WeakerAccess"})
abstract class TypedItemTouchHelperCallback<T extends RecyclerView.ViewHolder> {
    private final Wrapper<T> wrapper;

    TypedItemTouchHelperCallback() {
        wrapper = new Wrapper<>(this);
    }

    void setDefaultSwipeDirs(int defaultSwipeDirs) {
        wrapper.$setDefaultSwipeDirs(defaultSwipeDirs);
    }

    void setDefaultDragDirs(int defaultDragDirs) {
        wrapper.$setDefaultDragDirs(defaultDragDirs);
    }

    public int getSwipeDirs(RecyclerView recyclerView, T viewHolder) {
        return wrapper.$getSwipeDirs(recyclerView, viewHolder);
    }

    public int getDragDirs(RecyclerView recyclerView, T viewHolder) {
        return wrapper.$getDragDirs(recyclerView, viewHolder);
    }

    int getMovementFlags(RecyclerView recyclerView, T viewHolder) {
        return wrapper.$getMovementFlags(recyclerView, viewHolder);
    }

    int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return wrapper.$convertToAbsoluteDirection(flags, layoutDirection);
    }

    boolean canDropOver(RecyclerView recyclerView, T current, T target) {
        return wrapper.$canDropOver(recyclerView, current, target);
    }

    public abstract boolean onMove(RecyclerView recyclerView, T viewHolder, T target);

    boolean isLongPressDragEnabled() {
        return wrapper.$isLongPressDragEnabled();
    }

    boolean isItemViewSwipeEnabled() {
        return wrapper.$isItemViewSwipeEnabled();
    }

    int getBoundingBoxMargin() {
        return wrapper.$getBoundingBoxMargin();
    }

    float getSwipeThreshold(T viewHolder) {
        return wrapper.$getSwipeThreshold(viewHolder);
    }

    float getMoveThreshold(T viewHolder) {
        return wrapper.$getMoveThreshold(viewHolder);
    }

    float getSwipeEscapeVelocity(float defaultValue) {
        return wrapper.$getSwipeEscapeVelocity(defaultValue);
    }

    float getSwipeVelocityThreshold(float defaultValue) {
        return wrapper.$getSwipeVelocityThreshold(defaultValue);
    }

    public RecyclerView.ViewHolder chooseDropTarget(T selected, List<T> dropTargets, int curX, int curY) {
        return wrapper.$chooseDropTarget(selected, (List<RecyclerView.ViewHolder>) dropTargets, curX, curY);
    }

    public abstract void onSwiped(T viewHolder, int direction);

    void onSelectedChanged(T viewHolder, int actionState) {
        wrapper.$onSelectedChanged(viewHolder, actionState);
    }

    void onMoved(RecyclerView recyclerView, T viewHolder, int fromPos, T target, int toPos, int x, int y) {
        wrapper.$onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
    }

    public void clearView(RecyclerView recyclerView, T viewHolder) {
        wrapper.$clearView(recyclerView, viewHolder);
    }

    void onChildDraw(Canvas c, RecyclerView recyclerView, T viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        wrapper.$onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    void onChildDrawOver(Canvas c, RecyclerView recyclerView, T viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        wrapper.$onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
        return wrapper.$getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
    }

    int interpolateOutOfBoundsScroll(RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
        return wrapper.$interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll);
    }

    ItemTouchHelper.Callback asCallback(){
        return wrapper;
    }

    private static class Wrapper<T extends RecyclerView.ViewHolder> extends ItemTouchHelper.SimpleCallback {
        private final TypedItemTouchHelperCallback<T> delegate;

        private Wrapper(TypedItemTouchHelperCallback<T> delegate) {
            super(0, 0);
            this.delegate = delegate;
        }

        @Override
        public void setDefaultSwipeDirs(int defaultSwipeDirs) {
            delegate.setDefaultSwipeDirs(defaultSwipeDirs);
        }

        @Override
        public void setDefaultDragDirs(int defaultDragDirs) {
            delegate.setDefaultDragDirs(defaultDragDirs);
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return delegate.getSwipeDirs(recyclerView, (T) viewHolder);
        }

        @Override
        public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return delegate.getDragDirs(recyclerView, (T) viewHolder);
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return delegate.getMovementFlags(recyclerView, (T) viewHolder);
        }

        @Override
        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
            return delegate.convertToAbsoluteDirection(flags, layoutDirection);
        }

        @Override
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
            return delegate.canDropOver(recyclerView, (T) current, (T) target);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return delegate.onMove(recyclerView, (T) viewHolder, (T) target);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return delegate.isLongPressDragEnabled();
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return delegate.isItemViewSwipeEnabled();
        }

        @Override
        public int getBoundingBoxMargin() {
            return delegate.getBoundingBoxMargin();
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return delegate.getSwipeThreshold((T) viewHolder);
        }

        @Override
        public float getMoveThreshold(RecyclerView.ViewHolder viewHolder) {
            return delegate.getMoveThreshold((T) viewHolder);
        }

        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {
            return delegate.getSwipeEscapeVelocity(defaultValue);
        }

        @Override
        public float getSwipeVelocityThreshold(float defaultValue) {
            return delegate.getSwipeVelocityThreshold(defaultValue);
        }

        @Override
        public RecyclerView.ViewHolder chooseDropTarget(RecyclerView.ViewHolder selected, List<RecyclerView.ViewHolder> dropTargets, int curX, int curY) {
            return delegate.chooseDropTarget((T) selected, (List<T>) dropTargets, curX, curY);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            delegate.onSwiped((T) viewHolder, direction);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            delegate.onSelectedChanged((T) viewHolder, actionState);
        }

        @Override
        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
            delegate.onMoved(recyclerView, (T) viewHolder, fromPos, (T) target, toPos, x, y);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            delegate.clearView(recyclerView, (T) viewHolder);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            delegate.onChildDraw(c, recyclerView, (T) viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            delegate.onChildDrawOver(c, recyclerView, (T) viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
            return delegate.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
        }

        @Override
        public int interpolateOutOfBoundsScroll(RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            return delegate.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll);
        }

        private void $setDefaultSwipeDirs(int defaultSwipeDirs) {
            super.setDefaultSwipeDirs(defaultSwipeDirs);
        }

        private void $setDefaultDragDirs(int defaultDragDirs) {
            super.setDefaultDragDirs(defaultDragDirs);
        }

        private int $getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        private int $getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return super.getDragDirs(recyclerView, viewHolder);
        }

        private int $getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return super.getMovementFlags(recyclerView, viewHolder);
        }

        private int $convertToAbsoluteDirection(int flags, int layoutDirection) {
            return super.convertToAbsoluteDirection(flags, layoutDirection);
        }

        private boolean $canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
            return super.canDropOver(recyclerView, current, target);
        }

        private boolean $isLongPressDragEnabled() {
            return super.isLongPressDragEnabled();
        }

        private boolean $isItemViewSwipeEnabled() {
            return super.isItemViewSwipeEnabled();
        }

        private int $getBoundingBoxMargin() {
            return super.getBoundingBoxMargin();
        }

        private float $getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return super.getSwipeThreshold(viewHolder);
        }

        private float $getMoveThreshold(RecyclerView.ViewHolder viewHolder) {
            return super.getMoveThreshold(viewHolder);
        }

        private float $getSwipeEscapeVelocity(float defaultValue) {
            return super.getSwipeEscapeVelocity(defaultValue);
        }

        private float $getSwipeVelocityThreshold(float defaultValue) {
            return super.getSwipeVelocityThreshold(defaultValue);
        }

        private RecyclerView.ViewHolder $chooseDropTarget(RecyclerView.ViewHolder selected, List<RecyclerView.ViewHolder> dropTargets, int curX, int curY) {
            return super.chooseDropTarget(selected, dropTargets, curX, curY);
        }

        private void $onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
        }

        private void $onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }

        private void $clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
        }

        private void $onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        private void $onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        private long $getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
            return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
        }

        private int $interpolateOutOfBoundsScroll(RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            return super.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll);
        }

    }
}
