package hotspot.admin.common.jwt;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.ErrorResponse;
import hotspot.admin.common.exception.code.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && jwtProvider.validateToken(token)) {
                String principal = jwtProvider.getPrincipal(token);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (ApplicationException e) {
            setErrorResponse(response, e.getCode());
        }
    }

    private void setErrorResponse(HttpServletResponse response, BaseErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCustomCode(),
                errorCode.getMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
