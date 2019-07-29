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
            Transaction transaction = dgraphClient.newTransaction();
            //Transaction transaction = getGraphClient().newTransaction();

            dateDao.createDateNode(transaction);
        }
    }

    /*public static DgraphClient getGraphClient() {
        return  DgraphConfig.getInstance().getGraph();
    }*/

    public String makeCurrentDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
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

    public Map<DateUnit, Integer> getCurrentDate() {
        Map<DateUnit, Integer> dateMap = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        dateMap.put(DateUnit.YEAR, calendar.get(Calendar.YEAR));
        dateMap.put(DateUnit.MONTH, calendar.get(Calendar.MONTH) + 1);
        dateMap.put(DateUnit.DAY, calendar.get(Calendar.DAY_OF_MONTH));
        dateMap.put(DateUnit.HOUR, calendar.get(Calendar.HOUR_OF_DAY));

        return dateMap;
    }

    public Map<DateUnit, Integer> getFcstDate(String fcstDate, String fcstTime) {
        Map<DateUnit, Integer> dateMap = new HashMap<>();

        dateMap.put(DateUnit.YEAR, Integer.parseInt(fcstDate.substring(0, 4)));
        dateMap.put(DateUnit.MONTH, Integer.parseInt(fcstDate.substring(4, 6)));
        dateMap.put(DateUnit.DAY, Integer.parseInt(fcstDate.substring(6, 8)));
        dateMap.put(DateUnit.HOUR, Integer.parseInt(fcstTime.substring(0, 2)));

        return dateMap;
    }

    public Map<DateUnit, Integer> getTmDate(String tm) {
        Map<DateUnit, Integer> dateMap = new HashMap<>();

        dateMap.put(DateUnit.YEAR, Integer.parseInt(tm.substring(0, 4)));
        dateMap.put(DateUnit.MONTH, Integer.parseInt(tm.substring(5, 7)));
        dateMap.put(DateUnit.DAY, Integer.parseInt(tm.substring(8, 10)));

        return dateMap;
    }
}
