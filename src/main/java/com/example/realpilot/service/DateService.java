package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import io.dgraph.DgraphClient;
import io.dgraph.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DateService {
    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DateDao dateDao;

    @PostConstruct
    public void getDateNodeCountService() {
        int dateNodeCount = dateDao.getDateNodeCountDao(dgraphClient);

        if(dateNodeCount == 0) {
            addDateNode();
        }
    }

    public void addDateNode() {
        Transaction transaction = dgraphClient.newTransaction();
        //Transaction transaction = getGraphClient().newTransaction();

        dateDao.createDateNode(transaction);
    }

    /*public static DgraphClient getGraphClient() {
        return  DgraphConfig.getInstance().getGraph();
    }*/

}
