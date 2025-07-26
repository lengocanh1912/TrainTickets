package t3h.edu.vn.traintickets.controller.admin;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.RouteCreateDto;
import t3h.edu.vn.traintickets.dto.RouteUpdateDto;
import t3h.edu.vn.traintickets.entities.Route;
import t3h.edu.vn.traintickets.service.RouteService;
import t3h.edu.vn.traintickets.service.StationService;

import java.util.List;

@Controller
@RequestMapping("/admin/route")
//@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminRouteController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private StationService stationService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "route");
    }

    @GetMapping("/search")
    public String searchRoutes(@RequestParam String keyword, Model model) {
        List<Route> routes = routeService.searchRoutes(keyword);
        model.addAttribute("routes", routes);
        return "admin/route/search";
    }

    //    @GetMapping("list")
//    @ResponseBody
//    public Object list(Model model,
//                       @RequestParam(defaultValue = "0") Integer page,
//                       @RequestParam(defaultValue = "5") Integer perpage) {
//        return routeService.paging(page, perpage);
//    }
    @GetMapping("view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        model.addAttribute("page", routeService.paging(page, perpage));
        model.addAttribute("path", "/admin/route/view");
        return "admin/route/view";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("route", new RouteCreateDto());
        model.addAttribute("stations", stationService.findAll());
        model.addAttribute("action", "/admin/route/save"); // ✅ Thêm dòng này
        return "admin/route/form";
    }


    @PostMapping("save")
    public String save(@Valid @ModelAttribute("route") RouteCreateDto routeDto,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("stations", stationService.findAll());
            return "admin/route/form";
        }

        if (routeDto.getDepartureId().equals(routeDto.getArrivalId())) {
            bindingResult.rejectValue("arrivalId", "error.route", "Ga đi và ga đến không được trùng nhau");
            model.addAttribute("stations", stationService.findAll());
            return "admin/route/form";
        }

        routeService.create(routeDto);
        redirectAttributes.addFlashAttribute("message", "Tạo mới tuyến đường thành công");
        return "redirect:/admin/route/view";
    }

    @GetMapping("/update")
    public String update(@RequestParam Long id, Model model) {
        model.addAttribute("route", routeService.findByIdForUpdate(id));
        model.addAttribute("stations", stationService.findAll());
        model.addAttribute("action", "/admin/route/update"); // ✅ Thêm dòng này
        return "admin/route/form";
    }


    @PostMapping("update")
    public String update(@Valid @ModelAttribute("route") RouteUpdateDto routeDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("stations", stationService.findAll());
            return "admin/route/form";
        }

        if (routeDto.getDepartureId().equals(routeDto.getArrivalId())) {
            bindingResult.rejectValue("arrivalId", "error.route", "Ga đi và ga đến không được trùng nhau");
            model.addAttribute("stations", stationService.findAll());
            return "admin/route/form";
        }

        routeService.update(routeDto);
        redirectAttributes.addFlashAttribute("message", "Cập nhật tuyến đường thành công");
        return "redirect:/admin/route/view";
    }

    @GetMapping("detail")
    public String detail(@RequestParam Long id, Model model) {
        model.addAttribute("route", routeService.findById(id));
        return "admin/route/detail";
    }

    @GetMapping("delete")
    public String delete(@RequestParam Long id,
                         @RequestParam(name = "page", defaultValue = "0") Integer page,
                         RedirectAttributes redirectAttributes) {
        try {
            routeService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa tuyến đường thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/admin/route/view?page=" + page;
    }
}


