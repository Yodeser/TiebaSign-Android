package com.abcmmee.tieba.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.abcmmee.tieba.R;
import com.abcmmee.tieba.database.TiebaSQLiteDao;
import com.abcmmee.tieba.model.User;

public class ChoiceDialogFragment extends DialogFragment {
    private static final String TAG = "ChoiceDialogFragment";

    private static final String ARG_USER = "user";
    private static final String ARG_LOCATION = "location";

    private User mUser;
    private int[] location;

    private OnChoiceDialogListener mListener;

    public ChoiceDialogFragment() {
        // Required empty public constructor
    }

    public static ChoiceDialogFragment newInstance(User user, int[] location) {
        ChoiceDialogFragment fragment = new ChoiceDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        args.putIntArray(ARG_LOCATION, location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUser = getArguments().getParcelable(ARG_USER);
            location = getArguments().getIntArray(ARG_LOCATION);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_choice, container, false);
        // 一键签到按钮
        Button signAll = (Button) v.findViewById(R.id.sign_all);
        signAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSignAllTiebaListener(mUser); // 回调onSignAllTiebaListener()方法
                dismiss();
            }
        });

        // 刷新列表按钮
        Button flustList = (Button) v.findViewById(R.id.flush_list);
        flustList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当点击按钮后，重新连网获取贴吧列表
                mListener.onGetLikeTiebaListener(mUser);
                dismiss();
            }
        });

        // 删除账号按钮
        Button deleteAccount = (Button) v.findViewById(R.id.delete_account);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("确认删除该账号?")
                        .setCancelable(false)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 从数据库中删除该账户
                                TiebaSQLiteDao dao = TiebaSQLiteDao.getInstance(getActivity());
                                dao.removeUser(mUser);

                                mListener.onViewPagerFlushListener(); // 刷新ViewPager视图

                                dismiss(); // 退出对话框
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                        .create().show();
            }
        });
        return v;
    }

    /**
     * 旋转会有个BUG，未解决。暂时禁用旋转代替.
     */
    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(250, 560);

        // 来源: http://stackoverflow.com/questions/8045556/cant-make-the-custom-dialogfragment-transparent-over-the-fragment
        // 使DialogFragment变透明
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 来源: http://stackoverflow.com/questions/9698410/position-of-dialogfragment-in-android
        // 改变DialogFragment位置
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP | Gravity.LEFT);
        WindowManager.LayoutParams params = window.getAttributes();

        // 原来FloatActionButton的位置
        int sourceX = location[0];
        int sourceY = location[1];

        // DialogFragment的位置
        params.x = sourceX - dpToPx(20);
        params.y = sourceY - dpToPx(220);

        window.setAttributes(params);
    }

    private int dpToPx(float valueInDp) {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChoiceDialogListener) {
            mListener = (OnChoiceDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChoiceDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnChoiceDialogListener {
        void onViewPagerFlushListener();

        void onGetLikeTiebaListener(User user);

        void onSignAllTiebaListener(User user);
    }


}
