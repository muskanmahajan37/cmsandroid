
package com.zia.freshdocs.activity;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.zia.freshdocs.R;
import com.zia.freshdocs.widget.fileexplorer.FileManager;

/**
 * Provides information about a specific directory.
 * */
public class DirectoryInfoActivity extends Activity {
	private static final int KB = 1024;
	private static final int MG = KB * KB;
	private static final int GB = MG * KB;
	private String mPathName;
	private TextView mNameLabel, mPathLabel, mDirLabel,
					 mFileLabel, mTimeLabel, mTotalLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_layout);
		
		Intent i = getIntent();
		if(i != null) {
			if(i.getAction() != null && i.getAction().equals(Intent.ACTION_VIEW)) {
				mPathName = i.getData().getPath();
				
				if(mPathName == null)
					mPathName = "";
			} else {
				mPathName = i.getExtras().getString("PATH_NAME");
			}
		}
		
		mNameLabel = (TextView)findViewById(R.id.name_label);
		mPathLabel = (TextView)findViewById(R.id.path_label);
		mDirLabel = (TextView)findViewById(R.id.dirs_label);
		mFileLabel = (TextView)findViewById(R.id.files_label);
		mTimeLabel = (TextView)findViewById(R.id.time_stamp);
		mTotalLabel = (TextView)findViewById(R.id.total_size);
				
		/* make zip button visible and setup onclick logic to have zip button 
		 */
		Button zip = (Button)findViewById(R.id.zip_button);
		zip.setVisibility(Button.GONE);
		 
		
		Button back = (Button)findViewById(R.id.back_button);
		back.setOnClickListener(new ButtonHandler());
		
		new BackgroundWork().execute(mPathName);
		
	}
	
	/**
	 * This class allows to perform background operations and publish results on
	 * the UI thread without having to manipulate threads and/or handlers.
	 */

	private class BackgroundWork extends AsyncTask<String, Void, Long> {
		private ProgressDialog dialog;
		private String mDisplaySize;
		private int mFileCount = 0;
		private int mDirCount = 0;
		
		protected void onPreExecute(){
			dialog = ProgressDialog.show(DirectoryInfoActivity.this, "", "Calculating information...", true, true);
		}
		
		protected Long doInBackground(String... vals) {
			FileManager flmg = new FileManager();
			File dir = new File(vals[0]);
			long size = 0;
			int len = 0;

			File[] list = dir.listFiles();
			if(list != null){
				len = list.length;
			}
			
			for (int i = 0; i < len; i++){
				if(list[i].isFile()){
					mFileCount++;
				}else if(list[i].isDirectory()){
					mDirCount++;
				}
			}
			
			if(vals[0].equals("/")) {				
				StatFs fss = new StatFs(Environment.getRootDirectory().getPath());
				size = fss.getAvailableBlocks() * (fss.getBlockSize() / KB);
				
				mDisplaySize = (size > GB) ? 
						String.format("%.2f Gb ", (double)size / MG) :
						String.format("%.2f Mb ", (double)size / KB);
				
			}else if(vals[0].equals("/sdcard")) {
				StatFs fs = new StatFs(Environment.getExternalStorageDirectory()
										.getPath());
				size = fs.getBlockCount() * (fs.getBlockSize() / KB);
				
				mDisplaySize = (size > GB) ? 
					String.format("%.2f Gb ", (double)size / GB) :
					String.format("%.2f Gb ", (double)size / MG);
				
			} else {
				size = flmg.getDirSize(vals[0]);
						
				if (size > GB)
					mDisplaySize = String.format("%.2f Gb ", (double)size / GB);
				else if (size < GB && size > MG)
					mDisplaySize = String.format("%.2f Mb ", (double)size / MG);
				else if (size < MG && size > KB)
					mDisplaySize = String.format("%.2f Kb ", (double)size/ KB);
				else
					mDisplaySize = String.format("%.2f bytes ", (double)size);
			}
			
			return size;
		}
		
		protected void onPostExecute(Long result) {
			File dir = new File(mPathName);
			
			mNameLabel.setText(dir.getName());
			mPathLabel.setText(dir.getAbsolutePath());
			mDirLabel.setText(mDirCount + " folders ");
			mFileLabel.setText(mFileCount + " files ");
			mTotalLabel.setText(mDisplaySize);
			Date date  = new Date(dir.lastModified());
			mTimeLabel.setText(String.valueOf(date.getYear() + 1900) 
								+ "-"  + String.valueOf(Convert(date.getMonth() + 1))
								+ "-"  + String.valueOf(Convert(date.getDate()))
								+ " "  + String.valueOf(Convert(date.getHours()))
								+ ":"  + String.valueOf(Convert(date.getMinutes()))
			);
			
			dialog.cancel();
		}	
	}
	/**
	 * Convert number to right format
	 * */
    private String Convert(int number){
        String temp;
        if(number >= 10){
        	temp = "" + number;
        }else{
        	temp = "0" + number;
        } 
        return temp;
    }
	
	private class ButtonHandler implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.back_button)
				finish();
		}
	}
}
