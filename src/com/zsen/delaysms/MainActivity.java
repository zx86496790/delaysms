package com.zsen.delaysms;

import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends Activity {

	EditText editTextReceiver;
	EditText editTextContent;
	ImageButton imageButtonReceiver;
	ImageButton imageButtonSend;
	ImageButton imageButtonCancle;
	SeekBar seekBarTime;
	ProgressBar pb;
	Handler handler = new Handler();
	Handler handler2=new Handler();
	Runnable updateThread;
	String usernumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editTextReceiver = (EditText) findViewById(R.id.editTexReceiver);
		editTextContent = (EditText) findViewById(R.id.editTextContent);
		imageButtonReceiver = (ImageButton) findViewById(R.id.imageButtonReceiver);
		imageButtonSend = (ImageButton) findViewById(R.id.imageButtonSend);
		imageButtonCancle = (ImageButton) findViewById(R.id.imageButtonCancle);
		seekBarTime = (SeekBar) findViewById(R.id.seekBarTime);
		imageButtonReceiver.setOnClickListener(new ChooseReceiverListener());
		imageButtonSend.setOnClickListener(new SendListener());
		imageButtonCancle.setOnClickListener(new CancleListener());
		pb=(ProgressBar)findViewById(R.id.progressBar1);
		updateThread = new Runnable() {
			@Override
			public void run() {
				SmsManager manager = SmsManager.getDefault();
				ArrayList<String> messages = manager
						.divideMessage(editTextContent.getText().toString());
				for (String ms : messages) {
					manager.sendTextMessage(usernumber, null, ms, null, null);
					Toast.makeText(getApplicationContext(), "SENT!", 0).show();
				}
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			// ContentProvider展示数据类似一个单个数据库表
			// ContentResolver实例带的方法可实现找到指定的ContentProvider并获取到ContentProvider的数据
			ContentResolver reContentResolverol = getContentResolver();
			// URI,每个ContentProvider定义一个唯一的公开的URI,用于指定到它的数据集
			Uri contactData = data.getData();
			// 查询就是输入URI等参数,其中URI是必须的,其他是可选的,如果系统能找到URI对应的ContentProvider将返回一个Cursor对象.
			Cursor cursor = managedQuery(contactData, null, null, null, null);
			cursor.moveToFirst();
			// 获得DATA表中的名字
			String username = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			// 条件为联系人ID
			String contactId = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			// 获得DATA表中的电话号码，条件为联系人ID,因为手机号码可能会有多个
			Cursor phone = reContentResolverol.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ contactId, null, null);
			while (phone.moveToNext()) {
				usernumber = phone
						.getString(phone
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				editTextReceiver.setText("To:" + username);
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class ChooseReceiverListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI), 0);
		}

	}

	public class CancleListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			handler.removeCallbacks(updateThread);
			Toast.makeText(getApplicationContext(), "Your text has been cancled", Toast.LENGTH_SHORT).show();
		}

	}

	public class SendListener implements OnClickListener {
		public void onClick(View v) {
			pb.setMax(seekBarTime.getProgress());
			pb.setProgress(seekBarTime.getProgress());
			handler.postDelayed(updateThread, seekBarTime.getProgress() * 1000);
		}
	}

}
