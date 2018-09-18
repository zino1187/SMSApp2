package com.example.zino.smsapp2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    SmsManager smsManager;
    String dest="010-2867-9055";
    String msg="아이가 유치원에 도책했어요";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*static 메서드로 인스턴스 얻기!!
        * 시스템 의존적인 객체는 개발자가 new 하기보다는 static 메서드로
        * 얻는 방법을 주로 제공...
        * */
        smsManager = SmsManager.getDefault();
    }

    public void send(View view){
        smsManager.sendTextMessage(dest,null,msg,null,null);
    }

}
