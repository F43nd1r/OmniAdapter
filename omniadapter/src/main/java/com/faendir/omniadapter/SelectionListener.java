package com.faendir.omniadapter;

import java.util.List;

/**
 * Created on 10.08.2016.
 *
 * @author F43nd1r
 */

public interface SelectionListener<T extends Component> {
    void onSelectionChanged(List<T> selected);
    void onSelectionCleared();
}
