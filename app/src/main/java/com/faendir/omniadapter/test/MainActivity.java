package com.faendir.omniadapter.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.faendir.omniadapter.Action;
import com.faendir.omniadapter.Component;
import com.faendir.omniadapter.DeepObservableList;
import com.faendir.omniadapter.OmniAdapter;
import com.faendir.omniadapter.OmniBuilder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    DeepObservableList<Component> observableList;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        observableList = new DeepObservableList<>();
        Composite composite = new Composite("base");
        composite.add(new Leaf("leaf"));
        observableList.add(composite);
        OmniAdapter<Component> adapter = new OmniBuilder<>(this, observableList, new SimpleOmniController(android.R.layout.simple_list_item_1))
                .setClick(new Action.Click(Action.SELECT)
                        .setDefaultCompositeAction(Action.EXPAND))
                .setLongClick(new Action.LongClick(Action.CUSTOM))
                .setSwipeToRight(new Action.Swipe(Action.REMOVE))
                .setExpandUntilLevelOnStartup(1)
                .build();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        Composite components = new Composite("new" + counter);
        ((Composite) observableList.get(0)).add(components);
        components.add(new Leaf("leaf" + counter));
        counter++;
    }

    public static class Leaf extends com.faendir.omniadapter.Leaf {
        private final String name;

        private Leaf(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Composite extends com.faendir.omniadapter.Composite<Component> {
        private final String name;

        private Composite(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
