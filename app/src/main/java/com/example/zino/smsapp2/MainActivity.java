package com.example.zino.smsapp2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    String TAG=this.getClass().getName();

    /*문자관련 코드*/
    SmsManager smsManager;
    String dest="010-2867-9055";
    String msg="아이가 유치원에 도책했어요";
    public static final int REQUEST_SMS=1;

    /*NFC 관련*/
    NfcAdapter nfcAdapter;
    Intent intent; /* 시스템이 보내준 NDEF_DISCOVERED 인텐트*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*static 메서드로 인스턴스 얻기!!
        * 시스템 의존적인 객체는 개발자가 new 하기보다는 static 메서드로
        * 얻는 방법을 주로 제공...
        * */
        smsManager = SmsManager.getDefault();
        nfcCheck();
        readFromNfc();
    }

    /*NFC 지원여부를 체크하는 메서드 정의*/
    public void nfcCheck(){
        nfcAdapter=NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter==null){
            Toast.makeText(this, "이 디바이스는 NFC를 지원하지 않습니다", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "축하합니다", Toast.LENGTH_LONG).show();
        }
    }

    /*시스템이 넘겨준 인텐트로 부터 데이터를 추출해보자!!*/
    public void readFromNfc(){
        intent = getIntent();

        /*인텐트에 데이터를 전달할때 객체는 자체를 넣지는 못한다..안드로이드에서는
         intent 에 데이터를 넣을때 조각을 내서 탑재하고 이 과정을 Parcelable 인테페이스가
         담당한다...*/

        /*Tag > NdefMessage > NfedRecord */
        Parcelable[] parcelables=intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if(parcelables == null){
            //Toast.makeText(this, "데이터가 없네요", Toast.LENGTH_SHORT).show();
            return;
        }else{

            for(int i=0;i<parcelables.length;i++){
                /*메세지 추출하고 형변환*/
                NdefMessage ndefMessage=(NdefMessage) parcelables[i];

                /*메세지 안에 들어있는 레코드 갯수만큼....*/
                for(int a=0;a<ndefMessage.getRecords().length;a++){
                    /*레코드 추출*/
                    NdefRecord record=ndefMessage.getRecords()[a];
                    String data=decode(record.getPayload()); /*레코드에 들어있는 데이터 추출 반환형 byte[]*/

                    Toast.makeText(this,data,Toast.LENGTH_LONG).show();
                    /*읽혀진 메세지가 조건에 맞는다면 문자 보내자!!*/
                    if(data.equals("mommy")){
                        sendStart();
                    }
                }
            }

        }
    }

    /*byte[] 배열을 String으로 반환하는 메서드 */
    public String decode(byte[] buf) {
        String strText = "";
        String textEncoding = ((buf[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        int langCodeLen = buf[0] & 0077;

        try {
            strText = new String(buf, langCodeLen + 1, buf.length - langCodeLen - 1, textEncoding);
        } catch (Exception e) {
            Log.d("tag1", e.toString());
        }
        return strText;
    }

    public void send(View view){
        sendStart();
    }

    public void sendStart(){
        /*현재 앱이 해당 권한을 취득한 상태인지 물어봐서 처리해야 한다..*/

        /*현재 앱이 SMS 권한을 취득했는지 부터 체크하자....
        * 1) 취득 안한 상태라면? 유저에게 물어봐서 승인받아야 한다..
        * 2) 이미 승인 받았다면 하던거 진행...
        * */

        /*과거 스마트폰과의 코드 호환성을 위해서는 아래의 퍼미션 권한 코드를 조건문으로 처리해보자
        *
        * */
        Log.d(TAG, "당신의 SDK 버전은 "+ Build.VERSION.SDK_INT);
        /*안드로이드의 신규 보안 정책은 api 23부터 시작한다 따라서, 현재 이앱이 쓰고 있는
        * 버전이 23보다 작은지 큰지에 따라 보안 정책여부를 결정하는 코드를 작성하면 된다..
        * */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.SEND_SMS)  == PackageManager.PERMISSION_DENIED){
                /*요청시 sms 뿐 아니라 원하는 권한을 배열로 복수개 요청가능*/
                String[] permissions={Manifest.permission.SEND_SMS};
                requestPermissions(permissions, REQUEST_SMS);
            }else{/*권한을 이미 수락한 사람은 문자를 보내게 해준다...*/
                sendSMS();
            }
        }else{
            /*그냥 고전적 코드 작성*/
            sendSMS();
        }

    }

    /*저 위에서 requestPermissions() 메서드로 요청한  결과를 받는 메서드
    * 유저는 승인을 하거나, 거부하게 되면 아래의 메서드가 그결과를 받게된다...
    * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*내가 요청한 내용에 대한 응답이라면... 우리는 1로 요청했다, 따라서 응답도 1로 받아야 한다*/
        if(requestCode == REQUEST_SMS){
            /*grantResults[0] 에는 유저의 행동 결과가 들어있다...*/
            if(permissions.length >0 && grantResults[0] != PackageManager.PERMISSION_DENIED){
                sendSMS();
            }else{
                Toast.makeText(this, "승인을 하셔야 앱을 이용하실수 있습니다", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void sendSMS(){
        smsManager.sendTextMessage(dest,null,msg,null,null);
    }
}
