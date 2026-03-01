package t3h.edu.vn.traintickets.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import t3h.edu.vn.traintickets.event.OrderPaidEvent;
import t3h.edu.vn.traintickets.service.pdf_qr.MailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidListener {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPaid(OrderPaidEvent event) {
        try {
            mailService.sendTicketsByOrder(
                    event.orderId(),
                    event.email()
            );
            log.info("✅ Mail sent for order {}", event.orderId());
        } catch (Exception e) {
            log.error("❌ Send mail failed for order {}", event.orderId(), e);
            // TODO: retry / lưu DB / notify admin
        }
    }
}

