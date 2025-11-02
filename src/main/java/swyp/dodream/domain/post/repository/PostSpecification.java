package swyp.dodream.domain.post.repository;

import jakarta.persistence.criteria.*; // CriteriaBuilder, CriteriaQuery, Root, Subquery, Join, JoinType 포함
import org.springframework.data.jpa.domain.Specification;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.*;

import java.util.List;

public class PostSpecification {

    public static Specification<Post> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Post> hasType(ProjectType type) {
        return (root, query, cb) -> {
            if (type == null || type == ProjectType.ALL) return cb.conjunction();
            return cb.equal(root.get("projectType"), type);
        };
    }

    public static Specification<Post> hasAnyRole(List<String> roles) {
        return (root, query, cb) -> {
            if (roles == null || roles.isEmpty()) return cb.conjunction();

            // Post에 대한 EXISTS 서브쿼리를 생성합니다.
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<PostRole> postRoleRoot = subquery.from(PostRole.class); // FROM post_role
            subquery.select(postRoleRoot.get("post").get("id"))
                    .where(
                            // 1. 메인 쿼리의 Post와 서브쿼리의 PostRole을 연결
                            cb.equal(postRoleRoot.get("post"), root),
                            // 2. 역할 이름 조건
                            postRoleRoot.get("role").get("name").in(roles)
                    );

            // WHERE EXISTS (서브쿼리)
            return cb.exists(subquery);
        };
    }

    public static Specification<Post> hasAnyTech(List<String> techs) {
        return (root, query, cb) -> {
            if (techs == null || techs.isEmpty()) return cb.conjunction();

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<PostStack> postStackRoot = subquery.from(PostStack.class); // FROM post_stack
            subquery.select(postStackRoot.get("post").get("id"))
                    .where(
                            cb.equal(postStackRoot.get("post"), root),
                            postStackRoot.get("techSkill").get("name").in(techs)
                    );

            return cb.exists(subquery);
        };
    }

    public static Specification<Post> hasAnyInterest(List<String> interests) {
        return (root, query, cb) -> {
            if (interests == null || interests.isEmpty()) return cb.conjunction();

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<PostField> postFieldRoot = subquery.from(PostField.class); // FROM post_field
            subquery.select(postFieldRoot.get("post").get("id"))
                    .where(
                            cb.equal(postFieldRoot.get("post"), root),
                            postFieldRoot.get("interestKeyword").get("name").in(interests)
                    );

            return cb.exists(subquery);
        };
    }

    public static Specification<Post> hasActivityMode(ActivityMode mode) {
        return (root, query, cb) -> {
            if (mode == null) return cb.conjunction();
            return cb.equal(root.get("activityMode"), mode);
        };
    }

    public static Specification<Post> hasStatus(PostStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }
}