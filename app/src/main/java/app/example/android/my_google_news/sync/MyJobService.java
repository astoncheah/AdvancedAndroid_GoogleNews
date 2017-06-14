package app.example.android.my_google_news.sync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by cheah on 8/6/17.
 */
public class MyJobService extends JobService{
    @Override
    public boolean onStartJob(JobParameters job){
        Log.e("MyJobService", "onStartJob");
        downloadData(getApplicationContext());
        return false;
    }
    @Override
    public boolean onStopJob(JobParameters job){
        return false;
    }
    public static void downloadData(Context context){
        Intent i = new Intent(context, UpdaterService.class);
        context.startService(i);
    }

    private static final String SYNC_JOB_TAG = "my-unique-tag";
    public static synchronized void initialize(final Context context) {
        schedulePeriodic(context);
    }
    private static synchronized void schedulePeriodic(Context context) {
        final int ONE_HOUR = 60*60;

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job myJob = dispatcher.newJobBuilder()
            .setService(MyJobService.class)
            .setTag(SYNC_JOB_TAG)
            .setRecurring(true)
            .setLifetime(Lifetime.FOREVER)
            .setTrigger(Trigger.executionWindow(ONE_HOUR,(ONE_HOUR+60)))//60*60 sync every one hour
            .setReplaceCurrent(true)
            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
            .setConstraints(
                Constraint.ON_ANY_NETWORK
                //Constraint.DEVICE_CHARGING
            )
            .build();
        dispatcher.mustSchedule(myJob);
    }
}
