package net.risesoft.y9public.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.y9public.entity.Y9ClickedApp;
import net.risesoft.y9public.repository.Y9ClickedAppRepository;
import net.risesoft.y9public.service.Y9ClickedAppService;

/**
 *
 * @author guoweijun
 * @author shidaobang
 * @author mengjuhua
 *
 */
@Service
@RequiredArgsConstructor
public class Y9ClickedAppServiceImpl implements Y9ClickedAppService {

    private final Y9ClickedAppRepository y9ClickedAppRepository;

    @Override
    public void save(Y9ClickedApp clickedApp) {
        y9ClickedAppRepository.save(clickedApp);
    }

}
