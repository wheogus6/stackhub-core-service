package core.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 매일 새벽 2시에 전날 정산 배치 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final Job settlementJob;
    private final SettlementReader reader;

    @Scheduled(cron = "0 0 2 * * *") // 매일 02:00
    public void runSettlement() {
        try {
            reader.reset(); // Reader 초기화 (재실행 대비)

            JobParameters params = new JobParametersBuilder()
                    .addString("settledDate", LocalDate.now().minusDays(1).toString())
                    .addLong("timestamp", System.currentTimeMillis()) // 매번 다른 파라미터로 중복 실행 방지
                    .toJobParameters();

            log.info("[정산 배치] 시작 - 기준일: {}", LocalDate.now().minusDays(1));
            jobLauncher.run(settlementJob, params);
            log.info("[정산 배치] 완료");

        } catch (Exception e) {
            log.error("[정산 배치] 실패: {}", e.getMessage(), e);
        }
    }
}
