package com.example.prototype;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;

public class add_word extends AppCompatActivity {

    private long wWordbookId = -1;
    private long WordId = -2;
    private EditText spell, text1, text2, text3, text4, text5, wordbookId;
    Button submitButton;

    int count;

    LocalDate date;
    private DBHelper databaseHelper;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        spell = findViewById(R.id.spell_edit);
        text1 = findViewById(R.id.mean1_edit);
        text2 = findViewById(R.id.mean2_edit);
        text3 = findViewById(R.id.mean3_edit);
        text4 = findViewById(R.id.mean4_edit);
        text5 = findViewById(R.id.mean5_edit);
        wordbookId = findViewById(R.id.wordbookId);

        submitButton = findViewById(R.id.submitButton1);

        Intent intent = getIntent();

        databaseHelper = new DBHelper(this);
        db = databaseHelper.getWritableDatabase();   // 쓰기 모드

        final String WordbookId = Long.toString(intent.getLongExtra("wordbookId", -1));
        wWordbookId = intent.getLongExtra("wordbookId", -1);
        final String sTitle = intent.getStringExtra("title");
        final String sSubtitle = intent.getStringExtra("subtitle");

        wordbookId.setText(WordbookId);

        WordId = intent.getLongExtra("id", -2);
        String tspell = intent.getStringExtra("spell");
        String tmean1 = intent.getStringExtra("mean1");
        String tmean2 = intent.getStringExtra("mean2");
        String tmean3 = intent.getStringExtra("mean3");
        String tmean4 = intent.getStringExtra("mean4");
        String tmean5 = intent.getStringExtra("mean5");

        spell.setText(tspell);
        text1.setText(tmean1);
        text2.setText(tmean2);
        text3.setText(tmean3);
        text4.setText(tmean4);
        text5.setText(tmean5);



        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String spell1 = spell.getText().toString();
                String wordmean1 = text1.getText().toString();
                String wordmean2 = text2.getText().toString();
                String wordmean3 = text3.getText().toString();
                String wordmean4 = text4.getText().toString();
                String wordmean5 = text5.getText().toString();
                String wbId = wordbookId.getText().toString();
                count=0;
                date = LocalDate.now();
                String now_date = date_return();



                if(WordId == -2) {
                    boolean insertData = addData(spell1, wordmean1, wordmean2, wordmean3, wordmean4, wordmean5, wbId, count);
                    if (insertData == true) {


                        Toast.makeText(add_word.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        Intent intent1 = new Intent(add_word.this, wordMain.class);
                        intent1.putExtra("id", wWordbookId);
                        intent1.putExtra("title", sTitle);
                        intent1.putExtra("subtitle", sSubtitle);
                        //Cursor cu = db.rawQuery("select "+ DbContract.DbEntry2._ID +" from "+ DbContract.DbEntry2.TABLE_NAME +" where "+ ,null);
                        startActivity(intent1);
                    } else {
                        Toast.makeText(add_word.this, "저장에 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                //수정부분
                else{
                    boolean updateData = UpdateData(spell1, wordmean1, wordmean2, wordmean3, wordmean4, wordmean5, wbId);
                    if(updateData == true){
                        Toast.makeText(add_word.this, "수정 성공", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        Intent intent1 = new Intent(add_word.this, wordMain.class);
                        intent1.putExtra("id", wWordbookId);
                        intent1.putExtra("title", sTitle);
                        intent1.putExtra("subtitle", sSubtitle);
                        startActivity(intent1);
                    }else{
                        Toast.makeText(add_word.this, "수정에 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    public boolean addData(String spell, String text1, String text2, String text3, String text4, String text5, String wbId,int count ){


        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.DbEntry2.WORD_SPELL, spell);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN1, text1);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN2, text2);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN3, text3);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN4, text4);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN5, text5);
        contentValues.put(DbContract.DbEntry2.WORDBOOK_ID, wbId);
        contentValues.put(DbContract.DbEntry2.DATE,date_return());
        contentValues.put(DbContract.DbEntry2.CORRECT_ANSWER,count);

        SQLiteDatabase db = DBHelper.getInstance(this).getWritableDatabase();
        long newRowId = db.insert(DbContract.DbEntry2.TABLE_NAME, null, contentValues);
        if(newRowId == -1){
            return false;
        }else{
            return true;
        }
    }



    public boolean UpdateData(String spell, String text1, String text2, String text3, String text4, String text5, String wbId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.DbEntry2.WORD_SPELL, spell);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN1, text1);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN2, text2);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN3, text3);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN4, text4);
        contentValues.put(DbContract.DbEntry2.WORD_MEAN5, text5);
        contentValues.put(DbContract.DbEntry2.WORDBOOK_ID, wbId);
        Log.d("오류",""+date_return());
        SQLiteDatabase db = DBHelper.getInstance(this).getWritableDatabase();
        int count = db.update(DbContract.DbEntry2.TABLE_NAME, contentValues, DbContract.DbEntry2._ID + " = " + WordId, null);
        if(count == 0){
            return false;
        }else{
            return true;
        }
    }
    private String date_return(){
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date2 = new Date();
        return date_format.format(date2);
    }

}
