package com.example.realpilot.dgraph;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.DgraphProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DgraphConfig {
    private static final Logger log = LoggerFactory.getLogger(DgraphConfig.class);

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9080;

    private static DgraphGrpc.DgraphStub stub;

    @Bean
    public DgraphClient DgraphConnection() {
        try {
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
            createSchema(dgraphClient);

            return dgraphClient;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public void ReleaseDgraphConnection() {

    }

    public void createSchema(DgraphClient dgraphClient) {
        // TODO: Dgraph 새로 사용될 떄 이전에 비정상적으로 종료됐는지 확인해야하는지?

        // ** DB ALTER ** //
        // TODO: 필요할 때만 사용 (ex. 잘못된 데이터 삽입했을 경우)
        //dgraphClient.alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());

        String schema = "year: int @index(int) .\n" +
                "month: int @index(int) .\n" +
                "day: int @index(int) .\n" +
                "hour: int @index(int) .\n" +
                "sidoName: string @index(fulltext, trigram) .\n" +
                "sggName: string @index(fulltext, trigram) .\n" +
                "umdName: string @index(fulltext, trigram) .\n" +
                "hCode: int @index(int) .\n" +
                "createdDate: int @index(int) .\n" +
                "gridX: int @index(int) .\n" +
                "gridY: int @index(int) .\n" +
                "tmX: float @index(float) .\n" +
                "tmY: float @index(float) .\n" +
                "measureStationName: string @index(fulltext) .\n" +
                "baseDate: string @index(fulltext) .\n" +
                "baseTime: string @index(fulltext) .\n" +
                "fcstDate: string @index(fulltext) .\n" +
                "fcstTime: string @index(fulltext) .\n" +
                "tm: string @index(fulltext) .\n" +
                "date: string @index(fulltext) .\n" +
                "time: string @index(fulltext) .\n" +
                "hourlyWeathers: uid @reverse .\n" +
                "dailyWeathers: uid @reverse .\n" +
                "amWeathers: uid @reverse .\n" +
                "pmWeathers: uid @reverse .\n" +
                "airPollutionDetails: uid @reverse .\n";

        DgraphProto.Operation op = DgraphProto.Operation.newBuilder().setSchema(schema).build();
        dgraphClient.alter(op);

        log.info("[Dgraph] createSchema - DGraph 스키마 세팅 완료");
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