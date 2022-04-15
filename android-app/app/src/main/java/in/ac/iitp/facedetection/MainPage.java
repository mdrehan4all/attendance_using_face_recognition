package in.ac.iitp.facedetection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainPage extends AppCompatActivity implements AdvancedWebView.Listener{
    AdvancedWebView mWebView;
    ProgressBar pbbrowser;
    SQLiteDatabase db;
    String ipaddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mWebView = (AdvancedWebView) findViewById(R.id.webView);
        pbbrowser=(ProgressBar)findViewById(R.id.pbbrowser);
        pbbrowser.setVisibility(View.INVISIBLE);

        //SERVER URL
        db=openOrCreateDatabase("db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS userconfig (id TEXT PRIMARY KEY, userid TEXT, password TEXT, ipaddress TEXT);");

        Cursor c=db.rawQuery("SELECT ipaddress FROM userconfig",null);
        while(c.moveToNext()){
            ipaddress=c.getString(0);
        }

        mWebView.setListener(this, this);
        mWebView.setGeolocationEnabled(false);
        mWebView.setMixedContentAllowed(true);
        mWebView.setCookiesEnabled(true);
        mWebView.setThirdPartyCookiesEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //Toast.makeText(Browser.this, "Finished loading", Toast.LENGTH_SHORT).show();
            }
        });
        mWebView.addHttpHeader("X-Requested-With", "");
        mWebView.loadUrl(""+ipaddress);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            //getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }
    @Override
    public void onBackPressed() {
        //Toast.makeText(MainPage.this,"Double Click for 1st Page",Toast.LENGTH_SHORT).show();
        mWebView.goBack();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        pbbrowser.setVisibility(View.VISIBLE);
    }
    @Override
    public void onPageFinished(String url) {
        pbbrowser.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        Toast.makeText(MainPage.this, "onPageError(errorCode = "+errorCode+",  description = "+description+",  failingUrl = "+failingUrl+")", Toast.LENGTH_SHORT).show();
        pbbrowser.setVisibility(View.INVISIBLE);
        String mHTML = "<html><body><div style=\"text-align:center\"><h1>Error</h1><hr/><small>Error : Check Your Internet Connection</small> <a href=\"\">Refresh</a></div></body></html>";
        mWebView.loadData(mHTML, "text/html", null);
    }

    @Override
    public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        Toast.makeText(MainPage.this, "onDownloadRequested(url = "+url+",  userAgent = "+userAgent+",  contentDisposition = "+contentDisposition+",  mimetype = "+mimetype+",  contentLength = "+contentLength+")", Toast.LENGTH_LONG).show();

		/*final String filename = UUID.randomUUID().toString();

		if (AdvancedWebView.handleDownload(this, url, filename)) {
			// download successfully handled
		}
		else {
			// download couldn't be handled because user has disabled download manager app on the device
		}*/
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onExternalPageRequest(String url) {
        Toast.makeText(MainPage.this, "onExternalPageRequest(url = "+url+")", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(Intent.ACTION_CHOOSER);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
