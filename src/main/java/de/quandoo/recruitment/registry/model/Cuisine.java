package de.quandoo.recruitment.registry.model;

import com.google.common.base.Preconditions;

public record Cuisine(String name) {

  public Cuisine(String name) {
    this.name = Preconditions.checkNotNull(name, "Cuisine name could not be null!");
  }

  public static Cuisine of(String name) {
    return new Cuisine(name);
  }

}
