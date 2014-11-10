package rx.android.project.samples;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import rx.Observable;
import rx.android.exception.CancelledException;
import rx.android.observables.ActivityStarter;
import rx.android.observables.ReactiveNavigator;
import rx.functions.Func1;
import rx.resumable.ObserverFactory;
import rx.resumable.ResumableReference;
import rx.resumable.ResumableSubscriber;
import rx.resumable.observer.ResumableObserver;

/**
 * The activity implements Resumable reference, to be identified using a unique ID,
 * ObserverFactory to provide instances of observers according to their unique Ids and
 * ActivityStarter to allow calls to startActivityForResult.
 */
public class Home extends Activity implements ResumableReference, ObserverFactory, ActivityStarter {

    private ResumableSubscriber resumableSubscriber;
    private ReactiveNavigator reactiveNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //We create the resumableSubscriber using the activity as Reference, Factory, and passing the Vault that lives in the application context.
        resumableSubscriber = new ResumableSubscriber(this, this, getSampleApplication().getObservableVault());
        //We create the Navigator using the activity as ActivityStarter
        reactiveNavigator = new ReactiveNavigator(this);
        setupViews();
    }

    /**
     * onActivityResult delegates the events to the reactiveNavigator
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = reactiveNavigator.onActivityResult(requestCode, resultCode, data);
        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private SampleApplication getSampleApplication() {
        return (SampleApplication) getApplication();
    }

    private void setupViews() {
        findViewById(R.id.pick_date_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We get the Observable of a date using the ReactiveDialog
                Observable<Date> dateObservable = new ReactiveDatePicker().show(getFragmentManager());
                //We use the resumableSubscriber to subscribe the Observer to the dateObservable to allow this observable to be handled over rotation
                resumableSubscriber.subscribe(dateObservable, new DateObserver());
            }
        });
        findViewById(R.id.pick_file_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We use the reactiveNavigator to get an observable of the startActivityForResult call.
                //Since the observable only returns a value if the call was successful we can map a function to extract relevant data from the intent returned
                Observable<Uri> fileObservable = reactiveNavigator.toActivityForResult(createFileChoserIntent(), R.id.file_request)
                        .map(new Func1<Intent, Uri>() {
                            @Override
                            public Uri call(Intent intent) {
                                return intent.getData();
                            }
                        });
                //We use the resumableSubscriber to subscribe the Observer to this observable to allow it to be handled over rotation
                resumableSubscriber.subscribe(fileObservable, new FileObserver());
            }
        });
    }

    private Intent createFileChoserIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return Intent.createChooser(intent, "Pick a File");
    }

    /**
     * Method from the ObserverFactory, this allows the activity to create instances of non static Observers
     * this will be used by the resumableSubscriber to recreate Observers to reattach them to Observables from a previous configuration (eg: over rotation)
     * @param code
     * @return
     */
    @Override
    public ResumableObserver createObserver(int code) {
        switch (code) {
            case R.id.date_observer:
                return new DateObserver();
            default:
                throw new IllegalArgumentException("No case defined for observer id " + code);
        }
    }

    /**
     * Identifies this activity as a unique reference point across multiple instances of this activity.
     * @return
     */
    @Override
    public int getResumableId() {
        return R.id.home_activity;
    }

    private class DateObserver implements ResumableObserver<Date> {

        @Override
        public int getId() {
            return R.id.date_observer;
        }

        @Override
        public void onCompleted() {
            //Nothing to do
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof CancelledException) {
                Toast.makeText(Home.this, "User cancelled the action", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(Home.this, "An error has occurred " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onNext(Date date) {
            Toast.makeText(Home.this, "User picked the date " + SimpleDateFormat.getInstance().format(date), Toast.LENGTH_LONG).show();
        }
    }

    private class FileObserver implements ResumableObserver<Uri> {
        @Override
        public int getId() {
            return 0;
        }

        @Override
        public void onCompleted() {
            //Nothing to do
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof CancelledException) {
                Toast.makeText(Home.this, "User cancelled the action", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(Home.this, "An error has occurred " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onNext(Uri uri) {
            Toast.makeText(Home.this, "User picked the file " + uri.getPath(), Toast.LENGTH_LONG).show();
        }
    }
}
