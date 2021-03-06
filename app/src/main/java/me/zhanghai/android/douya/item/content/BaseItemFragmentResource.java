/*
 * Copyright (c) 2017 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.item.content;

import android.os.Bundle;

import java.util.List;

import me.zhanghai.android.douya.app.TargetedRetainedFragment;
import me.zhanghai.android.douya.network.api.ApiError;
import me.zhanghai.android.douya.network.api.info.frodo.CollectableItem;
import me.zhanghai.android.douya.network.api.info.frodo.Photo;
import me.zhanghai.android.douya.network.api.info.frodo.Rating;
import me.zhanghai.android.douya.network.api.info.frodo.Review;
import me.zhanghai.android.douya.photo.content.ItemPhotoListResource;
import me.zhanghai.android.douya.review.content.ItemReviewListResource;
import me.zhanghai.android.douya.util.FragmentUtils;

public abstract class BaseItemFragmentResource<SimpleItemType extends CollectableItem,
        ItemType extends SimpleItemType> extends TargetedRetainedFragment
        implements BaseItemResource.Listener<ItemType>, RatingResource.Listener,
        ItemPhotoListResource.Listener, ItemReviewListResource.Listener {

    private final String KEY_PREFIX = getClass().getName() + '.';

    private final String EXTRA_ITEM_ID = KEY_PREFIX + "item_id";
    private final String EXTRA_SIMPLE_ITEM = KEY_PREFIX + "simple_item";
    private final String EXTRA_ITEM = KEY_PREFIX + "item";

    private static final int ITEM_ID_INVALID = -1;

    private long mItemId = ITEM_ID_INVALID;
    private SimpleItemType mSimpleItem;

    private ItemType mItem;

    private BaseItemResource<SimpleItemType, ItemType> mItemResource;
    private RatingResource mRatingResource;
    private ItemPhotoListResource mPhotoListResource;
    private ItemReviewListResource mReviewListResource;

    private boolean mHasError;

    protected BaseItemFragmentResource setArguments(long itemId, SimpleItemType simpleItem,
                                                    ItemType item) {
        Bundle arguments = FragmentUtils.ensureArguments(this);
        arguments.putLong(EXTRA_ITEM_ID, itemId);
        arguments.putParcelable(EXTRA_SIMPLE_ITEM, simpleItem);
        arguments.putParcelable(EXTRA_ITEM, item);
        return this;
    }

    public long getItemId() {
        // Can be called before onCreate() is called.
        ensureArguments();
        return mItemId;
    }

    public SimpleItemType getSimpleItem() {
        // Can be called before onCreate() is called.
        ensureArguments();
        return mSimpleItem;
    }

    public boolean hasSimpleItem() {
        // Can be called before onCreate() is called.
        ensureArguments();
        return mSimpleItem != null;
    }

    public ItemType getItem() {
        // Can be called before onCreate() is called.
        ensureArguments();
        return mItem;
    }

    public boolean hasItem() {
        // Can be called before onCreate() is called.
        ensureArguments();
        return mItem != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ensureArguments();

        mItemResource = onAttachItemResource(mItemId, mSimpleItem, mItem);
        CollectableItem.Type itemType = getItemType();
        mRatingResource = RatingResource.attachTo(itemType, mItemId, this);
        if (hasPhotoList()) {
            mPhotoListResource = ItemPhotoListResource.attachTo(itemType, mItemId, this);
        }
        if (hasReviewList()) {
            mReviewListResource = ItemReviewListResource.attachTo(itemType, mItemId, this);
        }
    }

    private void ensureArguments() {
        if (mItemId != ITEM_ID_INVALID) {
            return;
        }
        Bundle arguments = getArguments();
        mItem = arguments.getParcelable(EXTRA_ITEM);
        if (mItem != null) {
            mSimpleItem = mItem;
            mItemId = mItem.id;
        } else {
            mSimpleItem = arguments.getParcelable(EXTRA_SIMPLE_ITEM);
            if (mSimpleItem != null) {
                mItemId = mSimpleItem.id;
            } else {
                mItemId = arguments.getLong(EXTRA_ITEM_ID);
            }
        }
    }

    protected abstract BaseItemResource<SimpleItemType, ItemType> onAttachItemResource(
            long itemId, SimpleItemType simpleItem, ItemType item);

    protected abstract CollectableItem.Type getItemType();

    protected abstract boolean hasPhotoList();

    protected abstract boolean hasReviewList();

    @Override
    public void onDestroy() {
        super.onDestroy();

        mItemResource.detach();
        mRatingResource.detach();
        if (mPhotoListResource != null) {
            mPhotoListResource.detach();
        }
        if (mReviewListResource != null) {
            mReviewListResource.detach();
        }

        Bundle arguments = getArguments();
        arguments.putLong(EXTRA_ITEM_ID, mItemId);
        arguments.putParcelable(EXTRA_SIMPLE_ITEM, mSimpleItem);
        arguments.putParcelable(EXTRA_ITEM, mItem);
    }

    @Override
    public void onLoadItemStarted(int requestCode) {}

    @Override
    public void onLoadItemFinished(int requestCode) {}

    @Override
    public void onLoadItemError(int requestCode, ApiError error) {
        notifyError(error);
    }

    @Override
    public void onItemChanged(int requestCode, ItemType newItem) {
        mItem = newItem;
        mSimpleItem = newItem;
        mItemId = newItem.id;
        getListener().onItemChanged(getRequestCode(), newItem);
        notifyChangedIfLoaded();
    }

    // TODO: Item collection.
    //@Override
    //public void onItemWriteStarted(int requestCode) {
    //    getListener().onItemWriteStarted(getRequestCode());
    //}
    //
    //@Override
    //public void onItemWriteFinished(int requestCode) {
    //    getListener().onItemWriteFinished(getRequestCode());
    //}

    @Override
    public void onLoadRatingStarted(int requestCode) {}

    @Override
    public void onLoadRatingFinished(int requestCode) {}

    @Override
    public void onLoadRatingError(int requestCode, ApiError error) {
        notifyError(error);
    }

    @Override
    public void onRatingChanged(int requestCode, Rating newRating) {
        notifyChangedIfLoaded();
    }

    @Override
    public void onLoadPhotoListStarted(int requestCode) {}

    @Override
    public void onLoadPhotoListFinished(int requestCode) {}

    @Override
    public void onLoadPhotoListError(int requestCode, ApiError error) {
        notifyError(error);
    }

    @Override
    public void onPhotoListChanged(int requestCode, List<Photo> newPhotoList) {
        notifyChangedIfLoaded();
    }

    @Override
    public void onPhotoListAppended(int requestCode, List<Photo> appendedPhotoList) {
        notifyChangedIfLoaded();
    }

    @Override
    public void onPhotoRemoved(int requestCode, int position) {
        notifyChangedIfLoaded();
    }

    @Override
    public void onLoadReviewListStarted(int requestCode) {}

    @Override
    public void onLoadReviewListFinished(int requestCode) {}

    @Override
    public void onLoadReviewListError(int requestCode, ApiError error) {
        notifyError(error);
    }

    @Override
    public void onReviewListChanged(int requestCode, List<Review> newReviewList) {
        notifyChangedIfLoaded();
    }

    @Override
    public void onReviewListAppended(int requestCode, List<Review> appendedReviewList) {
        notifyChangedIfLoaded();
    }

    @Override
    public void onReviewChanged(int requestCode, int position, Review newReview) {
        notifyChangedIfLoaded();
    }

    @Override
    public void onReviewRemoved(int requestCode, int position) {
        notifyChangedIfLoaded();
    }

    public boolean isLoaded() {
        return hasItem() && (mRatingResource != null && mRatingResource.has())
                && (!hasPhotoList() || (mPhotoListResource != null && mPhotoListResource.has()))
                && (!hasReviewList() || (mReviewListResource != null && mReviewListResource.has()));
    }

    public void notifyChangedIfLoaded() {
        if (isLoaded()) {
            // HACK: Add SimpleRating to Rating.
            ItemType item = getItem();
            Rating rating = mRatingResource.get();
            rating.rating = item.rating;
            notifyChanged(getRequestCode(), item, rating, mPhotoListResource.get(),
                    mReviewListResource.get());
        }
    }

    protected abstract void notifyChanged(int requestCode, ItemType newItem, Rating newRating,
                                          List<Photo> newPhotoList, List<Review> newReviewList);

    private void notifyError(ApiError error) {
        if (!mHasError) {
            mHasError = true;
            getListener().onLoadError(getRequestCode(), error);
        }
    }

    private Listener<ItemType> getListener() {
        //noinspection unchecked
        return (Listener<ItemType>) getTarget();
    }

    public interface Listener<ItemType> {
        void onLoadError(int requestCode, ApiError error);
        void onItemChanged(int requestCode, ItemType newItem);
        // TODO: Item collection
        //void onItemWriteStarted(int requestCode);
        //void onItemWriteFinished(int requestCode);
    }
}
