package com.example.prototype;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Multiple_choice extends AppCompatActivity {

    private DBHelper databaseHelper;
    private SQLiteDatabase db;

    private static final float FONT_SIZE = 20;   // 선택지 TextView 때문에
    private LinearLayout parent_option;
    TextView problem;

    int answer_id; // 정답 저장되는 변수

    ArrayList<Integer> problem_view_arr;

    int random; // 정답 랜덤으로 하기 위한 변수

    int number_of_correct_answers = 0;  // 정답 횟수
    int wrong_count = 0; // 틀린횟수

    private long mWordbookId = -1;
    public static final int REQUEST_CODE_INSERT = 1001;

    Cursor cursor, cu1;

    int temp1, temp2, temp3, temp4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        databaseHelper = new DBHelper(this);

        db = databaseHelper.getWritableDatabase();   // 쓰기 모드

        problem_view_arr = new ArrayList<Integer>();


        /**
         *
         * 문제 출력하는 곳
         * 일단 임시로 mean1에 있는 것만 들고 옴  2020-05-26
         *
         * **/

        Intent intent = getIntent();
        final String WordbookId = Long.toString(intent.getLongExtra("wordbookId", -1));
        problem = (TextView) findViewById(R.id.problem);

        //문제 커서
        cursor = db.rawQuery("SELECT " + DbContract.DbEntry2.WORD_SPELL + "," + DbContract.DbEntry2._ID + " FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId + " AND " + DbContract.DbEntry2.DATE + " = date('now') order by random()", null);
        cursor.moveToFirst();

        problem.setText(cursor.getString(0));
        answer_id = cursor.getInt(1);

        //보기 커서
        cu1 = db.rawQuery("select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from(" +
                "select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from (" +
                " select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from "
                + DbContract.DbEntry2.TABLE_NAME + " where " + DbContract.DbEntry2._ID + "=" + answer_id + ")" +
                " union " +
                "select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from(" +
                " select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from "
                + DbContract.DbEntry2.TABLE_NAME + " where "+ DbContract.DbEntry2.WORDBOOK_ID+"="+WordbookId+" and " + DbContract.DbEntry2._ID + "!=" + answer_id + " order by random() limit 3)) as a order by random()", null);

        parent_option = (LinearLayout) findViewById(R.id.parent_option);
        LinearLayout.LayoutParams parent_layout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        parent_option.setLayoutParams(parent_layout);

        problem_view();


    }

    /**
     * 영어단어 작을 때도 유동적으로 하기 위해 그때 그때 TextView 생성
     **/
    public void create_text_view(int a) {
        int random = (int) (Math.random() * 5 + 2);
        while (cu1.getString(random).equals("")) {
            random = (int) (Math.random() * 5 + 2);
        }
        TextView view = new TextView(this);
        view.setText(cu1.getString(random));
        view.setTextSize(FONT_SIZE);
        view.setTextColor(Color.BLACK);
        view.setBackgroundResource(R.drawable.border_radius2);
        view.setGravity(Gravity.CENTER);
        view.setTag(a);                    // 여기까지 TextView 설정(글자색, 폰트크기 등등)

        view.setOnClickListener(problem_text);          // 각각의 TextView의 클릭 이벤트 설정


        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                3
        );

        p.setMargins(50, 0, 50, 20);
        view.setLayoutParams(p);

        parent_option.addView(view);
    }

    /**
     * 보기를 누르면 출력값이 변경됨
     * 보기가 새로 만든거라서 다시 실행하면 보기가 계속생김 --> 그래서 삭제하고 다시 생성
     **/
    public void reset_activity() {

        LinearLayout background = (LinearLayout)findViewById(R.id.background);
        Intent intent = getIntent();
        final String WordbookId = Long.toString(intent.getLongExtra("wordbookId", -1));
        String new_id = WordbookId;
        cursor = db.rawQuery("SELECT " + DbContract.DbEntry2.WORD_SPELL + "," + DbContract.DbEntry2._ID + " FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId + " AND " + DbContract.DbEntry2.DATE + " = date('now') order by random()", null);

        if (cursor.getCount() == 0) {
            background.setVisibility(View.GONE);
            Intent go_to_result = new Intent(getApplicationContext(), Problem_result_activity.class);
            go_to_result.putExtra("wordbookId", new_id);
            go_to_result.putExtra("answer_count", number_of_correct_answers);
            go_to_result.putExtra("wrong_count", wrong_count);
            finish();
            startActivityForResult(go_to_result, REQUEST_CODE_INSERT);
        } else {
            cursor.moveToFirst();
            problem.setText(cursor.getString(0));
            answer_id = cursor.getInt(1);
            Log.d("err", "ttttt");


            cu1 = db.rawQuery("select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from(" +
                    "select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from (" +
                    " select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from "
                    + DbContract.DbEntry2.TABLE_NAME + " where " + DbContract.DbEntry2._ID + "=" + answer_id + ")" +
                    " union " +
                    "select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from(" +
                    " select " + DbContract.DbEntry2._ID + ", " + DbContract.DbEntry2.WORD_SPELL + ", " + DbContract.DbEntry2.WORD_MEAN1 + ", " + DbContract.DbEntry2.WORD_MEAN2 + ", " + DbContract.DbEntry2.WORD_MEAN3 + ", " + DbContract.DbEntry2.WORD_MEAN4 + ", " + DbContract.DbEntry2.WORD_MEAN5 + " from "
                    + DbContract.DbEntry2.TABLE_NAME + " where " + DbContract.DbEntry2.WORDBOOK_ID + " = " + WordbookId + " and " + DbContract.DbEntry2._ID + "!=" + answer_id + " order by random() limit 3)) as a order by random()", null);



            parent_option.removeAllViews(); // 기존 보기 삭제


            problem_view();
        }


    }

    private View.OnClickListener problem_text = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int view_tag = (Integer) v.getTag();

            switch (view_tag) {
                case 1:
                    if (temp1 == answer_id) {
                        Toast.makeText(getApplicationContext(), "정답", Toast.LENGTH_SHORT).show();
                        number_of_correct_answers += 1;
                        save_problem_count(random, true);
                        reset_activity();
                    } else {
                        Cursor answer_cu=db.rawQuery("select "+ DbContract.DbEntry2 .WORD_SPELL+" from "+ DbContract.DbEntry2.TABLE_NAME+" where "+ DbContract.DbEntry2._ID+"="+answer_id,null);
                        answer_cu.moveToFirst();

                        Toast.makeText(getApplicationContext(), "틀림\n정답:"+answer_cu.getString(0), Toast.LENGTH_SHORT).show();
                        wrong_count += 1;
                        save_problem_count(random, false);
                        reset_activity();
                    }
                    break;

                case 2:
                    if (temp2 == answer_id) {
                        Toast.makeText(getApplicationContext(), "정답", Toast.LENGTH_SHORT).show();
                        number_of_correct_answers += 1;
                        save_problem_count(random, true);
                        reset_activity();
                    } else {
                        Cursor answer_cu=db.rawQuery("select "+ DbContract.DbEntry2 .WORD_SPELL+" from "+ DbContract.DbEntry2.TABLE_NAME+" where "+ DbContract.DbEntry2._ID+"="+answer_id,null);
                        answer_cu.moveToFirst();

                        Toast.makeText(getApplicationContext(), "틀림\n정답:"+answer_cu.getString(0), Toast.LENGTH_SHORT).show();
                        wrong_count += 1;
                        save_problem_count(random, false);
                        reset_activity();
                    }
                    break;

                case 3:
                    if (temp3 == answer_id) {
                        Toast.makeText(getApplicationContext(), "정답", Toast.LENGTH_SHORT).show();
                        number_of_correct_answers += 1;
                        save_problem_count(random, true);
                        reset_activity();
                    } else {
                        Cursor answer_cu=db.rawQuery("select "+ DbContract.DbEntry2 .WORD_SPELL+" from "+ DbContract.DbEntry2.TABLE_NAME+" where "+ DbContract.DbEntry2._ID+"="+answer_id,null);
                        answer_cu.moveToFirst();

                        Toast.makeText(getApplicationContext(), "틀림\n정답:"+answer_cu.getString(0), Toast.LENGTH_SHORT).show();
                        wrong_count += 1;
                        save_problem_count(random, false);
                        reset_activity();
                    }
                    break;

                case 4:
                    if (temp4 == answer_id) {
                        Toast.makeText(getApplicationContext(), "정답", Toast.LENGTH_SHORT).show();
                        number_of_correct_answers += 1;
                        save_problem_count(random, true);
                        reset_activity();
                    } else {
                        Cursor answer_cu=db.rawQuery("select "+ DbContract.DbEntry2 .WORD_SPELL+" from "+ DbContract.DbEntry2.TABLE_NAME+" where "+ DbContract.DbEntry2._ID+"="+answer_id,null);
                        answer_cu.moveToFirst();

                        Toast.makeText(getApplicationContext(), "틀림\n정답:"+answer_cu.getString(0), Toast.LENGTH_SHORT).show();
                        wrong_count += 1;
                        save_problem_count(random, false);
                        reset_activity();
                    }
                    break;

                default:
                    // 아무일도 안 일어남
            }

        }
    };

    @Override
    public void onBackPressed() {
        Toast.makeText(
                getApplicationContext(),
                "뒤로가기 버튼을 눌러 \n결과를 저장하지 않고 돌아갑니다.",
                Toast.LENGTH_SHORT).show();
        Intent go_home = new Intent(this, MainActivity.class);
        finish();
        startActivity(go_home);
    }


    public void save_problem_count(int random, boolean i) {
        databaseHelper = new DBHelper(this);
        db = databaseHelper.getWritableDatabase();   // 쓰기 모드

        Intent intent = getIntent();
        final String WordbookId = Long.toString(intent.getLongExtra("wordbookId", -1));


        String answer_word = cursor.getString(0);  //정답 단어 가져오기

        Cursor cursor2 = db.rawQuery("select " + DbContract.DbEntry2.CORRECT_ANSWER + " from " + DbContract.DbEntry2.TABLE_NAME + " where " + DbContract.DbEntry2._ID + " = " + answer_id, null);
        cursor2.moveToFirst();

        if (i) {   //정답일 때  correct_answer + 1

            switch (cursor2.getInt(0)) {
                case 0:
                    /**
                     * 0일땐 correct_answer 증가
                     * 1일 뒤 문제 나오게 date 설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + "= date('now','+1 days'), " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "+ 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                case 1:
                    /**
                     * 1 일땐 correct_answer 증가
                     * 2일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + "= date('now','+2 days'), " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "+ 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                case 2:
                    /**
                     * 2 일땐 correct_answer 증가
                     * 3일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + "= date('now','+3 days'), " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "+ 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                case 3:
                    /**
                     * 3 일땐 correct_answer 증가
                     * 4일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + "= date('now','+4 days'), " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "+ 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                case 4:
                    /**
                     * 4 일땐 correct_answer 1증가
                     * date null 로 설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + "= null, " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "+ 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                default:
                    break;
            }
        } else {  //틀렸을 때 correct_answer -1
            switch (cursor2.getInt(0)) {
                case 0:
                    /**
                     * 0일땐
                     * 1일 뒤 문제 나오게 date 설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + " = date('now','+1 days') where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                case 1:
                    /**
                     * 1 일땐 correct_answer 감소
                     * 1일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + " = date('now','+1 days'), " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "- 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                case 2:
                    /**
                     * 2 일땐 correct_answer 감소
                     * 2일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + " = date('now','+2 days'), " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "- 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                case 3:
                    /**
                     * 3 일땐 correct_answer 감소
                     * 3일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + " = date('now','+3 days'), " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "- 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                case 4:
                    /**
                     * 4 일땐 correct_answer 1 감소
                     * 4일 뒤 문제 나오게 date 설정
                     * **/
                    db.execSQL("update " + DbContract.DbEntry2.TABLE_NAME + " set " + DbContract.DbEntry2.DATE + " = date('now','+4 days'), " + DbContract.DbEntry2.CORRECT_ANSWER + " = " + DbContract.DbEntry2.CORRECT_ANSWER + "- 1" +
                            " where " + DbContract.DbEntry2.WORD_SPELL + " = '" + answer_word + "'");
                    break;

                default:
                    break;
            }
        }


    }

    private void problem_view() {

        problem_view_arr = new ArrayList<Integer>();

        cu1.moveToFirst();
        Log.d("answer_id", "" + answer_id);

        temp1 = cu1.getInt(0);
        Log.d("test_number", "" + temp1);
        Log.d("arr_1", "" + temp1);

        cu1.moveToNext();
        temp2 = cu1.getInt(0);
        Log.d("arr_2", "" + temp2);

        cu1.moveToNext();
        temp3 = cu1.getInt(0);
        Log.d("arr_3", "" + temp3);

        cu1.moveToNext();
        temp4 = cu1.getInt(0);
        Log.d("arr_4", "" + temp4);


        cu1.moveToFirst();
        Log.d("id_1", "" + cu1.getInt(0));
        create_text_view(1);

        cu1.moveToNext();
        Log.d("id_2", "" + cu1.getInt(0));
        create_text_view(2);

        cu1.moveToNext();
        Log.d("id_3", "" + cu1.getInt(0));
        create_text_view(3);

        cu1.moveToNext();
        Log.d("id_4", "" + cu1.getInt(0));
        create_text_view(4);


    }


}


