package Hospital.system.Service;

import Hospital.system.DTO.OrderDto;
import Hospital.system.Entity.*;
import Hospital.system.Mapper.AppointmentMapper;
import Hospital.system.Mapper.OrderItemMapper;
import Hospital.system.Mapper.OrderMapper;
import Hospital.system.Repository.MedicineRepository;
import Hospital.system.Repository.OrderRepository;
import Hospital.system.Repository.UserRepository;
import Hospital.system.exception.AccessDeniedException;
import Hospital.system.exception.BadRequestException;
import Hospital.system.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderDto createOrder (OrderDto orderDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        if (orderDto.getPatientId() == null){
            orderDto.setPatientId(patient.getId());
        }
        if (!orderDto.getPatientId().equals(patient.getId())) {
            throw new AccessDeniedException("You can't place order with another patient's Id");
        }

        User pharmacy = userRepository.findByIdAndRole(orderDto.getPharmacyId(), Role.PHARMACY)
                .orElseThrow(() -> new ResourceNotFoundException("No Pharmacy with id " + orderDto.getPharmacyId()));

        orderDto.setStatus("PLACED");
        Order order = OrderMapper.toEntity(orderDto,patient,pharmacy,null);

        //build order items
        List<OrderItem> items = orderDto.getItems().stream().map(itemDto -> {
            Medicine medicine = medicineRepository.findByName(itemDto.getMedicineName())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + itemDto.getMedicineName()));

            if (medicine.getStock() < itemDto.getQuantity()) {
                throw new BadRequestException("Not enough stock for " + medicine.getName());
            }

            if (!medicine.getPharmacy().getId().equals(pharmacy.getId())) {
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
        return OrderMapper.toDto(savedOrder);
    }

    @Transactional
    public List<OrderDto> getPatientOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        return orderRepository.findByPatient_Id(patient.getId()).stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Transactional
    public List<OrderDto> getPatientOrdersByStatus(OrderStatus status){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));
        return orderRepository.findByPatient_IdAndStatus(patient.getId(),status).stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Transactional
    public List<OrderDto> getPharmacyOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User pharmacy = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));
        return orderRepository.findByPharmacy_Id(pharmacy.getId()).stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Transactional
    public List<OrderDto> getPharmacyOrdersByStatus(OrderStatus status){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User pharmacy = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));
        return orderRepository.findByPharmacy_IdAndStatus(pharmacy.getId(),status).stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto updateOrder(Long orderId, OrderDto updatedDto){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with id " + orderId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        if(loggedInUser.getRole()==Role.PATIENT){
            if(!email.equals(order.getPatient().getEmail())){
                throw new AccessDeniedException("You can't update another patient's order");
            }
        }else if (loggedInUser.getRole()==Role.PHARMACY){
            if(!email.equals(order.getPharmacy().getEmail())){
                throw new AccessDeniedException("You can't update another pharmacy's order");
            }
        }

        if(!order.getStatus().equals(OrderStatus.PLACED)){
            throw new BadRequestException("You can't change order with status "+ order.getStatus());
        }

        if(!order.getPharmacy().getId().equals(updatedDto.getPharmacyId()) ||
                !order.getPatient().getId().equals(updatedDto.getPatientId())){
            throw new BadRequestException("You can't change id fields");
        }

        // build new items
        List<OrderItem> newItems = updatedDto.getItems().stream().map(itemDto -> {
            Medicine medicine = medicineRepository.findByName(itemDto.getMedicineName())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + itemDto.getMedicineName()));

            if (medicine.getStock() < itemDto.getQuantity()) {
                throw new BadRequestException("Not enough stock for " + medicine.getName());
            }

            if (!medicine.getPharmacy().getId().equals(order.getPharmacy().getId())) {
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
                medicine.setStock(medicine.getStock() - qtyDiff);
            } else {
                medicine.setStock(medicine.getStock() - itemDto.getQuantity());
            }

            double itemPrice = medicine.getPrice() * itemDto.getQuantity();
            itemDto.setPrice(itemPrice);

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
        return OrderMapper.toDto(saved);

    }

    @Transactional
    public void cancelOrder(Long orderId){
        //soft delete
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with id " + orderId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Logged in user not found");
                });
        if(loggedInUser.getRole()==Role.PATIENT){
            if(!email.equals(order.getPatient().getEmail())){
                throw new AccessDeniedException("You can't cancel another patient's order");
            }
        }else if (loggedInUser.getRole()==Role.PHARMACY){
            if(!email.equals(order.getPharmacy().getEmail())){
                throw new AccessDeniedException("You can't cancel another pharmacy's order");
            }
        }

        if(order.getStatus() != OrderStatus.PLACED){
            throw new BadRequestException("You can't cancel order with status " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }


    @Transactional
    public OrderDto shipOrder(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with id " + orderId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Logged in user not found");
                });

        if (!(email.equals(order.getPharmacy().getEmail()))){
            throw new AccessDeniedException("You can't ship this order");
        }

        if(order.getStatus() != OrderStatus.PLACED){
            throw new BadRequestException("You can't ship order with status " + order.getStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        Order saved = orderRepository.save(order);
        return OrderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto completeOrder(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with id " + orderId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Logged in user not found");
                });
        if (!(email.equals(order.getPharmacy().getEmail()))){
            throw new AccessDeniedException("You can't ship this order");
        }

        if(order.getStatus() != OrderStatus.SHIPPED){
            throw new BadRequestException("You can't complete order with status " + order.getStatus());
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order saved = orderRepository.save(order);
        return OrderMapper.toDto(saved);
    }

}