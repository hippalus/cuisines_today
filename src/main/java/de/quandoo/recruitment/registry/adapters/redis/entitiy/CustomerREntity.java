package de.quandoo.recruitment.registry.adapters.redis.entitiy;

import de.quandoo.recruitment.registry.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO: This class should be immutable.Class Fields should be final and setter methods should be deleted.
// But redisson uses Jackson Codec for serialization and deserialization.
// Jackson  needs to setter getter and default constructor for serialization and deserialization.
// That's why  this class is mutable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerREntity {

  private String uuid;

  //for memory efficiency
  public static CustomerREntity of(final Customer customer) {
    return new CustomerREntity(customer.uuid());
  }

  public Customer toModel() {
    return new Customer(this.uuid);
  }
}
