package t3h.edu.vn.traintickets.service.pdf_qr;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.event.TicketPdfDto;
import t3h.edu.vn.traintickets.repository.TicketRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TicketPdfService ticketPdfService;
    private final TicketRepository ticketRepository;

    public void sendTicketsByOrder(Long orderId, String email)
            throws Exception {

        List<Long> ticketIds =
                ticketRepository.findTicketIdsByOrderId(orderId);

        if (ticketIds.isEmpty()) {
            throw new IllegalStateException("Order không có vé");
        }

        TicketPdfDto firstTicket =
                ticketRepository.findTicketPdfDto(ticketIds.get(0))
                        .orElseThrow();

        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String subject = String.format(
                "Vé tàu điện tử: %s → %s | %s",
                firstTicket.departureStation(),
                firstTicket.arrivalStation(),
                firstTicket.departureAt().format(fmt)
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText("""
            Xin chào,

            Vé tàu điện tử của bạn được đính kèm trong email này.
            Vui lòng xuất trình vé khi lên tàu.

            Trân trọng.
            """);

        // 🔹 Đính kèm từng vé (PDF riêng)
        for (Long ticketId : ticketIds) {
            byte[] pdf =
                    ticketPdfService.generateTicketPdf(ticketId);
            TicketPdfDto ticket =
                    ticketRepository.findTicketPdfDto(ticketId)
                    .orElseThrow();

            helper.addAttachment(
                    "ticket-" +
                            "Tàu: " + ticket.trainName() + "|" +
                            "Toa: " + ticket.coachCode() + "-"+ ticket.seatCode() + ".pdf",
                    new ByteArrayResource(pdf)
            );
        }

        mailSender.send(message);
    }


    public void sendSimpleMail(String email, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(content, false);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Gửi email thất bại", e);
        }
    }

}


