package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    private String contentType;   // pdf | image
    private String printType;     // color | bw
    private int copies;
    private String paperSize;     // A4 | A3
    private String binding;       // none | spiral | calico | hole
    private String description;
    private int totalPrice;

    @Column(length = 2000)
    private String fileNames;     // comma-separated saved file names

    private String status;        // PENDING | PAID | COMPLETED

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        status = "PENDING";
    }

    // ─── Getters & Setters ───────────────────────────────────────────

    public Long getId() { return id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getPrintType() { return printType; }
    public void setPrintType(String printType) { this.printType = printType; }

    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }

    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }

    public String getBinding() { return binding; }
    public void setBinding(String binding) { this.binding = binding; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

    public String getFileNames() { return fileNames; }
    public void setFileNames(String fileNames) { this.fileNames = fileNames; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}