package de.quandoo.recruitment.registry.model;

import com.google.common.base.Preconditions;

public record Customer(String uuid) {

  public Customer(String uuid) {
    this.uuid = Preconditions.checkNotNull(uuid, "Customer uuid could not be null!");
  }
}
