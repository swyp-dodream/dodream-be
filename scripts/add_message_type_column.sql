-- ============================================================
-- chat_message 테이블에 message_type 컬럼 추가
-- 실행일: 2025-11-26
-- 목적: ChatMessage 엔티티와 데이터베이스 스키마 일치
-- ============================================================

ALTER TABLE chat_message 
ADD COLUMN message_type VARCHAR(20) DEFAULT 'TALK';

SELECT '✅ message_type 컬럼 추가 완료' AS status;

