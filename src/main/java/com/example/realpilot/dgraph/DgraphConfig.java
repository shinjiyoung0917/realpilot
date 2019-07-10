package com.example.realpilot.dgraph;

import com.example.realpilot.dao.DateDao;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DgraphConfig {
    private static final Logger log = LoggerFactory.getLogger(DgraphConfig.class);

    @Autowired
    private DateDao dateDao;

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9080;
   /* DgraphClient dgraphClient  = null;
    private static DgraphConfig _instance = null;*/

    @Bean
    public DgraphClient DgraphConnection() {
        // ** 한 커넥션을 사용해 동기 클라이언트 생성 ** //
        // Channel의 라이프사이클을 관리하도록 gRPC 포트번호로 ManagedChannel 사용
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(HOSTNAME, PORT)
                .usePlaintext()
                .maxInboundMessageSize(9999999)
                .build();

        // gRPC 동작들을 수행하도록 Stub 사용
        DgraphGrpc.DgraphStub stub = DgraphGrpc.newStub(channel);

        DgraphClient dgraphClient = new DgraphClient(stub);

        dateDao.createSchema(dgraphClient);

        return dgraphClient;
    }


    /*
    public DgraphConfig() {
        openConnection();
    }

    private void openConnection() {
        if(dgraphClient == null ) {
            synchronized (this) {
                try {
                    ManagedChannel channel =  ManagedChannelBuilder
                            .forAddress(HOSTNAME, PORT)
                            .usePlaintext()
                            .maxInboundMessageSize(9999999)
                            .build();
                    DgraphGrpc.DgraphStub stub = DgraphGrpc.newStub(channel);
                    //DgraphGrpc.DgraphBlockingStub stub = DgraphGrpc.newBlockingStub((channel));
                    dgraphClient = new DgraphClient(stub);


                    dgraphClient.alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());

                    String schema = "year: int @index(int) .\n"
                            + "month: int @index(int) .\n"
                            + "day: int @index(int) .\n"
                            + "hour: int @index(int) .\n";

                    DgraphProto.Operation op = DgraphProto.Operation.newBuilder().setSchema(schema).build();
                    dgraphClient.alter(op);

                    log.info("[Config] DGraph 스키마 세팅 완료");
                } catch(Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    @Bean
    public static DgraphConfig getInstance() {
        if(_instance == null) {
            _instance = new DgraphConfig();
        }
        return _instance;
    }

    @Bean
    public DgraphClient getGraph() {
        if(dgraphClient == null) {
            openConnection();
        }
        return dgraphClient;
    }
    */

}