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
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.enums.Experience;
import swyp.dodream.domain.profile.enums.Gender;
import swyp.dodream.domain.profile.enums.AgeBand;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.ai.service.EmbeddingService;
import swyp.dodream.domain.recommendation.repository.VectorRepository;
import swyp.dodream.domain.recommendation.util.TextExtractor;
import swyp.dodream.jwt.util.JwtUtil;
import swyp.dodream.jwt.service.TokenService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 홈 화면 필터/정렬/페이지네이션 전부 테스트 가능하도록 "모집글 100개"를 한 번에 삽입하는 시드 컨트롤러.
 * - DB에 직접 INSERT (JdbcTemplate 사용) → Swagger로 하나씩 생성할 필요 없음
 * - 생성되는 데이터는 제목에 [SEED] 프리픽스가 들어가서 쉽게 구분/정리 가능
 * - popular 정렬 테스트를 위해 view_count를 임의의 값(0~999)으로 삽입합니다.
 * <p>
 * 전제:
 * - owner_user_id = 110435692680581120 사용(해당 유저가 DB에 존재해야 FK 통과)
 * - master 테이블(role, tech_skill, interest_keyword)은 이미 채워져 있어야 함
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevSeedController {

    private final JdbcTemplate jdbcTemplate;
    private final SnowflakeIdService snowflake;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final Optional<EmbeddingService> embeddingService;
    private final Optional<VectorRepository> vectorRepository;
    private final JwtUtil jwtUtil;
    private final Optional<TokenService> tokenService;

    private static final int TOTAL = 100;
    private static final int PROJECT_CNT = 50;
    private static final int STUDY_CNT = 50;
    
    // DB에서 동적으로 조회하도록 변경 (Snowflake ID 사용)
    private List<Long> roleIds;
    private List<Long> techSkillIds;
    private List<Long> interestKeywordIds;
    private static final ActivityMode[] MODES = {ActivityMode.ONLINE, ActivityMode.OFFLINE, ActivityMode.HYBRID};
    private static final DurationPeriod[] DURATIONS = {
            DurationPeriod.UNDECIDED, DurationPeriod.ONE_MONTH, DurationPeriod.TWO_MONTHS,
            DurationPeriod.THREE_MONTHS, DurationPeriod.SIX_MONTHS, DurationPeriod.LONG_TERM
    };

    @PostMapping("/seed")
    @Operation(
            summary = "시드 데이터 생성",
            description = """
                개발/테스트용 시드 데이터 100개를 한 번에 생성합니다.
                사용자가 없으면 자동으로 생성합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시드 생성 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> seed() {
        final LocalDateTime now = LocalDateTime.now();
        List<Long> createdPostIds = new ArrayList<>(TOTAL);

        // 사용자가 없으면 생성 (1명만 생성)
        List<Long> userIds = createUsers(1);
        long ownerId = userIds.get(0);

        for (int i = 0; i < PROJECT_CNT; i++) {
            long postId = createOnePost(ProjectType.PROJECT, i, now, ownerId);
            createdPostIds.add(postId);
        }
        for (int i = 0; i < STUDY_CNT; i++) {
            long postId = createOnePost(ProjectType.STUDY, i, now, ownerId);
            createdPostIds.add(postId);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created", createdPostIds.size());
        result.put("ownerId", ownerId);
        result.put("postIds", createdPostIds);
        return result;
    }


    @PostMapping("/seed/clear")
    @Operation(
            summary = "시드 데이터 전체 삭제",
            description = """
                [SEED] 프리픽스 붙은 모집글 관련 데이터만 전부 삭제합니다.
                post / post_role_requirement / post_stack / post_field / post_view 전체 정리됩니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시드 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> clearSeed() {
        // 자식 테이블부터 정리 (FK 제약 대응)
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

        int viewDeleted = jdbcTemplate.update(
                "DELETE pv FROM post_view pv " +
                        "JOIN post p ON pv.post_id = p.id " +
                        "WHERE p.title LIKE '[SEED] %'"
        );

        int postDeleted = jdbcTemplate.update(
                "DELETE FROM post WHERE title LIKE '[SEED] %'"
        );

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("deleted_post_role_rows", roleDeleted);
        res.put("deleted_post_stack_rows", stackDeleted);
        res.put("deleted_post_field_rows", fieldDeleted);
        res.put("deleted_post_view_rows", viewDeleted);
        res.put("deleted_post_rows", postDeleted);
        return res;
    }

    @PostMapping("/auth/token/{userId}")
    @Operation(
            summary = "개발용 JWT 발급",
            description = """
                지정한 사용자 ID로 Access/Refresh 토큰을 발급합니다.
                - 운영에서는 사용하지 마세요.
                - Authorization: Bearer <accessToken> 로 API 호출 테스트에 사용하세요.
                """
    )
    public Map<String, Object> issueDevToken(@PathVariable Long userId) {
        // 유저 존재 여부 확인
        List<Long> existingUserIds = jdbcTemplate.queryForList(
                "SELECT id FROM users WHERE id = ?",
                Long.class,
                userId
        );
        if (existingUserIds == null || existingUserIds.isEmpty()) {
            throw new IllegalStateException("사용자가 존재하지 않습니다: " + userId);
        }

        String email = "dev@local";
        String name = "DEV_USER";

        String accessToken = jwtUtil.generateAccessToken(userId, email, name);
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        tokenService.ifPresent(ts -> ts.saveRefreshToken(userId, name, refreshToken));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }

    // === 내부 구현 ===

    private long createOnePost(ProjectType type, int seq, LocalDateTime now, long ownerId) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        long postId = snowflake.generateId();
        PostStatus status = (seq % 7 == 0) ? PostStatus.COMPLETED : PostStatus.RECRUITING;
        ActivityMode mode = MODES[ (seq + rnd.nextInt(3)) % MODES.length ];
        DurationPeriod duration = DURATIONS[ (seq + rnd.nextInt(DURATIONS.length)) % DURATIONS.length ];
        int shiftDays = (seq % 9 == 0) ? -rnd.nextInt(1, 31) : rnd.nextInt(7, 91);
        LocalDateTime deadline = now.plusDays(shiftDays).withHour(rnd.nextInt(9, 23)).withMinute(rnd.nextInt(0, 60));
        String title = String.format("[SEED] %s 모집글 #%02d", type.name(), seq + 1);
        String content = makeContent(type, mode, duration);
        int viewCount = rnd.nextInt(1000); // 0~999

        // 1. Post 삽입
        insertPost(postId, ownerId, type, mode, duration, deadline, status, title, content, now);

        // 2. PostView 삽입
        insertPostView(postId, viewCount);

        // 3. 연관관계 삽입
        int roleCount = 1 + rnd.nextInt(3);
        insertRoles(postId, roleCount);
        int stackCount = 2 + rnd.nextInt(3);
        insertStacks(postId, stackCount, type, seq);
        int fieldCount = 2 + rnd.nextInt(2);
        insertFields(postId, fieldCount, seq);

        return postId;
    }

    private void insertPost(long postId,
                            long ownerId,
                            ProjectType projectType,
                            ActivityMode activityMode,
                            DurationPeriod duration,
                            LocalDateTime deadlineAt,
                            PostStatus status,
                            String title,
                            String content,
                            LocalDateTime now) {

        // view_count 컬럼이 없는 원본 Post INSERT SQL
        String sql = "INSERT INTO post " +
                "(id, owner_user_id, project_type, activity_mode, duration, deadline_at, status, title, content, deleted, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                postId, ownerId, projectType.name(), activityMode.name(), duration.name(),
                Timestamp.valueOf(deadlineAt), status.name(), title, content,
                0, Timestamp.valueOf(now), Timestamp.valueOf(now)
        );
    }

    /** insertPostView: 'views', 'deleted' 컬럼 사용 (created_at/updated_at 없음) */
    private void insertPostView(long postId, int viewCount) {

        // SQL 컬럼명을 'views', 'deleted'로 변경, 값 3개
        String sql = "INSERT INTO post_view (post_id, views, deleted) " +
                "VALUES (?, ?, ?)";

        jdbcTemplate.update(sql,
                postId,
                viewCount,
                0 // deleted = false
        );
    }

    private void insertRoles(long postId, int roleCount) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        if (roleIds == null || roleIds.isEmpty()) {
            loadMasterData();
        }
        long[] picked = pickDistinctLongs(roleIds, roleCount, rnd);
        for (long roleId : picked) {
            long prId = snowflake.generateId();
            int headcount = 1 + rnd.nextInt(3);
            String sql = "INSERT INTO post_role_requirement (id, post_id, role_id, headcount) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, prId, postId, roleId, headcount);
        }
    }

    private void insertStacks(long postId, int stackCount, ProjectType type, int seq) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        if (techSkillIds == null || techSkillIds.isEmpty()) {
            loadMasterData();
        }
        // 전체 기술 스택에서 랜덤 선택
        long[] picked = pickDistinctLongs(techSkillIds, stackCount, rnd);
        String sql = "INSERT INTO post_stack (post_id, tech_skill_id) VALUES (?, ?)";
        for (long skillId : picked) {
            jdbcTemplate.update(sql, postId, skillId);
        }
    }

    private void insertFields(long postId, int fieldCount, int seq) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        if (interestKeywordIds == null || interestKeywordIds.isEmpty()) {
            loadMasterData();
        }
        // 전체 관심 키워드에서 랜덤 선택
        long[] picked = pickDistinctLongs(interestKeywordIds, fieldCount, rnd);
        String sql = "INSERT INTO post_field (post_id, interest_keyword_id) VALUES (?, ?)";
        for (long ikId : picked) {
            jdbcTemplate.update(sql, postId, ikId);
        }
    }

    private static String makeContent(ProjectType type, ActivityMode mode, DurationPeriod duration) {
        return new StringBuilder()
                .append("시드 데이터입니다. 실제 운영 데이터가 아닙니다.\n")
                .append("- 유형: ").append(type).append("\n")
                .append("- 활동 방식: ").append(mode).append("\n")
                .append("- 활동 기간: ").append(duration.getLabel()).append("\n")
                .append("- 소개: 필터/정렬/페이지네이션 기능 검증용으로 자동 생성된 샘플 모집글입니다.\n")
                .toString();
    }

    private static int[] pickDistinct(int[] source, int k, ThreadLocalRandom rnd) {
        if (k >= source.length) return Arrays.copyOf(source, source.length);
        int[] copy = Arrays.copyOf(source, source.length);
        for (int i = 0; i < k; i++) {
            int j = i + rnd.nextInt(copy.length - i);
            int tmp = copy[i]; copy[i] = copy[j]; copy[j] = tmp;
        }
        return Arrays.copyOf(copy, k);
    }

    private static long[] pickDistinctLongs(List<Long> source, int k, ThreadLocalRandom rnd) {
        if (source == null || source.isEmpty()) {
            throw new IllegalStateException("소스 리스트가 비어있습니다.");
        }
        if (k <= 0) {
            return new long[0];
        }
        if (k >= source.size()) {
            return source.stream().mapToLong(Long::longValue).toArray();
        }
        List<Long> copy = new ArrayList<>(source);
        Collections.shuffle(copy, new Random(rnd.nextLong()));
        return copy.stream().limit(k).mapToLong(Long::longValue).toArray();
    }

    private static void addAll(List<Integer> list, int... arr) {
        for (int v : arr) list.add(v);
    }

    // ==================== 프로필 및 게시글 대량 생성 ====================

    @PostMapping("/seed/profile-for-user/{userId}")
    @Operation(
            summary = "현재 사용자용 프로필 생성",
            description = """
                지정한 사용자 ID로 프로필을 생성합니다.
                추천 기능 테스트용입니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 생성 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> createProfileForCurrentUser(
            @PathVariable Long userId
    ) {
        final LocalDateTime now = LocalDateTime.now();
        
        // 사용자가 이미 존재하는지 확인
        List<Long> existingUserIds = jdbcTemplate.queryForList(
                "SELECT id FROM users WHERE id = ?",
                Long.class,
                userId
        );
        
        if (existingUserIds == null || existingUserIds.isEmpty()) {
            throw new IllegalStateException("사용자가 존재하지 않습니다: " + userId);
        }
        
        // 이미 프로필이 있는지 확인
        List<Long> existingProfileIds = jdbcTemplate.queryForList(
                "SELECT id FROM profiles WHERE user_id = ?",
                Long.class,
                userId
        );
        
        if (existingProfileIds != null && !existingProfileIds.isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "이미 프로필이 존재합니다",
                    "profileId", existingProfileIds.get(0)
            );
        }
        
        // 마스터 데이터 조회
        loadMasterData();
        
        // 프로필 생성
        long profileId = createOneProfile(userId, 0, now);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("profileId", profileId);
        result.put("message", "프로필 생성 완료. 벡터화 API를 호출하세요.");
        return result;
    }

    @PostMapping("/seed/recommendation")
    @Operation(
            summary = "추천 테스트용 데이터 생성",
            description = """
                프로필 100개와 게시글 100개를 생성합니다.
                추천 기능 테스트용 시드 데이터입니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시드 생성 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> seedRecommendationData() {
        final LocalDateTime now = LocalDateTime.now();
        List<Long> createdProfileIds = new ArrayList<>();
        List<Long> createdPostIds = new ArrayList<>();

        // 0. 마스터 데이터 조회 (Snowflake ID 사용)
        loadMasterData();

        // 1. 사용자 100명 생성 (프로필 생성을 위해)
        List<Long> userIds = createUsers(100);

        // 2. 프로필 100개 생성
        for (int i = 0; i < 100; i++) {
            long profileId = createOneProfile(userIds.get(i), i, now);
            createdProfileIds.add(profileId);
        }

        // 3. 게시글 100개 생성 (생성된 사용자 ID 사용)
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < PROJECT_CNT; i++) {
            long ownerId = userIds.get(rnd.nextInt(userIds.size())); // 랜덤 사용자 선택
            long postId = createOnePost(ProjectType.PROJECT, i, now, ownerId);
            createdPostIds.add(postId);
        }
        for (int i = 0; i < STUDY_CNT; i++) {
            long ownerId = userIds.get(rnd.nextInt(userIds.size())); // 랜덤 사용자 선택
            long postId = createOnePost(ProjectType.STUDY, i, now, ownerId);
            createdPostIds.add(postId);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created_users", userIds.size());
        result.put("created_profiles", createdProfileIds.size());
        result.put("created_posts", createdPostIds.size());
        result.put("profileIds", createdProfileIds);
        result.put("postIds", createdPostIds);
        return result;
    }

    /**
     * 마스터 데이터 조회 (Snowflake ID 사용)
     */
    private void loadMasterData() {
        // Role 조회
        roleIds = jdbcTemplate.queryForList(
                "SELECT id FROM role ORDER BY id",
                Long.class
        );
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalStateException("Role 데이터가 없습니다. 마스터 데이터를 먼저 생성해주세요.");
        }

        // TechSkill 조회
        techSkillIds = jdbcTemplate.queryForList(
                "SELECT id FROM tech_skill ORDER BY id",
                Long.class
        );
        if (techSkillIds == null || techSkillIds.isEmpty()) {
            throw new IllegalStateException("TechSkill 데이터가 없습니다. 마스터 데이터를 먼저 생성해주세요.");
        }

        // InterestKeyword 조회
        interestKeywordIds = jdbcTemplate.queryForList(
                "SELECT id FROM interest_keyword ORDER BY id",
                Long.class
        );
        if (interestKeywordIds == null || interestKeywordIds.isEmpty()) {
            throw new IllegalStateException("InterestKeyword 데이터가 없습니다. 마스터 데이터를 먼저 생성해주세요.");
        }
    }

    /**
     * 사용자 생성 (프로필 생성을 위해 필요)
     */
    private List<Long> createUsers(int count) {
        List<Long> userIds = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            long userId = snowflake.generateId();
            String name = String.format("테스트유저%d", i + 1);
            
            // users 테이블에 삽입
            String sql = "INSERT INTO users (id, name, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    userId,
                    name,
                    1, // status = true
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now())
            );
            
            userIds.add(userId);
        }
        
        return userIds;
    }

    /**
     * 프로필 1개 생성
     */
    private long createOneProfile(long userId, int seq, LocalDateTime now) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        
        long profileId = snowflake.generateId();
        // nickname 중복 방지를 위해 Snowflake ID의 마지막 4자리 사용 (varchar(10) 제한)
        // 형식: "T1_1234" (최대 7자, seq가 100까지 가도 "T100_1234" = 9자)
        String nickname = String.format("T%d_%04d", seq + 1, Math.abs(profileId % 10000));
        Experience experience = Experience.values()[rnd.nextInt(Experience.values().length)];
        swyp.dodream.domain.profile.enums.ActivityMode activityMode = 
                swyp.dodream.domain.profile.enums.ActivityMode.values()[rnd.nextInt(swyp.dodream.domain.profile.enums.ActivityMode.values().length)];
        Gender gender = Gender.values()[rnd.nextInt(Gender.values().length)];
        AgeBand ageBand = AgeBand.values()[rnd.nextInt(AgeBand.values().length)];
        String introText = String.format("테스트 프로필 #%d입니다. 추천 기능 테스트용 데이터입니다.", seq + 1);
        boolean isPublic = true;
        int profileImageCode = 1 + rnd.nextInt(10);

        // 1. Profile 삽입
        String profileSql = "INSERT INTO profiles " +
                "(id, user_id, nickname, gender, age_band, experience, activity_mode, intro_text, is_public, profile_image_code, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(profileSql,
                profileId, userId, nickname, gender.name(), ageBand.name(), experience.name(),
                activityMode.name(), introText, isPublic ? 1 : 0, profileImageCode,
                Timestamp.valueOf(now), Timestamp.valueOf(now)
        );

        // 2. ProposalNotification 삽입
        long pnId = snowflake.generateId();
        String pnSql = "INSERT INTO proposal_notifications (id, profile_id, proposal_project_on, proposal_study_on, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(pnSql, pnId, profileId, 1, 1, Timestamp.valueOf(now), Timestamp.valueOf(now));

        // 3. ManyToMany 관계 삽입
        int roleCount = 1 + rnd.nextInt(3);
        insertProfileRoles(profileId, roleCount);
        
        int interestCount = 2 + rnd.nextInt(4); // 2~5개
        insertProfileInterests(profileId, interestCount);
        
        int techCount = 2 + rnd.nextInt(4); // 2~5개
        insertProfileTechSkills(profileId, techCount);

        return profileId;
    }

    private void insertProfileRoles(long profileId, int roleCount) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        if (roleIds == null || roleIds.isEmpty()) {
            loadMasterData();
        }
        long[] picked = pickDistinctLongs(roleIds, roleCount, rnd);
        String sql = "INSERT INTO profiles_roles (profile_id, roles_id) VALUES (?, ?)";
        for (long roleId : picked) {
            jdbcTemplate.update(sql, profileId, roleId);
        }
    }

    private void insertProfileInterests(long profileId, int interestCount) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        if (interestKeywordIds == null || interestKeywordIds.isEmpty()) {
            loadMasterData();
        }
        // 전체 관심 키워드에서 랜덤 선택
        long[] picked = pickDistinctLongs(interestKeywordIds, interestCount, rnd);
        String sql = "INSERT INTO profiles_interest_keywords (profile_id, interest_keywords_id) VALUES (?, ?)";
        for (long ikId : picked) {
            jdbcTemplate.update(sql, profileId, ikId);
        }
    }

    private void insertProfileTechSkills(long profileId, int techCount) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        if (techSkillIds == null || techSkillIds.isEmpty()) {
            loadMasterData();
        }
        // 전체 기술 스택에서 랜덤 선택
        long[] picked = pickDistinctLongs(techSkillIds, techCount, rnd);
        String sql = "INSERT INTO profiles_tech_skills (profile_id, tech_skills_id) VALUES (?, ?)";
        for (long skillId : picked) {
            jdbcTemplate.update(sql, profileId, skillId);
        }
    }

    // ==================== 벡터 저장 배치 작업 ====================

    @PostMapping("/seed/vectorize-profiles")
    @Operation(
            summary = "프로필 벡터 저장",
            description = """
                생성된 모든 프로필에 대해 벡터를 생성하고 저장합니다.
                시드 데이터 생성 후 이 API를 호출하여 벡터를 저장해야 추천 기능이 동작합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "벡터 저장 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> vectorizeProfiles() {
        if (embeddingService.isEmpty() || vectorRepository.isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "벡터 DB 미사용 환경입니다. EmbeddingService와 VectorRepository가 필요합니다."
            );
        }

        List<Profile> profiles = profileRepository.findAll();
        int successCount = 0;
        int failCount = 0;
        List<Long> failedProfileIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Rate limit 방지를 위해 각 요청 사이에 지연 추가
        for (Profile profile : profiles) {
            try {
                // 프로필 텍스트 추출
                String profileText = TextExtractor.extractFromProfile(profile);
                if (profileText == null || profileText.trim().isEmpty()) {
                    continue;
                }

                // 임베딩 생성 (재시도 로직 포함)
                float[] embedding = null;
                int retryCount = 0;
                int maxRetries = 3;
                while (retryCount < maxRetries) {
                    try {
                        embedding = embeddingService.get().embed(profileText);
                        break;
                    } catch (Exception e) {
                        retryCount++;
                        if (retryCount < maxRetries && e.getMessage() != null && e.getMessage().contains("429")) {
                            // Rate limit 오류인 경우 대기 후 재시도
                            Thread.sleep(2000 * retryCount); // 2초, 4초, 6초 대기
                            continue;
                        }
                        throw e;
                    }
                }

                if (embedding == null) {
                    throw new IllegalStateException("임베딩 생성 실패");
                }

                // 벡터 저장 (userId를 profileId로 사용)
                vectorRepository.get().upsertProfileVector(profile.getUserId(), embedding);
                successCount++;
                
                // Rate limit 방지를 위한 지연 (100ms)
                Thread.sleep(100);
            } catch (Exception e) {
                failCount++;
                failedProfileIds.add(profile.getId());
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 200) {
                    errorMsg = errorMsg.substring(0, 200) + "...";
                }
                errors.add(String.format("프로필 ID %d: %s", profile.getId(), errorMsg));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("total", profiles.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        if (!failedProfileIds.isEmpty()) {
            result.put("failedProfileIds", failedProfileIds);
        }
        if (!errors.isEmpty()) {
            result.put("errors", errors.subList(0, Math.min(10, errors.size()))); // 최대 10개만
        }
        return result;
    }

    @PostMapping("/seed/vectorize-posts")
    @Operation(
            summary = "게시글 벡터 저장",
            description = """
                생성된 모든 게시글에 대해 벡터를 생성하고 저장합니다.
                시드 데이터 생성 후 이 API를 호출하여 벡터를 저장해야 추천 기능이 동작합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "벡터 저장 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> vectorizePosts() {
        if (embeddingService.isEmpty() || vectorRepository.isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "벡터 DB 미사용 환경입니다. EmbeddingService와 VectorRepository가 필요합니다."
            );
        }

        List<Post> posts = postRepository.findAll();
        int successCount = 0;
        int failCount = 0;
        List<Long> failedPostIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Rate limit 방지를 위해 각 요청 사이에 지연 추가
        for (Post post : posts) {
            try {
                // 게시글 텍스트 추출
                String postText = TextExtractor.extractFromPost(post);
                if (postText == null || postText.trim().isEmpty()) {
                    continue;
                }

                // 임베딩 생성 (재시도 로직 포함)
                float[] embedding = null;
                int retryCount = 0;
                int maxRetries = 3;
                while (retryCount < maxRetries) {
                    try {
                        embedding = embeddingService.get().embed(postText);
                        break;
                    } catch (Exception e) {
                        retryCount++;
                        if (retryCount < maxRetries && e.getMessage() != null && e.getMessage().contains("429")) {
                            // Rate limit 오류인 경우 대기 후 재시도
                            Thread.sleep(2000 * retryCount); // 2초, 4초, 6초 대기
                            continue;
                        }
                        throw e;
                    }
                }

                if (embedding == null) {
                    throw new IllegalStateException("임베딩 생성 실패");
                }

                // 벡터 저장
                vectorRepository.get().upsertVector(post.getId(), embedding);
                successCount++;
                
                // Rate limit 방지를 위한 지연 (100ms)
                Thread.sleep(100);
            } catch (Exception e) {
                failCount++;
                failedPostIds.add(post.getId());
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 200) {
                    errorMsg = errorMsg.substring(0, 200) + "...";
                }
                errors.add(String.format("게시글 ID %d: %s", post.getId(), errorMsg));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("total", posts.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        if (!failedPostIds.isEmpty()) {
            result.put("failedPostIds", failedPostIds);
        }
        if (!errors.isEmpty()) {
            result.put("errors", errors.subList(0, Math.min(10, errors.size()))); // 최대 10개만
        }
        return result;
    }

    @PostMapping("/seed/vectorize-profiles-retry")
    @Operation(
            summary = "실패한 프로필 벡터 재저장",
            description = """
                이전에 실패한 프로필 ID 목록을 받아서 벡터를 다시 저장합니다.
                failedProfileIds 배열에 실패한 프로필 ID를 전달하세요.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "벡터 저장 성공",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @Transactional
    public Map<String, Object> vectorizeProfilesRetry(
            @RequestBody(required = false) Map<String, Object> request
    ) {
        if (embeddingService.isEmpty() || vectorRepository.isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "벡터 DB 미사용 환경입니다. EmbeddingService와 VectorRepository가 필요합니다."
            );
        }

        // 요청에서 failedProfileIds 추출, 없으면 모든 프로필 처리
        List<Long> targetProfileIds = new ArrayList<>();
        if (request != null && request.containsKey("failedProfileIds")) {
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) request.get("failedProfileIds");
            targetProfileIds = ids.stream().map(Number::longValue).toList();
        } else {
            // 모든 프로필 조회
            targetProfileIds = profileRepository.findAll().stream()
                    .map(Profile::getId)
                    .toList();
        }

        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long profileId : targetProfileIds) {
            Profile profile = profileRepository.findById(profileId).orElse(null);
            if (profile == null) {
                continue;
            }

            try {
                String profileText = TextExtractor.extractFromProfile(profile);
                if (profileText == null || profileText.trim().isEmpty()) {
                    continue;
                }

                // 재시도 로직
                float[] embedding = null;
                int retryCount = 0;
                int maxRetries = 3;
                while (retryCount < maxRetries) {
                    try {
                        embedding = embeddingService.get().embed(profileText);
                        break;
                    } catch (Exception e) {
                        retryCount++;
                        if (retryCount < maxRetries && e.getMessage() != null && e.getMessage().contains("429")) {
                            Thread.sleep(2000 * retryCount);
                            continue;
                        }
                        throw e;
                    }
                }

                if (embedding == null) {
                    throw new IllegalStateException("임베딩 생성 실패");
                }

                vectorRepository.get().upsertProfileVector(profile.getUserId(), embedding);
                successCount++;
                Thread.sleep(100);
            } catch (Exception e) {
                failCount++;
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 200) {
                    errorMsg = errorMsg.substring(0, 200) + "...";
                }
                errors.add(String.format("프로필 ID %d: %s", profileId, errorMsg));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("total", targetProfileIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        if (!errors.isEmpty()) {
            result.put("errors", errors.subList(0, Math.min(10, errors.size())));
        }
        return result;
    }
    
    /**
     * 지원자 데이터 생성 (테스트용)
     */
    @PostMapping("/seed/applications/{postId}")
    @Operation(
            summary = "지원자 데이터 생성",
            description = "특정 게시글에 대한 지원자 데이터 30개를 생성합니다. 프로필이 없으면 자동 생성합니다."
    )
    @Transactional
    public Map<String, Object> seedApplications(@PathVariable Long postId) {
        // 1. 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));
        
        // 2. master 데이터 로드
        loadMasterData();
        
        // 3. 사용자 30명 생성 (프로필과 함께)
        List<Long> userIds = createUsers(30);
        List<Long> profileIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < userIds.size(); i++) {
            long profileId = createOneProfile(userIds.get(i), i, now);
            profileIds.add(profileId);
        }
        
        // 4. 각 사용자가 게시글에 지원
        List<Long> applicationIds = new ArrayList<>();
        
        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            Long applicationId = snowflake.generateId();
            
            // 랜덤 직군 선택
            Long roleId = roleIds.get(ThreadLocalRandom.current().nextInt(roleIds.size()));
            
            // 지원 메시지 생성
            String message = generateApplicationMessage(i);
            
            String sql = """
                INSERT INTO application (id, post_id, applicant_id, role_id, application_message, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 'APPLIED', ?, ?)
            """;
            
            jdbcTemplate.update(sql, 
                applicationId, 
                postId, 
                userId, 
                roleId, 
                message,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
            );
            
            applicationIds.add(applicationId);
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("postId", postId);
        result.put("usersCreated", userIds.size());
        result.put("profilesCreated", profileIds.size());
        result.put("applicationsCreated", applicationIds.size());
        result.put("applicationIds", applicationIds);
        
        return result;
    }
    
    /**
     * 지원 메시지 생성
     */
    private String generateApplicationMessage(int index) {
        String[] messages = {
            "안녕하세요! 해당 프로젝트에 큰 관심이 있어 지원하게 되었습니다. Spring Boot와 React 경험이 있습니다.",
            "프로젝트 내용을 보고 꼭 참여하고 싶어서 지원합니다. 백엔드 개발 경험 2년 이상입니다.",
            "관련 경험이 있어 프로젝트에 기여할 수 있을 것 같습니다. 팀 프로젝트 다수 경험했습니다.",
            "기술 스택이 잘 맞아서 지원하게 되었습니다. Java, Spring 전문가입니다.",
            "프로젝트 주제가 흥미로워 지원합니다. 웹 개발 포트폴리오가 있습니다!",
            "이런 프로젝트를 찾고 있었습니다. 적극적으로 참여하겠습니다.",
            "팀원들과 협업하며 함께 성장하고 싶어 지원합니다. Git, Jira 사용 가능합니다.",
            "프로젝트 목표에 공감하며, 함께 만들어가고 싶습니다. 프론트엔드도 가능합니다.",
            "새로운 기술을 배우고 싶어 지원하게 되었습니다. 열정이 있습니다!",
            "열정과 책임감을 가지고 임하겠습니다. 마감일 준수 잘 합니다!"
        };
        
        return messages[index % messages.length];
    }
}