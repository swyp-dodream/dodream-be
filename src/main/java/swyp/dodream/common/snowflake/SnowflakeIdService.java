package swyp.dodream.common.snowflake;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Snowflake ID 서비스
// 애플리케이션에서 Snowflake ID를 생성하고 관리하는 서비스
@Service
@RequiredArgsConstructor
public class SnowflakeIdService {
    
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    
    // 새로운 Snowflake ID 생성
    public Long generateId() {
        return snowflakeIdGenerator.nextId();
    }
    
    // Snowflake ID 정보 파싱 (디버깅용)
    public String parseId(Long id) {
        if (id == null) {
            return "null";
        }
        return snowflakeIdGenerator.parseSnowflakeId(id);
    }
    
    // Snowflake ID에서 타임스탬프 추출
    public Long extractTimestamp(Long id) {
        if (id == null) {
            return null;
        }
        return snowflakeIdGenerator.extractTimestamp(id);
    }
    
    // Snowflake ID에서 노드 ID 추출
    public Long extractNodeId(Long id) {
        if (id == null) {
            return null;
        }
        return snowflakeIdGenerator.extractNodeId(id);
    }
    
    // Snowflake ID에서 시퀀스 번호 추출
    public Long extractSequence(Long id) {
        if (id == null) {
            return null;
        }
        return snowflakeIdGenerator.extractSequence(id);
    }
}
