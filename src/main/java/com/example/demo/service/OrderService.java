//package com.example.demo.service;
//
//import org.apache.pdfbox.pdmodel.PDDocument;
//import com.example.demo.model.Order;
//import com.example.demo.repository.OrderRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.OutputStream;
//import java.io.IOException;
//import java.nio.file.*;
//import java.util.*;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//@Service
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//
//    @Value("${file.upload-dir:uploads}")
//    private String uploadDir;
//
//    // Allowed MIME types
//    private static final Set<String> ALLOWED_TYPES = Set.of(
//        "application/pdf",
//        "application/msword",
//        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
//        "image/jpeg",
//        "image/png"
//    );
//
//    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
//
//    public OrderService(OrderRepository orderRepository) {
//        this.orderRepository = orderRepository;
//    }
//
//    public Order createOrder(
//            String phone,
//            String contentType,
//            String printType,
//            int copies,
//            String paperSize,
//            String binding,
//            String description,
//            List<MultipartFile> files) throws IOException {
//
//        // Validate phone
//        if (phone == null || !phone.matches("\\d{10}")) {
//            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
//        }
//
//        if (files == null || files.isEmpty()) {
//            throw new IllegalArgumentException("At least one file is required.");
//        }
//
//        if (copies < 1) {
//            throw new IllegalArgumentException("Copies must be at least 1.");
//        }
//
//        Path uploadPath = Paths.get(uploadDir);
//        Files.createDirectories(uploadPath);
//
//        List<String> savedFileNames = new ArrayList<>();
//        int totalPages = 0;
//
//        for (MultipartFile file : files) {
//
//            if (file.getSize() > MAX_FILE_SIZE) {
//                throw new IllegalArgumentException("File too large");
//            }
//
//            String originalName = file.getOriginalFilename().toLowerCase();
//            String uniqueName = UUID.randomUUID() + "_" + originalName;
//            Path filePath = uploadPath.resolve(uniqueName);
//
//            // Save original temporarily
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//            Path finalPdfPath;
//
//            // ✅ If already PDF
//            if (originalName.endsWith(".pdf")) {
//
//                finalPdfPath = filePath;
//
//            }
//            // ✅ If DOCX → Convert to PDF
//            else if (originalName.endsWith(".docx")) {
//
//                try {
//                    String pdfFileName = uniqueName.replace(".docx", ".pdf");
//                    finalPdfPath = uploadPath.resolve(pdfFileName);
//
//                    WordprocessingMLPackage wordMLPackage =
//                            WordprocessingMLPackage.load(filePath.toFile());
//
//                    try (OutputStream os = Files.newOutputStream(finalPdfPath)) {
//                        Docx4J.toPDF(wordMLPackage, os);
//                    }
//
//                    // Delete original DOCX after conversion
//                    Files.delete(filePath);
//
//                } catch (Exception e) {
//                    throw new RuntimeException("DOCX conversion failed", e);
//                }
//
//            }
//            // ✅ Images → Convert to PDF (1 page)
//            else if (originalName.endsWith(".jpg") ||
//                     originalName.endsWith(".jpeg") ||
//                     originalName.endsWith(".png")) {
//
//                try {
//                    String pdfFileName = uniqueName + ".pdf";
//                    finalPdfPath = uploadPath.resolve(pdfFileName);
//
//                    PDDocument document = new PDDocument();
//                    PDDocument pdf = new PDDocument();
//                    PDDocument imgDoc = new PDDocument();
//
//                    // Simple 1-page PDF (basic)
//                    PDDocument imagePdf = new PDDocument();
//                    imagePdf.addPage(new org.apache.pdfbox.pdmodel.PDPage());
//                    imagePdf.save(finalPdfPath.toFile());
//                    imagePdf.close();
//
//                    Files.delete(filePath);
//
//                } catch (Exception e) {
//                    throw new RuntimeException("Image conversion failed", e);
//                }
//
//            }
//            else {
//                throw new IllegalArgumentException("Unsupported file type");
//            }
//
//            // ✅ Count pages from final PDF
//            try (PDDocument document = PDDocument.load(finalPdfPath.toFile())) {
//                totalPages += document.getNumberOfPages();
//            }
//
//            savedFileNames.add(finalPdfPath.getFileName().toString());
//        }
//
//        // ✅ Pricing logic
//        int basePrice = "color".equals(printType) ? 8 : 1;
//        int bindingPrice = switch (binding) {
//            case "spiral" -> 30;
//            case "calico" -> 25;
//            case "hole" -> 5;
//            default -> 0;
//        };
//
//        int totalPrice = basePrice * copies * totalPages + bindingPrice;
//
//        // ✅ Save order
//        Order order = new Order();
//        order.setPhone(phone);
//        order.setContentType("pdf"); // everything now stored as PDF
//        order.setPrintType(printType);
//        order.setCopies(copies);
//        order.setPaperSize(paperSize);
//        order.setBinding(binding);
//        order.setDescription(description);
//        order.setFileNames(String.join(",", savedFileNames));
//        order.setTotalPrice(totalPrice);
//
//        return orderRepository.save(order);
//    }
//    public Order getOrderById(Long id) {
//        return orderRepository.findById(id)
//                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
//    }
//
//    public List<Order> getAllOrders() {
//        return orderRepository.findAll();
//    }
//
//    public List<Order> getOrdersByPhone(String phone) {
//        return orderRepository.findByPhone(phone);
//    }
//
//    public Order updateStatus(Long id, String status) {
//        Order order = getOrderById(id);
//        order.setStatus(status);
//        return orderRepository.save(order);
//    }
//}


