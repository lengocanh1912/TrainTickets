package t3h.edu.vn.traintickets.controller.admin;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.CoachDto;
import t3h.edu.vn.traintickets.dto.TrainCreateDto;
import t3h.edu.vn.traintickets.dto.TrainUpdateDto;
import t3h.edu.vn.traintickets.entities.Train;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.enums.ReviewStatus;
import t3h.edu.vn.traintickets.enums.TrainState;
import t3h.edu.vn.traintickets.service.TrainService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/train")
public class AdminTrainController {

    @Autowired
    TrainService trainService;


    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "train");
    }


    @GetMapping("/list")
    @ResponseBody
    public Object list(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {

        return trainService.paging(page, perpage);
    }

    @GetMapping("/view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        model.addAttribute("page", trainService.paging(page, perpage));
        model.addAttribute("path", "/admin/train/view");
        model.addAttribute("trainStates", TrainState.class);

        return "admin/train/view";
    }

    @GetMapping("/create")
    public String showCreateTrainForm(Model model) {
        TrainCreateDto train = new TrainCreateDto();
        train.getCoaches().add(new CoachDto()); // để hiện form ban đầu
        model.addAttribute("train", train);
        return "admin/train/create";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        TrainUpdateDto trainDto = trainService.getTrainUpdateDtoById(id);
        System.out.println(">>> Train ID: " + id);

        trainDto.getCoaches()
                .sort(Comparator.comparing(CoachDto::getPosition));

        int totalSeats = trainDto.getCoaches().stream()
                .mapToInt(CoachDto::getCapacity)
                .sum();

        model.addAttribute("train", trainDto);
        model.addAttribute("totalSeats", totalSeats);
        return "admin/train/update";// hiển thị trang update
    }

    // cập nhật, chỉnh sửa ttin tàu
    @PostMapping("/update")
    public String updateTrain(@ModelAttribute TrainUpdateDto trainDto,
                              RedirectAttributes redirectAttributes) {
        try {
            trainService.updateTrain(trainDto); // bạn phải có hàm update trong service
            redirectAttributes.addFlashAttribute("message", "Cập nhật tàu thành công!");
            System.out.println("DTO ID: " + trainDto.getId());
            System.out.println("Name: " + trainDto.getName());
            System.out.println("Code: " + trainDto.getCode());
            System.out.println("Coaches: " + trainDto.getCoaches().size());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/admin/train/view"; // hoặc về danh sách
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("id") Long id,
                         @RequestParam(name = "page", defaultValue = "0") Integer page,
                         RedirectAttributes redirectAttributes) {
        try {
            trainService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa tàu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thất bại: " + e.getMessage());
        }
        // Redirect về trang hiện tại
        return "redirect:/admin/train/view?page=" + page;
    }

    @GetMapping("/detail")
    public String trainDetail (@RequestParam("id") Long id,
                             Model model) {
        Train train = trainService.findById(id);
        model.addAttribute("train", train);
        return "/admin/train/detail";
    }

    @GetMapping("/search")
    public String searchTrains(@RequestParam String keyword, Model model) {
        List<Train> trains = trainService.searchTrains(keyword);
        model.addAttribute("trains", trains);
        return "admin/train/search";
    }


}
