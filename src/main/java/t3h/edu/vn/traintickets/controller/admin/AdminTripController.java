package t3h.edu.vn.traintickets.controller.admin;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.TripCreateDto;
import t3h.edu.vn.traintickets.dto.TripUpdateDto;
import t3h.edu.vn.traintickets.entities.Trip;
import t3h.edu.vn.traintickets.service.RouteService;
import t3h.edu.vn.traintickets.service.TrainService;
import t3h.edu.vn.traintickets.service.TripDtoService;
import t3h.edu.vn.traintickets.service.TripService;

import java.util.List;

@Controller
@RequestMapping("/admin/trip")
public class AdminTripController {

    @Autowired
    private TripService tripService;
    @Autowired
    private TrainService trainService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private TripDtoService tripDtoService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "trip");
    }

    @GetMapping("/search")
    public String searchTrips(@RequestParam String keyword, Model model) {
        List<Trip> trips = tripService.searchTripsByRoute(keyword);
        model.addAttribute("trips", trips);
        return "admin/trip/search";
    }

    @GetMapping("list")
    @ResponseBody
    public Object list(Model model,
                       @RequestParam(defaultValue = "0  ") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        return tripService.paging(page, perpage);
    }
    @GetMapping("view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        model.addAttribute("page", tripService.paging(page, perpage));
        model.addAttribute("path", "/admin/trip/view");
        return "admin/trip/view";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("trip", new TripCreateDto());
        model.addAttribute("trains", trainService.findAll());
        model.addAttribute("routes", routeService.findAll());
        model.addAttribute("actionUrl", "/admin/trip/save");

        return "admin/trip/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("trip") @Valid TripCreateDto dto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("trains", trainService.findAll());
            model.addAttribute("routes", routeService.findAll());
            return "admin/trip/form";
        }

        try {
            tripService.createTrip(dto);
            redirectAttributes.addFlashAttribute("message", "Tạo chuyến đi thành công");
            return "redirect:/admin/trip/view";
        } catch (Exception e) {
            bindingResult.reject("error", e.getMessage());
            model.addAttribute("trains", trainService.findAll());
            model.addAttribute("routes", routeService.findAll());
            return "admin/trip/form";
        }
    }

    @GetMapping("/update")
    public String showUpdateForm(@RequestParam Long id, Model model) {
        TripUpdateDto dto = tripService.findById(id);
        model.addAttribute("trip", dto);
        model.addAttribute("trains", trainService.findAll());
        model.addAttribute("routes", routeService.findAll());
        model.addAttribute("actionUrl", "/admin/trip/update");
        return "admin/trip/update";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("trip") @Valid TripUpdateDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("trains", trainService.findAll());
            model.addAttribute("routes", routeService.findAll());
            return "admin/trip/update";
        }

        try {
            tripService.updateTrip(dto);
            redirectAttributes.addFlashAttribute("message", "Cập nhật chuyến đi thành công");
            return "redirect:/admin/trip/view";
        } catch (Exception e) {
            bindingResult.reject("error", e.getMessage());
            model.addAttribute("trains", trainService.findAll());
            model.addAttribute("routes", routeService.findAll());
            return "admin/trip/update";
        }
    }

    @GetMapping("/delete")
    public String deleteTrip(@RequestParam("id") Long id,
                             @RequestParam(name = "page", defaultValue = "0") Integer page,
                             RedirectAttributes redirectAttributes) {
        try {
            tripService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa chuyến đi thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/admin/trip/view?page=" + page;
    }


}
