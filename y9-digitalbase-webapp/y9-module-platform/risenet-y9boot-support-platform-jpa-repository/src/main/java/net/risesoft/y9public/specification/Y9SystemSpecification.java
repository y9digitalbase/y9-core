package net.risesoft.y9public.specification;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class Y9SystemSpecification<Y9System> implements Specification<Y9System> {
    private static final long serialVersionUID = 646889627202200605L;

    private String type;
    private String name;
    private String isvId;

    public Y9SystemSpecification() {
        super();
    }

    public Y9SystemSpecification(String isvId, String name) {
        super();
        this.name = name;
        this.isvId = isvId;
    }

    public Y9SystemSpecification(String type, String name, String isvId) {
        super();
        this.type = type;
        this.name = name;
        this.isvId = isvId;
    }

    public String getIsvId() {
        return isvId;
    }

    public void setIsvId(String isvId) {
        this.isvId = isvId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Predicate toPredicate(Root<Y9System> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();
        List<Expression<Boolean>> expressions = predicate.getExpressions();
        if (StringUtils.hasText(type)) {
            expressions.add(cb.equal(root.get("type").as(String.class), type));
        }
        if (StringUtils.hasText(name)) {
            expressions.add(cb.like(root.get("name").as(String.class), "%" + name + "%"));
        }
        if (StringUtils.hasText(isvId)) {
            expressions.add(cb.equal(root.get("isvGuid").as(String.class), isvId));
        }
        return predicate;
    }
}
