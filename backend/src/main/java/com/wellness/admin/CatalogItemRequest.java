package com.wellness.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for creating/updating a goal, diet type or training location. The code comes from the URL, not the body. */
public record CatalogItemRequest(@NotBlank @Size(max = 80) String name, @Size(max = 255) String description) {
}
