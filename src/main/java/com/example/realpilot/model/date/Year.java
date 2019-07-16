package com.example.realpilot.model.date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Year {
    private String uid;

    private Integer year;

    private List<Month> months; //= new ArrayList<>();

    public void setDate(int currentYear, List<Month> monthList) {
        this.year = currentYear;
        this.months = monthList;
    }


    /*private static final Logger log = LoggerFactory.getLogger(Year.class);

    private static final int TOTAL_TIME_OF_DAY = 24;
    private static final int TOTAL_MONTHS_OF_YEAR = 12;

    public Year setDate() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(calendar.YEAR);

        this.year = currentYear;

        List<Hour> hourList = new ArrayList<>();
        Hour hour;
        for(int h = 0 ; h < TOTAL_TIME_OF_DAY ; ++h) {
            hour = new Hour();
            hour.setHour(h);
            hourList.add(hour);
        }

        List<Month> monthList = new ArrayList<>();
        Month month;
        Day day;

        for(int m = 1 ; m <= TOTAL_MONTHS_OF_YEAR ; ++m) {
            month = new Month();
            month.setMonth(m);
            monthList.add(month);

            List<Day> dayList = monthList.get(m-1).getDays();

            calendar.set(this.year, month.getMonth() - 1, 1);
            int daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            log.info("[Model] Year - " + month.getMonth() + "월의 일 수 : " + daysOfMonth);

            for (int d = 1; d <= daysOfMonth; ++d) {
                day = new Day();
                day.setDay(d);
                day.setHours(hourList);
                dayList.add(day);
            }
            monthList.get(m-1).setDays(dayList);
        }
        this.months = monthList;

        return this;
    }*/
}
