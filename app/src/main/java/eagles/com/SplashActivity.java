package eagles.com;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.overridePendingTransition(0, 0);
        SplashActivity.this.finish();
    }

    @Override
    public void onBackPressed() {

    }
}