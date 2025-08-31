package Hospital.system.Controller;

import Hospital.system.DTO.OrderDto;
import Hospital.system.DTO.OrderItemDto;
import Hospital.system.Entity.OrderStatus;
import Hospital.system.Service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")

class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderDto sampleOrder;

    @BeforeEach
    void setUp() {
        OrderItemDto item = new OrderItemDto();
        item.setMedicineName("PANADOL");
        item.setQuantity(2);

        sampleOrder = new OrderDto();
        sampleOrder.setPatientId(10L);
        sampleOrder.setPharmacyId(20L);
        sampleOrder.setTotalPrice(50.0);
        sampleOrder.setStatus(OrderStatus.PLACED.name());
        sampleOrder.setItems(List.of(item));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        Mockito.when(orderService.createOrder(any(OrderDto.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/orders/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(OrderStatus.PLACED.name()));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatientOrdersByStatus_ShouldReturnList() throws Exception {
        Mockito.when(orderService.getPatientOrdersByStatus(eq(OrderStatus.PLACED)))
                .thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders/patient/status/PLACED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(OrderStatus.PLACED.name()));
    }

    @Test
    @WithMockUser(roles = "PHARMACY")
    void getPharmacyOrdersByStatus_ShouldReturnList() throws Exception {
        Mockito.when(orderService.getPharmacyOrdersByStatus(eq(OrderStatus.SHIPPED)))
                .thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders/pharmacy/status/SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(OrderStatus.PLACED.name()));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void cancelOrder_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/orders/{id}/cancel", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(orderService).cancelOrder(1L);
    }

    @Test
    @WithMockUser(roles = "PHARMACY")
    void shipOrder_ShouldReturnUpdatedOrder() throws Exception {
        sampleOrder.setStatus(OrderStatus.SHIPPED.name());
        Mockito.when(orderService.shipOrder(1L)).thenReturn(sampleOrder);

        mockMvc.perform(patch("/api/orders/{id}/ship", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.SHIPPED.name()));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testCreateOrder() throws Exception {
        // نخلي الـ service يرجّع نفس الـ sampleOrder
        Mockito.when(orderService.createOrder(any(OrderDto.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/orders/patient")
                        .contentType("application/json")
                        .content("""
                        {
                            "patientId": 10,
                            "pharmacyId": 20,
                            "totalPrice": 50.0,
                            "items": [
                                {
                                    "medicineName": "PANADOL",
                                    "quantity": 2,
                                    "price": 25.0
                                }
                            ]
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(10))
                .andExpect(jsonPath("$.pharmacyId").value(20))
                .andExpect(jsonPath("$.status").value("PLACED")); // متطابق مع sampleOrder
    }


    @Test
    @WithMockUser(roles = "PATIENT")
    void testGetPatientOrders() throws Exception {
        Mockito.when(orderService.getPatientOrders()).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(10))
                .andExpect(jsonPath("$[0].pharmacyId").value(20));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void testGetPatientOrdersByStatus() throws Exception {
        Mockito.when(orderService.getPatientOrdersByStatus(OrderStatus.PLACED))
                .thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders/patient/status/PLACED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PLACED"));
    }

    @Test
    @WithMockUser(roles = "PHARMACY")
    void testGetPharmacyOrders() throws Exception {
        Mockito.when(orderService.getPharmacyOrders()).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders/pharmacy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pharmacyId").value(20));
    }

    @Test
    @WithMockUser(roles = "PHARMACY")
    void testGetPharmacyOrdersByStatus() throws Exception {
        Mockito.when(orderService.getPharmacyOrdersByStatus(OrderStatus.PLACED))
                .thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders/pharmacy/status/PLACED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PLACED"));
    }

    @Test
    @WithMockUser(roles = {"PATIENT","PHARMACY"})
    void testUpdateOrder() throws Exception {
        Mockito.when(orderService.updateOrder(eq(1L), any(OrderDto.class))).thenReturn(sampleOrder);

        mockMvc.perform(put("/api/orders/1")
                        .contentType("application/json")
                        .content("""
                        {
                            "patientId": 10,
                            "pharmacyId": 20,
                            "totalPrice": 50.0,
                            "items": [
                                {
                                    "medicineName": "PANADOL",
                                    "quantity": 2,
                                    "price": 25.0
                                }
                            ]
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(10))
                .andExpect(jsonPath("$.status").value("PLACED"));
    }

    @Test
    @WithMockUser(roles = {"PATIENT","PHARMACY"})
    void testCancelOrder() throws Exception {
        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isNoContent());
        Mockito.verify(orderService).cancelOrder(1L);
    }

    @Test
    @WithMockUser(roles = "PHARMACY")
    void testShipOrder() throws Exception {
        sampleOrder.setStatus("SHIPPED");
        Mockito.when(orderService.shipOrder(1L)).thenReturn(sampleOrder);

        mockMvc.perform(patch("/api/orders/1/ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    @WithMockUser(roles = "PHARMACY")
    void testCompleteOrder() throws Exception {
        sampleOrder.setStatus("COMPLETED");
        Mockito.when(orderService.completeOrder(1L)).thenReturn(sampleOrder);

        mockMvc.perform(patch("/api/orders/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
