package com.kpjunaid.shared;

import com.kpjunaid.entity.User;
import com.kpjunaid.common.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserPrincipalSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockAuthUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User user = MockResource.getMockUserJohn();
        UserPrincipal userPrincipal = new UserPrincipal(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal.getUsername(), userPrincipal.getPassword(), userPrincipal.getAuthorities()
        );
        context.setAuthentication(authentication);
        return context;
    }
}
