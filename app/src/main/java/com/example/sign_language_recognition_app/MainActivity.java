package com.example.sign_language_recognition_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


/*
 페이지 설명: 첫 페이지
 기능: 2가지 버튼(수어인식, 도움말)
*/



public class MainActivity extends AppCompatActivity {
    Button main_btn_recognition, main_btn_information;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_btn_recognition = (Button)findViewById(R.id.main_btn_recognition);
        main_btn_information = (Button)findViewById(R.id.main_btn_information);

        // 기능 1. 수어인식 버튼 [수어인식 페이지로 인텐트]
        main_btn_recognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Recognize_Sign_Language.class);
                startActivity(intent);
            }
        });
        // 기능 2. 도움말 버튼 [도움말 페이지로 인텐트]
        main_btn_information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Information.class);
                startActivity(intent);
            }
        });
    }



}

