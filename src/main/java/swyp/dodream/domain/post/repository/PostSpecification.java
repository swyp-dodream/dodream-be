package swyp.dodream.domain.post.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import swyp.dodream.domain.master.domain.InterestKeyword;
import swyp.dodream.domain.master.domain.TechSkill;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.PostField;
import swyp.dodream.domain.post.domain.PostRole;
import swyp.dodream.domain.post.domain.PostStack;

import java.util.List;

public class PostSpecification {

    public static Specification<Post> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted")); // ← 여기가 추가된 부분
    }

    public static Specification<Post> hasType(ProjectType type) {
        return (root, query, cb) -> {
            if (type == ProjectType.ALL) return cb.conjunction();
            return cb.equal(root.get("projectType"), type);
        };
    }

    public static Specification<Post> containsKeyword(String keyword) {
        return (root, query, cb) -> cb.or(
                cb.like(root.get("title"), "%" + keyword + "%"),
                cb.like(root.get("content"), "%" + keyword + "%")
        );
    }

    public static Specification<Post> hasAnyRole(List<String> roles) {
        return (root, query, cb) -> {
            Join<Post, PostRole> postRole = root.join("roleRequirements", JoinType.LEFT);
            CriteriaBuilder.In<String> inClause = cb.in(postRole.get("role").get("name"));
            roles.forEach(inClause::value);
            return inClause;
        };
    }

    public static Specification<Post> hasAnyTech(List<String> techs) {
        return (root, query, cb) -> {
            Join<Post, PostStack> postStack = root.join("stacks", JoinType.LEFT);
            CriteriaBuilder.In<String> inClause = cb.in(postStack.get("techSkill").get("name"));
            techs.forEach(inClause::value);
            return inClause;
        };
    }

    public static Specification<Post> hasAnyInterest(List<String> interests) {
        return (root, query, cb) -> {
            Join<Post, PostField> fieldJoin = root.join("fields", JoinType.LEFT);
            CriteriaBuilder.In<String> inClause = cb.in(fieldJoin.get("interestKeyword").get("name"));
            interests.forEach(inClause::value);
            return inClause;
        };
    }


    public static Specification<Post> hasActivityMode(ActivityMode mode) {
        return (root, query, cb) -> cb.equal(root.get("activityMode"), mode);
    }

    public static Specification<Post> hasStatus(PostStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
