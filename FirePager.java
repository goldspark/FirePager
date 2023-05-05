package com.innerin.retreat.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Simple pager for Firebase which you can use to load more items
 * by Luka Kolic
 */
public class FirePager {

    public static String TAG = "MYAPP";

    /**
     * Used to be implemented by RecyclerView.Adapter extended class
     */
    public interface IAdapter {
        void addItem(GlobalModel model);
        void addItems(List<GlobalModel> models);
        int getCount();
    }

    /**
     * Base Model class to be extended by
     * Serializable firebase model classes
     * I recommend getValue being int as it
     * is best to compare int for example id
     * in equals and more performant
     */
    public static class GlobalModel{
        public int getValue()
        {
            return 0;
        }
    }


    public static <T extends GlobalModel> void loadReviews(IAdapter adapter, List<T> models, int limit, DatabaseReference ref, String childToOrderBy, Class<T> modelClass){
        if (limit == 0) {
            return;
        }

        Query query = ref
                .orderByChild(childToOrderBy)
                .limitToFirst(limit);

        if (adapter.getCount() < 1) {
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        List<GlobalModel> newModels = new ArrayList<>();
                        for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                            T model = reviewSnapshot.getValue(modelClass);
                            newModels.add(model);
                        }
                        //Log.d(TAG, "Loading more reviews");

                        adapter.addItems(newModels);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load reviews", error.toException());
                }
            });
        } else {
            GlobalModel lastModel = models.get(models.size() - 1);
            Query filteredQuery = query.startAfter(lastModel.getValue());
            filteredQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        List<T> newModels = new ArrayList<>();
                        for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                            T model = reviewSnapshot.getValue(modelClass);
                            if (!models.contains(model)) {
                                newModels.add(model);
                            }
                        }

                        adapter.addItems((List<GlobalModel>) newModels);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load more reviews", error.toException());
                }
            });
        }
    }
}
