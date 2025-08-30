package Hospital.system.Service;

import Hospital.system.DTO.OrderDto;
import Hospital.system.Entity.*;
import Hospital.system.Mapper.OrderItemMapper;
import Hospital.system.Mapper.OrderMapper;
import Hospital.system.Repository.MedicineRepository;
import Hospital.system.Repository.OrderRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.exception.AccessDeniedException;
import Hospital.system.exception.BadRequestException;
import Hospital.system.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderDto createOrder (OrderDto orderDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Creating order for patient email={}", email);

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });

        if (orderDto.getPatientId() == null){
            orderDto.setPatientId(patient.getId());
        }
        if (!orderDto.getPatientId().equals(patient.getId())) {
            log.warn("Patient {} tried to place order for another patient {}", email, orderDto.getPatientId());
            throw new AccessDeniedException("You can't place order with another patient's Id");
        }

        User pharmacy = userRepository.findByIdAndRole(orderDto.getPharmacyId(), Role.PHARMACY)
                .orElseThrow(() -> {
                    log.error("No Pharmacy with id {}", orderDto.getPharmacyId());
                    return new ResourceNotFoundException("No Pharmacy with id " + orderDto.getPharmacyId());
                });

        log.debug("Placing order at pharmacy id={}", pharmacy.getId());
        orderDto.setStatus("PLACED");
        Order order = OrderMapper.toEntity(orderDto,patient,pharmacy,null);

        //build order items
        List<OrderItem> items = orderDto.getItems().stream().map(itemDto -> {
            Medicine medicine = medicineRepository.findByName(itemDto.getMedicineName())
                    .orElseThrow(() -> {
                        log.error("Medicine not found: {}", itemDto.getMedicineName());
                        return new ResourceNotFoundException("Medicine not found: " + itemDto.getMedicineName());
                    });
            if (medicine.getStock() < itemDto.getQuantity()) {
                log.warn("Not enough stock for medicine {} (requested={}, available={})",
                        medicine.getName(), itemDto.getQuantity(), medicine.getStock());
                throw new BadRequestException("Not enough stock for " + medicine.getName());
            }

            if (!medicine.getPharmacy().getId().equals(pharmacy.getId())) {
                log.warn("Medicine {} is not available in pharmacy {}", medicine.getId(), pharmacy.getId());
                throw new BadRequestException("Medicine " + medicine.getId() +
                        " is not available in pharmacy " + pharmacy.getId());
            }

            //deduct stock
            medicine.setStock(medicine.getStock() - itemDto.getQuantity());

            double itemPrice = medicine.getPrice() * itemDto.getQuantity();
            itemDto.setPrice(itemPrice);

            return OrderItemMapper.toEntity(itemDto,order,medicine);
        }).collect(Collectors.toList());

        //calculate total and set items
        double total = items.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();

        order.setItems(items);
        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: orderId={}, patientId={}, pharmacyId={}, total={}",
                savedOrder.getId(), patient.getId(), pharmacy.getId(), savedOrder.getTotalPrice());

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public List<OrderDto> getPatientOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Fetching all orders for patient email={}", email);

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });
        List<OrderDto> orders =  orderRepository.findByPatient_Id(patient.getId()).stream()
                .map(OrderMapper::toDto)
                .toList();
        log.info("Fetched {} orders for patient id={}", orders.size(), patient.getId());
        return orders;
    }

    @Transactional
    public List<OrderDto> getPatientOrdersByStatus(OrderStatus status){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Fetching orders for patient email={} with status={}", email, status);

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });
        List<OrderDto> orders =  orderRepository.findByPatient_IdAndStatus(patient.getId(),status).stream()
                .map(OrderMapper::toDto)
                .toList();
        log.info("Fetched {} orders for patient id={} with status={}", orders.size(), patient.getId(), status);
        return orders;
    }

    @Transactional
    public List<OrderDto> getPharmacyOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Fetching all orders for pharmacy email={}", email);

        User pharmacy = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });
        List<OrderDto> orders =  orderRepository.findByPharmacy_Id(pharmacy.getId()).stream()
                .map(OrderMapper::toDto)
                .toList();

        log.info("Fetched {} orders for pharmacy id={}", orders.size(), pharmacy.getId());
        return orders;
    }

    @Transactional
    public List<OrderDto> getPharmacyOrdersByStatus(OrderStatus status){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Fetching orders for pharmacy email={} with status={}", email, status);

        User pharmacy = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in pharmacy not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });
        List<OrderDto> orders = orderRepository.findByPharmacy_IdAndStatus(pharmacy.getId(),status).stream()
                .map(OrderMapper::toDto)
                .toList();
        log.info("Fetched {} orders for pharmacy id={} with status={}", orders.size(), pharmacy.getId(), status);
        return orders;
    }

    @Transactional
    public OrderDto updateOrder(Long orderId, OrderDto updatedDto){
        log.debug("Updating order id={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id={}", orderId);
                    return new ResourceNotFoundException("Order not found with id " + orderId);
                });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });

        if(loggedInUser.getRole()==Role.PATIENT){
            if(!email.equals(order.getPatient().getEmail())){
                log.warn("Patient {} tried to update another patient's order id={}", email, orderId);
                throw new AccessDeniedException("You can't update another patient's order");
            }
        }else if (loggedInUser.getRole()==Role.PHARMACY){
            if(!email.equals(order.getPharmacy().getEmail())){
                log.warn("Pharmacy {} tried to update another pharmacy's order id={}", email, orderId);
                throw new AccessDeniedException("You can't update another pharmacy's order");
            }
        }

        if(!order.getStatus().equals(OrderStatus.PLACED)){
            log.warn("Cannot update order id={} with status={}", orderId, order.getStatus());
            throw new BadRequestException("You can't change order with status "+ order.getStatus());
        }

        if(!order.getPharmacy().getId().equals(updatedDto.getPharmacyId()) ||
                !order.getPatient().getId().equals(updatedDto.getPatientId())){
            log.warn("Cannot update id fields");
            throw new BadRequestException("You can't change id fields");
        }

        // build new items
        List<OrderItem> newItems = updatedDto.getItems().stream().map(itemDto -> {
            Medicine medicine = medicineRepository.findByName(itemDto.getMedicineName())
                    .orElseThrow(() -> {
                        log.error("Medicine not found: {}", itemDto.getMedicineName());
                        return new ResourceNotFoundException("Medicine not found: " + itemDto.getMedicineName());
                    });
            if (medicine.getStock() < itemDto.getQuantity()) {
                log.warn("Not enough stock for medicine {} (requested={}, available={})",
                        medicine.getName(), itemDto.getQuantity(), medicine.getStock());
                throw new BadRequestException("Not enough stock for " + medicine.getName());
            }

            if (!medicine.getPharmacy().getId().equals(order.getPharmacy().getId())) {
                log.warn("Medicine {} (pharmacyId={}) is not available in pharmacy {}",
                        medicine.getId(), medicine.getPharmacy().getId(), order.getPharmacy().getId());
                throw new BadRequestException("Medicine " + medicine.getId() +
                        " is not available in pharmacy " + order.getPharmacy().getId());
            }

            // handle stock adjustments
            OrderItem existingItem = order.getItems().stream()
                    .filter(i -> i.getMedicine().getId().equals(medicine.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                int qtyDiff = itemDto.getQuantity() - existingItem.getQuantity();
                log.debug("Adjusting stock for medicine {}: existingQty={}, newQty={}, qtyDiff={}",
                        medicine.getName(), existingItem.getQuantity(), itemDto.getQuantity(), qtyDiff);
                medicine.setStock(medicine.getStock() - qtyDiff);
            } else {
                log.debug("Reducing stock for new medicine {}: quantity={}", medicine.getName(), itemDto.getQuantity());
                medicine.setStock(medicine.getStock() - itemDto.getQuantity());
            }

            double itemPrice = medicine.getPrice() * itemDto.getQuantity();
            itemDto.setPrice(itemPrice);
            log.debug("Prepared order item for medicine {}: quantity={}, price={}", medicine.getName(), itemDto.getQuantity(), itemPrice);

            return OrderItemMapper.toEntity(itemDto, order, medicine);
        }).toList();

        order.getItems().clear();      // keep same collection reference
        order.getItems().addAll(newItems);

        // update total
        double newTotal = newItems.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();
        order.setTotalPrice(newTotal);

        Order saved = orderRepository.save(order);
        log.info("Order updated successfully: orderId={}, total={}", saved.getId(), saved.getTotalPrice());
        return OrderMapper.toDto(saved);

    }

    @Transactional
    public void cancelOrder(Long orderId){
        //soft delete
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id={}", orderId);
                    return new ResourceNotFoundException("Order not found with id " + orderId);
                });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });
        if(loggedInUser.getRole()==Role.PATIENT){
            if(!email.equals(order.getPatient().getEmail())){
                log.warn("Patient {} tried to cancel order id={} without permission", email, orderId);
                throw new AccessDeniedException("You can't cancel another patient's order");
            }
        }else if (loggedInUser.getRole()==Role.PHARMACY){
            if(!email.equals(order.getPharmacy().getEmail())){
                log.warn("Pharmacy {} tried to complete order id={} without permission", email, orderId);
                throw new AccessDeniedException("You can't cancel another pharmacy's order");
            }
        }

        if(order.getStatus() != OrderStatus.PLACED){
            log.warn("Order id={} cannot be cancelled. Current status={}", orderId, order.getStatus());
            throw new BadRequestException("You can't cancel order with status " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order cancelled successfully: orderId={}, pharmacyId={}", saved.getId(), loggedInUser.getId());
    }


    @Transactional
    public OrderDto shipOrder(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id={}", orderId);
                    return new ResourceNotFoundException("Order not found with id " + orderId);
                });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });

        if (!(email.equals(order.getPharmacy().getEmail()))){
            log.warn("User {} tried to complete ship id={} without permission", email, orderId);
            throw new AccessDeniedException("You can't ship this order");
        }

        if(order.getStatus() != OrderStatus.PLACED){
            log.warn("Order id={} cannot be shipped. Current status={}", orderId, order.getStatus());
            throw new BadRequestException("You can't ship order with status " + order.getStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        Order saved = orderRepository.save(order);
        log.info("Order shipped successfully: orderId={}, pharmacyId={}", saved.getId(), loggedInUser.getId());
        return OrderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto completeOrder(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id={}", orderId);
                    return new ResourceNotFoundException("Order not found with id " + orderId);
                });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged in user not found: {}", email);
                    return new ResourceNotFoundException("Logged in user not found");
                });
        if (!(email.equals(order.getPharmacy().getEmail()))){
            log.warn("User {} tried to complete order id={} without permission", email, orderId);
            throw new AccessDeniedException("You can't ship this order");
        }

        if(order.getStatus() != OrderStatus.SHIPPED){
            log.warn("Order id={} cannot be completed. Current status={}", orderId, order.getStatus());
            throw new BadRequestException("You can't complete order with status " + order.getStatus());
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order saved = orderRepository.save(order);
        log.info("Order completed successfully: orderId={}, pharmacyId={}", saved.getId(), loggedInUser.getId());
        return OrderMapper.toDto(saved);
    }

}