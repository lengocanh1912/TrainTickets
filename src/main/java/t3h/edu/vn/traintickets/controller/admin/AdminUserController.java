package t3h.edu.vn.traintickets.controller.admin;

import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.UserCreateDto;
import t3h.edu.vn.traintickets.dto.UserUpdateDto;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.service.UserService;

import java.util.List;


@Controller
@RequestMapping("/admin/user")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class    AdminUserController {
    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "user");
    }

    @GetMapping("list")
    @ResponseBody
    public Object list(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        return userService.paging(page, perpage);
    }

    @GetMapping("view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        model.addAttribute("page", userService.paging(page, perpage));
        model.addAttribute("path", "/admin/user/view");
        return "admin/user/view";
    }

    @GetMapping("create")
    public String create(Model model) {
        model.addAttribute("user", new UserCreateDto());
        return "/admin/user/create";
    }

    @GetMapping("update")
    public String update(@RequestParam Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return "/admin/user/update";
    }

    @PostMapping("save")
    public String save(Model model, @Valid @ModelAttribute(value = "user") UserCreateDto user,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "/admin/user/create";
        }
        if (!user.getPassword().equals(user.getRePassword())){
            bindingResult.rejectValue("rePassword", "error.user",
                    "Mật khẩu không trùng khớp");
            return "/admin/user/create";
        }
        userService.createUser(user);
        redirectAttributes.addFlashAttribute("message",
                "Tạo mới tài khoản thành công");
        return "redirect:/admin/user/view";
    }

    @PostMapping("update")
    public String save(Model model, @Valid @ModelAttribute(value = "user") UserUpdateDto user,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "/admin/user/update";
        }
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("message",
                "Cập nhật tài khoản thành công");
        return "redirect:/admin/user/view";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam("keyword") String keyword, Model model) {
        List<User> users = userService.searchUsers(keyword);
        model.addAttribute("users", users);
        return "admin/user/search";
    }

    @GetMapping("/detail")
    public String userDetail(@RequestParam("id") Long id,
                             Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "/admin/user/detail";
    }

    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             @RequestParam(name = "page", defaultValue = "0") Integer page,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("message",
                    "Xóa người dùng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/admin/user/view?page=" + page;
    }



}
