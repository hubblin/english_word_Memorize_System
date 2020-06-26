package com.example.prototype;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class add_Wordbook_Activity extends AppCompatActivity {

    private long mWordbookId = -1;

    private EditText mTitleEditText;
    private EditText mSubtitleEditText;
    Button submitButton;
    //wdawadwaddwaadw
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__wordbook_);

        //액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mTitleEditText = findViewById(R.id.title_edit);
        mSubtitleEditText = findViewById(R.id.subtitle_edit);
        submitButton = findViewById(R.id.submitButton);

        Intent intent = getIntent();
        if(intent != null){
            mWordbookId = intent.getLongExtra("id", -1);
            String title = intent.getStringExtra("title");
            String subtitle = intent.getStringExtra("subtitle");

            mTitleEditText.setText(title);
            mSubtitleEditText.setText(subtitle);
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEditText.getText().toString();
                String subtitle = mSubtitleEditText.getText().toString();
                //저장하는 부분
                if(title.getBytes().length <=0){
                    Toast.makeText(add_Wordbook_Activity.this, "타이틀을 입력해 주세요", Toast.LENGTH_SHORT).show();
                }else {
                    if (mWordbookId == -1) {
                        boolean insertData = addData(title, subtitle);
                        if (insertData == true) {
                            Toast.makeText(add_Wordbook_Activity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            startActivity(new Intent(add_Wordbook_Activity.this, MainActivity.class));
                        } else {
                            Toast.makeText(add_Wordbook_Activity.this, "저장에 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    //수정하는 부분
                    else {
                        boolean updateData = UpdateData(title, subtitle);
                        if (updateData == true) {
                            Toast.makeText(add_Wordbook_Activity.this, "수정 성공", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            startActivity(new Intent(add_Wordbook_Activity.this, MainActivity.class));
                        } else {
                            Toast.makeText(add_Wordbook_Activity.this, "수정에 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
    public boolean addData(String title, String subtitle){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.DbEntry.WORDBOOK_NAME_TITLE, title);
        contentValues.put(DbContract.DbEntry.WORDBOOK_NAME_SUBTITLE, subtitle);

        SQLiteDatabase db = DBHelper.getInstance(this).getWritableDatabase();
        long newRowId = db.insert(DbContract.DbEntry.TABLE_NAME, null, contentValues);
        if(newRowId == -1){
            return false;
        }else{
            return true;
        }

    }

    public boolean UpdateData(String title, String subtitle){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.DbEntry.WORDBOOK_NAME_TITLE, title);
        contentValues.put(DbContract.DbEntry.WORDBOOK_NAME_SUBTITLE, subtitle);

        SQLiteDatabase db= DBHelper.getInstance(this).getWritableDatabase();
        int count = db.update(DbContract.DbEntry.TABLE_NAME, contentValues, DbContract.DbEntry._ID + " = " + mWordbookId, null);
        if(count == 0){
            return false;
        }else{
            return true;
        }
    }
}
