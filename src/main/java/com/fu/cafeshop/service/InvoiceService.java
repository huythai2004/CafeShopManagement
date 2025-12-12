package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.Invoice;
import com.fu.cafeshop.entity.Order;
import com.fu.cafeshop.entity.User;
import com.fu.cafeshop.repository.InvoiceRepository;
import com.fu.cafeshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    private static final AtomicLong invoiceSequence = new AtomicLong(1);

    public Invoice getInvoiceByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId).orElse(null);
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));
    }

    @Transactional
    public Invoice createInvoice(Long orderId, User printedBy) {
        // Check if invoice already exists
        if (invoiceRepository.existsByOrderId(orderId)) {
            return invoiceRepository.findByOrderId(orderId).get();
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!"DONE".equals(order.getStatus())) {
            throw new RuntimeException("Cannot create invoice for incomplete order");
        }

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .order(order)
                .invoiceNumber(invoiceNumber)
                .printedByUser(printedBy)
                .printedAt(LocalDateTime.now())
                .build();

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice printInvoice(Long orderId, User printedBy) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId).orElse(null);
        
        if (invoice == null) {
            invoice = createInvoice(orderId, printedBy);
        } else {
            // Update print info
            invoice.setPrintedByUser(printedBy);
            invoice.setPrintedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
        }

        return invoice;
    }

    private String generateInvoiceNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = invoiceSequence.getAndIncrement();
        return String.format("INV-%s-%06d", datePrefix, seq);
    }

    public boolean hasInvoice(Long orderId) {
        return invoiceRepository.existsByOrderId(orderId);
    }
}

