-- ============================================================
-- 스키마 검증 에러 수정을 위한 CREATE 문 모음
-- 실행일: 2025-11-04
-- 목적: Entity 정의와 일치하는 올바른 스키마 생성
-- ============================================================

-- ============================================================
-- 1. post 테이블 수정 (올바른 버전)
-- ============================================================
-- duration_text 제거, duration ENUM 추가, content TINYTEXT, deleted 추가
DROP TABLE IF EXISTS post;
CREATE TABLE post (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id   BIGINT NOT NULL,
    project_type    ENUM('project','study') NOT NULL,
    activity_mode   ENUM('online','offline','hybrid') NOT NULL,
    duration        ENUM('UNDECIDED', 'UNDER_ONE_MONTH', 'ONE_MONTH', 'TWO_MONTHS', 'THREE_MONTHS', 'FOUR_MONTHS', 'FIVE_MONTHS', 'SIX_MONTHS', 'LONG_TERM') NOT NULL DEFAULT 'UNDECIDED',
    deadline_at     DATETIME NULL,
    status          ENUM('recruiting','completed') NOT NULL DEFAULT 'recruiting',
    title           VARCHAR(120) NOT NULL,
    content         TINYTEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_post_owner_user FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_post_status_deadline (status, deadline_at),
    INDEX idx_post_owner (owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. post_view 테이블 수정 (올바른 버전)
-- ============================================================
-- deleted 컬럼 추가
DROP TABLE IF EXISTS post_view;
CREATE TABLE post_view (
    post_id BIGINT NOT NULL,
    views   BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (post_id),
    CONSTRAINT fk_post_view_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. suggestion 테이블 수정 (올바른 버전)
-- ============================================================
-- status, withdrawn_at, updated_at 컬럼 추가
DROP TABLE IF EXISTS suggestion;
CREATE TABLE suggestion (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    suggestion_message VARCHAR(1000) NOT NULL,
    post_id            BIGINT NOT NULL,
    from_user_id       BIGINT NOT NULL,
    to_user_id         BIGINT NOT NULL,
    status             ENUM('SENT', 'ACCEPTED', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'SENT',
    withdrawn_at       DATETIME NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sugg_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_sugg_from FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sugg_to   FOREIGN KEY (to_user_id)   REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_sugg UNIQUE (post_id, to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. users 테이블 수정 (올바른 버전)
-- ============================================================
-- status 컬럼 추가 (users 테이블이 이미 존재하는 경우를 대비)
-- 실제로는 users 테이블이 이미 존재하므로 ALTER가 필요하지만,
-- CREATE 문만 원한다면 이 부분은 주석 처리
-- ALTER TABLE users ADD COLUMN status BOOLEAN NOT NULL DEFAULT TRUE;

-- users 테이블 전체 CREATE 문 (참고용)
-- CREATE TABLE IF NOT EXISTS users (
--     id              BIGINT PRIMARY KEY,
--     name            VARCHAR(255) NOT NULL,
--     profile_image_url VARCHAR(255),
--     status          BOOLEAN NOT NULL DEFAULT TRUE,
--     created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at      DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. matched 테이블 수정 (올바른 버전)
-- ============================================================
-- is_canceled 컬럼 추가, canceled 컬럼 제거 또는 변경
DROP TABLE IF EXISTS matched;
CREATE TABLE matched (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id            BIGINT NOT NULL,
    user_id            BIGINT NOT NULL,
    application_id     BIGINT NULL,
    matched_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    canceled_at        DATETIME NULL,
    canceled_by        ENUM('leader','member') NULL,
    cancel_reason_code ENUM('other_member','schedule','role_mismatch','other') NULL,
    is_canceled        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_matched_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_matched_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_matched_app  FOREIGN KEY (application_id) REFERENCES application(id) ON DELETE SET NULL,
    CONSTRAINT uk_matched UNIQUE (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. feedback 테이블 수정 (올바른 버전)
-- ============================================================
-- match_id → post_id, writer_user_id → from_user_id, 
-- target_user_id → to_user_id, polarity → feedback_type
DROP TABLE IF EXISTS feedback_options;
DROP TABLE IF EXISTS feedback;
CREATE TABLE feedback (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id       BIGINT NOT NULL,
    from_user_id  BIGINT NOT NULL,
    to_user_id    BIGINT NOT NULL,
    feedback_type ENUM('POSITIVE', 'NEGATIVE') NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_post  FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_from  FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_to    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_feedback UNIQUE (post_id, from_user_id, to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- feedback_options 테이블 (ElementCollection)
CREATE TABLE feedback_options (
    feedback_id BIGINT NOT NULL,
    option_value ENUM('GOOD_COMMUNICATION', 'KEEPS_PROMISES', 'RESPONSIBLE', 'POSITIVE_ENERGY', 'PROBLEM_SOLVER', 'RESPECTS_OPINIONS', 'POOR_COMMUNICATION', 'IGNORES_OPINIONS', 'LACKS_RESPONSIBILITY', 'NEGATIVE_INFLUENCE', 'POOR_PROBLEM_SOLVING', 'BREAKS_PROMISES') NOT NULL,
    PRIMARY KEY (feedback_id, option_value),
    CONSTRAINT fk_feedback_options_feedback FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 7. bookmark 테이블 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS bookmark (
    id         BIGINT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    post_id    BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmark_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmark_post  FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT uk_bookmark UNIQUE (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 8. profiles 테이블 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS profiles (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    nickname        VARCHAR(10) NOT NULL UNIQUE,
    gender          ENUM('남성', '여성', '선택안함') NOT NULL DEFAULT '선택안함',
    age_band        ENUM('십대', '이십대', '삼십대', '사십대이상', '선택안함') NOT NULL DEFAULT '선택안함',
    experience      ENUM('신입', '일년이상삼년미만', '삼년이상오년미만', '오년이상') NOT NULL,
    activity_mode   ENUM('온라인', '오프라인', '하이브리드') NOT NULL,
    intro_text      VARCHAR(200),
    is_public       BOOLEAN NOT NULL DEFAULT TRUE,
    profile_image_code INT NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_profile_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 9. profile_tech_stacks 테이블 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS profile_tech_stacks (
    id         BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    tech_stack ENUM('JAVASCRIPT', 'TYPESCRIPT', 'REACT', 'VUE', 'SVELTE', 'NEXTJS', 'JAVA', 'SPRING', 'NODEJS', 'NESTJS', 'GO', 'KOTLIN', 'EXPRESS', 'MYSQL', 'MONGODB', 'RUBY', 'PYTHON', 'DJANGO', 'PHP', 'GRAPHQL', 'FIREBASE', 'SWIFT', 'OBJECTIVE_C', 'KOTLIN_MOBILE', 'JAVA_MOBILE', 'FLUTTER', 'REACT_NATIVE', 'ZEPLIN', 'FIGMA', 'SKETCH', 'ADOBE_XD') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tech_stack_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    INDEX idx_tech_stack_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 10. profile_urls 테이블 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS profile_urls (
    id         BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    label      ENUM('GITHUB', 'BLOG', 'PORTFOLIO', 'OTHER') NOT NULL,
    url        VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_url_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    INDEX idx_profile_url_profile (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 11. proposal_notifications 테이블 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS proposal_notifications (
    id                  BIGINT PRIMARY KEY,
    profile_id          BIGINT NOT NULL UNIQUE,
    proposal_project_on BOOLEAN NOT NULL DEFAULT TRUE,
    proposal_study_on   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_proposal_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 12. ManyToMany Join Tables 생성
-- ============================================================
-- profiles_roles (Hibernate는 복수형 필드명을 사용하므로 roles_id로 생성)
CREATE TABLE IF NOT EXISTS profiles_roles (
    profile_id BIGINT NOT NULL,
    roles_id   BIGINT NOT NULL,
    PRIMARY KEY (profile_id, roles_id),
    CONSTRAINT fk_profiles_roles_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_profiles_roles_role FOREIGN KEY (roles_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- profiles_interest_keywords (Hibernate는 복수형 필드명을 사용하므로 interest_keywords_id로 생성)
CREATE TABLE IF NOT EXISTS profiles_interest_keywords (
    profile_id           BIGINT NOT NULL,
    interest_keywords_id BIGINT NOT NULL,
    PRIMARY KEY (profile_id, interest_keywords_id),
    CONSTRAINT fk_profiles_interest_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_profiles_interest_keyword FOREIGN KEY (interest_keywords_id) REFERENCES interest_keyword(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- profiles_tech_skills (Hibernate는 복수형 필드명을 사용하므로 tech_skills_id로 생성)
CREATE TABLE IF NOT EXISTS profiles_tech_skills (
    profile_id    BIGINT NOT NULL,
    tech_skills_id BIGINT NOT NULL,
    PRIMARY KEY (profile_id, tech_skills_id),
    CONSTRAINT fk_profiles_tech_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_profiles_tech_skill FOREIGN KEY (tech_skills_id) REFERENCES tech_skill(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 13. master 데이터 테이블에 created_at, updated_at 추가
-- ============================================================
-- interest_category에 created_at, updated_at 추가
ALTER TABLE interest_category 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- interest_keyword에 created_at, updated_at 추가
ALTER TABLE interest_keyword 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- tech_category에 created_at, updated_at 추가
ALTER TABLE tech_category 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- tech_skill에 created_at, updated_at 추가
ALTER TABLE tech_skill 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- role에 created_at, updated_at 추가
ALTER TABLE role 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ============================================================
-- 14. oauth_accounts 테이블 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS oauth_accounts (
    id           BIGINT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    provider     ENUM('GOOGLE', 'NAVER') NOT NULL,
    email        VARCHAR(255) NOT NULL,
    last_login_at DATETIME NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_oauth_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 완료 메시지
-- ============================================================
SELECT '✅ 스키마 CREATE 문 생성 완료' AS status;
