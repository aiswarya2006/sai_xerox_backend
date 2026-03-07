package com.example.demo.repository;

import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPhone(String phone);
   // List<Order> findByStatus(String status);
}
// http://localhost:8080/api/orders/track-order?phone=9360667080