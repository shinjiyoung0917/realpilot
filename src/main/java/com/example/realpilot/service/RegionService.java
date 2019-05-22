package com.example.realpilot.service;

import com.example.realpilot.model.Region;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegionService {
    private static final Logger log = LoggerFactory.getLogger(RegionService.class);

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    public RegionService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Region create(Long hCode, String siDo, String siGunGu, String eubMyeonDong, Integer createdDate) {
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
