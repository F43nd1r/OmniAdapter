package com.faendir.omniadapter.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faendir.omniadapter.model.Action;
import com.faendir.omniadapter.model.Component;
import com.faendir.omniadapter.model.DeepObservableList;
import com.faendir.omniadapter.OmniAdapter;
import com.faendir.omniadapter.OmniBuilder;
import com.faendir.omniadapter.model.SelectionMode;
import com.faendir.omniadapter.model.SimpleComposite;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DeepObservableList<Component> observableList;
    private int counter = 0;
    private OmniAdapter<Component> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        observableList = new DeepObservableList<>(Component.class);
        Composite composite = new Composite("composite");
        composite.getChildren().add(new Leaf("leaf"));
        observableList.add(composite);
        adapter = new OmniBuilder<>(this, observableList,new SimpleController())
                .setClick(new Action.Click(Action.NONE)
                        .setDefaultCompositeAction(Action.EXPAND))
                .setLongClick(new Action.LongClick(Action.DRAG))
                .setSwipeToRight(new Action.Swipe(Action.REMOVE))
                .setSwipeToLeft(new Action.Swipe(Action.SELECT))
                .enableUndoForAction(Action.MOVE, "Item moved")
                .enableUndoForAction(Action.REMOVE, "Item removed")
                .setExpandUntilLevelOnStartup(1)
                .setSelectionMode(SelectionMode.SINGLE)
                .setInsetDpPerLevel(10)
                .attach(recyclerView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.commitPendingUndoIfAny();
    }

    @Override
    public void onClick(View view) {
        List<Component> selection = adapter.getSelection();
        DeepObservableList<Component> addTo = !selection.isEmpty() && selection.get(0) instanceof Composite
                ? ((Composite) selection.get(0)).getChildren() : observableList;
        switch (view.getId()){
            case R.id.button_addLeaf:
                addTo.add(new Leaf("leaf" + counter));
                break;
            case R.id.button_addComposite:
                addTo.add(new Composite("composite" + counter));
                break;
        }
        if(selection.get(0) instanceof Composite){
            ((Composite) selection.get(0)).getState().setExpanded(true);
        }
        counter++;
    }

    public static class Leaf extends com.faendir.omniadapter.model.Leaf {
        private final String name;

        private Leaf(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Composite extends SimpleComposite<Component> {
        private final String name;

        private Composite(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class SimpleController extends OmniAdapter.BaseController<Component> {

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
}
