package core.domain.settlement.batch;


import core.domain.settlement.entity.Settlement;
import core.global.jdbc.SettlementJdbcSqlSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JDBC 기반 대용량 정산 Writer
 *
 * JPA saveAll() 대신 TEMP 테이블 + batchUpdate + MERGE(upsert) 패턴 적용
 * - JPA saveAll: N번 INSERT (JPA 오버헤드 포함)
 * - 이 방식: 1번 batchUpdate → 1번 MERGE → 완료
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementWriter implements ItemWriter<Settlement> {

    private final JdbcTemplate jdbcTemplate;

     /**
     * JDBC 기반 대용량 정산 Writer
     *
     * JPA saveAll() 대신 TEMP 테이블 + batchUpdate + MERGE(upsert) 패턴 적용
     * - JPA saveAll: N번 INSERT (JPA 오버헤드 포함)
     * - 이 방식: 1번 batchUpdate → 1번 MERGE → 완료
     **/
    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        List<? extends Settlement> items = chunk.getItems();
        if (items.isEmpty()) return;

        long start = System.currentTimeMillis();

        // 1. TEMP 테이블 생성 (트랜잭션 종료 시 자동 삭제)
        jdbcTemplate.execute(SettlementJdbcSqlSpec.CREATE_TEMP_TABLE.getSql());

        // 2. TEMP 테이블에 bulk INSERT
        bulkInsertToTemp(items);

        // 3. MERGE: TEMP → settlements (upsert)
        int[] result = mergeToMain();

        log.info("[정산 배치] JDBC merge 완료 - inserted={}, updated={}, elapsed={}ms",
                result[0], result[1], System.currentTimeMillis() - start);
    }

    private void bulkInsertToTemp(List<? extends Settlement> items) {
        jdbcTemplate.batchUpdate(
                SettlementJdbcSqlSpec.BULK_INSERT_TEMP.getSql(),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Settlement s = items.get(i);
                        ps.setLong(1, s.getMember().getId());
                        ps.setLong(2, s.getTotalAmount());
                        ps.setLong(3, s.getFeeAmount());
                        ps.setLong(4, s.getSettledAmount());
                        ps.setDate(5, java.sql.Date.valueOf(s.getSettledDate()));
                        ps.setString(6, s.getStatus().name());
                        ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                }
        );
        log.info("[정산 배치] TEMP 테이블 bulk insert 완료 - {}건", items.size());
    }

    private int[] mergeToMain() {
        return jdbcTemplate.query(
                SettlementJdbcSqlSpec.MERGE_TEMP_TO_MAIN.getSql(),
                rs -> {
                    if (rs.next()) {
                        return new int[]{rs.getInt("inserted_count"), rs.getInt("updated_count")};
                    }
                    return new int[]{0, 0};
                }
        );
    }
}
