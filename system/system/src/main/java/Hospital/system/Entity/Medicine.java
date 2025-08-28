package Hospital.system.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Entity
@Table(name = "medicines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medicine {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "Medicine name is required")
        @Column(unique = true)
        private String name;

        @Min(value = 1, message = "Price must be greater than 0")
        private Double price;

        @Min(value = 0, message = "Stock cannot be negative")
        private Integer stock;

        @ManyToOne
        @JoinColumn(name = "pharmacy_id")
        private User pharmacy;
}
