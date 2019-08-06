package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import com.example.realpilot.utilAndConfig.DateUnit;
import com.example.realpilot.utilAndConfig.ExternalWeatherApi;
import io.dgraph.DgraphClient;
import io.dgraph.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class DateService {
    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DateDao dateDao;

    public void addDateNode() {
        int dateNodeCount = dateDao.getDateNodeCount(dgraphClient);

        if(dateNodeCount == 0) {
            // TODO: 삽입할 때 이미 존재하는 노드인지 확인하는 로직 추가?
            // TODO: 날짜 노드를 더 추가해야되는 시점?
            dateDao.createDateNode();
        }
    }

    public String makeCurrentDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(new Date());

        return date;
    }

    public String makeCurrentDateFormatWithBar() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());

        return date;
    }

    public String makeCurrentTimeFormat(ExternalWeatherApi api) {
        String hourString = makeCurrentHourFormat(api);
        String minuteString = makeCurrentMinuteFormat(api);

       return hourString + minuteString;
    }

    public String makeCurrentHourFormat(ExternalWeatherApi api) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if(api.equals(ExternalWeatherApi.FORECAST_SPACE)) {
            if(hour >= 2 && hour < 5) {
                hour = 2;
            } else if(hour >= 5 && hour < 8) {
                hour = 5;
            } else if(hour >= 8 && hour < 11) {
                hour = 8;
            } else if(hour >= 11 && hour < 14) {
                hour = 11;
            } else if(hour >= 14 && hour < 17) {
                hour = 14;
            } else if(hour >= 17 && hour < 20) {
                hour = 17;
            } else if(hour >= 20 && hour < 23) {
                hour = 20;
            } else if(hour >= 23 && hour < 2) {
                hour = 23;
            }
        }

        return convertToDoubleDigit(hour);
    }

    public String makeCurrentMinuteFormat(ExternalWeatherApi api) {
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);

        return convertToDoubleDigit(minute);
    }

    public String convertToDoubleDigit(int number) {
        String numberString;
        if(number < 10) {
            numberString = "0" + number;
        } else {
            numberString = String.valueOf(number);
        }

        return numberString;
    }

    // TODO: 시간 얻는 메서드는 Service에서만 사용하도록 수정
    public Map<DateUnit, Integer> getCurrentDate() {
        Map<DateUnit, Integer> dateMap = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        dateMap.put(DateUnit.YEAR, calendar.get(Calendar.YEAR));
        dateMap.put(DateUnit.MONTH, calendar.get(Calendar.MONTH) + 1);
        dateMap.put(DateUnit.DAY, calendar.get(Calendar.DAY_OF_MONTH));
        dateMap.put(DateUnit.HOUR, calendar.get(Calendar.HOUR_OF_DAY));

        return dateMap;
    }

    public Map<DateUnit, Integer> splitDateAndTime(String date, String time) {
        Map<DateUnit, Integer> dateMap = new HashMap<>();

        dateMap.put(DateUnit.YEAR, Integer.parseInt(date.substring(0, 4)));
        dateMap.put(DateUnit.MONTH, Integer.parseInt(date.substring(4, 6)));
        dateMap.put(DateUnit.DAY, Integer.parseInt(date.substring(6, 8)));
        dateMap.put(DateUnit.HOUR, Integer.parseInt(time.substring(0, 2)));

        return dateMap;
    }

    public Map<DateUnit, Integer> splitDateIncludingDelim(String date) {
        Map<DateUnit, Integer> dateMap = new HashMap<>();

        dateMap.put(DateUnit.YEAR, Integer.parseInt(date.substring(0, 4)));
        dateMap.put(DateUnit.MONTH, Integer.parseInt(date.substring(5, 7)));
        dateMap.put(DateUnit.DAY, Integer.parseInt(date.substring(8, 10)));

        return dateMap;
    }
}
