package com.zero211.moviemaestro;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class AbstractTMDBCardListAdapter<VH extends ViewHolder> extends Adapter<VH>
{
    protected static final String TMDB_IMAGE_PATH_PREFIX = "https://image.tmdb.org/t/p/";
    protected static final String FRESCO_RESOURCES_IMAGE_PATH_PREFIX = "res:///";

    private ArrayList<Map<String,Object>> itemList = new ArrayList<Map<String, Object>>();

    private int total_pages;
    private int total_results;

    private Float cardsPerViewPort;
    private Integer cardLayoutID;

    private Object loadingIndicator;
    private TextView label;
    private RecyclerView recyclerView;

    public AbstractTMDBCardListAdapter(@NonNull Activity activity, @NonNull Integer cardLayoutID, @NonNull Integer recyclerViewID, @Nullable Integer labelID, @Nullable Integer loadingIndicatorID)
    {
        this(activity, cardLayoutID, 2.5f, recyclerViewID, labelID, loadingIndicatorID);
    }

    public AbstractTMDBCardListAdapter(@NonNull Activity activity, @NonNull Integer cardLayoutID, @NonNull Float cardsPerViewPort, @NonNull Integer recyclerViewID, @Nullable Integer labelID, @Nullable Integer loadingIndicatorID)
    {
        if (labelID != null)
        {
            this.label = activity.findViewById(labelID);
        }

        if (loadingIndicatorID != null)
        {
            this.loadingIndicator = activity.findViewById(loadingIndicatorID);
        }

        this.recyclerView = activity.findViewById(recyclerViewID);
        this.recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.setAdapter(this);

        this.cardLayoutID = cardLayoutID;
        this.cardsPerViewPort = cardsPerViewPort;
    }

    public void setTotal_pages(int total_pages)
    {
        this.total_pages = total_pages;
    }

    public void setTotal_results(int total_results)
    {
        this.total_results = total_results;
    }

    public List<Map<String,Object>> getItemList()
    {
        return this.itemList;
    }

    @Override
    public int getItemCount()
    {
        return this.itemList.size();
    }

    public Map<String,Object> getItem(int i)
    {
        return this.itemList.get(i);
    }

    public void clearAndAddList(List<Map<String,Object>> newItemList)
    {
        this.itemList.clear();

        if ((newItemList != null) && (newItemList.size() > 0))
        {
            this.itemList.addAll(newItemList);
            this.notifyDataSetChanged();

            if (this.label != null)
            {
                this.label.setVisibility(View.VISIBLE);
            }

            this.recyclerView.setVisibility(View.VISIBLE);
        }
        else
        {
            if (this.label != null)
            {
                this.label.setVisibility(View.GONE);
            }
            this.recyclerView.setVisibility(View.GONE);
        }

        if (this.loadingIndicator != null)
        {
            if (this.loadingIndicator instanceof SwipeRefreshLayout)
            {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) this.loadingIndicator;
                swipeRefreshLayout.setRefreshing(false);
            }
            else if (this.loadingIndicator instanceof ProgressBar)
            {
                ProgressBar progressBar = (ProgressBar) this.loadingIndicator;
                progressBar.setVisibility(View.GONE);
            }
        }

    }

    public void addList(List<Map<String,Object>> listToAdd)
    {
        if ((listToAdd != null) && (listToAdd.size() > 0))
        {
            int insertPos = itemList.size() - 1;
            itemList.addAll(listToAdd);
            this.notifyItemRangeInserted(insertPos, listToAdd.size());
        }
    }

    protected View getItemView(@NonNull ViewGroup viewGroup)
    {
        View itemView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(this.cardLayoutID, viewGroup, false);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = (int) (viewGroup.getWidth() * ( 1.0f / this.cardsPerViewPort));
        itemView.setLayoutParams(layoutParams);

        return itemView;
    }

}
