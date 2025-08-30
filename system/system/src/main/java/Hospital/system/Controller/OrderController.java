package Hospital.system.Controller;

import Hospital.system.DTO.OrderDto;
import Hospital.system.Entity.OrderStatus;
import Hospital.system.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('PATIENT', 'PHARMACY')")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/patient")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderDto));
    }

    @GetMapping("/patient")
    public ResponseEntity<List<OrderDto>> getPatientOrders(){
        return ResponseEntity.ok(orderService.getPatientOrders());
    }

    @GetMapping("/patient/status/{status}")
    public ResponseEntity<List<OrderDto>> getPatientOrdersByStatus(@PathVariable OrderStatus status){
        return ResponseEntity.ok(orderService.getPatientOrdersByStatus(status));
    }

    @GetMapping("/pharmacy")
    public ResponseEntity<List<OrderDto>> getPharmacyOrders(){
        return ResponseEntity.ok(orderService.getPharmacyOrders());
    }

    @GetMapping("/pharmacy/status/{status}")
    public ResponseEntity<List<OrderDto>> getPharmacyOrdersByStatus(@PathVariable OrderStatus status){
        return ResponseEntity.ok(orderService.getPharmacyOrdersByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderDto updateDto){
        return ResponseEntity.ok(orderService.updateOrder(id, updateDto));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<OrderDto> shipOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.shipOrder(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<OrderDto> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.completeOrder(id));
    }

}
