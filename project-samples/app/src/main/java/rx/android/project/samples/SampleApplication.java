package rx.android.project.samples;

import android.app.Application;

import rx.resumable.ObservableVault;

public class SampleApplication extends Application {

    private ObservableVault vault;

    @Override
    public void onCreate() {
        super.onCreate();
        vault = new ObservableVault();
    }

    public ObservableVault getObservableVault() {
        return vault;
    }
}
