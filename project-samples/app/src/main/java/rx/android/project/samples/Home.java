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

public class Home extends Activity implements ResumableReference, ObserverFactory, ActivityStarter {

    private ResumableSubscriber resumableSubscriber;
    private ReactiveNavigator reactiveNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        resumableSubscriber = new ResumableSubscriber(this, this, getSampleApplication().getObservableVault());
        reactiveNavigator = new ReactiveNavigator(this);
        setupViews();
    }

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
        findViewById(R.id.pick_date_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable<Date> dateObservable = new ReactiveDatePicker().show(getFragmentManager());
                resumableSubscriber.subscribe(dateObservable, new DateObserver());
            }
        });
        findViewById(R.id.pick_file_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable<Uri> fileObservable = reactiveNavigator.toActivityForResult(createFileChoserIntent(), R.id.file_request)
                        .map(new Func1<Intent, Uri>() {
                            @Override
                            public Uri call(Intent intent) {
                                return intent.getData();
                            }
                        });
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

    @Override
    public ResumableObserver createObserver(int code) {
        switch (code) {
            case R.id.date_observer:
                return new DateObserver();
            default:
                throw new IllegalArgumentException("No case defined for observer id " + code);
        }
    }

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
