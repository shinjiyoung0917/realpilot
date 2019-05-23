package com.example.realpilot.controller;

import com.example.realpilot.service.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegionController {
    private static final Logger log = LoggerFactory.getLogger(RegionController.class);

    @Autowired
    private RegionService regionService;

    @PostMapping(value = "/csv/region")
    public void loadCSVFilesTest() {
        log.info("loadCSVFilesTest 로그  - 진입");

        regionService.createRegionNodes();
        /*
        return ResponseEntity
                .ok()
                .body(data);
        */
    }

    @PostMapping(value = "/region/{hCode}/{siDo}/{siGunGu}/{eubMyeonDong}/{createdDate}")
    public void testInsert(@PathVariable("hCode") Long hCode, @PathVariable("siDo") String siDo, @PathVariable("siGunGu") String siGunGu,
                           @PathVariable("eubMyeonDong") String eubMyeonDong, @PathVariable("createdDate") Integer createdDate) {
        log.info("testInsert 로그  - 진입");

        regionService.createNodeTest(hCode, siDo, siGunGu, eubMyeonDong, createdDate);
    }
}
