package rx.android.project.samples;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

import rx.android.observables.ReactiveDialog;

public class ReactiveDatePicker extends ReactiveDialog<Date> {

    private final Calendar calendar = Calendar.getInstance();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(year, monthOfYear, dayOfMonth);
                //When a date is selected we can use the listener (extension of Observer) to send the data of the Dialogs type (here a Date)
                //to the observable. I'm using here onCompleteWith which will call onNext with the value before calling onComplete.
                getListener().onCompleteWith(calendar.getTime());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

}
