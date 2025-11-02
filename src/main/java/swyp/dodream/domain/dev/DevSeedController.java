package swyp.dodream.domain.dev;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.DurationPeriod;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 홈 화면 필터 전부 테스트 가능하도록 "모집글 50개"를 한 번에 삽입하는 시드 컨트롤러.
 * - DB에 직접 INSERT (JdbcTemplate 사용) → Swagger로 하나씩 생성할 필요 없음
 * - 생성되는 데이터는 제목에 [SEED] 프리픽스가 들어가서 쉽게 구분/정리 가능
 *
 * 전제:
 *  - owner_user_id = 110435692680581120 사용(해당 유저가 DB에 존재해야 FK 통과)
 *  - master 테이블(role, tech_skill, interest_keyword)은 이미 채워져 있어야 함
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevSeedController {

    private final JdbcTemplate jdbcTemplate;
    private final SnowflakeIdService snowflake;

    // 고정 owner
    private static final long OWNER_ID = 110435692680581120L;

    // 분포 설정
    private static final int TOTAL = 50;          // 총 50개
    private static final int PROJECT_CNT = 25;    // PROJECT 25
    private static final int STUDY_CNT = 25;      // STUDY 25

    // 마스터 id 풀 (네가 준 값 기반)
    private static final int[] ROLE_IDS = {1,2,3,4,5,6,7,8};

    // tech_skill: 카테고리별 id 묶음 (프론트/백/모바일/디자인)
    private static final int[] FE_STACK = {1,2,3,4,5,6};
    private static final int[] BE_STACK = {7,8,9,10,11,12,13,14,15,16,17,18,19,20};
    private static final int[] MO_STACK = {21,22,23,24,25};
    private static final int[] DE_STACK = {26,27,28,29};

    // interest_keyword(관심사) id (AI/모빌/데이터/…)
    private static final int[] IK_TECH = {1,2,3};            // AI, 모빌리티, 데이터
    private static final int[] IK_BIZ  = {4,5,6};            // 이커머스, O2O, 금융
    private static final int[] IK_SOC  = {7,8,9};            // 환경, 지역, 교육
    private static final int[] IK_LIFE = {10,11,12,13,14,15};// F&B, 패션&뷰티, 건강, 여행, 스포츠, 반려동물
    private static final int[] IK_CULT = {16,17,18};         // 게임, 미디어, 예술&공연

    private static final ActivityMode[] MODES = {
            ActivityMode.ONLINE, ActivityMode.OFFLINE, ActivityMode.HYBRID
    };

    private static final DurationPeriod[] DURATIONS = {
            DurationPeriod.UNDECIDED, DurationPeriod.ONE_MONTH, DurationPeriod.TWO_MONTHS,
            DurationPeriod.THREE_MONTHS, DurationPeriod.SIX_MONTHS, DurationPeriod.LONG_TERM
    };

    @PostMapping("/seed")
    @Operation(
            summary = "시드 데이터 생성 (50개)",
            description = """
                개발/테스트용 시드 데이터 50개를 한 번에 생성합니다.
                - PROJECT 25개 / STUDY 25개
                - title에 [SEED] prefix 붙어서 일반 데이터와 구분됩니다.
                - master(role, tech_skill, interest_keyword)는 사전 등록 필수입니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시드 생성 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> seed() {
        // 생성 전제 확인(필수 유저 존재 여부 등)은 DB 제약으로 실패 시 예외로 드러나므로 여기서는 생략

        final LocalDateTime now = LocalDateTime.now();
        List<Long> createdPostIds = new ArrayList<>(TOTAL);

        // 1) PROJECT 25개
        for (int i = 0; i < PROJECT_CNT; i++) {
            long postId = createOnePost(ProjectType.PROJECT, i, now);
            createdPostIds.add(postId);
        }

        // 2) STUDY 25개
        for (int i = 0; i < STUDY_CNT; i++) {
            long postId = createOnePost(ProjectType.STUDY, i, now);
            createdPostIds.add(postId);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created", createdPostIds.size());
        result.put("postIds", createdPostIds);
        return result;
    }

    @PostMapping("/seed/clear")
    @Operation(
            summary = "시드 데이터 전체 삭제",
            description = """
                [SEED] 프리픽스 붙은 모집글 관련 데이터만 전부 삭제합니다.
                post / post_role_requirement / post_stack / post_field 전체 정리됩니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시드 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> clearSeed() {
        // 자식 테이블부터 정리 (FK 제약 대응)
        // 시드 데이터는 title에 [SEED] 프리픽스를 달아두었음
        int roleDeleted = jdbcTemplate.update(
                "DELETE pr FROM post_role_requirement pr " +
                        "JOIN post p ON pr.post_id = p.id " +
                        "WHERE p.title LIKE '[SEED] %'"
        );
        int stackDeleted = jdbcTemplate.update(
                "DELETE ps FROM post_stack ps " +
                        "JOIN post p ON ps.post_id = p.id " +
                        "WHERE p.title LIKE '[SEED] %'"
        );
        int fieldDeleted = jdbcTemplate.update(
                "DELETE pf FROM post_field pf " +
                        "JOIN post p ON pf.post_id = p.id " +
                        "WHERE p.title LIKE '[SEED] %'"
        );
        int postDeleted = jdbcTemplate.update(
                "DELETE FROM post WHERE title LIKE '[SEED] %'"
        );

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("deleted_post_role_rows", roleDeleted);
        res.put("deleted_post_stack_rows", stackDeleted);
        res.put("deleted_post_field_rows", fieldDeleted);
        res.put("deleted_post_rows", postDeleted);
        return res;
    }

    // === 내부 구현 ===

    /** 단일 Post + 연관(post_role_requirement, post_stack, post_field) 삽입 */
    private long createOnePost(ProjectType type, int seq, LocalDateTime now) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // post id (Snowflake)
        long postId = snowflake.generateId();

        // 상태: 대부분 RECRUITING, 일부 COMPLETED 섞기 (정렬/필터 확인용)
        PostStatus status = (seq % 7 == 0) ? PostStatus.COMPLETED : PostStatus.RECRUITING;

        // 활동 방식 다양화
        ActivityMode mode = MODES[ (seq + rnd.nextInt(3)) % MODES.length ];

        // 기간 다양화
        DurationPeriod duration = DURATIONS[ (seq + rnd.nextInt(DURATIONS.length)) % DURATIONS.length ];

        // 마감일: now + 7~90일 임의, 일부는 과거(-30~-1)로 넣어서 edge 케이스 확인
        int shiftDays = (seq % 9 == 0) ? -rnd.nextInt(1, 31) : rnd.nextInt(7, 91);
        LocalDateTime deadline = now.plusDays(shiftDays).withHour(rnd.nextInt(9, 23)).withMinute(rnd.nextInt(0, 60));

        // 제목/내용
        String title = String.format("[SEED] %s 모집글 #%02d", type.name(), seq + 1);
        String content = makeContent(type, mode, duration);

        // deleted=false, created_at/updated_at=now
        insertPost(postId, type, mode, duration, deadline, status, title, content, now);

        // 연관관계 삽입
        // roles: 1~3개
        int roleCount = 1 + rnd.nextInt(3);
        insertRoles(postId, roleCount);

        // stack: FE/BE/MO/DE 중 2~4개 혼합
        int stackCount = 2 + rnd.nextInt(3);
        insertStacks(postId, stackCount, type, seq);

        // interest keywords: 카테고리별에서 2~3개
        int fieldCount = 2 + rnd.nextInt(2);
        insertFields(postId, fieldCount, seq);

        return postId;
    }

    private void insertPost(long postId,
                            ProjectType projectType,
                            ActivityMode activityMode,
                            DurationPeriod duration,
                            LocalDateTime deadlineAt,
                            PostStatus status,
                            String title,
                            String content,
                            LocalDateTime now) {

        String sql = "INSERT INTO post " +
                "(id, owner_user_id, project_type, activity_mode, duration, deadline_at, status, title, content, deleted, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                postId,
                OWNER_ID,
                projectType.name(),
                activityMode.name(),
                duration.name(),
                Timestamp.valueOf(deadlineAt),
                status.name(),
                title,
                content,
                0,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    private void insertRoles(long postId, int roleCount) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        // role 중복 없이 뽑기
        int[] picked = pickDistinct(ROLE_IDS, roleCount, rnd);
        for (int roleId : picked) {
            long prId = snowflake.generateId(); // post_role_requirement PK
            int headcount = 1 + rnd.nextInt(3); // 1~3명
            String sql = "INSERT INTO post_role_requirement (id, post_id, role_id, headcount) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, prId, postId, roleId, headcount);
        }
    }

    private void insertStacks(long postId, int stackCount, ProjectType type, int seq) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // 프로젝트/스터디별로 스택 구성을 살짝 다르게: PROJECT면 BE/FE 위주, STUDY면 FE/MO 위주
        List<Integer> pool = new ArrayList<>();
        if (type == ProjectType.PROJECT) {
            addAll(pool, BE_STACK);
            addAll(pool, FE_STACK);
            if (seq % 3 == 0) addAll(pool, DE_STACK);
        } else {
            addAll(pool, FE_STACK);
            addAll(pool, MO_STACK);
            if (seq % 4 == 0) addAll(pool, BE_STACK);
        }

        int[] poolArr = pool.stream().mapToInt(Integer::intValue).toArray();
        int[] picked = pickDistinct(poolArr, stackCount, rnd);

        String sql = "INSERT INTO post_stack (post_id, tech_skill_id) VALUES (?, ?)";
        for (int skillId : picked) {
            jdbcTemplate.update(sql, postId, skillId);
        }
    }

    private void insertFields(long postId, int fieldCount, int seq) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // 관심사는 카테고리별 묶음에서 하나씩 섞어 담기
        List<int[]> buckets = Arrays.asList(IK_TECH, IK_BIZ, IK_SOC, IK_LIFE, IK_CULT);
        List<Integer> pool = new ArrayList<>();
        // 매 시드마다 2~3개 골라 섞기
        Collections.shuffle(buckets, new Random(seq * 31L + 7));
        for (int i = 0; i < Math.min(fieldCount, buckets.size()); i++) {
            int[] b = buckets.get(i);
            pool.add(b[ rnd.nextInt(b.length) ]);
        }
        // 중복 제거
        int[] picked = pool.stream().distinct().mapToInt(Integer::intValue).toArray();

        String sql = "INSERT INTO post_field (post_id, interest_keyword_id) VALUES (?, ?)";
        for (int ikId : picked) {
            jdbcTemplate.update(sql, postId, ikId);
        }
    }

    private static String makeContent(ProjectType type, ActivityMode mode, DurationPeriod duration) {
        return new StringBuilder()
                .append("시드 데이터입니다. 실제 운영 데이터가 아닙니다.\n")
                .append("- 유형: ").append(type).append("\n")
                .append("- 활동 방식: ").append(mode).append("\n")
                .append("- 활동 기간: ").append(duration.getLabel()).append("\n")
                .append("- 소개: 필터/정렬 기능 검증용으로 자동 생성된 샘플 모집글입니다.\n")
                .toString();
    }

    /** 배열에서 중복 없는 임의 추출 */
    private static int[] pickDistinct(int[] source, int k, ThreadLocalRandom rnd) {
        if (k >= source.length) return Arrays.copyOf(source, source.length);
        int[] copy = Arrays.copyOf(source, source.length);
        // Fisher–Yates shuffle (부분)
        for (int i = 0; i < k; i++) {
            int j = i + rnd.nextInt(copy.length - i);
            int tmp = copy[i]; copy[i] = copy[j]; copy[j] = tmp;
        }
        return Arrays.copyOf(copy, k);
    }

    private static void addAll(List<Integer> list, int... arr) {
        for (int v : arr) list.add(v);
    }
}
