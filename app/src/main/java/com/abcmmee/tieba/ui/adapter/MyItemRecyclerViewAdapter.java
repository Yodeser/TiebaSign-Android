package com.abcmmee.tieba.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abcmmee.tieba.R;
import com.abcmmee.tieba.model.Tieba;
import com.abcmmee.tieba.ui.fragment.ItemFragment.OnRecyclerItemListener;

import java.util.List;

/**
 * RecyclerView适配器
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "MyItemRecyclerViewAdapter";

    private List<Tieba> mTiebas;
    private int position; // 当前选择的位置
    private OnRecyclerItemListener mListener;

    public MyItemRecyclerViewAdapter(List<Tieba> tiebas, OnRecyclerItemListener listener) {
        mTiebas = tiebas;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tieba tieba = mTiebas.get(position);

        holder.mTieba = tieba;
        holder.mTiebaName.setText(tieba.getName());
        holder.mLevelView.setText(tieba.getLevel());
        holder.mExpView.setText(tieba.getExp());

        holder.itemView.setTag(position);
        holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                String name = ((TextView) v.findViewById(R.id.tieba_name)).getText().toString();
                menu.setHeaderTitle("请选择要对\"" + name + "\"吧进行的操作:");
                menu.add(0, R.id.context_menu_sign, 0, "签到");
                menu.add(0, R.id.context_menu_delete, 1, "删除");
                setPosition((int) v.getTag());
            }
        });
    }


    @Override
    public int getItemCount() {
        return mTiebas.size();
    }

    /**
     * 得到当前选择的Item的位置
     * 参考: http://stackoverflow.com/questions/26466877/how-to-create-context-menu-for-recyclerview
     */
    public int getPosition() {
        return position;
    }

    /**
     * 设置当前的Item的位置
     */
    public void setPosition(int position) {
        this.position = position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTiebaName;
        public final TextView mLevelView;
        public final TextView mExpView;
        public Tieba mTieba;

        public ViewHolder(View view) {
            super(view);
            mTiebaName = (TextView) view.findViewById(R.id.tieba_name);
            mLevelView = (TextView) view.findViewById(R.id.level);
            mExpView = (TextView) view.findViewById(R.id.exp);
        }

    }
}
