package com.example.sueobmwodeudji;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sueobmwodeudji.databinding.ActivityTimeTableClassBinding;
import com.example.sueobmwodeudji.dto.CallSchoolData;
import com.example.sueobmwodeudji.dto.TimeTableDTO;
import com.example.sueobmwodeudji.rest_api.NEIS_API;
import com.example.sueobmwodeudji.rest_api.Row;
import com.example.sueobmwodeudji.rest_api.SchoolInfo;
import com.example.sueobmwodeudji.rest_api.SchoolResponse;
import com.example.sueobmwodeudji.rest_api.SchoolTimeTable;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimeTableClassActivity extends AppCompatActivity {
    public static boolean checkCall = false;
    private ActivityTimeTableClassBinding binding;

    Call<SchoolResponse> callInfo, callTimeTable;

    List<String> perioList = new ArrayList<>();
    List<String> classCntntList = new ArrayList<>();

    private TimeTableDTO data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTimeTableClassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        data = (TimeTableDTO) intent.getSerializableExtra("data");

        // 툴바
        Toolbar toolbar = binding.toolBar.mainToolBar;
        //toolbar.s
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        final String[] years = {"2023년"};
        final String[] semesters = {"1학기", "2학기"};
        final String[] grades = {"1학년", "2학년", "3학년"};
        final String[] className = {"1반", "2반", "3반", "4반", "5반", "6반", "7반", "8반", "9반", "10반"};

//        ArrayAdapter adapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, years);
//        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.yearSpin.setAdapter(adapter1);
//
//        ArrayAdapter adapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters);
//        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.semesterSpin.setAdapter(adapter2);

        ArrayAdapter adapter3 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, grades);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.gradeSpin.setAdapter(adapter3);

        ArrayAdapter adapter4 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, className);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.classNameSpin.setAdapter(adapter4);


        binding.tableAddBtn.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading TimeTable..");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
            progressDialog.show();

            //createList();
            schoolCallInfo();
