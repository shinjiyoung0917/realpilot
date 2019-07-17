package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import com.example.realpilot.utilAndConfig.ExternalWeatherApi;
import io.dgraph.DgraphClient;
import io.dgraph.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    public String makeBaseDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(new Date());

        return date;
    }

    public String makeBaseTimeFormat(ExternalWeatherApi api) {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String hourString = "";
        String minuteString = "";

        if(hour < 10) {
            hourString = "0" + hour;
        } else {
            hourString = String.valueOf(hour);
        }

        if(minute < 10) {
            minuteString = "0" + minute;
        } else {
            minuteString = String.valueOf(minute);
        }

        /*if(api.equals(ExternalWeatherApi.FORECAST_GRIB)) {
            if (minute >= 0 && minute <= 59) {
                minuteString = "00";
            }
        } else if(api.equals(ExternalWeatherApi.FORECAST_TIME)) {
            if (minute >= 0 && minute < 30) {
                minuteString = "00";
            } else {
                minuteString = "30";
            }
        }*/

        // 동네예보는 지정돼있는 base_time 내에서 분을 제외한 시만 맞으면 호출됨
        // ex) 현재 시각 11:20일 경우 0830, 1105 등은 호출되고, 1000, 1030 등은 호출 안됨
        // 대신 API 제공 시간(base_time + 10분) 이후에만 호출 가능

       return hourString + minuteString;
    }
}
