package org.apereo.cas.authentication.metadata;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.configuration.model.core.ticket.RememberMeAuthenticationProperties;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

import y9.util.common.Y9Util;

/**
 * Determines if the credential provided are for Remember Me Services and then sets the appropriate Authentication
 * attribute if remember me services have been requested.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@ToString(callSuper = true)
@RequiredArgsConstructor
public class RememberMeAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    private final RememberMeAuthenticationProperties properties;

    /*
     * @Override public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) { transaction.getPrimaryCredential().ifPresent(r -> { if (RememberMeCredential.class.cast(r).isRememberMe()) {
     * LOGGER.debug("Credential is configured to be remembered. Captured this as [{}] attribute", RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME); var rememberMe = true; val clientInfo = ClientInfoHolder.getClientInfo(); if (clientInfo != null) { if
     * (StringUtils.isNotBlank(properties.getSupportedUserAgents()) && StringUtils.isNotBlank(properties.getSupportedIpAddresses())) { rememberMe = RegexUtils.find(properties.getSupportedUserAgents(), clientInfo.getUserAgent()) && RegexUtils.find(properties.getSupportedIpAddresses(),
     * clientInfo.getClientIpAddress()); } else if (StringUtils.isNotBlank(properties.getSupportedUserAgents())) { rememberMe = RegexUtils.find(properties.getSupportedUserAgents(), clientInfo.getUserAgent()); } else if (StringUtils.isNotBlank(properties.getSupportedIpAddresses())) { rememberMe =
     * RegexUtils.find(properties.getSupportedIpAddresses(), clientInfo.getClientIpAddress()); } }
     * 
     * if (rememberMe) { builder.addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE); } } }); }
     */

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        transaction.getPrimaryCredential().ifPresent(r -> {
            if (RememberMeCredential.class.cast(r).isRememberMe()) {
                builder.addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);
            } else {
                boolean rememberMe = false;
                val clientInfo = ClientInfoHolder.getClientInfo();
                if (clientInfo != null) {
                    String ip = clientInfo.getClientIpAddress();
                    String ips = properties.getSupportedIpAddresses();
                    if (StringUtils.isNotBlank(ips)) {
                        List<String> list = Y9Util.StringToList(ips, ",");
                        for (String s : list) {
                            if (s.equalsIgnoreCase(ip)) {
                                rememberMe = true;
                                break;
                            }
                        }
                    }
                }

                if (rememberMe) {
                    builder.addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);
                }
            }

        });
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RememberMeCredential;
    }
}
