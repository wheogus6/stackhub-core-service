package core.controller;


import core.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchController {

    @Autowired
    FileService fileService;

    //백업 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void performBatchJob() {
        fileService.backUpImg();
    }


}
