package calendar.gui.elements;

import calendar.gui.resources.font.CustomFont;

import java.util.Calendar;
import java.util.Date;

import javax.swing.JComboBox;

public class TimeComboBox extends JComboBox<String> {
    public TimeComboBox() {
        this(0);
    }
    public TimeComboBox(int initialIndex) {
        super(generateTimeOptions());
        setFont(CustomFont.DEFAULT_FONT);

        if (initialIndex >= 0 && initialIndex < getItemCount()) {
            setSelectedIndex(initialIndex);
        } else {
            setSelectedIndex(0);
        }
    }

    public long getDatetime(Date date) {
        int selectedIndex = getSelectedIndex();
        return convertToMillis(date, selectedIndex);
    }

    public long getDateStarttime(Date date) {
        // 00:00으로 지정
        return convertToMillis(date, 0);
    }

    public long getDateEndtime(Date date) {
        // 다음날 00:00으로 지정
        Date dayAfter = new Date(date.getTime() + 86400000);    // 1000 * 60 * 60 * 24
        return convertToMillis(dayAfter, 0);
    }

    private static String[] generateTimeOptions() {
        String[] timeOptions = new String[96]; // 총 24시간 * 4개 (15분 간격)
        int index = 0;

        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                String time = String.format("%s %02d:%02d",
                        (hour < 12) ? "오전" : "오후",
                        (hour == 0 || hour == 12) ? 12 : hour % 12, // 12시간제 표시
                        minute);

                timeOptions[index++] = time;
            }
        }

        return timeOptions;
    }

    // convert selectedTime to long type
    private static long convertToMillis(Date date, int selectedIndex) {
        int hour = selectedIndex / 4;
        int minute = (selectedIndex % 4) * 15;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}

