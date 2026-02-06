package t3h.edu.vn.traintickets.controller.verify;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import t3h.edu.vn.traintickets.service.QrCodeService;

@Controller
@RequiredArgsConstructor
public class QrVerifyController {

    private final QrCodeService qrCodeService;

    @GetMapping("/verify")
    public String verifyQr(
            @RequestParam String data,
            Model model
    ) {
        System.out.println("✅ Đã lấy đc verify url ");
        var result = qrCodeService.verify(data);

        model.addAttribute("valid", result.valid());
        model.addAttribute("message", result.message());
        model.addAttribute("ticket", result.ticket());

        return "qr/verify";
    }
}

