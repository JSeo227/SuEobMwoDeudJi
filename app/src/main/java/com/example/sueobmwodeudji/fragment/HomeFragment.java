package com.example.sueobmwodeudji.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.sueobmwodeudji.R;
import com.example.sueobmwodeudji.SearchActivity;
import com.example.sueobmwodeudji.adapter.HomePagerAdapter;
import com.example.sueobmwodeudji.adapter.HomeTimeTableAdapter;
import com.example.sueobmwodeudji.databinding.FragmentHomeBinding;
import com.example.sueobmwodeudji.dto.HomeTimeTableData;
import com.example.sueobmwodeudji.fragment.view_pager.HomeRatingFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private boolean check = true;
    private boolean first = true;
    private int currentItemIndex;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            binding.homeSubTitleViewPager.setCurrentItem(++currentItemIndex);
        }
    };

    public static HomeFragment getInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true); // Activity 의 Toolbar 보다 Fragment 의 Toolbar 를 우선
        HomeTimeTableView();
        HomePostViewPager2();

        // 프로필 이미지
        String uri_text = SettingsFragment.imgLoad(getContext());
        Uri uri;

        if (SettingsFragment.checkImg)
            uri = Uri.parse(uri_text);
        else
            uri = null;

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.account_circle) // 이미지 로딩 시작하기 전 표시할 이미지
                .error(R.drawable.account_circle) // 로딩 에러 발생 시 표시할 이미지
                .fallback(R.drawable.account_circle); // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지

        Glide.with(this)
                .load(uri) // 불러올 이미지 uri
                .apply(requestOptions)
                .circleCrop() // 동그랗게 자르기
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.profileTv); // 이미지를 넣을 뷰

        // 닉네임
        String name = SettingsFragment.nameLoad(getContext());
        binding.infoTv.setText(name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection("사용자")
                .document(user.getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (getActivity() == null) return;

                        String school_name = documentSnapshot.getString("school_name");
                        String school_username = school_name +" / " +  user.getDisplayName();

                        binding.infoTv.setText(school_username);
                    }
                });

        binding.infoTv.setText(user.getDisplayName());

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu(); // onCreateOptionsMenu() 재호출
        handler.postDelayed(runnable, 4000); // 4초마다 실행
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // 프래그먼트를 벗어나면 실행이 멈춤
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 메뉴바 생성
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frame_tool_bar, menu);
    }

    // 메뉴바 클릭
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    // 일정을 보여주기 위한 뷰페이저
    private void HomeTimeTableView() {
        List<HomeTimeTableData> list = new ArrayList<>();

        list.add(new HomeTimeTableData("01.01 신정"));
        list.add(new HomeTimeTableData("01.21~01.23 설날"));
        list.add(new HomeTimeTableData("01.24 대체공휴일"));
        list.add(new HomeTimeTableData("03.01 삼일절"));
        list.add(new HomeTimeTableData("05.05 어린이날"));
        list.add(new HomeTimeTableData("05.27 부처님오신날"));
        list.add(new HomeTimeTableData("05.29 대체공휴일"));
        list.add(new HomeTimeTableData("06.06 현충일"));
        list.add(new HomeTimeTableData("08.15 광복절"));
        list.add(new HomeTimeTableData("10.03 개천절"));
        list.add(new HomeTimeTableData("10.09 한글날"));
        list.add(new HomeTimeTableData("12.25 크리스마스"));

        HomeTimeTableAdapter adapter = new HomeTimeTableAdapter(getContext(), list);
        binding.homeSubTitleViewPager.setAdapter(adapter);

        // 현재 페이지 여백
        float currentScale = getResources().getDisplayMetrics().density;
        int currentVisibleItemPx = (int) (currentScale);
        // 이전, 이후 페이지 여백
        float nextScale = getResources().getDisplayMetrics().density;
        int nextVisibleItemPxInt = (int) (-8f * nextScale);
        // 이동 시킬 페이지 값
        int pageTranslationX = nextVisibleItemPxInt + currentVisibleItemPx;

        // 페이지의 왼쪽, 오른쪽 여백 만듬 -> 겹치지 않게 하기 위해서
        binding.homeSubTitleViewPager.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.right = (int) currentVisibleItemPx;
                outRect.left = (int) currentVisibleItemPx;
            }
        });

        // 값만큼 현재 페이지 기준으로 양옆에 페이지를 미리만듬
        binding.homeSubTitleViewPager.setOffscreenPageLimit(1);

        // 양 옆 페이지들을 현재 페이지가 있는 곳으로 (-pageTranslationX * position) 만큼 이동
        binding.homeSubTitleViewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setTranslationX(-pageTranslationX * position);
            }
        });

        // 오버 스크롤 효과 비 활성화
        binding.homeSubTitleViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        // 첫 번째 페이지 설정
        if (check) {
            binding.homeSubTitleViewPager.setCurrentItem(999, false);
            check = false;
        }

        // 자동 스크롤
        binding.homeSubTitleViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            // 현재 페이지를 스크롤할 때 -> 터치 스크롤의 일부로 호출
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                // 첫 번째 페이지 설정 후 다음 페이지 설정
                if (first && positionOffset == 0 && positionOffsetPixels == 0) {
                    onPageSelected(999);
                    first = false;
                }
                //Log.d("Tag.onPageScrolled", String.valueOf(position));
            }

            // 새 페이지가 선택될 때 호출
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentItemIndex = position;
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 4000);
                //Log.d("Tag.onPageSelected", String.valueOf(position));
            }

            // 스크롤 상태가 변경될 때 호출
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                switch (state) {
                    case ViewPager2.SCROLL_STATE_IDLE:
                        // 결정 X
                        break;

                    case ViewPager2.SCROLL_STATE_DRAGGING:
                        // 결정 X
                        break;

                    case ViewPager2.SCROLL_STATE_SETTLING:
                        // 결정 X
                        break;
                }

            }

        });

    }

    // 선별한 글들을 보여주기 위한 뷰페이저
    private void HomePostViewPager2() {
        HomePagerAdapter adapter = new HomePagerAdapter(getActivity());
        binding.homePostViewPager.setAdapter(adapter);
        binding.homePostViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL); //수평
        binding.homePostViewPager.setOffscreenPageLimit(1);

        List<String> list = Arrays.asList("인기 게시글", "인기 평가글");

        // TabLayout과 ViewPager2를 연결
        new TabLayoutMediator(binding.tabLayout, binding.homePostViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                TextView textView = new TextView(getContext());
                textView.setText(list.get(position));
                textView.setTextSize(20);
                textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tab.setCustomView(textView);
            }
        }).attach();
    }
}