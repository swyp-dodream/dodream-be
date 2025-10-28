package swyp.dodream.domain.post.repository;

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
import swyp.dodream.domain.post.domain.PostRole;
import swyp.dodream.domain.post.domain.PostStack;

public class PostSpecification {

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

    public static Specification<Post> hasRole(String roleCode) {
        return (root, query, cb) -> {
            Join<Post, PostRole> join = root.join("postRoles", JoinType.INNER);
            Join<PostRole, Role> roleJoin = join.join("role", JoinType.INNER);
            return cb.equal(roleJoin.get("code"), roleCode);
        };
    }

    public static Specification<Post> hasTech(String techName) {
        return (root, query, cb) -> {
            Join<Post, PostStack> join = root.join("postStacks", JoinType.INNER);
            Join<PostStack, TechSkill> techJoin = join.join("techSkill", JoinType.INNER);
            return cb.equal(techJoin.get("name"), techName);
        };
    }

    public static Specification<Post> hasInterest(String interestName) {
        return (root, query, cb) -> {
            Join<Post, InterestKeyword> join = root.join("interests", JoinType.INNER);
            return cb.equal(join.get("name"), interestName);
        };
    }

    public static Specification<Post> hasActivityMode(ActivityMode mode) {
        return (root, query, cb) -> cb.equal(root.get("activityMode"), mode);
    }

    public static Specification<Post> onlyRecruiting() {
        return (root, query, cb) -> cb.equal(root.get("status"), PostStatus.RECRUITING);
    }
}
