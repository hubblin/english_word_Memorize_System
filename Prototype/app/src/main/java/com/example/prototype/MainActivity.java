package com.example.prototype;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_INSERT = 1000;

    FloatingActionButton fab_add_list, fab_list_desc, fab_list_asc;

    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //플로팅 액션 버튼
        fab_add_list = findViewById(R.id.fab_add_list);
        fab_list_desc = findViewById(R.id.fab_desc);
        fab_list_asc = findViewById(R.id.fab_asc);

        fab_add_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, add_Wordbook_Activity.class), REQUEST_CODE_INSERT);
            }
        });


        //리스트 뷰
        final ListView listView = findViewById(R.id.title_list);
        //리스트 뷰 헤더
        final ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.listview_header, null, false);
        //리스트 뷰 푸터
        ViewGroup footer = (ViewGroup)getLayoutInflater().inflate(R.layout.wordbook_footer, null,false);

        listView.addHeaderView(header, null, false);
        listView.addFooterView(footer, null,false);

        final Cursor cursor = getListCursor();
        mAdapter = new ListAdapter(this, cursor);
        listView.setAdapter(mAdapter);

        fab_list_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Cursor cursor = getListCursor();
                mAdapter = new ListAdapter(MainActivity.this, cursor);
                listView.setAdapter(mAdapter);
            }
        });

        fab_list_asc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Cursor cursor = getListCursor2();
                mAdapter = new ListAdapter(MainActivity.this, cursor);
                listView.setAdapter(mAdapter);
            }
        });

        //다음 액티비티를 띄우면서 내용을 보여준다.(새로운 테이블로 넘어가야 한다,)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, wordMain.class);

                final long seletId = id;
                String title = "";
                String subtitle = "";

                SQLiteDatabase db = DBHelper.getInstance(MainActivity.this).getReadableDatabase();
                Cursor cursor1 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry.TABLE_NAME + " WHERE " + DbContract.DbEntry._ID + "=" + id, null);
                while(cursor1.moveToNext()){
                    int colid = cursor1.getColumnIndex(DbContract.DbEntry.WORDBOOK_NAME_TITLE);
                    int colid2 = cursor1.getColumnIndex(DbContract.DbEntry.WORDBOOK_NAME_SUBTITLE);
                    title = cursor1.getString(colid);
                    subtitle = cursor1.getString(colid2);
                }

                Toast.makeText(MainActivity.this,  title+"선택", Toast.LENGTH_SHORT).show();

                intent.putExtra("id", id);
                intent.putExtra("title", title);
                intent.putExtra("subtitle", subtitle);

                startActivity(intent);
            }
        });


        //길게 누르면 반응하는 부분
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final long deletedId = id;
                final String wordbook_id = Long.toString(id);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("단어장 변경");
                builder.setMessage("단어장을 변경하시겠습니까?");
                //긍정버튼
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = DBHelper.getInstance(MainActivity.this).getWritableDatabase();
                        //============================================================================================//
                        int deleteWord = db.delete(DbContract.DbEntry2.TABLE_NAME, DbContract.DbEntry2.WORDBOOK_ID + " = " + wordbook_id, null);
                        //============================================================================================//
                        int deletedCount = db.delete(DbContract.DbEntry.TABLE_NAME, DbContract.DbEntry._ID + " = " + deletedId, null);
                        if(deletedCount == 0 || deleteWord == 0){
                            Toast.makeText(MainActivity.this, "삭제에 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                        }else{
                            mAdapter.swapCursor(getListCursor());
                            Toast.makeText(MainActivity.this, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("수정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, add_Wordbook_Activity.class);
                String title = "";
                String subtitle = "";

                SQLiteDatabase db = DBHelper.getInstance(MainActivity.this).getReadableDatabase();
                Cursor cursor1 = db.rawQuery("SELECT * FROM " + DbContract.DbEntry.TABLE_NAME + " WHERE " + DbContract.DbEntry._ID + "=" + deletedId, null);
                while(cursor1.moveToNext()){
                    int colid = cursor1.getColumnIndex(DbContract.DbEntry.WORDBOOK_NAME_TITLE);
                    int colid2 = cursor1.getColumnIndex(DbContract.DbEntry.WORDBOOK_NAME_SUBTITLE);
                    title = cursor1.getString(colid);
                    subtitle = cursor1.getString(colid2);
                }

                intent.putExtra("id", deletedId);
                intent.putExtra("title", title);
                intent.putExtra("subtitle", subtitle);

                startActivityForResult(intent, REQUEST_CODE_INSERT);
            }
        });
        builder.show();
                return true;
            }
        });
    }



    //검색한거 뿌리는 부분
    private Cursor getListCursor(){
        DBHelper dbHelper = DBHelper.getInstance(this);
        return dbHelper.getReadableDatabase().query(DbContract.DbEntry.TABLE_NAME, null,null,null,null,null,DbContract.DbEntry._ID + " DESC");
    }

    private Cursor getListCursor2(){
        DBHelper dbHelper = DBHelper.getInstance(this);
        return dbHelper.getReadableDatabase().query(DbContract.DbEntry.TABLE_NAME, null,null,null,null,null,null);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_INSERT && requestCode == RESULT_OK){
            mAdapter.swapCursor(getListCursor());
        }
    }


    //리스트 어뎁터
    private static class ListAdapter extends CursorAdapter{

        public ListAdapter(Context context, Cursor c) {
            super(context, c, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            //레이아웃 불러오기
            return LayoutInflater.from(context).inflate(R.layout.row , parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //불러온 레이아웃에 아이디 값에 값 붙이기
            TextView titleView = view.findViewById(R.id.textView1);
            TextView subtitleView = view.findViewById(R.id.textView2);
            titleView.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.DbEntry.WORDBOOK_NAME_TITLE)));
            subtitleView.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.DbEntry.WORDBOOK_NAME_SUBTITLE)));
        }
    }
}
