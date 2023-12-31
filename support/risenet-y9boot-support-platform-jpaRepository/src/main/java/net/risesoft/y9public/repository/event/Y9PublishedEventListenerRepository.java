package net.risesoft.y9public.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.y9public.entity.event.Y9PublishedEventListener;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Repository
@Transactional(value = "rsPublicTransactionManager", readOnly = true)
public interface Y9PublishedEventListenerRepository
    extends JpaRepository<Y9PublishedEventListener, String>, JpaSpecificationExecutor<Y9PublishedEventListener> {

}
