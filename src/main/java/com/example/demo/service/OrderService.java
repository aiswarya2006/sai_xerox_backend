package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // Allowed MIME types
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "image/jpeg",
        "image/png"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(
            String phone,
            String contentType,
            String printType,
            int copies,
            String paperSize,
            String binding,
            String description,
            List<MultipartFile> files) throws IOException {

        // Validate phone
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        }

        // Validate files
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required.");
        }

        // Validate copies
        if (copies < 1) {
            throw new IllegalArgumentException("Copies must be at least 1.");
        }

        // Save files to disk
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        List<String> savedFileNames = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException(
                    "File '" + file.getOriginalFilename() + "' exceeds 10 MB limit.");
            }
            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                throw new IllegalArgumentException(
                    "File type not allowed: " + file.getContentType());
            }

            String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(uniqueName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            savedFileNames.add(uniqueName);
        }

        // Calculate price (same logic as your React frontend)
        int basePrice = "color".equals(printType) ? 8 : 1;
        int bindingPrice = switch (binding) {
            case "spiral" -> 30;
            case "calico" -> 25;
            case "hole"   -> 5;
            default       -> 0;
        };
        int totalPrice = basePrice * copies * files.size() + bindingPrice;

        // Build Order entity
        Order order = new Order();
        order.setPhone(phone);
        order.setContentType(contentType);
        order.setPrintType(printType);
        order.setCopies(copies);
        order.setPaperSize(paperSize);
        order.setBinding(binding);
        order.setDescription(description);
        order.setFileNames(String.join(",", savedFileNames));
        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByPhone(String phone) {
        return orderRepository.findByPhone(phone);
    }

    public Order updateStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}