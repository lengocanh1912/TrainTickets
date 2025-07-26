package t3h.edu.vn.traintickets.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import t3h.edu.vn.traintickets.dto.UserCreateDto;
import t3h.edu.vn.traintickets.config.UserDetailServiceImpl.UserDetailImpl;
import t3h.edu.vn.traintickets.dto.UserPasswordDto;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Lấy thông tin người dùng từ authentication
        UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
        // Chuyển đổi UserDetailImpl sang UserCreateDto
        UserPasswordDto userPassword = new UserPasswordDto();
        userPassword.setNewPassword(userDetails.getPassword());
        // (Cập nhật các thuộc tính khác nếu cần)
        // Lưu thông tin vào session dưới dạng UserCreateDto
        HttpSession session = request.getSession();
        session.setAttribute("user", userPassword);

        // Lấy URL từ ?redirect=
        String redirectParam = request.getParameter("redirect");
        // Lấy URL đã bị chặn trước login
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        if (redirectParam != null && !redirectParam.isEmpty()) {
            response.sendRedirect(redirectParam);
            return;
        }
        if (savedRequest != null) {
            String redirectUrl = savedRequest.getRedirectUrl();
            response.sendRedirect(redirectUrl);
            return;
        }

        // Lấy các quyền (authorities) của người dùng
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Duyệt qua các quyền và thực hiện chuyển hướng phù hợp
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role) || "ROLE_EMPLOYEE".equals(role)) {
                response.sendRedirect("/trainticket/admin/home");
                return;
            } else if ("ROLE_CUSTOMER".equals(role)) {
                response.sendRedirect("/trainticket/user/home");
                return;
            }
        }

        // Mặc định nếu không khớp quyền, chuyển hướng đến trang mặc định
        response.sendRedirect("/default");
    }
}
