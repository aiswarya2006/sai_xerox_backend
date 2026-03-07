

package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.OrderService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RequestHeader;
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    
    @GetMapping("/track-order")
    public ResponseEntity<?> trackOrder(@RequestParam String phone) {

        List<Order> orders = orderService.trackOrder(phone);

        if (orders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(orders);
    }

    /**
     * CREATE ORDER
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestParam("phone") String phone,
            @RequestParam("contentType") String contentType,
            @RequestParam("printType") String printType,
            @RequestParam("copies") int copies,
            @RequestParam("paperSize") String paperSize,
            @RequestParam("binding") String binding,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam("files") List<MultipartFile> files) {

        try {

            Order order = orderService.createOrder(
                    phone,
                    contentType,
                    printType,
                    copies,
                    paperSize,
                    binding,
                    description,
                    files
            );

            return ResponseEntity.ok(Map.of(
                    "orderId", order.getId(),
                    "totalPrice", order.getTotalPrice(),
                    "status", order.getStatus(),
                    "message", "Order placed successfully"
            ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    /**
     * GET ALL ORDERS
     * GET /api/orders
     */
//    @GetMapping
//    public ResponseEntity<List<Order>> getAllOrders() {
//        return ResponseEntity.ok(orderService.getAllOrders());
//    }
    
    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestHeader(value = "Authorization", required = false) String token) {

        if (token == null || !JwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing token");
        }

        return ResponseEntity.ok(orderService.getAllOrders());
    }
    /**
     * GET ORDER BY ID
     * GET /api/orders/{id}
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
     * GET ORDERS BY PHONE
     * GET /api/orders/phone/{phone}
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<List<Order>> getOrdersByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(orderService.getOrdersByPhone(phone));
    }

    /**
     * DOWNLOAD / VIEW FILE
     * GET /api/orders/file/{filename}
     */
//    @GetMapping("/file/{filename}")
//    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws Exception {
//
//        Path path = Paths.get("uploads").resolve(filename);
//        Resource resource = new UrlResource(path.toUri());
//
//        if (!resource.exists()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        String contentType = Files.probeContentType(path);
//        if (contentType == null) {
//            contentType = "application/octet-stream";
//        }
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "inline; filename=\"" + filename + "\"")
//                .header(HttpHeaders.CONTENT_TYPE, contentType)
//                .body(resource);
//    }
    @GetMapping("/file/{filename}")
    public ResponseEntity<?> getFile(
            @PathVariable String filename,
            @RequestHeader(value = "Authorization", required = false) String token) throws Exception {

        // 🔒 Check JWT token
        if (token == null || !JwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing token");
        }

        Path path = Paths.get("uploads").resolve(filename);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

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
   
//    @GetMapping("/api/orders")
//    public ResponseEntity<?> getOrders(@RequestHeader("Authorization") String token) {
//
//        if (!"admin-session".equals(token)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
//        }
//
//        return ResponseEntity.ok(orderService.getAllOrders());
//    }
//    @GetMapping("/api/orders")
//    public ResponseEntity<?> getOrders(@RequestHeader("Authorization") String token) {
//
//        if (!JwtUtil.validateToken(token)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
//        }
//
//        return ResponseEntity.ok(orderService.getAllOrders());
//    }
    @GetMapping("/api/orders")
    public ResponseEntity<?> getOrders(@RequestHeader(value = "Authorization", required = false) String token) {

        if (token == null || !JwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        return ResponseEntity.ok(orderService.getAllOrders());
    }
    /**
     * UPDATE ORDER STATUS
     * PATCH /api/orders/{id}/status
     */
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<?> updateStatus(
//            @PathVariable Long id,
//            @RequestBody Map<String, String> body) {
//
//        try {
//
//            Order updated = orderService.updateStatus(id, body.get("status"));
//            return ResponseEntity.ok(updated);
//
//        } catch (Exception e) {
//
//            return ResponseEntity.notFound().build();
//        }
//    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (token == null || !JwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing token");
        }

        try {

            Order updated = orderService.updateStatus(id, body.get("status"));
            return ResponseEntity.ok(updated);

        } catch (Exception e) {

            return ResponseEntity.notFound().build();
        }
    }
}