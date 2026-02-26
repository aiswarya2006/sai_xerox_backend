package com.example.demo.controller;

import com.example.demo.model.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173") // React dev server port
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/orders
     * Called by React handleSubmit with multipart/form-data
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestParam("phone")        String phone,
            @RequestParam("contentType")  String contentType,
            @RequestParam("printType")    String printType,
            @RequestParam("copies")       int copies,
            @RequestParam("paperSize")    String paperSize,
            @RequestParam("binding")      String binding,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam("files")        List<MultipartFile> files) {

        try {
            Order order = orderService.createOrder(
                    phone, contentType, printType,
                    copies, paperSize, binding, description, files);

            return ResponseEntity.ok(Map.of(
                "orderId",    order.getId(),
                "totalPrice", order.getTotalPrice(),
                "status",     order.getStatus(),
                "message",    "Order placed successfully"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "File upload failed. Please try again."));
        }
    }

    /**
     * GET /api/orders
     * Fetch all orders (for shop owner/admin view)
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * GET /api/orders/{id}
     * Fetch a single order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/orders/phone/{phone}
     * Fetch orders by customer phone number
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<List<Order>> getOrdersByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(orderService.getOrdersByPhone(phone));
    }

//    @GetMapping("/file/{filename}")
//    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws IOException {
//
//        Path path = Paths.get("uploads").resolve(filename);
//        Resource resource = new UrlResource(path.toUri());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "inline; filename=\"" + filename + "\"")
//                .body(resource);
//    }
    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws Exception {

        Path path = Paths.get("uploads").resolve(filename);
        Resource resource = new UrlResource(path.toUri());

        // Detect file type automatically
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }
    /**
     * PATCH /api/orders/{id}/status
     * Update order status: PENDING → PAID → COMPLETED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            Order updated = orderService.updateStatus(id, body.get("status"));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
        
    }
}