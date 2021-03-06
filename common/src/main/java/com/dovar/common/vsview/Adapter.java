package com.dovar.common.vsview;

import android.database.Observable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import static com.dovar.common.vsview.DRecyclerView.NO_ID;

/**
 * Created by Administrator on 2018/5/13.
 */

public abstract class Adapter<VH extends DRecyclerView.ViewHolder> {
    private final AdapterDataObservable mObservable = new AdapterDataObservable();
    private boolean mHasStableIds = false;

    /**
     * Called when RecyclerView needs a new {@link DRecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(DRecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(DRecyclerView.ViewHolder, int)
     */
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link DRecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link android.widget.ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link DRecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(DRecyclerView.ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    public abstract void onBindViewHolder(VH holder, int position);

    /**
     * Called by RecyclerView to display the data at the specified position. This method
     * should update the contents of the {@link DRecyclerView.ViewHolder#itemView} to reflect the item at
     * the given position.
     * <p>
     * Note that unlike {@link android.widget.ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link DRecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Partial bind vs full bind:
     * <p>
     * The payloads parameter is a merge list from {@link #notifyItemChanged(int, Object)} or
     * {@link #notifyItemRangeChanged(int, int, Object)}.  If the payloads list is not empty,
     * the ViewHolder is currently bound to old data and Adapter may run an efficient partial
     * update using the payload info.  If the payload is empty,  Adapter must run a full bind.
     * Adapter should not assume that the payload passed in notify methods will be received by
     * onBindViewHolder().  For example when the view is not attached to the screen, the
     * payload in notifyItemChange() will be simply dropped.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @param payloads A non-null list of merged payloads. Can be empty list if requires full
     *                 update.
     */
    public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, position);
    }

    /**
     * This method calls {@link #onCreateViewHolder(ViewGroup, int)} to create a new
     * {@link DRecyclerView.ViewHolder} and initializes some private fields to be used by RecyclerView.
     *
     * @see #onCreateViewHolder(ViewGroup, int)
     */
    public final VH createViewHolder(ViewGroup parent, int viewType) {
        final VH holder = onCreateViewHolder(parent, viewType);
        holder.mItemViewType = viewType;
        return holder;
    }

    /**
     * This method internally calls {@link #onBindViewHolder(DRecyclerView.ViewHolder, int)} to update the
     * {@link DRecyclerView.ViewHolder} contents with the item at the given position and also sets up some
     * private fields to be used by RecyclerView.
     *
     * @see #onBindViewHolder(DRecyclerView.ViewHolder, int)
     */
    public final void bindViewHolder(VH holder, int position) {
        holder.mPosition = position;
        if (hasStableIds()) {
            holder.mItemId = getItemId(position);
        }
        holder.setFlags(DRecyclerView.ViewHolder.FLAG_BOUND,
                DRecyclerView.ViewHolder.FLAG_BOUND | DRecyclerView.ViewHolder.FLAG_UPDATE | DRecyclerView.ViewHolder.FLAG_INVALID
                        | DRecyclerView.ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN);
        onBindViewHolder(holder, position, holder.getUnmodifiedPayloads());
        holder.clearPayload();
        final ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams instanceof RecyclerView.LayoutParams) {
            ((DRecyclerView.LayoutParams) layoutParams).mInsetsDirty = true;
        }
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    public int getItemViewType(int position) {
        return 0;
    }

    public abstract int getItemCount();

    /**
     * Indicates whether each item in the data set can be represented with a unique identifier
     * of type {@link java.lang.Long}.
     *
     * @param hasStableIds Whether items in data set have unique identifiers or not.
     * @see #hasStableIds()
     * @see #getItemId(int)
     */
    public void setHasStableIds(boolean hasStableIds) {
        if (hasObservers()) {
            throw new IllegalStateException("Cannot change whether this adapter has " +
                    "stable IDs while the adapter has registered observers.");
        }
        mHasStableIds = hasStableIds;
    }

    /**
     * Return the stable ID for the item at <code>position</code>. If {@link #hasStableIds()}
     * would return false this method should return {@link DRecyclerView#NO_ID}. The default implementation
     * of this method returns {@link DRecyclerView#NO_ID}.
     *
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    public long getItemId(int position) {
        return NO_ID;
    }

    /**
     * Returns true if this adapter publishes a unique <code>long</code> value that can
     * act as a key for the item at a given position in the data set. If that item is relocated
     * in the data set, the ID returned for that item should be the same.
     *
     * @return true if this adapter's items have stable IDs
     */
    public final boolean hasStableIds() {
        return mHasStableIds;
    }

    /**
     * Called when a view created by this adapter has been recycled.
     * <p>
     * <p>A view is recycled when a {@link DRecyclerView.LayoutManager} decides that it no longer
     * needs to be attached to its parent {@link RecyclerView}. This can be because it has
     * fallen out of visibility or a set of cached views represented by views still
     * attached to the parent RecyclerView. If an item view has large or expensive data
     * bound to it such as large bitmaps, this may be a good place to release those
     * resources.</p>
     * <p>
     * RecyclerView calls this method right before clearing ViewHolder's internal data and
     * sending it to RecycledViewPool. This way, if ViewHolder was holding valid information
     * before being recycled, you can call {@link DRecyclerView.ViewHolder#getAdapterPosition()} to get
     * its adapter position.
     *
     * @param holder The ViewHolder for the view being recycled
     */
    public void onViewRecycled(VH holder) {
    }

    /**
     * Called by the RecyclerView if a ViewHolder created by this Adapter cannot be recycled
     * due to its transient state. Upon receiving this callback, Adapter can clear the
     * animation(s) that effect the View's transient state and return <code>true</code> so that
     * the View can be recycled. Keep in mind that the View in question is already removed from
     * the RecyclerView.
     * <p>
     * In some cases, it is acceptable to recycle a View although it has transient state. Most
     * of the time, this is a case where the transient state will be cleared in
     * {@link #onBindViewHolder(DRecyclerView.ViewHolder, int)} call when View is rebound to a new position.
     * For this reason, RecyclerView leaves the decision to the Adapter and uses the return
     * value of this method to decide whether the View should be recycled or not.
     * <p>
     * Note that when all animations are created by {@link RecyclerView.ItemAnimator}, you
     * should never receive this callback because RecyclerView keeps those Views as children
     * until their animations are complete. This callback is useful when children of the item
     * views create animations which may not be easy to implement using an {@link DRecyclerView.ItemAnimator}.
     * <p>
     * You should <em>never</em> fix this issue by calling
     * <code>holder.itemView.setHasTransientState(false);</code> unless you've previously called
     * <code>holder.itemView.setHasTransientState(true);</code>. Each
     * <code>View.setHasTransientState(true)</code> call must be matched by a
     * <code>View.setHasTransientState(false)</code> call, otherwise, the state of the View
     * may become inconsistent. You should always prefer to end or cancel animations that are
     * triggering the transient state instead of handling it manually.
     *
     * @param holder The ViewHolder containing the View that could not be recycled due to its
     *               transient state.
     * @return True if the View should be recycled, false otherwise. Note that if this method
     * returns <code>true</code>, RecyclerView <em>will ignore</em> the transient state of
     * the View and recycle it regardless. If this method returns <code>false</code>,
     * RecyclerView will check the View's transient state again before giving a final decision.
     * Default implementation returns false.
     */
    public boolean onFailedToRecycleView(VH holder) {
        return false;
    }

    /**
     * Called when a view created by this adapter has been attached to a window.
     * <p>
     * <p>This can be used as a reasonable signal that the view is about to be seen
     * by the user. If the adapter previously freed any resources in
     * {@link #onViewDetachedFromWindow(DRecyclerView.ViewHolder) onViewDetachedFromWindow}
     * those resources should be restored here.</p>
     *
     * @param holder Holder of the view being attached
     */
    public void onViewAttachedToWindow(VH holder) {
    }

    /**
     * Called when a view created by this adapter has been detached from its window.
     * <p>
     * <p>Becoming detached from the window is not necessarily a permanent condition;
     * the consumer of an Adapter's views may choose to cache views offscreen while they
     * are not visible, attaching and detaching them as appropriate.</p>
     *
     * @param holder Holder of the view being detached
     */
    public void onViewDetachedFromWindow(VH holder) {
    }

    /**
     * Returns true if one or more observers are attached to this adapter.
     *
     * @return true if this adapter has observers
     */
    public final boolean hasObservers() {
        return mObservable.hasObservers();
    }

    /**
     * Register a new observer to listen for data changes.
     * <p>
     * <p>The adapter may publish a variety of events describing specific changes.
     * Not all adapters may support all change types and some may fall back to a generic
     * {@link android.support.v7.widget.RecyclerView.AdapterDataObserver#onChanged()
     * "something changed"} event if more specific data is not available.</p>
     * <p>
     * <p>Components registering observers with an adapter are responsible for
     * {@link #unregisterAdapterDataObserver(AdapterDataObserver)
     * unregistering} those observers when finished.</p>
     *
     * @param observer Observer to register
     * @see #unregisterAdapterDataObserver(AdapterDataObserver)
     */
    public void registerAdapterDataObserver(AdapterDataObserver observer) {
        mObservable.registerObserver(observer);
    }

    /**
     * Unregister an observer currently listening for data changes.
     * <p>
     * <p>The unregistered observer will no longer receive events about changes
     * to the adapter.</p>
     *
     * @param observer Observer to unregister
     * @see #registerAdapterDataObserver(AdapterDataObserver)
     */
    public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    /**
     * Called by RecyclerView when it starts observing this Adapter.
     * <p>
     * Keep in mind that same adapter may be observed by multiple RecyclerViews.
     *
     * @param recyclerView The RecyclerView instance which started observing this adapter.
     * @see #onDetachedFromRecyclerView(DRecyclerView)
     */
    public void onAttachedToRecyclerView(DRecyclerView recyclerView) {
    }

    /**
     * Called by RecyclerView when it stops observing this Adapter.
     *
     * @param recyclerView The RecyclerView instance which stopped observing this adapter.
     * @see #onAttachedToRecyclerView(DRecyclerView)
     */
    public void onDetachedFromRecyclerView(DRecyclerView recyclerView) {
    }

    /**
     * Notify any registered observers that the data set has changed.
     * <p>
     * <p><code>RecyclerView</code> will attempt to synthesize visible structural change events
     * for adapters that report that they have {@link #hasStableIds() stable IDs} when
     * this method is used. This can help for the purposes of animation and visual
     * object persistence but individual item views will still need to be rebound
     * and relaid out.</p>
     * <p>
     *
     * @see #notifyItemChanged(int)
     * @see #notifyItemInserted(int)
     * @see #notifyItemRemoved(int)
     * @see #notifyItemRangeChanged(int, int)
     * @see #notifyItemRangeInserted(int, int)
     * @see #notifyItemRangeRemoved(int, int)
     */
    public final void notifyDataSetChanged() {
        mObservable.notifyChanged();
    }

    /**
     * Notify any registered observers that the item at <code>position</code> has changed.
     * Equivalent to calling <code>notifyItemChanged(position, null);</code>.
     *
     * @see #notifyItemRangeChanged(int, int)
     */
    public final void notifyItemChanged(int position) {
        mObservable.notifyItemRangeChanged(position, 1);
    }

    /**
     * Notify any registered observers that the item at <code>position</code> has changed with an
     * optional payload object.
     * <p>
     * Client can optionally pass a payload for partial change. These payloads will be merged
     * and may be passed to adapter's {@link #onBindViewHolder(DRecyclerView.ViewHolder, int, List)} if the
     * item is already represented by a ViewHolder and it will be rebound to the same
     * ViewHolder. A notifyItemRangeChanged() with null payload will clear all existing
     * payloads on that item and prevent future payload until
     * {@link #onBindViewHolder(DRecyclerView.ViewHolder, int, List)} is called. Adapter should not assume
     * that the payload will always be passed to onBindViewHolder(), e.g. when the view is not
     * attached, the payload will be simply dropped.
     *
     * @param payload  Optional parameter, use null to identify a "full" update
     * @see #notifyItemRangeChanged(int, int)
     */
    public final void notifyItemChanged(int position, Object payload) {
        mObservable.notifyItemRangeChanged(position, 1, payload);
    }

    /**
     * Notify any registered observers that the <code>itemCount</code> items starting at
     * position <code>positionStart</code> have changed.
     * Equivalent to calling <code>notifyItemRangeChanged(position, itemCount, null);</code>.
     *
     * @param positionStart Position of the first item that has changed
     * @param itemCount     Number of items that have changed
     * @see #notifyItemChanged(int)
     */
    public final void notifyItemRangeChanged(int positionStart, int itemCount) {
        mObservable.notifyItemRangeChanged(positionStart, itemCount);
    }

    /**
     * Notify any registered observers that the <code>itemCount</code> items starting at
     * position <code>positionStart</code> have changed. An optional payload can be
     * passed to each changed item.
     * <p>
     * Client can optionally pass a payload for partial change. These payloads will be merged
     * and may be passed to adapter's {@link #onBindViewHolder(DRecyclerView.ViewHolder, int, List)} if the
     * item is already represented by a ViewHolder and it will be rebound to the same
     * ViewHolder. A notifyItemRangeChanged() with null payload will clear all existing
     * payloads on that item and prevent future payload until
     * {@link #onBindViewHolder(DRecyclerView.ViewHolder, int, List)} is called. Adapter should not assume
     * that the payload will always be passed to onBindViewHolder(), e.g. when the view is not
     * attached, the payload will be simply dropped.
     *
     * @param positionStart Position of the first item that has changed
     * @param itemCount     Number of items that have changed
     * @param payload       Optional parameter, use null to identify a "full" update
     * @see #notifyItemChanged(int)
     */
    public final void notifyItemRangeChanged(int positionStart, int itemCount, Object payload) {
        mObservable.notifyItemRangeChanged(positionStart, itemCount, payload);
    }

    /**
     * Notify any registered observers that the item reflected at <code>position</code>
     * has been newly inserted. The item previously at <code>position</code> is now at
     * position <code>position + 1</code>.
     *
     * @param position Position of the newly inserted item in the data set
     * @see #notifyItemRangeInserted(int, int)
     */
    public final void notifyItemInserted(int position) {
        mObservable.notifyItemRangeInserted(position, 1);
    }

    /**
     * Notify any registered observers that the item reflected at <code>fromPosition</code>
     * has been moved to <code>toPosition</code>.
     *
     * @param fromPosition Previous position of the item.
     * @param toPosition   New position of the item.
     */
    public final void notifyItemMoved(int fromPosition, int toPosition) {
        mObservable.notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * Notify any registered observers that the currently reflected <code>itemCount</code>
     * items starting at <code>positionStart</code> have been newly inserted. The items
     * previously located at <code>positionStart</code> and beyond can now be found starting
     * at position <code>positionStart + itemCount</code>.
     *
     * @param positionStart Position of the first item that was inserted
     * @param itemCount     Number of items inserted
     * @see #notifyItemInserted(int)
     */
    public final void notifyItemRangeInserted(int positionStart, int itemCount) {
        mObservable.notifyItemRangeInserted(positionStart, itemCount);
    }

    /**
     * Notify any registered observers that the item previously located at <code>position</code>
     * has been removed from the data set. The items previously located at and after
     * <code>position</code> may now be found at <code>oldPosition - 1</code>.
     *
     * @see #notifyItemRangeRemoved(int, int)
     */
    public final void notifyItemRemoved(int position) {
        mObservable.notifyItemRangeRemoved(position, 1);
    }

    /**
     * Notify any registered observers that the <code>itemCount</code> items previously
     * located at <code>positionStart</code> have been removed from the data set. The items
     * previously located at and after <code>positionStart + itemCount</code> may now be found
     * at <code>oldPosition - itemCount</code>.
     *
     * @param positionStart Previous position of the first item that was removed
     * @param itemCount     Number of items removed from the data set
     */
    public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
        mObservable.notifyItemRangeRemoved(positionStart, itemCount);
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        public boolean hasObservers() {
            return !mObservers.isEmpty();
        }

        public void notifyChanged() {
            // since onChanged() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount, null);
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount, Object payload) {
            // since onItemRangeChanged() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeChanged(positionStart, itemCount, payload);
            }
        }

        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            // since onItemRangeInserted() is implemented by the app, it could do anything,
            // including removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeInserted(positionStart, itemCount);
            }
        }

        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            // since onItemRangeRemoved() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeRemoved(positionStart, itemCount);
            }
        }

        public void notifyItemMoved(int fromPosition, int toPosition) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeMoved(fromPosition, toPosition, 1);
            }
        }
    }

    public static abstract class AdapterDataObserver {
        public void onChanged() {
            // Do nothing
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
            // do nothing
        }

        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            // fallback to onItemRangeChanged(positionStart, itemCount) if app
            // does not override this method.
            onItemRangeChanged(positionStart, itemCount);
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            // do nothing
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            // do nothing
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            // do nothing
        }
    }
}
