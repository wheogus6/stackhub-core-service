package core.config;


import core.batch.SettlementItem;
import core.batch.SettlementProcessor;
import core.batch.SettlementReader;
import core.batch.SettlementWriter;
import core.entity.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SettlementReader reader;
    private final SettlementProcessor processor;
    private final SettlementWriter writer;

    @Bean
    public Job settlementJob() {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep())
                .build();
    }

    @Bean
    public Step settlementStep() {
        return new StepBuilder("settlementStep", jobRepository)
                .<SettlementItem, Settlement>chunk(100, transactionManager) // 100건씩 처리
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)   // 개별 실패 건은 skip
                .skipLimit(10)           // 10건 초과 실패 시 Job 중단
                .build();
    }
}
