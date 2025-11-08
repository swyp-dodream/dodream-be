package swyp.dodream.common.snowflake;

import org.springframework.stereotype.Component;

import java.time.Instant;

// Snowflake ID 생성기
// 
// Snowflake 알고리즘 구조:
// [1비트: 부호][41비트: 타임스탬프][10비트: 노드 ID][12비트: 시퀀스 번호]
// 
// - 타임스탬프: 순차성 보장
// - 노드 ID: 분산 환경에서 고유성 보장
// - 시퀀스 번호: 동일 밀리초 내 고유성 보장
@Component
public class SnowflakeIdGenerator {
    
    // Snowflake 알고리즘 상수
    private static final long EPOCH = 1735689600000L; // 2025-01-01 00:00:00 UTC
    private static final long NODE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    
    // 최대값 계산
    private static final long MAX_NODE_ID = (1L << NODE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    
    // 비트 시프트 값
    private static final long NODE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = NODE_ID_BITS + SEQUENCE_BITS;
    
    // 노드 ID (환경변수 또는 설정에서 가져올 수 있음)
    private final long nodeId;
    
    // 시퀀스 번호와 마지막 타임스탬프
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    public SnowflakeIdGenerator() {
        // 기본 노드 ID (실제 환경에서는 환경변수나 설정에서 가져와야 함)
        this.nodeId = 1L;
        
        if (nodeId > MAX_NODE_ID || nodeId < 0) {
            throw new IllegalArgumentException("Node ID must be between 0 and " + MAX_NODE_ID);
        }
    }
    
    public SnowflakeIdGenerator(long nodeId) {
        this.nodeId = nodeId;
        
        if (nodeId > MAX_NODE_ID || nodeId < 0) {
            throw new IllegalArgumentException("Node ID must be between 0 and " + MAX_NODE_ID);
        }
    }
    
    // 다음 Snowflake ID 생성
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();
        
        // 시계가 뒤로 가는 경우 처리
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + 
                (lastTimestamp - timestamp) + " milliseconds");
        }
        
        // 같은 밀리초 내에서 시퀀스 증가
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                // 시퀀스가 최대값에 도달하면 다음 밀리초까지 대기
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 새로운 밀리초이므로 시퀀스 초기화
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        // Snowflake ID 조합
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT) |
               (nodeId << NODE_ID_SHIFT) |
               sequence;
    }
    
    /**
     * 현재 타임스탬프 가져오기
     */
    private long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }
    
    /**
     * 다음 밀리초까지 대기
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
    
    /**
     * Snowflake ID에서 타임스탬프 추출
     */
    public long extractTimestamp(long snowflakeId) {
        return (snowflakeId >> TIMESTAMP_SHIFT) + EPOCH;
    }
    
    /**
     * Snowflake ID에서 노드 ID 추출
     */
    public long extractNodeId(long snowflakeId) {
        return (snowflakeId >> NODE_ID_SHIFT) & MAX_NODE_ID;
    }
    
    /**
     * Snowflake ID에서 시퀀스 번호 추출
     */
    public long extractSequence(long snowflakeId) {
        return snowflakeId & MAX_SEQUENCE;
    }
    
    /**
     * Snowflake ID 정보 디버깅용
     */
    public String parseSnowflakeId(long snowflakeId) {
        long timestamp = extractTimestamp(snowflakeId);
        long nodeId = extractNodeId(snowflakeId);
        long sequence = extractSequence(snowflakeId);
        
        return String.format("SnowflakeId[%d] -> Timestamp[%d] NodeId[%d] Sequence[%d]", 
            snowflakeId, timestamp, nodeId, sequence);
    }
}
