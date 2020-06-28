package com.example.prototype;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;



import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class WebMain extends AppCompatActivity {

    WebView webView;
    String source = "";

    FloatingActionButton button;

    String spell ="";
    String mean1="", mean2="", mean3="",mean4="", mean5="";

    public static final int REQUEST_CODE_INSERT = 1001;

    private long mWordbookId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_main);





        webView = findViewById(R.id.webView);
        button = findViewById(R.id.fab);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 자바스크립트 인터페이스로 연결되어 있는 getHTML를 실행
                // 자바스크립트 기본 메소드로 html 소스를 통째로 지정해서 인자로 넘김
                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('body')[0].innerHTML);");
                button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                button.setVisibility(View.INVISIBLE);
            }
        });

        webView.loadUrl("https://dic.daum.net/");


        Intent intent = getIntent();

        mWordbookId = intent.getLongExtra("wordbookId", -1);

        final String sTitle = intent.getStringExtra("title");
        final String sSubtitle = intent.getStringExtra("subtitle");



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(source == ""){
                    Toast.makeText(WebMain.this, "작업이 모두 완료될 때까지 기다려 주세요", Toast.LENGTH_SHORT).show();
                }else {
                    Document doc = Jsoup.parse(source);
                    Elements contents1;
                    Element contents2;
                    Element contents3;
                    Element contents4;
                    Element contents5;
                    Element contents6;

                    try {
                        contents1 = doc.select("a.txt_cleansch span");
                        if(contents1.text() == ""){
                            contents1 = doc.select("div.clean_word h3.tit_cleanword span.txt_cleanword");
                        }
                        spell = contents1.text();
                    } catch (Exception e) {
                        spell = "";
                    }
                    try {
                        contents2 = doc.select("div.cleanword_type ul.list_search li span.txt_search").first();
                        mean1 = contents2.text();
                    } catch (Exception e) {
                        try{
                            contents2 = doc.select("ul.list_mean li span.txt_mean").first();
                            mean1 = contents2.text();
                        }catch(Exception e1){
                            mean1 = "";
                        }
                    }
                    try {
                        contents3 = doc.select("div.cleanword_type ul.list_search li").next().first();
                        mean2 = contents3.select("span.txt_search").text();
                    } catch (Exception e) {
                        try{
                            contents3 = doc.select("ul.list_mean li").next().first();
                            mean2 = contents3.select("span.txt_mean").text();
                        }catch(Exception e1){
                            mean2 = "";
                        }
                    }
                    try {
                        contents4 = doc.select("div.cleanword_type ul.list_search li").next().next().first();
                        mean3 = contents4.select("span.txt_search").text();
                    } catch (Exception e) {
                        try{
                            contents4 = doc.select("ul.list_mean li").next().next().first();
                            mean3 = contents4.select(" span.txt_mean").text();
                        }catch(Exception e1){
                            mean3 = "";
                        }
                    }
                    try {
                        contents5 = doc.select("div.cleanword_type ul.list_search li").next().next().next().first();
                        mean4 = contents5.select("span.txt_search").text();
                    } catch (Exception e) {
                        try{
                            contents5 = doc.select("ul.list_mean li").next().next().next().first();
                            mean4 = contents5.select("span.txt_mean").text();
                        }catch(Exception e1){
                            mean4 ="";
                        }
                    }
                    try {
                        contents6 = doc.select("div.cleanword_type ul.list_search li").next().next().next().next().first();
                        mean5 = contents6.select("span.txt_search").text();
                    } catch (Exception e) {
                        try{
                            contents6 = doc.select("ul.list_mean li").next().next().next().next().first();
                            mean5 = contents6.select("span.txt_mean").text();
                        }catch(Exception e1){
                            mean5 = "";
                        }
                    }
                    if (spell == "" && mean1 == "" && mean2 == "" && mean3 == "" && mean4 == "" && mean5 == "") {
                        Toast.makeText(WebMain.this, "페이지에 단어가 없거나 로딩이 끝나지 않았습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent1 = new Intent(WebMain.this, add_word.class);
                        intent1.putExtra("wordbookId", mWordbookId);
                        intent1.putExtra("title", sTitle);
                        intent1.putExtra("subtitle", sSubtitle);
                        intent1.putExtra("spell", spell);
                        intent1.putExtra("mean1", mean1);
                        intent1.putExtra("mean2", mean2);
                        intent1.putExtra("mean3", mean3);
                        intent1.putExtra("mean4", mean4);
                        intent1.putExtra("mean5", mean5);

                        startActivityForResult(intent1, REQUEST_CODE_INSERT);
                    }
                }
            }
        });

    }


    public class MyJavascriptInterface {
        @JavascriptInterface
        public void getHtml(String html) {
            //위 자바스크립트가 호출되면 여기로 html이 반환됨
            source = html;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_back:
                if(webView.canGoBack()){
                    webView.goBack();
                }
                return true;
            case R.id.action_forward:
                if(webView.canGoForward()){
                    webView.goForward();
                }
                return true;
            case R.id.action_refresh:
                webView.reload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
