package com.example.realpilot.dgraph;

import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.DgraphProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DgraphConfig implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(DgraphConfig.class);

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9080;
   /* @Autowired
    private ManagedChannel channel;

    @Bean
    public ManagedChannel managedChannel() {
        // ** 한 커넥션을 사용해 동기 클라이언트 생성 ** //
        // Channel의 라이프사이클을 관리하도록 gRPC 포트번호로 ManagedChannel 사용
        channel = ManagedChannelBuilder
                .forAddress(HOSTNAME, PORT)
                .usePlaintext()
                .maxInboundMessageSize(9999999)
                .build();

        return channel;
    }
*/
    @Bean
    public DgraphClient dgraphClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(HOSTNAME, PORT)
                .usePlaintext()
                .maxInboundMessageSize(9999999)
                .build();

        try {
            // gRPC 동작들을 수행하도록 Stub 사용
            DgraphGrpc.DgraphStub stub = DgraphGrpc.newStub(channel);
            DgraphClient dgraphClient = new DgraphClient(stub);
            log.info("[DgraphConfig] dgraphClient - DgraphClient 객체 생성 완료");
            //createSchema(dgraphClient);

            return dgraphClient;
        } catch (Exception e) {
            e.printStackTrace();
            channel.shutdown();
            throw new IllegalStateException(e);
        }

        //return dgraphClient;
       /* try {
            createSchema(dgraphClient);

            return dgraphClient;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }*/
    }

    @Override
    public void close() {
        //channel.shutdown();
        log.info("[DgraphConfig] close");
    }

    public void createSchema(DgraphClient dgraphClient) {
        // ** DB ALTER ** //
        // TODO: 필요할 때만 사용
        //dgraphClient.alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());

        String schema = "year: int @index(int) .\n" +
                "month: int @index(int) .\n" +
                "day: int @index(int) .\n" +
                "hour: int @index(int) .\n" +
                "sidoName: string @index(fulltext, trigram) .\n" +
                "sggName: string @index(fulltext, trigram) .\n" +
                "umdName: string @index(fulltext, trigram) .\n" +
                "countryName: string @index(fulltext, trigram) .\n" +
                "hCode: int @index(int) .\n" +
                "createdDate: int @index(int) .\n" +
                "gridX: int @index(int) .\n" +
                "gridY: int @index(int) .\n" +
                "tmX: float @index(float) .\n" +
                "tmY: float @index(float) .\n" +
                "measureStationName: string @index(fulltext) .\n" +
                "airPollutionCode: string @index(fulltext) .\n" +
                "releaseDate: string @index(fulltext) .\n" +
                "releaseTime: string @index(fulltext) .\n" +
                "forecastDate: string @index(fulltext) .\n" +
                "forecastTime: string @index(fulltext) .\n" +
                "occurrenceDate: string @index(fulltext) .\n" +
                "occurrenceTime: string @index(fulltext) .\n" +
                "hourlyWeathers: uid @reverse .\n" +
                "dailyWeathers: uid @reverse .\n" +
                "amWeathers: uid @reverse .\n" +
                "pmWeathers: uid @reverse .\n" +
                "airPollutionDetails: uid @reverse .\n" +
                "airPollutionOveralls: uid @reverse .\n" +
                "worldDailyWeathers: uid @reverse .\n" +
                "earthquakes: uid @reverse .\n";

        DgraphProto.Operation op = DgraphProto.Operation.newBuilder().setSchema(schema).build();
        dgraphClient.alter(op);

        log.info("[DgraphConfig] createSchema - DGraph 스키마 세팅 완료");
    }
}