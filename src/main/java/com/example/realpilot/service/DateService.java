package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import io.dgraph.DgraphClient;
import io.dgraph.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DateService {
    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DateDao dateDao;

    // TODO: DgraphClient, Transaction 모두 dao쪽에서 선언하는 것으로 수정
    public void addDateNode() {
        int dateNodeCount = dateDao.getDateNodeCountDao(dgraphClient);

        if(dateNodeCount == 0) {
            Transaction transaction = dgraphClient.newTransaction();
            //Transaction transaction = getGraphClient().newTransaction();

            dateDao.createDateNode(transaction);
        }
    }

    /*public static DgraphClient getGraphClient() {
        return  DgraphConfig.getInstance().getGraph();
    }*/

}
