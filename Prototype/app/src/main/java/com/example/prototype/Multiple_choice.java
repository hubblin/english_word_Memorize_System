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

public class Multiple_choice extends AppCompatActivity {

    private DBHelper databaseHelper;
    private SQLiteDatabase db;

    private static final float FONT_SIZE = 20;   // 선택지 TextView 때문에
    private LinearLayout parent_option;

    int answer; // 정답 저장되는 변수
    int result[] = new int[4]; // 보기 커서 위치 용
    int answer_arr[]= new int[30];

    int random; // 정답 랜덤으로 하기 위한 변수
    int result_arr_length;
    int count =0;
    int count2 = 0;

    int number_of_correct_answers = 0;  // 정답 횟수
    int wrong_count = 0; // 틀린횟수

    private long mWordbookId = -1;
    public static final int REQUEST_CODE_INSERT = 1001;

    Cursor cursor;
    int tempnumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        databaseHelper = new DBHelper(this);

        db = databaseHelper.getWritableDatabase();   // 쓰기 모드



        /**
         *
         * 문제 출력하는 곳
         * 일단 임시로 mean1에 있는 것만 들고 옴  2020-05-26
         *
         * **/

        Intent intent = getIntent();
        final String WordbookId = Long.toString(intent.getLongExtra("wordbookId", -1));
        TextView problem = (TextView) findViewById(R.id.problem);
        cursor = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId+" AND "+ DbContract.DbEntry2.DATE +" = date('now')",null);
        tempnumber = cursor.getCount();
        for(int i=0;i<cursor.getCount();i++){  // 문제 중복제거
            answer_arr[i] = (int)(Math.random()*cursor.getCount());

            for (int j=0;j<i;j++){
                if(answer_arr[i] == answer_arr[j]){
                    i--;
                }
            }

        }

        random = answer_arr[count2];  // 해당 단어장의 단어DB 테이블의 행의 크기 만큼 랜덤
        cursor.moveToPosition(random);
        problem.setText(cursor.getString(1));
        count2 =+ 1;

        parent_option = (LinearLayout) findViewById(R.id.parent_option);
        LinearLayout.LayoutParams parent_layout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        parent_option.setLayoutParams(parent_layout);



        for(int i=0;i<4;i++){  // 문제 보기 중복 제거
            result[i] = (int)(Math.random()*cursor.getCount());
            for (int j=0;j<i;j++){
                if(result[i] == result[j]){
                    i--;
                }
            }
        }
        for(int i = 0; i<4; i++){
            if (result[i] == random){
                count += 1;
            }
        }                              // 랜덤으로 생성한 보기에 정답이 있으면 아래 if 패스  // 없으면 정답 랜덤으로 지정
        if(count==0){
            result_arr_length = (int) (Math.random()*3);
            result[result_arr_length] = random;
        }
        count =0;

        /**
         * 보기 생성 및 단어 가져오기
         * **/
        Cursor cu1 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        Cursor cu2 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        Cursor cu3 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        Cursor cu4 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        create_text_view(1, result[0], cu1);
        create_text_view(2, result[1], cu2);
        create_text_view(3, result[2], cu3);
        create_text_view(4, result[3], cu4);




    }
    /**
     * 영어단어 작을 때도 유동적으로 하기 위해 그때 그때 TextView 생성
     *
     * **/
    public void create_text_view(int a, int i, Cursor cu){
        cu.moveToPosition(i);
        TextView view = new TextView(this);
        view.setText(cu.getString(2));
        view.setTextSize(FONT_SIZE);
        view.setTextColor(Color.BLACK);
        view.setBackgroundResource(R.drawable.border_radius);
        view.setGravity(Gravity.CENTER);
        view.setTag(a);                    // 여기까지 TextView 설정(글자색, 폰트크기 등등)

        view.setOnClickListener(problem_text);          // 각각의 TextView의 클릭 이벤트 설정


        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //p.gravity = Gravity.CENTER;
        p.setMargins(50, 0 ,50, 20);
        view.setLayoutParams(p);

        parent_option.addView(view);
    }
    /**
     * 보기를 누르면 출력값이 변경됨
     * 보기가 새로 만든거라서 다시 실행하면 보기가 계속생김 --> 그래서 삭제하고 다시 생성
     * **/
    public void reset_activity(){
        Intent intent = getIntent();
        final String WordbookId = Long.toString(intent.getLongExtra("wordbookId", -1));
        String new_id = WordbookId;


        Log.d(new_id,"응ㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇ");
        if(tempnumber == number_of_correct_answers+wrong_count){  // 디비에 저장된 단어수에 따라 문제생성후 끝내기

            Intent go_to_result = new Intent(getApplicationContext(), Problem_result_activity.class);
            go_to_result.putExtra("wordbookId",new_id);
            go_to_result.putExtra("answer_count",number_of_correct_answers);
            go_to_result.putExtra("wrong_count",wrong_count);
            finish();
            startActivityForResult(go_to_result,REQUEST_CODE_INSERT);
        }

        Toast.makeText(getApplicationContext(), "정답횟수:"+number_of_correct_answers+"\n틀린횟수:"+wrong_count,Toast.LENGTH_SHORT).show();

        parent_option.removeAllViews(); // 기존 보기 삭제

        TextView problem = (TextView) findViewById(R.id.problem);
        Cursor cursor = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        random = answer_arr[count2];  // 해당 단어장의 단어DB 테이블의 행의 크기 만큼 랜덤
        cursor.moveToPosition(random);
        problem.setText(cursor.getString(1));
        //Toast.makeText(getApplicationContext(), count2,Toast.LENGTH_SHORT).show();
        count2 += 1;

        for(int i=0;i<4;i++){  // 문제 보기 중복 제거
            result[i] = (int)(Math.random()*cursor.getCount());
            for (int j=0;j<i;j++){
                if(result[i] == result[j]){
                    i--;
                }
            }
        }
        for(int i = 0; i<4; i++){
            if (result[i] == random){
                count += 1;
            }
        }                              // 랜덤으로 생성한 보기에 정답이 있으면 아래 if 패스  // 없으면 정답 랜덤으로 지정
        if(count==0){
            result_arr_length = (int) (Math.random()*3);
            result[result_arr_length] = random;
        }

        count = 0;

        /**
         * 보기 생성 및 단어 가져오기
         * **/
        Cursor cu1 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        Cursor cu2 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        Cursor cu3 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        Cursor cu4 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry2.TABLE_NAME + " WHERE " + DbContract.DbEntry2.WORDBOOK_ID + "=" + WordbookId,null);
        create_text_view(1, result[0], cu1);
        create_text_view(2, result[1], cu2);
        create_text_view(3, result[2], cu3);
        create_text_view(4, result[3], cu4);


    }

    private View.OnClickListener problem_text = new View.OnClickListener() {
        @Override
        public void onClick(View v ) {
            int view_tag = (Integer)v.getTag();

            switch (view_tag){
                case 1:
                    if (result[0] == random){
                        //Toast.makeText(getApplicationContext(),"정답",Toast.LENGTH_SHORT).show();
                        number_of_correct_answers += 1;
                        save_problem_count(random, true);
                        reset_activity();
                    }
                    else {
                        //Toast.makeText(getApplicationContext(),"틀림",Toast.LENGTH_SHORT).show();
                        wrong_count += 1;
                        save_problem_count(random, false);
                        reset_activity();
                    }
                    break;

                case 2:
                    if (result[1] == random){
                        //Toast.makeText(getApplicationContext(),"정답",Toast.LENGTH_SHORT).show();
                        number_of_correct_answers += 1;
                        save_problem_count(random, true);
                        reset_activity();
                    }
                    else {
                        //Toast.makeText(getApplicationContext(),"틀림",Toast.LENGTH_SHORT).show();
                        wrong_count += 1;
                        save_problem_count(random, false);
                        reset_activity();
                    }
                    break;

                case 3:
                    if (result[2] == random){
                        //Toast.makeText(getApplicationContext(),"정답",Toast.LENGTH_SHORT).show();
                        number_of_correct_answers += 1;
                        save_problem_count(random, true);
                        reset_activity();
                    }
                    else {
                        //Toast.makeText(getApplicationContext(),"틀림",Toast.LENGTH_SHORT).show();
                        wrong_count += 1;
                        save_problem_count(random, false);
                        reset_activity();
                    }
                    break;

                case 4:
                    if (result[3] == random){
                        //Toast.makeText(getApplicationContext(),"정답",Toast.LENGTH_SHORT).show();
                        number_of_correct_answers += 1;
                        save_problem_count(random, true);
                        reset_activity();
                    }
                    else {
                        //Toast.makeText(getApplicationContext(),"틀림",Toast.LENGTH_SHORT).show();
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
    public void onBackPressed(){
        Toast.makeText(
                getApplicationContext(),
                "뒤로가기 버튼을 눌러 \n결과를 저장하지 않고 돌아갑니다.",
                Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }




    public void save_problem_count(int random, boolean i){
        databaseHelper = new DBHelper(this);
        db = databaseHelper.getWritableDatabase();   // 쓰기 모드

        Intent intent = getIntent();
        final String WordbookId = Long.toString(intent.getLongExtra("wordbookId", -1));

        Cursor cursor1 = db.rawQuery("select "+ DbContract.DbEntry2.WORD_SPELL+ " from "+ DbContract.DbEntry2.TABLE_NAME + " where "+ DbContract.DbEntry2.WORDBOOK_ID + " = "+WordbookId,null);
        cursor1.moveToPosition(random);
        String answer_word = cursor1.getString(0);  //정답 단어 가져오기

        Cursor cursor2 = db.rawQuery("select "+ DbContract.DbEntry2.CORRECT_ANSWER+" from "+ DbContract.DbEntry2.TABLE_NAME +" where "+ DbContract.DbEntry2.WORD_SPELL +" = '"+answer_word+"'",null);
        cursor2.moveToFirst();

        if(i){   //정답일 때  correct_answer + 1

            switch (cursor2.getInt(0)){
                case 0:
                    /**
                     * 0일땐 correct_answer 증가
                     * 2일 뒤 문제 나오게 date 설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +"= date('now','+2 days'), "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"+ 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                case 1:
                    /**
                     * 1 일땐 correct_answer 증가
                     * 3일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +"= date('now','+3 days'), "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"+ 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                case 2:
                    /**
                     * 2 일땐 correct_answer 증가
                     * 4일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +"= date('now','+4 days'), "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"+ 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                case 3:
                    /**
                     * 3 일땐 correct_answer 증가
                     * 8일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +"= date('now','+8 days'), "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"+ 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                case 4:
                    /**
                     * 4 일땐 correct_answer 1증가
                     * date null 로 설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +"= null, "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"+ 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                default:
                    break;
            }
        }
        else{  //틀렸을 때 correct_answer -1
            switch (cursor2.getInt(0)){
                case 0:
                    /**
                     * 0일땐
                     * 1일 뒤 문제 나오게 date 설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +" = date('now','+1 days') where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                case 1:
                    /**
                     * 1 일땐 correct_answer 감소
                     * 2일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +" = date('now','+2 days'), "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"- 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                case 2:
                    /**
                     * 2 일땐 correct_answer 감소
                     * 3일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +" = date('now','+3 days'), "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"- 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                case 3:
                    /**
                     * 3 일땐 correct_answer 감소
                     * 4일 뒤 문제 나오게  date설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +" = date('now','+4 days'), "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"- 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                case 4:
                    /**
                     * 4 일땐 correct_answer 1 감소
                     * 8일 뒤 문제 나오게 date 설정
                     * **/
                    db.execSQL("update "+ DbContract.DbEntry2.TABLE_NAME +" set "+ DbContract.DbEntry2.DATE +" = date('now','+8 days'), "+ DbContract.DbEntry2.CORRECT_ANSWER+ " = "+DbContract.DbEntry2.CORRECT_ANSWER+"- 1"+
                            " where "+ DbContract.DbEntry2.WORD_SPELL+ " = '"+answer_word+"'");
                    break;

                default:
                    break;
            }
        }


    }
}

