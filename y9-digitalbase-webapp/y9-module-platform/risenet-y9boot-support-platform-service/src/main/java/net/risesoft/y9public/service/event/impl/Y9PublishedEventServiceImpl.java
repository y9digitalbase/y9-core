package net.risesoft.y9public.service.event.impl;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.pojo.Y9PageQuery;
import net.risesoft.y9public.entity.event.Y9PublishedEvent;
import net.risesoft.y9public.repository.event.Y9PublishedEventRepository;
import net.risesoft.y9public.service.event.Y9PublishedEventService;
import net.risesoft.y9public.specification.Y9PublishedEventSpecification;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Service
@RequiredArgsConstructor
public class Y9PublishedEventServiceImpl implements Y9PublishedEventService {

    private final Y9PublishedEventRepository y9PublishedEventRepository;

    @Override
    public List<Y9PublishedEvent> listByTenantId(String tenantId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createTime");
        return y9PublishedEventRepository.findByTenantId(tenantId, sort);
    }

    @Override
    public List<Y9PublishedEvent> listByTenantId(String tenantId, Date startTime) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createTime");
        Y9PublishedEventSpecification spec = new Y9PublishedEventSpecification(tenantId, null, null, startTime, null);
        return y9PublishedEventRepository.findAll(spec, sort);
    }

    @Override
    public Page<Y9PublishedEvent> page(Y9PageQuery pageQuery, String tenantId, String eventName,
        String eventDescription, Date startTime, Date endTime) {
        Pageable pageable =
            PageRequest.of(pageQuery.getPage4Db(), pageQuery.getSize(), Sort.by(Sort.Direction.DESC, "createTime"));
        Y9PublishedEventSpecification spec =
            new Y9PublishedEventSpecification(tenantId, eventName, eventDescription, startTime, endTime);
        return y9PublishedEventRepository.findAll(spec, pageable);
    }
}
