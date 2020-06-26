package com.example.prototype;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.util.Locale;

public class wordMain extends AppCompatActivity {

    //==========================================//
    //TTS 부분//
    private TextToSpeech mTTS;
    private SeekBar mSeekBarSpeed;

    public static final int REQUEST_CODE_INSERT = 1001;

    private long mWordbookId = -1;

    ListView word_list;

    FloatingActionButton fab_add_word_list, fab_serch_word, fab_solve_problems;

    private ListAdapter wAdapter;

    private DBHelper databaseHelper;
    private SQLiteDatabase db;

    Cursor check_cur1,check_cur2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_main);
        databaseHelper = new DBHelper(this);

        db = databaseHelper.getWritableDatabase();   // 쓰기 모드

        //==========================================//
        //tts정의 부분
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported");
                    }
                }else{
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        //=========================================//


        //액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = getIntent();

        word_list = findViewById(R.id.word_list);

        //헤더 부분
        final ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.listview_header, null, false);
        TextView mTitle = header.findViewById(R.id.wordbook_title);
        TextView mSubtitle = header.findViewById(R.id.wordbook_subtitle);

        mWordbookId = intent.getLongExtra("id", -1);
        final String sTitle = intent.getStringExtra("title");
        final String sSubtitle = intent.getStringExtra("subtitle");

        mTitle.setText(sTitle);
        mSubtitle.setText(sSubtitle);

        word_list.addHeaderView(header, null, false);

        //플로팅 액션 버튼
        fab_add_word_list = findViewById(R.id.fab_add_word_list);
        fab_serch_word = findViewById(R.id.fab_serch_word);
        fab_solve_problems = findViewById(R.id.fab_solve_problems);

        fab_add_word_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(wordMain.this, add_word.class);
                intent1.putExtra("wordbookId", mWordbookId);
                intent1.putExtra("title", sTitle);
                intent1.putExtra("subtitle", sSubtitle);
                startActivityForResult(intent1,REQUEST_CODE_INSERT);
            }
        });

        fab_serch_word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(wordMain.this, "단어 추가하기 버튼", Toast.LENGTH_SHORT).show();
            }
        });

        // 문제 풀기 버튼
        fab_solve_problems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_cur1 = db.rawQuery("select * from "+DbContract.DbEntry2.TABLE_NAME+" WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + mWordbookId+" AND "+ DbContract.DbEntry2.DATE +" = date('now')",null);
                check_cur2 = db.rawQuery("select "+ DbContract.DbEntry2.DATE+" from "+DbContract.DbEntry2.TABLE_NAME+" where "+ DbContract.DbEntry2.DATE +" < date('now')",null);
                if (check_cur1.getCount() == 0){
                    if (check_cur2.getCount() == 0){
                        //(정상적) 오늘 풀 문제가 없는 경우
                        Toast.makeText(getApplicationContext(),
                                "풀 문제 없음",Toast.LENGTH_SHORT).show();
                    }
                    else { //(비정상)문제 풀 시간이 지나 문제가 밀린 경우
                        Toast.makeText(getApplicationContext(),
                                "문제가 밀림",Toast.LENGTH_SHORT).show();

                    }
                }
                else { // 풀 문제가 있을때
                    db.execSQL("update wordbook set problem_count = problem_count + 1 where _id = "+mWordbookId);
                    Intent go_to_problems = new Intent(getApplicationContext(), Multiple_choice.class);
                    go_to_problems.putExtra("wordbookId",mWordbookId);
                    startActivityForResult(go_to_problems,REQUEST_CODE_INSERT);
                }



            }
        });

        final Cursor cursor = getListCursor();
        wAdapter = new ListAdapter(this, cursor);
        word_list.setAdapter(wAdapter);

        //단어 아이탬 선택시 반응하는 부분(소리가 나야한다.)
        word_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final long seletId = id;
                String Spell ="";

                SQLiteDatabase db = DBHelper.getInstance(wordMain.this).getReadableDatabase();
                Cursor cursor1 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry._ID + "=" + id, null);
                while(cursor1.moveToNext()){
                    int colid = cursor1.getColumnIndex(DbContract.DbEntry2.WORD_SPELL);
                    Spell = cursor1.getString(colid);
                }

                //
                speak(Spell);
                //
                Toast.makeText(wordMain.this, Spell+"선택", Toast.LENGTH_SHORT).show();
            }
        });

        //길게 누르면 실행되는 부분
        word_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final long deletedId = id;

                AlertDialog.Builder builder = new AlertDialog.Builder(wordMain.this);
                builder.setTitle("단어 변경");
                builder.setMessage("단어를 변경하거나 삭제하시겠습니까?");
                //긍정버튼
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = DBHelper.getInstance(wordMain.this).getWritableDatabase();
                        int deletCount = db.delete(DbContract.DbEntry2.TABLE_NAME, DbContract.DbEntry2._ID + " = " + deletedId, null);
                        if(deletCount == 0){
                            Toast.makeText(wordMain.this, "삭제에 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }else{
                            wAdapter.swapCursor(getListCursor());
                            Toast.makeText(wordMain.this, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("수정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent1 = new Intent(wordMain.this, add_word.class);
                        String spell = "";
                        String text1 ="", text2="", text3="", text4="", text5 = "";


                        SQLiteDatabase db = DBHelper.getInstance(wordMain.this).getReadableDatabase();
                        Cursor cursor1 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2._ID + "=" + deletedId, null);
                        while(cursor1.moveToNext()){
                            int solid1 = cursor1.getColumnIndex(DbContract.DbEntry2.WORD_SPELL);
                            int solid2 = cursor1.getColumnIndex(DbContract.DbEntry2.WORD_MEAN1);
                            int solid3 = cursor1.getColumnIndex(DbContract.DbEntry2.WORD_MEAN2);
                            int solid4 = cursor1.getColumnIndex(DbContract.DbEntry2.WORD_MEAN3);
                            int solid5 = cursor1.getColumnIndex(DbContract.DbEntry2.WORD_MEAN4);
                            int solid6 = cursor1.getColumnIndex(DbContract.DbEntry2.WORD_MEAN5);
                            spell = cursor1.getString(solid1);
                            text1 = cursor1.getString(solid2);
                            text2 = cursor1.getString(solid3);
                            text3 = cursor1.getString(solid4);
                            text4 = cursor1.getString(solid5);
                            text5 = cursor1.getString(solid6);
                        }
                        intent1.putExtra("wordbookId", mWordbookId);
                        intent1.putExtra("title", sTitle);
                        intent1.putExtra("subtitle", sSubtitle);
                        intent1.putExtra("id", deletedId);
                        intent1.putExtra("spell", spell);
                        intent1.putExtra("mean1", text1);
                        intent1.putExtra("mean2", text2);
                        intent1.putExtra("mean3", text3);
                        intent1.putExtra("mean4", text4);
                        intent1.putExtra("mean5", text5);

                        startActivityForResult(intent1, REQUEST_CODE_INSERT);
                    }
                });
                builder.show();
                return true;
            }
        });


    }

    private Cursor getListCursor(){
        SQLiteDatabase db = DBHelper.getInstance(this).getReadableDatabase();
        Cursor cursor1 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + mWordbookId, null);
        return cursor1;
    }

    private static class ListAdapter extends CursorAdapter{
        public ListAdapter(Context context, Cursor c) {super(context, c,false);}

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.wordrow, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView spell = view.findViewById(R.id.spellText);
            TextView text1 = view.findViewById(R.id.TextMean1);
            TextView text2 = view.findViewById(R.id.TextMean2);
            TextView text3 = view.findViewById(R.id.TextMean3);
            TextView text4 = view.findViewById(R.id.TextMean4);
            TextView text5 = view.findViewById(R.id.TextMean5);
            spell.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.DbEntry2.WORD_SPELL)));
            text1.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.DbEntry2.WORD_MEAN1)));
            text2.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.DbEntry2.WORD_MEAN2)));
            text3.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.DbEntry2.WORD_MEAN3)));
            text4.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.DbEntry2.WORD_MEAN4)));
            text5.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.DbEntry2.WORD_MEAN5)));
        }
    }

    private void speak(String spell){
        float speed = (float)mSeekBarSpeed.getProgress()/50;
        if(speed <0.1) speed = 0.1f;

        mTTS.setSpeechRate(speed);

        mTTS.speak(spell, TextToSpeech.QUEUE_FLUSH, null);
    }

    //뒤로가기 버튼 재정의
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(wordMain.this, MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }
}