//            finish();
        });

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 교육청 코드와 학교 코드를 구하는 메소드
    public void schoolCallInfo() {
        callInfo = NEIS_API.getInfoService().getSchoolInfo(
                "9da752136d5849b985288deb5036dba1",
                "json",
                RegistrationActivity.school_name
        );

        callInfo.enqueue(new Callback<SchoolResponse>() {
            @Override
            public void onResponse(Call<SchoolResponse> call, Response<SchoolResponse> response) {
                if (response.isSuccessful()) {
                    SchoolResponse schoolInfoResponse = response.body();
                    SchoolInfo schoolInfo = schoolInfoResponse.getSchoolInfo().get(1); //row
                    List<Row> rows = schoolInfo.getRow();
                    Row row = rows.get(0);

                    schoolCallTimeTable(row.getMinistryCode(), row.getShcoolCode());

//                    Log.d("ministryCode", row.getMinistryCode());
//                    Log.d("schoolCode", row.getShcoolCode());

                }
            }

            @Override
            public void onFailure(Call<SchoolResponse> call, Throwable t) {

            }
        });

    }

    // 월요일 ~ 금요일까지 몇 교시에 무슨 과목인지 구하는 메소드 (class_name="1")
    public void schoolCallTimeTable(String ministryCode, String schoolCode) {

        callTimeTable = NEIS_API.getTimeTableService().getSchoolTimeTable(
                "9da752136d5849b985288deb5036dba1",
                "json",
                ministryCode,
                schoolCode,
                "2023",
                "1",
                binding.gradeSpin.getSelectedItem().toString().substring(0, 1),
                binding.classNameSpin.getSelectedItem().toString().substring(0, 1),
                "20230313",
                "20230317"
        );

        callTimeTable.enqueue(new Callback<SchoolResponse>() {
            @Override
            public void onResponse(Call<SchoolResponse> call, Response<SchoolResponse> response) {
                if (response.isSuccessful()) {
                    try {
                        SchoolResponse schoolTimeTableResponse = response.body();
                        Log.d("response", String.valueOf(response.raw()));
                        SchoolTimeTable schoolTimeTable = schoolTimeTableResponse.getSchoolTimeTable().get(1); // row
                        List<Row> rows = schoolTimeTable.getRow();

                        for (int i = 0; i < rows.size(); i++) {
                            Row row = rows.get(i);
                            perioList.add(row.getPeriority());
                            classCntntList.add(row.getClassContent());
                        }
//                        Log.d("perioList", String.valueOf(perioList));
//                        Log.d("classCntntList", String.valueOf(classCntntList));
                        createList();
                    } catch (NullPointerException e) {
                        //Log.e("ERROR", "NullPointerException");
                        // SchoolService 값 변경
                        SchoolCallTimeTableError(ministryCode, schoolCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<SchoolResponse> call, Throwable t) {
            }
        });
    }

    // 월요일 ~ 금요일까지 몇 교시에 무슨 과목인지 구하는 메소드 (class_name="01")
    public void SchoolCallTimeTableError(String ministryCode, String schoolCode) {

        callTimeTable = NEIS_API.getTimeTableService().getSchoolTimeTable(
                "9da752136d5849b985288deb5036dba1",
                "json",
                ministryCode,
                schoolCode,
                "2023",
                "1",
                binding.gradeSpin.getSelectedItem().toString().substring(0, 1),
                "0" + binding.classNameSpin.getSelectedItem().toString().substring(0, 1),
                "20230313",
                "20230317"
        );

        callTimeTable.enqueue(new Callback<SchoolResponse>() {
            @Override
            public void onResponse(Call<SchoolResponse> call, Response<SchoolResponse> response) {
                if (response.isSuccessful()) {
                    try {
                        SchoolResponse schoolTimeTableResponse = response.body();
                        Log.d("response", String.valueOf(response.raw()));
                        SchoolTimeTable schoolTimeTable = schoolTimeTableResponse.getSchoolTimeTable().get(1); // row
                        List<Row> rows = schoolTimeTable.getRow();

                        for (int i = 0; i < rows.size(); i++) {
                            Row row = rows.get(i);
                            perioList.add(row.getPeriority());
                            classCntntList.add(row.getClassContent());
                        }

                        Log.d("perioList", String.valueOf(perioList));
                        Log.d("classCntntList", String.valueOf(classCntntList));

                        createList();

                    } catch (NullPointerException e) {
                        Log.e("ERROR", "NullPointerException");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<SchoolResponse> call, Throwable t) {

            }
        });

    }

    public void createList() {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"}; // 월요일 ~ 금요일
        int dayNum = 0; // 0:월요일 ~ 4:금요일
        int cnt = 0; // 첫번째 월요일 계산 용도

        ArrayList<CallSchoolData> list = new ArrayList<>();

        ArrayList<String> subList;
        CallSchoolData schoolData;

        for (String day : days) {
            subList = new ArrayList<>();
            schoolData = new CallSchoolData(day, subList);
            list.add(schoolData);
        }

        for (int i = 0; i < perioList.size(); i++) {
            if (perioList.get(i).equals("1") && cnt != 0) {
                dayNum++;
            }
            list.get(dayNum).classCntnt.add(classCntntList.get(i));
            cnt++;
        }

        //TimeTableFragment.newInstance(list);

        checkCall = true;

        for (CallSchoolData data : list) {
            Log.d("TAG", data.toString());
        }

        Map<String, Object> map = new HashMap<>();

        for(CallSchoolData dto : list){
            if(dto.day.equals("Mon")) map.put("mon", dto.classCntnt);
            if(dto.day.equals("Tue")) map.put("tue", dto.classCntnt);
            if(dto.day.equals("Wed")) map.put("wed", dto.classCntnt);
            if(dto.day.equals("Thu")) map.put("thu", dto.classCntnt);
            if(dto.day.equals("Fri")) map.put("fri", dto.classCntnt);
        }

        FirebaseFirestore f = FirebaseFirestore.getInstance();
        f.collection("시간표")
                        .document(data.getEmail() + " " + data.getYear() + " - " + data.getSemester())
                                .update(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                finish();
                                            }
                                        });



    }
}
