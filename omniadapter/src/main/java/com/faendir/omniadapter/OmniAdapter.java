package com.faendir.omniadapter;

import android.support.annotation.IntRange;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created on 09.08.2016.
 *
 * @author F43nd1r
 */
@SuppressWarnings("unused")
public abstract class OmniAdapter<T extends Component> extends RecyclerView.Adapter<ComponentViewHolder<T>>{
    public abstract void clearSelection();

    public abstract List<? extends T> getSelection();

    public abstract List<? extends T> getSelectionByLevel(@IntRange(from = 0) int level);
}
