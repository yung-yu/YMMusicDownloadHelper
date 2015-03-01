package andy.android.com.youmarkermusic;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by andy on 2015/3/1.
 */
public class UpdateListTask extends AsyncTask<String, String, String> {

    Context context;
    public UpdateListTask (Context context){
        this.context = context;
    }
    @Override
    protected String doInBackground(String... params) {
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }
}
