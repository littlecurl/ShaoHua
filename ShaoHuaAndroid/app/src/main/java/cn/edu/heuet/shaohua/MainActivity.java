package cn.edu.heuet.shaohua;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vondear.rxtool.RxBarTool;
import com.vondear.rxtool.RxDeviceTool;
import com.vondear.rxtool.RxTool;

import java.util.ArrayList;
import java.util.List;

import cn.edu.heuet.shaohua.arouter.ARouterPath;
import cn.edu.heuet.shaohua.databinding.ActivityMainBinding;
import cn.edu.heuet.shaohua.fragment.BaseFragment;
import cn.edu.heuet.shaohua.fragment.MineFragment;

/**
 * 首页
 */
@Route(path = ARouterPath.ACTIVITY_MAIN)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding bind;

    // collections
    private List<Fragment> fragments;// used for ViewPager adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在布局设置之前初始化RxTool
        initRxTool();
//        setContentView(R.layout.activity_with_view_pager);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initData();
        initView();
        initEvent();
    }

    private void initRxTool(){
        RxBarTool.noTitle(this);
        RxTool.init(this);
        RxDeviceTool.setPortrait(this);
    }


    /**
     * create fragments
     */
    private void initData() {
        fragments = new ArrayList<>(4);

        String[] titles = new String[]{
                getString(R.string.home),
                getString(R.string.track),
                getString(R.string.message),
                getString(R.string.mine)
        };

        // create 4 fragments and add them
        for (int i = 0; i < 3; i++) {
            BaseFragment fragment = new BaseFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title", titles[i]);
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }
        MineFragment mineFragment = new MineFragment();
        fragments.add(mineFragment);
    }


    /**
     * change BottomNavigationViewEx style
     */
    private void initView() {
        // 关闭 bnve 所有动画效果
        bind.bnve.enableItemShiftingMode(false);
        bind.bnve.enableShiftingMode(false);
        bind.bnve.enableAnimation(false);

        // set adapter
        VpAdapter adapter = new VpAdapter(getSupportFragmentManager(), fragments);
        bind.vp.setAdapter(adapter);
    }

    /**
     * set listeners
     */
    private void initEvent() {
        // 横向滑动事件响应
        // set listener to change the current checked item of bottom nav when scroll view pager
        bind.vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "-----ViewPager-------- previous item:" + bind.bnve.getCurrentItem() + " current item:" + position + " ------------------");
                if (position >= 2)// 2 is center
                    position++;// if page is 2, need set bottom item to 3, and the same to 3 -> 4
                bind.bnve.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // 底部导航 Item 点击事件响应
        // set listener to change the current item of view pager when click bottom nav item
        bind.bnve.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            private int previousPosition = -1;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int position = 0;
                switch (item.getItemId()) {
                    case R.id.id_home:
                        position = 0;
                        break;
                    case R.id.id_track:
                        position = 1;
                        break;
                    case R.id.id_message:
                        position = 2;
                        break;
                    case R.id.id_mine:
                        position = 3;
                        break;
                    case R.id.id_empty: {
                        return false;
                    }
                }
                if (previousPosition != position) {
                    bind.vp.setCurrentItem(position, false);
                    previousPosition = position;
                    Log.i(TAG, "-----bnve-------- previous item:" + bind.bnve.getCurrentItem() + " current item:" + position + " ------------------");
                }

                return true;
            }
        });


        // center item click listener
        bind.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Center", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * view pager adapter
     */
    private static class VpAdapter extends FragmentPagerAdapter {
        private List<Fragment> data;

        VpAdapter(FragmentManager fm, List<Fragment> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return data.get(position);
        }
    }

}

