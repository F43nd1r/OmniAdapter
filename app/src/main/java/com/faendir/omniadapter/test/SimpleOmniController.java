package com.faendir.omniadapter.test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faendir.omniadapter.Component;
import com.faendir.omniadapter.OmniAdapter;

/**
 * Created on 08.08.2016.
 *
 * @author F43nd1r
 */

public class SimpleOmniController extends OmniAdapter.BaseController<Component> {

    @Override
    public View createView(ViewGroup parent, int level) {
        return LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    @Override
    public void bindView(View view, Component component, int level) {
        String name = component.toString();
        ((TextView) view.findViewById(android.R.id.text1)).setText("Level " + level + " " + name);
    }
}
