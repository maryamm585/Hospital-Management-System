package Hospital.system.Service;


import Hospital.system.DTO.OrderDto;
import Hospital.system.DTO.OrderItemDto;
import Hospital.system.Entity.*;
import Hospital.system.Repository.MedicineRepository;
import Hospital.system.Repository.OrderRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.Service.OrderService;
import Hospital.system.exception.AccessDeniedException;
import Hospital.system.exception.ResourceNotFoundException;
import Hospital.system.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderDto orderDto;
    private Order order;
    private User patient;
    private User pharmacy;
    private Medicine medicine;
    private OrderItemDto orderItemDto;

    @BeforeEach
    void setUp() {
        patient = User.builder()
                .id(1L)
                .name("Patient")
                .email("patient@test.com")
                .role(Role.PATIENT)
                .build();

        pharmacy = User.builder()
                .id(2L)
                .name("Pharmacy")
                .email("pharmacy@test.com")
                .role(Role.PHARMACY)
                .build();

        medicine = Medicine.builder()
                .id(1L)
                .name("Aspirin")
                .price(10.0)
                .stock(100)
                .pharmacy(pharmacy)
                .build();

        orderItemDto = new OrderItemDto();
        orderItemDto.setMedicineName("Aspirin");
        orderItemDto.setQuantity(2);
        orderItemDto.setPrice(20.0);

        orderDto = new OrderDto();
        orderDto.setPatientId(1L);
        orderDto.setPharmacyId(2L);
        orderDto.setTotalPrice(20.0);
        orderDto.setItems(Arrays.asList(orderItemDto));

        order = Order.builder()
                .id(1L)
                .patient(patient)
                .pharmacy(pharmacy)
                .totalPrice(20.0)
                .status(OrderStatus.PLACED)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void createOrder_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.PHARMACY)).thenReturn(Optional.of(pharmacy));
        when(medicineRepository.findByName("Aspirin")).thenReturn(Optional.of(medicine));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDto result = orderService.createOrder(orderDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals(2L, result.getPharmacyId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_InsufficientStock_ThrowsBadRequestException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        medicine.setStock(1); // Less than required quantity
        orderItemDto.setQuantity(5);

        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.PHARMACY)).thenReturn(Optional.of(pharmacy));
        when(medicineRepository.findByName("Aspirin")).thenReturn(Optional.of(medicine));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.createOrder(orderDto)
        );
        assertEquals("Not enough stock for Aspirin", exception.getMessage());
    }

    @Test
    void getPatientOrders_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(orderRepository.findByPatient_Id(1L)).thenReturn(Arrays.asList(order));

        // Act
        List<OrderDto> result = orderService.getPatientOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shipOrder_Success() {
        // Arrange
        mockSecurityContext("pharmacy@test.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("pharmacy@test.com")).thenReturn(Optional.of(pharmacy));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDto result = orderService.shipOrder(1L);

        // Assert
        assertNotNull(result);
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void cancelOrder_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.cancelOrder(1L);

        // Assert
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createOrder_PatientIdAutoSet() {
        // Arrange
        orderDto.setPatientId(null); // Patient ID not set
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.PHARMACY)).thenReturn(Optional.of(pharmacy));
        when(medicineRepository.findByName("Aspirin")).thenReturn(Optional.of(medicine));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDto result = orderService.createOrder(orderDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
    }

    @Test
    void createOrder_WrongPatientId_ThrowsAccessDeniedException() {
        // Arrange
        orderDto.setPatientId(99L); // Different patient ID
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> orderService.createOrder(orderDto)
        );
        assertEquals("You can't place order with another patient's Id", exception.getMessage());
    }

    @Test
    void createOrder_PharmacyNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.PHARMACY)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(orderDto)
        );
        assertEquals("No Pharmacy with id 2", exception.getMessage());
    }

    @Test
    void createOrder_MedicineNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.PHARMACY)).thenReturn(Optional.of(pharmacy));
        when(medicineRepository.findByName("Aspirin")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(orderDto)
        );
        assertEquals("Medicine not found: Aspirin", exception.getMessage());
    }
    @Test
    void createOrder_MedicineFromDifferentPharmacy_ThrowsBadRequestException() {
        // Arrange
        User otherPharmacy = User.builder().id(99L).role(Role.PHARMACY).build();
        medicine.setPharmacy(otherPharmacy);

        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(userRepository.findByIdAndRole(2L, Role.PHARMACY)).thenReturn(Optional.of(pharmacy));
        when(medicineRepository.findByName("Aspirin")).thenReturn(Optional.of(medicine));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.createOrder(orderDto)
        );
        assertEquals("Medicine 1 is not available in pharmacy 2", exception.getMessage());
    }


    @Test
    void getPatientOrdersByStatus_Success() {
        // Arrange
        mockSecurityContext("patient@test.com");
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(orderRepository.findByPatient_IdAndStatus(1L, OrderStatus.PLACED)).thenReturn(Arrays.asList(order));

        // Act
        List<OrderDto> result = orderService.getPatientOrdersByStatus(OrderStatus.PLACED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PLACED", result.get(0).getStatus());
    }

    @Test
    void getPharmacyOrders_Success() {
        // Arrange
        mockSecurityContext("pharmacy@test.com");
        when(userRepository.findByEmail("pharmacy@test.com")).thenReturn(Optional.of(pharmacy));
        when(orderRepository.findByPharmacy_Id(2L)).thenReturn(Arrays.asList(order));

        // Act
        List<OrderDto> result = orderService.getPharmacyOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getPharmacyId());
    }
    @Test
    void shipOrder_WrongPharmacy_ThrowsAccessDeniedException() {
        // Arrange
        mockSecurityContext("otherpharmacy@test.com");
        User otherPharmacy = User.builder().id(99L).email("otherpharmacy@test.com").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("otherpharmacy@test.com")).thenReturn(Optional.of(otherPharmacy));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> orderService.shipOrder(1L)
        );
        assertEquals("You can't ship this order", exception.getMessage());
    }

    @Test
    void shipOrder_OrderNotPlaced_ThrowsBadRequestException() {
        // Arrange
        order.setStatus(OrderStatus.SHIPPED);
        mockSecurityContext("pharmacy@test.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("pharmacy@test.com")).thenReturn(Optional.of(pharmacy));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.shipOrder(1L)
        );
        assertEquals("You can't ship order with status SHIPPED", exception.getMessage());
    }
    @Test
    void cancelOrder_OrderNotPlaced_ThrowsBadRequestException() {
        // Arrange
        order.setStatus(OrderStatus.SHIPPED);
        mockSecurityContext("patient@test.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.cancelOrder(1L)
        );
        assertEquals("You can't cancel order with status SHIPPED", exception.getMessage());
    }

    @Test
    void completeOrder_Success() {
        // Arrange
        order.setStatus(OrderStatus.SHIPPED);
        mockSecurityContext("pharmacy@test.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("pharmacy@test.com")).thenReturn(Optional.of(pharmacy));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDto result = orderService.completeOrder(1L);

        // Assert
        assertNotNull(result);
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void completeOrder_OrderNotShipped_ThrowsBadRequestException() {
        // Arrange - Order is still in PLACED status
        mockSecurityContext("pharmacy@test.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("pharmacy@test.com")).thenReturn(Optional.of(pharmacy));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.completeOrder(1L)
        );
        assertEquals("You can't complete order with status PLACED", exception.getMessage());
    }

    @Test
    void updateOrder_Success() {
        // Arrange
        OrderItemDto newItem = new OrderItemDto();
        newItem.setMedicineName("Aspirin");
        newItem.setQuantity(3);
        newItem.setPrice(30.0);

        OrderDto updateDto = new OrderDto();
        updateDto.setPatientId(1L);
        updateDto.setPharmacyId(2L);
        updateDto.setItems(Arrays.asList(newItem));

        mockSecurityContext("patient@test.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(medicineRepository.findByName("Aspirin")).thenReturn(Optional.of(medicine));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDto result = orderService.updateOrder(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrder_ChangeIds_ThrowsBadRequestException() {
        // Arrange
        OrderDto updateDto = new OrderDto();
        updateDto.setPatientId(99L);
        updateDto.setPharmacyId(2L);
        updateDto.setItems(Arrays.asList(orderItemDto));

        mockSecurityContext("patient@test.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.updateOrder(1L, updateDto)
        );
        assertEquals("You can't change id fields", exception.getMessage());
    }
}