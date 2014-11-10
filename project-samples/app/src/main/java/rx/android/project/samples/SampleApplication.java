package rx.android.project.samples;

import android.app.Application;

import rx.resumable.ObservableVault;

/**
 * The observerVault being the holder for the Observables over rotation it can live in the application context.
 */
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
