package andy.android.com.youmarkermusic;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.List;


public class MainActivity extends Activity {
    ListView listView;
    Button bt_query;
    EditText et_keyWorld;
    String currentKeyWorld;
    YouMakerPaser mYouMakerPaser;
    MusicAdapter musicAdapter;
    ProgressDialog mProgressDialog;
    ProgressBar pb_search_wait;
    int backCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        bt_query = (Button) findViewById(R.id.button);
        et_keyWorld = (EditText) findViewById(R.id.editText);
        pb_search_wait = (ProgressBar) findViewById(R.id.progressBar);
        mYouMakerPaser = new YouMakerPaser();
        musicAdapter = new MusicAdapter(this);
        listView.setAdapter(musicAdapter);
        bt_query.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
              String keyWorld = et_keyWorld.getText().toString();
                if(!keyWorld.isEmpty()){
                    if(!keyWorld.equals(currentKeyWorld)) {
                        bt_query.setVisibility(View.GONE);
                        pb_search_wait.setVisibility(View.VISIBLE);
                        currentKeyWorld = keyWorld;
                        mYouMakerPaser.query(keyWorld);
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        backCount = 0;
        mYouMakerPaser.setOnDataUpdateListener(new YouMakerPaser.OnDataUpdateListener(){
            @Override
            public void onDataUpdate(List<Music> data, final String msg) {
               if(data!=null){
                   musicAdapter.setData(data);
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           bt_query.setVisibility(View.VISIBLE);
                           pb_search_wait.setVisibility(View.GONE);
                           et_keyWorld.setText("");
                           musicAdapter.notifyDataSetChanged();

                       }
                   });
               }else{
                   musicAdapter.setData(null);
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           bt_query.setVisibility(View.VISIBLE);
                           pb_search_wait.setVisibility(View.GONE);
                           musicAdapter.notifyDataSetChanged();
                           Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();

                       }
                   });
               }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mYouMakerPaser.setOnDataUpdateListener(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(backCount==0){
                backCount++;
                Toast.makeText(MainActivity.this,getString(R.string.back_tip),Toast.LENGTH_SHORT).show();
            }
            else if(backCount==1){
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private ProgressDialog getProgressDialog(){
        if(mProgressDialog==null){
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        return mProgressDialog;
    }
    public class MusicAdapter extends BaseAdapter{
        Context context;
        List<Music> data;

        public List<Music> getData() {
            return data;
        }

        public Context getContext() {
            return context;
        }


        public void setData(List<Music> data) {
            this.data = data;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public MusicAdapter(Context context){
            this.context = context;
        }


        @Override
        public int getCount() {
            if(data!=null)
               return data.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }
        private class ViewHolder{
            TextView title;
            TextView time;
            Button tryListen;
            Button download;
            HorizontalScrollView scrollView;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if(convertView==null){
                vh = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.musicitem,null,true);
                vh.title = (TextView) convertView.findViewById(R.id.textView);
                vh.time = (TextView) convertView.findViewById(R.id.textView2);
                vh.tryListen = (Button) convertView.findViewById(R.id.listen);
                vh.download = (Button) convertView.findViewById(R.id.download);
                vh.scrollView = (HorizontalScrollView) convertView.findViewById(R.id.horizontalScrollView);
                convertView.setTag(vh);
            }else{
              vh = (ViewHolder) convertView.getTag();
            }
            vh.scrollView.scrollTo(0,0);
            vh.title.setSelected(true);
            vh.title.setText(data.get(position).getName());
            vh.time.setText(data.get(position).getMusicTime());
            vh.tryListen.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    getProgressDialog().setMessage(getString(R.string.data_loading));
                    getProgressDialog().show();
                    mYouMakerPaser.findMP3(data.get(position),position,
                            new YouMakerPaser.MusicCallBack(){
                                @Override
                                public void onSuccess(int position,String url) {
                                    data.get(position).setMp3Url(url);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(url), "audio/*");
                                    context.startActivity(intent);
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getProgressDialog().cancel();
                                        }
                                    });

                                }

                                @Override
                                public void onFiled(final String msg) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getProgressDialog().cancel();
                                            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });
                }
            });
            vh.download.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    getProgressDialog().setMessage(getString(R.string.data_loading));
                    getProgressDialog().show();
                    mYouMakerPaser.findMP3(data.get(position),position,
                            new YouMakerPaser.MusicCallBack()
                            {
                                @Override
                                public void onSuccess(final int position,final String url) {
                                    data.get(position).setMp3Url(url);
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getProgressDialog().cancel();
                                            showDownloadDialog(data.get(position), url);
                                        }
                                    });

                                }

                                @Override
                                public void onFiled(final String msg) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getProgressDialog().cancel();
                                            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });

                }
            });
            return convertView;
        }
    }
   public void showDownloadDialog(final Music music, final String url){
       String fileName = music.getName().replaceAll(File.separatorChar+"","_")+".mp3";
       final DownloadDialog downloaddialog =   new  DownloadDialog(MainActivity.this, fileName);
       downloaddialog.setNegativeButton(R.string.alert_cancel, null)
               .setPositiveButton(R.string.download, new DialogInterface.OnClickListener(){
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       if(downloaddialog.getCurrentFileName().length()>0
                               &&downloaddialog.getCurrentFileName().endsWith(".mp3")) {
                           dialog.cancel();
                           String path = downloaddialog.getCurrentFilePath();
                           new DownloadTask(MainActivity.this)
                                   .execute(
                                           url,
                                           path ,
                                           downloaddialog.getCurrentFileName());
                       }else{
                            Toast.makeText(MainActivity.this,"請輸入正確檔案名稱",Toast.LENGTH_SHORT).show();
                       }

                   }
               });
       downloaddialog.create().show();
   }
   public void download(String dirctory,String fileName ,String url){
       DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

       DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
       request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,fileName);

       request.allowScanningByMediaScanner();
       request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
       request.setAllowedOverRoaming(false);
       request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

       long downloadId = downloadManager.enqueue(request);
   }
}
