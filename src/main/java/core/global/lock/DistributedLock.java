package core.global.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final RedissonClient redissonClient;
    private static final String LOCK_PREFIX = "lock:";

    /**
     * 분산락 실행
     * @param key       락 키 (예: "payment:memberId")
     * @param waitTime  락 획득 대기시간 (초)
     * @param leaseTime 락 보유시간 (초)
     * @param task      실행할 로직
     */
    public <T> T execute(String key, long waitTime, long leaseTime, LockTask<T> task) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("락 획득 실패: " + key);
            }
            return task.execute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 대기 중 인터럽트 발생: " + key, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @FunctionalInterface
    public interface LockTask<T> {
        T execute();
    }
}
