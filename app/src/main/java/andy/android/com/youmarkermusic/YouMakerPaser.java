package andy.android.com.youmarkermusic;


import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by andy on 2015/2/27.
 */
public class YouMakerPaser {
     public static final String YOUMAKER_URL = "http://www.youmaker.com/video";

    public  static final String SerachURL = "/search?&sk=";
    public static final String musicParm = "&t=&flvflag=1";
    public static final String PAGE = "&page=";

    public  void query(String key,int page){
       final String url = YOUMAKER_URL+SerachURL+key+musicParm+PAGE+page;
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   HttpParams httpParams = new BasicHttpParams();
                   //設定timeout 30s
                   HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
                   HttpClient client = new DefaultHttpClient();
                   HttpGet get = new HttpGet(url);
                   HttpResponse response = client.execute(get);
                   if(response.getStatusLine().getStatusCode()==200) {
                       Document doc = Jsoup.parse(response.getEntity().getContent(),"utf8",url);

                       List<Music> data = new ArrayList<Music>();

                       Elements list = doc.getElementsByClass("newslistsearchtextrighttitle");
                       Music music;

                       for (Element e : list) {
                           if(e.getElementsByClass("redcolor").get(0).text().equals("(音)")) {
                               music = new Music();
                               Element item_a = e.getElementsByTag("a").get(0);
                               music.setName(item_a.ownText());
                               music.setUrl(YOUMAKER_URL + "/" + item_a.attr("href"));
                               music.setMusicTime(e.getElementsByTag("small").text());
                               data.add(music);
                               Log.d("music", music.getName());
                               Log.d("music", music.getUrl());
                           }
                       }
                       if (onDataUpdateListener != null)
                           onDataUpdateListener.onDataUpdate(data, "update Success");
                   }else{
                       if(onDataUpdateListener!=null)
                           onDataUpdateListener.onDataUpdate(null,response.getStatusLine().getReasonPhrase());
                   }
               } catch (IOException e) {
                   e.printStackTrace();
                   if(onDataUpdateListener!=null)
                       onDataUpdateListener.onDataUpdate(null,e.toString());
               }catch (Exception e){
                   e.printStackTrace();
                   if(onDataUpdateListener!=null)
                       onDataUpdateListener.onDataUpdate(null,e.toString());
               }
           }
       }).start();
    }

    public interface OnDataUpdateListener{
        void onDataUpdate( List<Music> data ,String msg );

    }

    public void setOnDataUpdateListener(OnDataUpdateListener onDataUpdateListener) {
        this.onDataUpdateListener = onDataUpdateListener;
    }

    OnDataUpdateListener onDataUpdateListener;
    public interface  MusicCallBack{
        void onSuccess(int position,String url);
        void onFiled(String msg);
    }
    public void findMP3(final Music music , final int position , final MusicCallBack callBack){
        if(music.getMp3Url()!=null){
            if(callBack!=null)
             callBack.onSuccess(position,music.getMp3Url());
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(music.getUrl());
                    HttpResponse response = client.execute(get);
                    if(response.getStatusLine().getStatusCode()==200) {
                        Document doc = Jsoup.parse(response.getEntity().getContent(),"utf8",music.getUrl());
                        Elements scriptTags = doc.getElementsByTag("script");
                        Pattern p = Pattern.compile("http://[a-zA-Z0-9\\./_/-]+.mp3"); // Regex for the value of the html
                        Matcher m = p.matcher(scriptTags.html()); // you have to use html here and NOT text! Text will drop the 'html' part
                        List<String> urls = new ArrayList<String>();

                        while (m.find()) {
                            String token = m.group();
                            if (token.endsWith(".mp3")) {
                                urls.add(token);
                            }

                        }
                        if (callBack != null) {
                            if (urls.size() > 0) {
                                music.setMp3Url( urls.get(0));
                                callBack.onSuccess(position, urls.get(0));
                            }
                            else {
                                callBack.onFiled("not found mp3!");
                            }
                        }
                    }else{
                        if(callBack!=null){
                            callBack.onFiled(response.getStatusLine().getReasonPhrase());
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    if(callBack!=null){
                        callBack.onFiled(e.toString());
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                    if(callBack!=null){
                        callBack.onFiled(e.toString());
                    }

                }
            }
        }).start();
    }


}
