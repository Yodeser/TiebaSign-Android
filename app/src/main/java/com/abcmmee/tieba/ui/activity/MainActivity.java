package com.abcmmee.tieba.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.abcmmee.tieba.R;
import com.abcmmee.tieba.database.TiebaSQLiteDao;
import com.abcmmee.tieba.model.Tieba;
import com.abcmmee.tieba.model.User;
import com.abcmmee.tieba.ui.adapter.ViewPagerAdapter;
import com.abcmmee.tieba.ui.fragment.ChoiceDialogFragment;
import com.abcmmee.tieba.ui.fragment.ItemFragment;
import com.abcmmee.tieba.utils.BaiduTiebaUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemFragment.OnRecyclerItemListener, ChoiceDialogFragment.OnChoiceDialogListener {
    private static final String TAG = "MainActivity";
    private static final int LOGIN_REQUEST_CODE = 0;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mPagerAdapter;
    private ProgressBar mProgressView;

    private UserGetTiebaTask mTiebaTask; // 获取贴吧列表的异步线程
    private SignTiebaTask mSignTiebaTask; // 签到单个贴吧的异步线程
    private SignAllTiebaTask mSignAllTiebaTask; // 签到所有贴吧的异步线程

    private TiebaSQLiteDao mDao;
    private List<User> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_main);
        setSupportActionBar(toolbar);

        // 从数据库中获取所有用户
        mDao = TiebaSQLiteDao.getInstance(this);
        mUsers = mDao.getAllUser();

        // 获取Fragment管理器
        final FragmentManager fm = getSupportFragmentManager();

        // 设置ViewPager相关
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mPagerAdapter = new ViewPagerAdapter(fm, mUsers);
        mViewPager.setAdapter(mPagerAdapter);

        // 设置TabLayout相关
        mTabLayout = (TabLayout) findViewById(R.id.tab);
        mTabLayout.setupWithViewPager(mViewPager);

        // 进度条
        mProgressView = (ProgressBar) findViewById(R.id.flush_progress);

        // FloatingActionButton
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // FloatingActionBar的位置
                int[] location = new int[2];
                view.getLocationInWindow(location);

                // ViewPager当前选项卡的用户
                int position = mViewPager.getCurrentItem();
                if (mUsers.size() == 0) {
                    return;
                }
                User user = mUsers.get(position);

                ChoiceDialogFragment choiceDialog = ChoiceDialogFragment.newInstance(user, location);
                choiceDialog.show(fm, "choice");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.add:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(loginIntent, LOGIN_REQUEST_CODE);
                break;
            case R.id.action_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // 获取数据库最后一条用户的记录，也就是登录成功后插入数据库最后面的用户
                mUsers = mDao.getAllUser();
                User user = mUsers.get(mUsers.size() - 1);
                // 获取用户关注的贴吧
                onGetLikeTiebaListener(user);
            }
        }
    }


    /**
     * 刷新ViewPager视图
     * 如果用mPagerAdapter.notifyDataSetChanged()刷新，不起作用
     */
    @Override
    public void onViewPagerFlushListener() {
        mUsers = mDao.getAllUser();

        int position = mViewPager.getCurrentItem(); // 记录以前ViewPager的Item位置

        FragmentManager fm = getSupportFragmentManager();
        mPagerAdapter = new ViewPagerAdapter(fm, mUsers);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setCurrentItem(position); // 恢复ViewPager的Item位置

        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * 获取用户关注的贴吧
     *
     * @param user
     */
    @Override
    public void onGetLikeTiebaListener(User user) {
        if (mTiebaTask != null) {
            return;
        }
        showProgress(true);
        mTiebaTask = new UserGetTiebaTask(user);
        mTiebaTask.execute((Void) null);
    }

    /**
     * 签到所有贴吧
     *
     * @param user
     */
    @Override
    public void onSignAllTiebaListener(User user) {
        if (mSignAllTiebaTask != null) {
            return;
        }
        showProgress(true);
        mSignAllTiebaTask = new SignAllTiebaTask(user);
        mSignAllTiebaTask.execute((Void) null);
    }

    /**
     * @param tieba
     */
    @Override
    public void onSignTiebaListener(User user, Tieba tieba) {
        if (mSignTiebaTask != null) {
            return;
        }
        showProgress(true);
        mSignTiebaTask = new SignTiebaTask(user, tieba);
        mSignTiebaTask.execute((Void) null);
    }

    /**
     * 获取用户关注的贴吧的异步线程
     */
    private class UserGetTiebaTask extends AsyncTask<Void, Void, Boolean> {

        private User mUser;

        public UserGetTiebaTask(User user) {
            mUser = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // 尝试获取贴吧列表
            try {
                BaiduTiebaUtils utils = BaiduTiebaUtils.getInstance(MainActivity.this);
                boolean success = utils.getLikeForum(mUser);
                if (success) {
                    return true;
                } else {
                    return false;
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mTiebaTask = null;
            showProgress(false);

            if (success) {
                onViewPagerFlushListener(); // 刷新ViewPager视图
                Toast.makeText(MainActivity.this, R.string.get_tieba_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, R.string.error_get_tieba_fail, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mTiebaTask = null;
            showProgress(false);
        }
    }

    private class SignAllTiebaTask extends AsyncTask<Void, Void, int[]> {
        private User mUser;

        public SignAllTiebaTask(User user) {
            mUser = user;
        }

        @Override
        protected int[] doInBackground(Void... params) {
            List<Tieba> tiebas = mUser.getTiebas();

            int successCount = 0;
            int alreadyCount = 0;
            int errorCount = 0;

            for (Tieba tieba : tiebas) {
                try {
                    BaiduTiebaUtils utils = BaiduTiebaUtils.getInstance(MainActivity.this);

                    int result = utils.signTiebaWithClient(mUser, tieba);

                    if (result == 0) {
                        alreadyCount++;
                    } else if (result == 1) {
                        successCount++;
                    } else {
                        errorCount++;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    errorCount++;
                }
            }

            return new int[]{successCount, alreadyCount, errorCount};
        }


        @Override
        protected void onPostExecute(int[] result) {
            mSignAllTiebaTask = null;
            showProgress(false);

            int successCount = result[0];
            int alreadyCount = result[1];
            int errorCount = result[2];

            if (alreadyCount == 0 && errorCount == 0) {
                Toast.makeText(MainActivity.this, "用户\"" + mUser.getName() + "\"全部贴吧签到成功", Toast.LENGTH_LONG).show();
            } else {
                String msg = "用户\"" + mUser.getName() + "\"" + successCount + "个贴吧签到成功\n" +
                        alreadyCount + "个贴吧已经签到\n" +
                        errorCount + "个贴吧签到错误";
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mSignAllTiebaTask = null;
            showProgress(false);
        }
    }

    private class SignTiebaTask extends AsyncTask<Void, Void, Integer> {
        private User mUser;
        private Tieba mTieba;

        public SignTiebaTask(User user, Tieba tieba) {
            mUser = user;
            mTieba = tieba;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                BaiduTiebaUtils utils = BaiduTiebaUtils.getInstance(MainActivity.this);

                int result = utils.signTiebaWithClient(mUser, mTieba);

                return result;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return 2; // 签到错误
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            mSignTiebaTask = null;
            showProgress(false);

            if (integer == 0) {
                Toast.makeText(MainActivity.this, "已经签到过了", Toast.LENGTH_SHORT).show();
            } else if (integer == 1) {
                Toast.makeText(MainActivity.this, "签到成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "签到错误", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mSignTiebaTask = null;
            showProgress(false);
        }

    }

    /**
     * Shows the progress UI and hides the ViewPager.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
            mViewPager.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}



