package net.risesoft.y9public.manager.resource;

import net.risesoft.y9public.entity.resource.Y9Operation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 按钮 manager
 * 
 * @author shidaobang
 * @date 2023/07/26
 * @since 9.6.3
 */
public interface Y9OperationManager {
    Y9Operation findById(String id);

    Y9Operation getById(String id);

    Y9Operation save(Y9Operation y9Operation);
    
    void delete(Y9Operation y9Operation);

    @Transactional(readOnly = false)
    Y9Operation updateTabIndex(String id, int index);
}
