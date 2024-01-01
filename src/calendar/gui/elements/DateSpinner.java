package calendar.gui.elements;

import java.util.Calendar;
import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import calendar.gui.resources.font.CustomFont;

public class DateSpinner extends JSpinner {
    private Calendar calendar;

    public DateSpinner() {
        this(System.currentTimeMillis());
    }

    public DateSpinner(long datetime) {
        Date startDate = new Date(datetime);

        calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        super.setModel(new SpinnerDateModel(calendar.getTime(), null, null, Calendar.DAY_OF_MONTH));

        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(this, "yyyy년 MM월 dd일");
        this.setEditor(startDateEditor);

        setFont(CustomFont.DEFAULT_FONT);
    }

    public Date getDate() {
        return (Date) getValue();
    }
}
