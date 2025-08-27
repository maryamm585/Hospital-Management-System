package Hospital.system.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null) {
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    logger.warn("JWT Token is blacklisted (logged out).");
                    filterChain.doFilter(request, response);
                    return; // Skip authentication for blacklisted tokens
                }

                if (jwtUtil.validateToken(jwt) && !jwtUtil.isTokenExpired(jwt)) {
                    authenticateUser(request, jwt);
                }
            }
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private void authenticateUser(HttpServletRequest request, String jwt) {
        String email = jwtUtil.getEmailFromToken(jwt);
        String role = jwtUtil.getRoleFromToken(jwt);

        if (StringUtils.hasText(email) &&
                StringUtils.hasText(role) &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            String formattedRole = formatRole(role);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(formattedRole));

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, null,
                    authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("Successfully authenticated user: {} with role: {}", email, formattedRole);
        }
    }

    private String formatRole(String role) {
        if (role != null && !role.startsWith("ROLE_")) {
            return "ROLE_" + role.toUpperCase();
        }
        return role != null ? role.toUpperCase() : null;
    }
}