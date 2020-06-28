package com.example.prototype;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Problem_result_activity extends AppCompatActivity {

    TextView result1,result2;

    Button bts1;

    private DBHelper databaseHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_result_activity);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        bts1 = (Button)findViewById(R.id.bts1);

        databaseHelper = new DBHelper(this);
        db = databaseHelper.getWritableDatabase();   // 쓰기 모드


        Intent intent = getIntent();

        int answer_count = intent.getExtras().getInt("answer_count");
        int wrong_count = intent.getExtras().getInt("wrong_count");
        String WordbookId = intent.getExtras().getString("wordbookId");

        Cursor cursor = db.rawQuery("SELECT "+ DbContract.DbEntry.PROBLEM_COUNT+" FROM " + DbContract.DbEntry.TABLE_NAME + " WHERE " + DbContract.DbEntry._ID + " = " + WordbookId,null);
        Log.d(DbContract.DbEntry.TABLE_NAME, "테이블 명");
        Log.d(DbContract.DbEntry._ID, "id");
        Log.d(WordbookId + "", "아이디");
        cursor.moveToFirst();
        result1 = (TextView) findViewById(R.id.problem_count);
        result1.setText(Integer.toString(cursor.getInt(0)));
        result1.setTextSize(30);
        result1.setGravity(Gravity.CENTER);
        result1.setTextColor(Color.BLACK);

        result2 = (TextView) findViewById(R.id. scoring_result);
        result2.setText("정답 횟수: "+answer_count + "\n틀린횟수: "+wrong_count);
        result2.setTextSize(30);
        result2.setGravity(Gravity.CENTER);
        result2.setTextColor(Color.BLACK);

        bts1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });




    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"수고하셨습니다 !",Toast.LENGTH_SHORT).show();
        Intent go_home = new Intent(this, MainActivity.class);
        finish();
        startActivity(go_home);
    }
}
