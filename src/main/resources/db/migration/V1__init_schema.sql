-- MySQL 8.x / InnoDB / UTC 권장
SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- =========================================================
-- USER 테이블은 외부 시스템(회원)이라고 가정: users(id) FK만 참조
-- =========================================================

-- =======================
-- post (모집글)
-- =======================
CREATE TABLE IF NOT EXISTS post (
                                    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    owner_user_id   BIGINT NOT NULL,
                                    project_type    ENUM('project','study') NOT NULL,
                                    activity_mode   ENUM('online','offline','hybrid') NOT NULL,
                                    duration_text   VARCHAR(100),
                                    deadline_at     DATETIME NULL,
                                    status          ENUM('recruiting','completed') NOT NULL DEFAULT 'recruiting',
                                    title           VARCHAR(120) NOT NULL,
                                    content         TEXT NOT NULL,
                                    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    deleted_at      BOOLEAN DEFAULT FALSE, -- (요구사항 명칭 그대로 사용: 삭제 여부)
                                    CONSTRAINT fk_post_owner_user
                                        FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_post_status_deadline ON post(status, deadline_at);
CREATE INDEX idx_post_owner ON post(owner_user_id);

-- =======================
-- post_view (조회수 집계)
-- =======================
CREATE TABLE IF NOT EXISTS post_view (
                                         post_id BIGINT NOT NULL,
                                         views   BIGINT NOT NULL DEFAULT 0,
                                         PRIMARY KEY (post_id),
                                         CONSTRAINT fk_post_view_post
                                             FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- interest_category / interest_keyword (관심 분야 마스터)
-- =======================
CREATE TABLE IF NOT EXISTS interest_category (
                                                 id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 name  VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS interest_keyword (
                                                id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                category_id  BIGINT NOT NULL,
                                                name         VARCHAR(50) NOT NULL,
                                                CONSTRAINT fk_interest_keyword_cat
                                                    FOREIGN KEY (category_id) REFERENCES interest_category(id) ON DELETE CASCADE,
                                                CONSTRAINT uk_interest_keyword UNIQUE (category_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- post_field (모집글의 분야 태그: 최대 2개는 애플리케이션 로직으로 보장)
-- =======================
CREATE TABLE IF NOT EXISTS post_field (
                                          post_id             BIGINT NOT NULL,
                                          interest_keyword_id BIGINT NOT NULL,
                                          PRIMARY KEY (post_id, interest_keyword_id),
                                          CONSTRAINT fk_post_field_post
                                              FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                          CONSTRAINT fk_post_field_keyword
                                              FOREIGN KEY (interest_keyword_id) REFERENCES interest_keyword(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- Role (직군 마스터)
-- =======================
CREATE TABLE IF NOT EXISTS role (
                                    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    code  ENUM('FE','BE','iOS','AOS','Designer','PM','Planner','Marketer') NOT NULL,
                                    name  VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- post_role_requirement (모집 직군 및 정원)
-- =======================
CREATE TABLE IF NOT EXISTS post_role_requirement (
                                                     id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     post_id  BIGINT NOT NULL,
                                                     role_id  BIGINT NOT NULL,
                                                     headcount INT   NOT NULL,
                                                     CONSTRAINT fk_prr_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                                     CONSTRAINT fk_prr_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- tech_category / tech_skill (기술 스택 마스터)
-- =======================
CREATE TABLE IF NOT EXISTS tech_category (
                                             id   BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             name VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tech_skill (
                                          id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          category_id BIGINT NOT NULL,
                                          name        VARCHAR(50) NOT NULL,
                                          CONSTRAINT fk_tech_skill_cat FOREIGN KEY (category_id) REFERENCES tech_category(id) ON DELETE CASCADE,
                                          CONSTRAINT uk_tech_skill UNIQUE (category_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- post_stack (모집글 요구 기술 스택)
-- =======================
CREATE TABLE IF NOT EXISTS post_stack (
                                          post_id       BIGINT NOT NULL,
                                          tech_skill_id BIGINT NOT NULL,
                                          PRIMARY KEY (post_id, tech_skill_id),
                                          CONSTRAINT fk_post_stack_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                          CONSTRAINT fk_post_stack_skill FOREIGN KEY (tech_skill_id) REFERENCES tech_skill(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- application (지원)
-- =======================
CREATE TABLE IF NOT EXISTS application (
                                           id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           post_id               BIGINT NOT NULL,
                                           applicant_user_id     BIGINT NOT NULL,
                                           application_message   VARCHAR(1000) NOT NULL,
                                           status  ENUM('applied','withdrawn','accepted','rejected') NOT NULL DEFAULT 'applied',
                                           created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           withdrawn_at          DATETIME NULL,
                                           CONSTRAINT fk_app_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                           CONSTRAINT fk_app_user FOREIGN KEY (applicant_user_id) REFERENCES users(id) ON DELETE CASCADE,
                                           CONSTRAINT uk_app_unique UNIQUE (post_id, applicant_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- suggestion (지원 제안: Leader -> User)
-- =======================
CREATE TABLE IF NOT EXISTS suggestion (
                                          id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          suggestion_message VARCHAR(1000) NOT NULL,
                                          post_id            BIGINT NOT NULL,
                                          from_user_id       BIGINT NOT NULL,
                                          to_user_id         BIGINT NOT NULL,
                                          created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          CONSTRAINT fk_sugg_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                          CONSTRAINT fk_sugg_from FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
                                          CONSTRAINT fk_sugg_to   FOREIGN KEY (to_user_id)   REFERENCES users(id) ON DELETE CASCADE,
                                          CONSTRAINT uk_sugg UNIQUE (post_id, to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- matched (매칭)
-- =======================
CREATE TABLE IF NOT EXISTS matched (
                                       id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       post_id            BIGINT NOT NULL,
                                       user_id            BIGINT NOT NULL,
                                       application_id     BIGINT NOT NULL,
                                       matched_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       canceled_at        DATETIME NULL,
                                       canceled_by        ENUM('leader','member') NULL,
                                       cancel_reason_code ENUM('other_member','schedule','role_mismatch','other') NULL,
                                       canceled           BOOLEAN NOT NULL DEFAULT FALSE,
                                       CONSTRAINT fk_matched_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_matched_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_matched_app  FOREIGN KEY (application_id) REFERENCES application(id) ON DELETE CASCADE,
                                       CONSTRAINT uk_matched UNIQUE (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- feedback (후기) + dict + detail
-- =======================
CREATE TABLE IF NOT EXISTS feedback (
                                        id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        match_id       BIGINT NOT NULL,
                                        writer_user_id BIGINT NOT NULL,
                                        target_user_id BIGINT NOT NULL,
                                        polarity       ENUM('positive','negative') NOT NULL,
                                        created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        CONSTRAINT fk_feedback_match  FOREIGN KEY (match_id) REFERENCES matched(id) ON DELETE CASCADE,
                                        CONSTRAINT fk_feedback_writer FOREIGN KEY (writer_user_id) REFERENCES users(id) ON DELETE CASCADE,
                                        CONSTRAINT fk_feedback_target FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS feedback_detail_dict (
                                                    code     VARCHAR(30) PRIMARY KEY,
                                                    polarity ENUM('positive','negative') NOT NULL,
                                                    label    VARCHAR(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS feedback_detail (
                                               feedback_id BIGINT NOT NULL,
                                               detail_code VARCHAR(30) NOT NULL,
                                               PRIMARY KEY (feedback_id, detail_code),
                                               CONSTRAINT fk_feedback_detail_fb   FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE,
                                               CONSTRAINT fk_feedback_detail_code FOREIGN KEY (detail_code)  REFERENCES feedback_detail_dict(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =======================
-- chat_room (요청에 따라 포함: 1:1 채팅방)
-- =======================
CREATE TABLE IF NOT EXISTS chat_room (
                                         id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         post_id          BIGINT NOT NULL,
                                         leader_user_id   BIGINT NOT NULL,
                                         member_user_id   BIGINT NOT NULL,
                                         created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         first_message_at DATETIME NULL,
                                         CONSTRAINT fk_chat_post   FOREIGN KEY (post_id)        REFERENCES post(id) ON DELETE CASCADE,
                                         CONSTRAINT fk_chat_leader FOREIGN KEY (leader_user_id) REFERENCES users(id) ON DELETE CASCADE,
                                         CONSTRAINT fk_chat_member FOREIGN KEY (member_user_id) REFERENCES users(id) ON DELETE CASCADE,
                                         CONSTRAINT uk_chat_triplet UNIQUE (post_id, leader_user_id, member_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 추천: 자주 조회되는 조합 인덱스
CREATE INDEX idx_application_post ON application(post_id);
CREATE INDEX idx_matched_post ON matched(post_id);
CREATE INDEX idx_suggestion_post ON suggestion(post_id);
