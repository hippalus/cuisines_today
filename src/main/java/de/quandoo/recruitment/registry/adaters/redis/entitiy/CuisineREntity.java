package de.quandoo.recruitment.registry.adaters.redis.entitiy;

import de.quandoo.recruitment.registry.model.Cuisine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO: This class should be immutable.Class Fields should be final and setter methods should be deleted.
// But redisson use Jackson Codec for serialization and deserialization.
// They need to setter getter and default constructor for serialization and deserialization.
// That's why  this class is mutable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CuisineREntity {

  private String name;

  //for memory efficiency
  public static CuisineREntity of(final Cuisine cuisine) {
    return new CuisineREntity(cuisine.name());
  }

  public Cuisine toModel() {
    return new Cuisine(this.name);
  }
}
