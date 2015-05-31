package com.thetonrifles.activitydetector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thetonrifles.activitydetector.adapter.AbstractDetectionItem;
import com.thetonrifles.activitydetector.adapter.ListItemType;
import com.thetonrifles.activitydetector.adapter.NewDetectionItem;
import com.thetonrifles.activitydetector.adapter.SameDetectionItem;
import com.thetonrifles.activitydetector.controls.DividerItemDecoration;
import com.thetonrifles.activitydetector.core.DetectionItem;
import com.thetonrifles.activitydetector.core.DetectionManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ui elements
    private RecyclerView mItemsView;
    private ItemsAdapter mItemsAdapter;

    // business objects
    private List<AbstractDetectionItem> mItems;

    // broadcast receivers
    private NewActivityReceiver mNewItemReceiver;
    private SameActivityReceiver mSameItemReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // building ui elements
        mItemsView = (RecyclerView) findViewById(R.id.lst_items);

        // initializing items list
        mItems = new ArrayList<>();

        // defining receiver for new items
        mNewItemReceiver = new NewActivityReceiver();
        mSameItemReceiver = new SameActivityReceiver();

        // populating list
        mItemsAdapter = new ItemsAdapter();
        mItemsView.setLayoutManager(getLayoutManager());
        mItemsView.addItemDecoration(getItemDecoration());
        mItemsView.setAdapter(mItemsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mNewItemReceiver, new IntentFilter(DetectionManager.NEW_DETECTION));
        registerReceiver(mSameItemReceiver, new IntentFilter(DetectionManager.SAME_DETECTION));
        DetectionManager.getInstance().start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNewItemReceiver);
        unregisterReceiver(mSameItemReceiver);
        DetectionManager.getInstance().halt();
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        return llm;
    }

    private RecyclerView.ItemDecoration getItemDecoration() {
        return new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
    }

    /**
     * View holder to be used in the RecyclerView
     */
    private class ItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView title;
        TextView subtitle;

        public ItemViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(android.R.id.text1);
            subtitle = (TextView) view.findViewById(android.R.id.text2);
        }

        @Override
        public void onClick(View view) {
        }

    }

    /**
     * Adapter to be used for rendering items in RecyclerView
     */
    private class ItemsAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).getType().ordinal();
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            AbstractDetectionItem item = mItems.get(position);
            if (item.getType() == ListItemType.NEW_ACTIVITY) {
                holder.title.setText(item.getActivity());
                holder.subtitle.setText(item.getTimestamp());
            } else {
                holder.title.setText("-");
                holder.subtitle.setText("");
            }
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ItemViewHolder(itemView);
        }

    }

    /**
     * Broadcast Receiver for handling activity recognition updates
     * including new detection if compared with latest tracked one.
     */
    public class NewActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DetectionItem item = (DetectionItem) intent.getSerializableExtra(
                    DetectionManager.DETECTED_ACTIVITY);
            mItems.add(new NewDetectionItem(item));
            mItemsAdapter.notifyItemInserted(mItems.size() - 1);
        }

    }

    /**
     * Broadcast Receiver for handling activity recognition updates
     * including detection of same activity as the latest tracked one.
     */
    public class SameActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DetectionItem item = (DetectionItem) intent.getSerializableExtra(
                    DetectionManager.DETECTED_ACTIVITY);
            mItems.add(new SameDetectionItem(item));
            mItemsAdapter.notifyItemInserted(mItems.size() - 1);
        }

    }

}
