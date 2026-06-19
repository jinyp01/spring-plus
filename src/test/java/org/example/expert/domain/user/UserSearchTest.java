package org.example.expert.domain.user;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SplittableRandom;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Tag("bulk")
class UserSearchTest {

    private static final int TOTAL_COUNT = 1_000_000;
    private static final int BATCH_SIZE = 10_000;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createMillionUsersAndMeasureNicknameSearch() {
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_users_username");

        String targetNickname = bulkInsertUsers();

        double fullScanTime = measureAverageSearchTime(
                "SELECT * FROM users WHERE username = ?",
                targetNickname,
                5
        );

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");

        double indexedSelectAllTime = measureAverageSearchTime(
                "SELECT * FROM users WHERE username = ?",
                targetNickname,
                1_000
        );
        double indexedProjectionTime = measureAverageSearchTime(
                "SELECT id, email, username FROM users WHERE username = ?",
                targetNickname,
                1_000
        );

        System.out.println("targetNickname = " + targetNickname);
        System.out.printf(Locale.US, "full scan select * = %.3f ms%n", fullScanTime);
        System.out.printf(Locale.US, "indexed select * = %.3f ms%n", indexedSelectAllTime);
        System.out.printf(Locale.US, "indexed projection = %.3f ms%n", indexedProjectionTime);
    }

    private String bulkInsertUsers() {
        String sql = "INSERT INTO users (email, username, password, user_role, created_at, modified_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        SplittableRandom random = new SplittableRandom(20260618L);
        String targetNickname = null;

        for (int offset = 0; offset < TOTAL_COUNT; offset += BATCH_SIZE) {
            List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);

            for (int i = offset; i < offset + BATCH_SIZE && i < TOTAL_COUNT; i++) {
                String nickname = createNickname(i, random.nextLong());
                if (i == TOTAL_COUNT / 2) {
                    targetNickname = nickname;
                }
                LocalDateTime now = LocalDateTime.now();
                batchArgs.add(new Object[]{
                        "bulk-user-" + i + "@example.com",
                        nickname,
                        "password",
                        "USER",
                        Timestamp.valueOf(now),
                        Timestamp.valueOf(now)
                });
            }

            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        return targetNickname;
    }

    private String createNickname(int index, long randomValue) {
        return "nick_" + index + "_" + Long.toUnsignedString(randomValue, 36);
    }

    private double measureAverageSearchTime(String sql, String nickname, int repeatCount) {
        jdbcTemplate.queryForList(sql, nickname);

        long start = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            jdbcTemplate.queryForList(sql, nickname);
        }
        return (System.nanoTime() - start) / 1_000_000.0 / repeatCount;
    }
}
