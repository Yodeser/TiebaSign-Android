package com.abcmmee.tieba.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.abcmmee.tieba.R;
import com.abcmmee.tieba.database.TiebaSQLiteDao;
import com.abcmmee.tieba.model.Tieba;
import com.abcmmee.tieba.model.User;
import com.abcmmee.tieba.ui.adapter.MyItemRecyclerViewAdapter;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnRecyclerItemListener}
 * interface.
 */
public class ItemFragment extends Fragment {
    private static final String TAG = "ItemFragment";

    private static final String ARG_USER = "user";

    private User mUser;
    private List<Tieba> mTiebas;

    private RecyclerView mRecyclerView;
    private MyItemRecyclerViewAdapter mRecyclerViewAdapter;
    private OnRecyclerItemListener mListener;


    public ItemFragment() {
    }


    public static ItemFragment newInstance(User user) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecyclerItemListener) {
            mListener = (OnRecyclerItemListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRecyclerItemListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUser = getArguments().getParcelable(ARG_USER);
            mTiebas = mUser.getTiebas();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            // 注册上下文菜单
            registerForContextMenu(mRecyclerView);

            // 添加分割经线
            mRecyclerView.addItemDecoration(new DeviderItemDecoration(getActivity()));

            // 设置垂直布局
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            // 设置适配器
            mRecyclerViewAdapter = new MyItemRecyclerViewAdapter(mTiebas, mListener);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
        }
        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // 参考: http://blog.csdn.net/tongcpp/article/details/42237863
        if (!getUserVisibleHint()) {
            return false;
        }

        int position = mRecyclerViewAdapter.getPosition();
        TiebaSQLiteDao dao = TiebaSQLiteDao.getInstance(getActivity());
        Tieba tieba = mTiebas.get(position);

        switch (item.getItemId()) {
            case R.id.context_menu_sign: // 对指定贴吧签到
                if (mListener != null) {
                    mListener.onSignTiebaListener(mUser, tieba);
                }
                break;
            case R.id.context_menu_delete: // 删除指定贴吧
                dao.removeTieba(tieba);
                mTiebas.remove(tieba);
                mRecyclerViewAdapter.notifyDataSetChanged();
                break;
        }

        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * https://developer.android.com/training/basics/fragments/communicating.html
     */
    public interface OnRecyclerItemListener {
        void onSignTiebaListener(User user, Tieba tieba);
    }
}