package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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

    @Value("${libreoffice.path}")
    private String libreOfficePath;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    
    
    public List<Order> trackOrder(String phone) {
        return orderRepository.findByPhone(phone);
    }

    public Order createOrder(
            String phone,
            String contentType,
            String printType,
            int copies,
            String paperSize,
            String binding,
            String description,
            List<MultipartFile> files) throws Exception {

        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required.");
        }

        if (copies < 1) {
            throw new IllegalArgumentException("Copies must be at least 1.");
        }

        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        List<String> savedFileNames = new ArrayList<>();
        int totalPages = 0;

        for (MultipartFile file : files) {

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File too large");
            }

            String originalName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
            String uniqueName = UUID.randomUUID() + "_" + originalName;
            Path filePath = uploadPath.resolve(uniqueName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Path finalPdfPath;

            // ✅ If already PDF
            if (originalName.endsWith(".pdf")) {
                finalPdfPath = filePath;
            }

            // ✅ DOCX → PDF using LibreOffice
            else if (originalName.endsWith(".docx")) {

                String pdfFileName = uniqueName.replace(".docx", ".pdf");
                finalPdfPath = uploadPath.resolve(pdfFileName);

                ProcessBuilder processBuilder = new ProcessBuilder(
                        libreOfficePath,
                        "--headless",
                        "--convert-to", "pdf",
                        filePath.toAbsolutePath().toString(),
                        "--outdir", uploadPath.toAbsolutePath().toString()
                );

                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("LibreOffice conversion failed");
                }

                Files.delete(filePath);
            }

            // ✅ Image → PDF
            else if (originalName.endsWith(".jpg") ||
                     originalName.endsWith(".jpeg") ||
                     originalName.endsWith(".png")) {

                String pdfFileName = uniqueName + ".pdf";
                finalPdfPath = uploadPath.resolve(pdfFileName);

                try (PDDocument document = new PDDocument()) {

                    PDPage page = new PDPage();
                    document.addPage(page);

                    PDImageXObject image = PDImageXObject.createFromFile(
                            filePath.toAbsolutePath().toString(),
                            document
                    );

                    try (PDPageContentStream contentStream =
                                 new PDPageContentStream(document, page)) {

                        float pageWidth = page.getMediaBox().getWidth();
                        float pageHeight = page.getMediaBox().getHeight();

                        contentStream.drawImage(image, 0, 0, pageWidth, pageHeight);
                    }

                    document.save(finalPdfPath.toFile());
                }

                Files.delete(filePath);
            }

            else {
                Files.delete(filePath);
                throw new IllegalArgumentException("Unsupported file type");
            }

            // ✅ Count pages
            try (PDDocument document = PDDocument.load(finalPdfPath.toFile())) {
                totalPages += document.getNumberOfPages();
            }

            savedFileNames.add(finalPdfPath.getFileName().toString());
        }

        // ✅ Pricing
        int basePrice = "color".equalsIgnoreCase(printType) ? 8 : 1;

        int bindingPrice = switch (binding) {
            case "spiral" -> 30;
            case "calico" -> 25;
            case "hole" -> 5;
            default -> 0;
        };

        int totalPrice = basePrice * copies * totalPages + bindingPrice;

        // ✅ Save Order
        Order order = new Order();
        order.setPhone(phone);
        order.setContentType("pdf");
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

