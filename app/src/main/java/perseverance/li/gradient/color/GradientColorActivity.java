package perseverance.li.gradient.color;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import perseverance.li.gradient.color.view.GradientView;

public class GradientColorActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "GradientColorActivity";
    private static final int LEVEL_WHAT = 1;
    private static final int GRADIENT_MAX_VALUE = 10;
    private GradientView mGradientView;
    private Random mRandom = new Random();
    private Button mStartButton;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gradient_color);
        initView();
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case LEVEL_WHAT:
                int level = msg.arg1;
                mGradientView.startGradientAnim(level);
                mStartButton.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onMenuHome() {
    }

    private void initView() {
        mGradientView = (GradientView) findViewById(R.id.gradient_view);
        mStartButton = (Button) findViewById(R.id.start);
        mStartButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStartButton.setVisibility(View.VISIBLE);
    }

    private void startAnimTask() {
        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int level = 0;
                int mCurrentGradient = mRandom.nextInt(GRADIENT_MAX_VALUE);
                Log.d(TAG, "timer task mCurrentGradient: " + mCurrentGradient);
                if (mCurrentGradient >= 0 && mCurrentGradient <= 2) {
                    level = 3;
                } else if (mCurrentGradient > 2 && mCurrentGradient <= 5) {
                    level = 2;
                } else if (mCurrentGradient > 5 && mCurrentGradient <= 8) {
                    level = 1;
                } else if (mCurrentGradient > 8 && mCurrentGradient <= 10) {
                    level = 0;
                }
                startAnim(level);
            }
        };
        mTimer.schedule(timerTask, 0, 4000);
    }

    private void startAnim(int level) {
        Message msg = Message.obtain();
        msg.what = LEVEL_WHAT;
        msg.arg1 = level;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.start) {
            startAnimTask();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
