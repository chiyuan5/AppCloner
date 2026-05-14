package com.appcloner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appcloner.R;
import com.appcloner.model.AppInfo;

import java.util.List;

public class CloneListAdapter extends RecyclerView.Adapter<CloneListAdapter.CloneViewHolder> {

    private final Context context;
    private List<AppInfo> appList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AppInfo appInfo);
        void onStartClick(AppInfo appInfo);
        void onDeleteClick(AppInfo appInfo);
    }

    public CloneListAdapter(Context context, List<AppInfo> appList) {
        this.context = context;
        this.appList = appList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<AppInfo> newList) {
        this.appList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CloneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clone_list, parent, false);
        return new CloneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CloneViewHolder holder, int position) {
        final AppInfo appInfo = appList.get(position);

        holder.appIcon.setImageDrawable(appInfo.getIcon());
        holder.appName.setText(appInfo.getAppName());
        holder.packageName.setText(appInfo.getClonedPackageName());

        holder.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onStartClick(appInfo);
                }
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(appInfo);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(appInfo);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList != null ? appList.size() : 0;
    }

    static class CloneViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView packageName;
        Button btnStart;
        Button btnDelete;

        CloneViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            packageName = itemView.findViewById(R.id.package_name);
            btnStart = itemView.findViewById(R.id.btn_start);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
