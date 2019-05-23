package com.example.realpilot.service;

import com.example.realpilot.model.Region;
import com.example.realpilot.repository.RegionRepository;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

@Service
public class RegionService {
    private static final Logger log = LoggerFactory.getLogger(RegionService.class);

    @Autowired
    protected RegionRepository regionRepo;
    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    public RegionService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @PostConstruct
    public void createRegionNodes() {
        log.info("createRegionNode 로그  - 진입");

        Long countOfRegionNodes = regionRepo.findCountOfRegionNodes();
        log.info("createRegionNode 로그  - 지역데이터 노드 수 : " + countOfRegionNodes);

        if(countOfRegionNodes == 0) {
            Collection<Object> data = regionRepo.saveAllExcelData();
            log.info("createRegionNode 로그  - 데이터 : " + data);
        }
    }

    public Region createNodeTest(Long hCode, String siDo, String siGunGu, String eubMyeonDong, Integer createdDate) {
        log.info("create 로그  - 진입");

        Region region = Region.builder()
                .hCode(hCode)
                .siDo(siDo)
                .siGunGu(siGunGu)
                .eubMyeonDong(eubMyeonDong)
                .createdDate(createdDate)
                .build();

        Session session = sessionFactory.openSession();

        session.save(region);

        return region;
    }


}
