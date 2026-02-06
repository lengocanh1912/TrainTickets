package t3h.edu.vn.traintickets.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.dto.TicketPdfDto;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.entities.OrderTicket;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.repository.TicketRepository;
import java.awt.Color;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TicketPdfService {

    @Value("${qr.secret}")
    private String secretKey;
    @Value("${qr.url}")
    private String url;

    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    private static final NumberFormat VND_FORMAT =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    public byte[] generateTicketPdf(Long ticketId) throws Exception {

        TicketPdfDto ticket = ticketRepository
                .findTicketPdfDto(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A6.rotate(), 12, 12, 12, 12);
        PdfWriter.getInstance(document, out);
        document.open();

        // ===== COLORS =====
        Color ORANGE = new Color(255, 140, 0);
        Color DARK = new Color(60, 60, 60);
        Color GRAY = new Color(120, 120, 120);
        Color LIGHT_GRAY = new Color(245, 245, 245);

        // ===== FONTS =====
        Font headerFont = new Font(
                Font.HELVETICA,
                14,
                Font.BOLD,
                Color.WHITE
        );

        Font routeFont = new Font(
                Font.HELVETICA,
                13,
                Font.BOLD,
                DARK
        );

        Font timeFont = new Font(
                Font.HELVETICA,
                11,
                Font.BOLD,
                DARK
        );

        Font labelFont = new Font(
                Font.HELVETICA,
                9,
                Font.BOLD,
                GRAY
        );
        Font valueFont = new Font(
                Font.HELVETICA,
                9,
                Font.NORMAL,
                DARK
        );

        Font smallFont = new Font(
                Font.HELVETICA,
                8,
                Font.NORMAL,
                GRAY
        );


        // ===== HEADER =====
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell hCell = new PdfPCell(new Phrase("TRAIN TICKET", headerFont));
        hCell.setBackgroundColor(ORANGE);
        hCell.setBorder(Rectangle.NO_BORDER);
        hCell.setPadding(8);
        hCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        header.addCell(hCell);
        document.add(header);

        // ===== ROUTE =====
        Paragraph route = new Paragraph(
                ticket.departureStation() + "  →  " + ticket.arrivalStation(),
                routeFont
        );
        route.setSpacingBefore(6);
        route.setAlignment(Element.ALIGN_CENTER);
        document.add(route);

        String timeLine =
                ticket.departureAt().format(DateTimeFormatter.ofPattern("HH:mm"))
                        + " → "
                        + ticket.arrivalAt().format(DateTimeFormatter.ofPattern("HH:mm"))
                        + " | "
                        + ticket.departureAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Paragraph time = new Paragraph(timeLine, timeFont);
        time.setAlignment(Element.ALIGN_CENTER);
        time.setSpacingAfter(8);
        document.add(time);

        // ===== MAIN CONTENT =====
        PdfPTable main = new PdfPTable(new float[]{3, 2});
        main.setWidthPercentage(100);

        // ===== LEFT INFO =====
        PdfPCell info = new PdfPCell();
        info.setBorder(Rectangle.NO_BORDER);
        info.setPadding(6);

        info.addElement(infoLine("Hành khách", ticket.passengerName(), labelFont, valueFont));
        info.addElement(infoLine("Căn cước công dân:", ticket.cccd(), labelFont, valueFont));
        info.addElement(infoLine("Ngày sinh:", ticket.birthday().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), labelFont, valueFont));
        info.addElement(infoLine("Loại vé", getTicketTypeName(ticket.ticketType()), labelFont, valueFont));

        info.addElement(infoLine("Tàu", ticket.trainName(), labelFont, valueFont));
        info.addElement(infoLine(
                "Chỗ",
                "Toa " + ticket.coachCode() + " - Ghế " + ticket.seatCode(),
                labelFont, valueFont
        ));
        info.addElement(infoLine(
                "Giá",
                VND_FORMAT.format(ticket.price()) + " VND",
                labelFont, valueFont
        ));

        // ===== QR =====
        String payload = ticket.ticketId() + "|" +
                hmacSHA256(ticket.ticketId() + ":" + ticket.ticketCode(), secretKey);

        String verifyUrl = url + URLEncoder.encode(payload, StandardCharsets.UTF_8);
        byte[] qrBytes = qrCodeService.generateQrBytes(verifyUrl, 120);
        Image qr = Image.getInstance(qrBytes);
        qr.scaleToFit(120, 120);

        PdfPCell qrCell = new PdfPCell(qr);
        qrCell.setBorder(Rectangle.NO_BORDER);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        qrCell.setBackgroundColor(LIGHT_GRAY);

        main.addCell(info);
        main.addCell(qrCell);

        document.add(main);

        // ===== FOOTER =====
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(6);

        PdfPCell fCell = new PdfPCell(new Phrase(
                "Mã vé: " + ticket.ticketCode() + "\nVui lòng có mặt trước 15 phút",
                smallFont
        ));
        fCell.setBorder(Rectangle.TOP);
        fCell.setPaddingTop(4);
        fCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        footer.addCell(fCell);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private Paragraph infoLine(
            String label,
            String value,
            Font labelFont,
            Font valueFont
    ) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", labelFont));
        p.add(new Chunk(value != null ? value : "-", valueFont));
        p.setSpacingAfter(4);
        return p;
    }


    private String getTicketTypeName(Byte type) {
        return switch (type) {
            case 0 -> "Người lớn";
            case 1 -> "Trẻ em";
            case 2 -> "Sinh viên";
            case 3 -> "Người cao tuổi";
            default -> "Khác";
        };
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            mac.init(secretKeySpec);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new RuntimeException("QR signature error", e);
        }
    }
}


