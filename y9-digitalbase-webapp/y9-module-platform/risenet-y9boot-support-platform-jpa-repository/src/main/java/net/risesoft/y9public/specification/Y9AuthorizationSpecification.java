package net.risesoft.y9public.specification;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import net.risesoft.y9.Y9LoginUserHolder;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
public class Y9AuthorizationSpecification<RoleResourcePermission> implements Specification<RoleResourcePermission> {

    private static final long serialVersionUID = -7096661628110348011L;

    private String resourceId;
    private String roleId;
    private String tenantId;
    private String code;
    private Integer inherit;
    private Integer negative;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getInherit() {
        return inherit;
    }

    public void setInherit(Integer inherit) {
        this.inherit = inherit;
    }

    public Integer getNegative() {
        return negative;
    }

    public void setNegative(Integer negative) {
        this.negative = negative;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Predicate toPredicate(Root<RoleResourcePermission> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();
        List<Expression<Boolean>> expressions = predicate.getExpressions();

        if (resourceId != null) {
            expressions.add(cb.equal(root.get("resourceId"), resourceId));
        }
        if (roleId != null) {
            expressions.add(cb.equal(root.get("roleId"), roleId));
        }
        if (code != null) {
            expressions.add(cb.equal(root.get("code"), code));
        }
        if (inherit != null) {
            expressions.add(cb.equal(root.get("inherit"), inherit));
        } else {
            expressions.add(cb.isNull(root.get("inherit")));
        }

        expressions.add(cb.equal(root.get("tenantId"), Y9LoginUserHolder.getTenantId()));

        if (negative != null && negative == 1) {
            expressions.add(cb.equal(root.get("negative"), 1));
        } else {
            expressions.add(cb.or(cb.isNull(root.get("negative")), cb.equal(root.get("negative"), 0)));
        }

        return predicate;
    }
}
