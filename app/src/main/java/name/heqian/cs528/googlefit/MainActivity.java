package name.heqian.cs528.googlefit;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import static com.google.android.gms.location.DetectedActivity.*;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient mApiClient;
    private BroadcastReceiver mMessageReceiver;
    private MediaPlayer mediaPlayer;
    private TextView textView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageActivityView);
        mediaPlayer = MediaPlayer.create(this, R.raw.beat_02);
//        mediaPlayer.prepareAsync();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                int activityType = intent.getIntExtra("activityType", -1);
                Log.d("receiver", "Got message: " + activityType);
                if (activityType == RUNNING || activityType == WALKING) {
//                if (activityType == STILL) {
                    Log.d("receiver", "play song");
                    mediaPlayer.start(); // no need to call prepare(); create() does that for you
                }
                else {
                    Log.d("receiver", "pause song");
                    mediaPlayer.pause();
                }
                switch( activityType ) {
                    case IN_VEHICLE: {
                        Log.d( "receiver", "In Vehicle: show picture" );
                        textView.setText("In Vehicle");
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.in_vehicle, getTheme()));
                        break;
                    }
                    case ON_BICYCLE: {
                        Log.e( "receiver", "On Bicycle");
                        break;
                    }
                    case DetectedActivity.ON_FOOT: {
                        Log.e( "receiver", "On Foot");
                        break;
                    }
                    case RUNNING: {
                        Log.d( "receiver", "Running: show picture" );
                        textView.setText("Running");
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.running, getTheme()));
                        break;
                    }
                    case STILL: {
                        Log.d( "receiver", "Still: show picture" );
                        textView.setText("Still");
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.still, getTheme()));
                        break;
                    }
                    case DetectedActivity.TILTING: {
                        Log.e( "receiver", "Tilting: ");
                        textView.setText("Tilting");
                        break;
                    }
                    case WALKING: {
                        Log.d( "receiver", "Walking: show picture" );
                        textView.setText("Walking");
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.walking, getTheme()));
                        break;
                    }
                    case UNKNOWN: {
                        Log.e( "ActivityRecogition", "Unknown: "); //  + activity.getConfidence()
                        break;
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("ActivityTypeUpdates"));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 1000, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
