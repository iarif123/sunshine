package com.example.android.sunshine.app;

import android.support.v7.widget.RecyclerView;


import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple adapter implementation analog to {@link android.widget.ArrayAdapter} for {@link
 * android.support.v7.widget.RecyclerView}. Holds to a list of objects of type {@link T}
 *
 * Created by Pascal Welsch on 04.07.14.
 *
 * source: https://gist.github.com/passsy/f8eecc97c37e3de46176
 */
public class ArrayAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<ArrayAdapter.ViewHolder> {

    private ListItemClickListener mOnClickListener;
    private List<T> mObjects;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            text= (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }

    @Override
    public ArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ArrayAdapter.ViewHolder holder, int position) {
        holder.text.setText(mObjects.get(position).toString());
    }



    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public interface StableIdProvider<T> {

        Object getId(T item);
    }



    public ArrayAdapter(@Nullable final List<T> objects, @Nullable ListItemClickListener listener) {
        mObjects = objects != null ? objects : new ArrayList<T>();
        mOnClickListener = listener;
    }
    public ArrayAdapter(@Nullable final List<T> objects) {
        mObjects = objects != null ? objects : new ArrayList<T>();
    }
    public ArrayAdapter() {
        this(null);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(final T object) {
        mObjects.add(object);
        notifyItemInserted(getItemCount() - 1);
    }

    /**
     * Adds the specified list of objects at the end of the array.
     *
     * @param objects The objects to add at the end of the array.
     */
    public void addAll(final List<T> objects) {
        if (objects == null) {
            mObjects.clear();
        } else {
            mObjects.addAll(objects);
        }
        notifyDataSetChanged();
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object   The object to insert into the array.
     * @param position The index at which the object must be inserted.
     */
    public void addItem(final int position, final T object) {
        mObjects.add(position, object);
        notifyItemInserted(position);
    }

    /**
     * Adds the specified object to the end of the array.
     *
     * @param object The object to append
     */
    public void addItem(final T object) {
        addItem(mObjects.size(), object);
    }

    public void addItem(final T object, ListItemClickListener listener) {
        addItem(mObjects.size(), object);
        mOnClickListener = listener;
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        final int size = getItemCount();
        mObjects.clear();
        notifyItemRangeRemoved(0, size);
    }

    public T getItem(final int position) {
        try {
            return mObjects.get(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    public long getItemId(final int position) {
        return position;
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(final T item) {
        return mObjects.indexOf(item);
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        final int position = getPosition(object);
        mObjects.remove(object);
        notifyItemRemoved(position);
    }

    public void removeLastObject() {
        mObjects.remove(mObjects.size() - 1);
        notifyItemRemoved(mObjects.size() - 1);
    }

    public void replaceItem(final T oldObject, final T newObject) {
        final int position = getPosition(oldObject);
        mObjects.remove(position);
        mObjects.add(position, newObject);
        notifyItemChanged(position);
    }

    public void replaceItemWithHeader(final T oldObject, final T newObject) {
        final int position = getPosition(oldObject);
        mObjects.remove(position);
        mObjects.add(position, newObject);
        notifyItemChanged(position + 1);
    }

    /**
     * Removes all elements and replaces them with the given guys from the list, all done with an
     * animation.
     *
     * @param objects The new Objects to display
     */
    public void replaceObjectsWithAnimations(final List<T> objects) {
        animateRemoveObjects(objects);
        animateAddObjects(objects);
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        Collections.sort(mObjects, comparator);
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * replaces the data with the given list
     *
     * @param objects new data
     */
    public void swap(final List<T> objects) {
        swap(objects, new StableIdProvider<T>() {
            @Override
            public Object getId(final T item) {
                // id is unknown, at least return the item itself as id so it doesn't get
                // notified when it doesn't change at all.
                return item;
            }
        });
    }

    /**
     * replaces the data with the given list
     *
     * @param newObjects new data
     * @param idProvider function to determine an identification for each element to detect same
     *                   (but updated) items
     */
    public void swap(final List<T> newObjects, final StableIdProvider<T> idProvider) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public boolean areContentsTheSame(final int oldItemPosition,
                                              final int newItemPosition) {
                final T oldItem = mObjects.get(oldItemPosition);
                final T newItem = newObjects.get(newItemPosition);
                return (oldItem == newItem) || (oldItem != null && oldItem.equals(newItem));
            }

            @Override
            public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
                final T oldItem = mObjects.get(oldItemPosition);
                final T newItem = newObjects.get(newItemPosition);

                if (oldItem == null && newItem == null) {
                    return true;
                }
                if (oldItem == null || newItem == null) {
                    return false;
                }

                final Object oldId = idProvider.getId(oldItem);
                final Object newId = idProvider.getId(newItem);

                return (oldId == newId) || (oldId != null && oldId.equals(newId));
            }

            @Override
            public int getNewListSize() {
                return newObjects != null ? newObjects.size() : 0;
            }

            @Override
            public int getOldListSize() {
                return mObjects != null ? mObjects.size() : 0;
            }
        });
        mObjects = newObjects;
        result.dispatchUpdatesTo(this);
    }

    protected T removeItem(final int position) {
        final T object = mObjects.remove(position);
        notifyItemRemoved(position);
        return object;
    }

    protected T removeItemWithHeader(final int position) {
        final T object = mObjects.remove(position);
        notifyItemRemoved(position + 1);
        return object;
    }

    private void animateAddObjects(final List<T> objects) {
        for (int i = 0; i < objects.size(); i++) {
            final T object = objects.get(i);
            if (!mObjects.contains(object)) {
                addItem(object);
            }
        }
    }

    private void animateRemoveObjects(final List<T> objects) {
        for (int i = mObjects.size() - 1; i >= 0; i--) {
            final T object = mObjects.get(i);
            if (!objects.contains(object)) {
                removeItem(i);
            }
        }
    }

    private void moveItem(final int fromPosition, final int toPosition) {
        final T object = mObjects.remove(fromPosition);
        mObjects.add(toPosition, object);
        notifyItemMoved(fromPosition, toPosition);
    }
}
