package com.example.realpilot.dgraph;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.dgraph.DgraphProto;
import io.dgraph.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DgraphOperations<T> implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(DgraphOperations.class);

    @Autowired
    private Gson gson = new Gson();

    public void mutate(Transaction transaction, T object) {
        String json = gson.toJson(object);
        DgraphProto.Mutation mu = DgraphProto.Mutation
                .newBuilder()
                .setSetJson(ByteString.copyFromUtf8(json))
                .build();

        try {
            transaction.mutate(mu);
            transaction.commit();
            log.info("[DgraphOperations] mutate - 커밋 완료 => ["+ object.getClass().toString() + "]");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            transaction.discard();
        }
    }

    @Override
    public void close() {
        log.info("[DgraphOperations] close");
    }
}
