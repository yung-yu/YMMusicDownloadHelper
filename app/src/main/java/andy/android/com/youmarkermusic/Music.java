package andy.android.com.youmarkermusic;

/**
 * Created by andy on 2015/2/28.
 */
public  class Music{
    String name;
    String url;
    String musicTime;
    String mp3Url;
    String filePath;
    String keyWorld;
    public Music (){

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMusicTime() {
        return musicTime;
    }

    public void setMusicTime(String musicTime) {
        this.musicTime = musicTime;
    }

    public String getMp3Url() {
        return mp3Url;
    }

    public void setMp3Url(String mp3Url) {
        this.mp3Url = mp3Url;
    }
}