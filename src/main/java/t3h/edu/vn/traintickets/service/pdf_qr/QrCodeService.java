package t3h.edu.vn.traintickets.service.pdf_qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.event.QrVerifyResult;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.repository.TicketRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final  TicketRepository ticketRepository;

    @Value("${qr.secret}")
    private String secretKey;


    public BufferedImage generateQRCode(String content, int size) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size);

        BufferedImage image =
                new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                image.setRGB(
                        x, y,
                        bitMatrix.get(x, y)
                                ? Color.BLACK.getRGB()
                                : Color.WHITE.getRGB()
                );
            }
        }
        return image;
    }

    public byte[] generateQrBytes(String content, int size) throws Exception {
        BufferedImage image = generateQRCode(content, size);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return out.toByteArray();
    }

    public QrVerifyResult verify(String data) {

        try {

            // 1️⃣ Validate format QR
            if (data == null || !data.contains("|")) {
                return new QrVerifyResult(false, "QR không hợp lệ", null);
            }

            String[] parts = data.split("\\|");

            if (parts.length != 2) {
                return new QrVerifyResult(false, "QR không hợp lệ", null);
            }

            Long ticketId = Long.valueOf(parts[0]);
            String signature = parts[1];

            // 2️⃣ Lấy vé
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

            if (ticket == null) {
                return new QrVerifyResult(false, "Không tìm thấy vé", null);
            }

            // 3️⃣ Verify chữ ký QR
            String expected = hmacSHA256(
                    ticketId + ":" + ticket.getTicketCode(),
                    secretKey
            );

            if (!expected.equalsIgnoreCase(signature)) {
                return new QrVerifyResult(false, "QR bị sửa hoặc giả mạo", null);
            }

            // 4️⃣ Check trạng thái vé
            if (ticket.getStatus() == TicketStatus.USED) {
                return new QrVerifyResult(false, "Vé đã được sử dụng", ticket);
            }

            if (ticket.getStatus() != TicketStatus.PAID) {
                return new QrVerifyResult(false, "Vé chưa hợp lệ để sử dụng", ticket);
            }

            // 5️⃣ Đánh dấu vé đã sử dụng
            ticket.setStatus(TicketStatus.USED);
            ticketRepository.save(ticket);

            System.out.println("✅ Hành khách đã quét vé");

            return new QrVerifyResult(true, "Vé hợp lệ", ticket);

        } catch (Exception e) {
            return new QrVerifyResult(false, "Lỗi xử lý QR", null);
        }
    }
    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
            return HexFormat.of().formatHex(
                    mac.doFinal(data.getBytes())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

