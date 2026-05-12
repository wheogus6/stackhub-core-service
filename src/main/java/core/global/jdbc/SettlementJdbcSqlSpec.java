package core.global.jdbc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정산 대용량 처리용 JDBC SQL 스펙
 *
 * 흐름: TEMP 테이블 생성 → batchUpdate bulk INSERT → MERGE (upsert)
 */
@Getter
@RequiredArgsConstructor
public enum SettlementJdbcSqlSpec {

    CREATE_TEMP_TABLE(
            "CREATE TEMP TABLE IF NOT EXISTS temp_settlements (" +
            "  member_id       BIGINT       NOT NULL, " +
            "  total_amount    BIGINT       NOT NULL, " +
            "  fee_amount      BIGINT       NOT NULL, " +
            "  settled_amount  BIGINT       NOT NULL, " +
            "  settled_date    DATE         NOT NULL, " +
            "  status          VARCHAR(20)  NOT NULL, " +
            "  created_at      TIMESTAMP    NOT NULL, " +
            "  PRIMARY KEY (member_id, settled_date) " +
            ") ON COMMIT DELETE ROWS"   // 트랜잭션 끝나면 자동 삭제
    ),

    BULK_INSERT_TEMP(
            "INSERT INTO temp_settlements " +
            "  (member_id, total_amount, fee_amount, settled_amount, settled_date, status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)"
    ),

    MERGE_TEMP_TO_MAIN(
            "WITH upsert AS ( " +
            "  INSERT INTO settlements " +
            "    (member_id, total_amount, fee_amount, settled_amount, settled_date, status, created_at, updated_at) " +
            "  SELECT " +
            "    member_id, total_amount, fee_amount, settled_amount, settled_date, status, created_at, NOW() " +
            "  FROM temp_settlements " +
            "  ON CONFLICT (member_id, settled_date) DO UPDATE SET " +
            "    total_amount   = EXCLUDED.total_amount, " +
            "    fee_amount     = EXCLUDED.fee_amount, " +
            "    settled_amount = EXCLUDED.settled_amount, " +
            "    status         = EXCLUDED.status, " +
            "    updated_at     = NOW() " +
            "  RETURNING xmax = 0 AS inserted " +
            ") " +
            "SELECT " +
            "  COUNT(*) FILTER (WHERE inserted)     AS inserted_count, " +
            "  COUNT(*) FILTER (WHERE NOT inserted) AS updated_count " +
            "FROM upsert"
    );

    private final String sql;
}
