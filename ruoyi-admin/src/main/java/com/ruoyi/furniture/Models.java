package com.ruoyi.furniture;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
import java.time.LocalDateTime;

public final class Models {
  private Models() {}

  public record Product(
      Long id,
      @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 100) String slug,
      @NotBlank @Pattern(regexp = "wardrobes|living|kitchens|bathroom|outdoor") String category,
      @Size(max = 160) String nameRu,
      @NotBlank @Size(max = 160) String nameEn,
      @NotBlank @Size(max = 160) String nameZh,
      @Size(max = 4000) String descriptionRu,
      @NotBlank @Size(max = 4000) String descriptionEn,
      @NotBlank @Size(max = 4000) String descriptionZh,
      @Size(max = 4000) String specsRu,
      @Size(max = 4000) String specsEn,
      @Size(max = 4000) String specsZh,
      @NotBlank
          @Pattern(regexp = "/images/[a-zA-Z0-9_.-]+|/profile/[a-zA-Z0-9_./-]+")
          @Size(max = 500)
          String imageUrl,
      @NotBlank @Size(max = 50) String model,
      @NotNull Boolean published,
      @NotNull @Min(0) @Max(9999) Integer sortOrder,
      @Size(max = 30) List<@NotNull @Valid Variant> variants,
      @Size(max = 2) List<@NotBlank @Size(max = 500)
          @Pattern(regexp = "/images/[a-zA-Z0-9_.-]+|/profile/[a-zA-Z0-9_./-]+") String> supportingImages) {}

  public record Variant(
      @NotBlank @Size(max = 50) String code,
      @NotBlank @Size(max = 120) String dimensions,
      @NotBlank @Size(max = 120) String colorEn,
      @NotBlank @Size(max = 120) String colorZh,
      @NotBlank @Size(max = 160) String materialEn,
      @NotBlank @Size(max = 160) String materialZh,
      @NotNull @Min(1) @Max(1000000) Integer moq,
      @NotBlank @Size(max = 500)
      @Pattern(regexp = "/images/[a-zA-Z0-9_.-]+|/profile/[a-zA-Z0-9_./-]+") String imageUrl) {}

  public record InquiryInput(
      @NotBlank @Size(max = 80) String name,
      @NotBlank @Email @Size(max = 180) String email,
      @Size(max = 120) String company,
      @NotBlank @Size(max = 100) String country,
      @NotBlank @Size(max = 100) String city,
      @Size(max = 80) String contact,
      @Size(max = 100) String productSlug,
      @NotBlank @Size(min = 10, max = 4000) String message,
      @NotBlank @Pattern(regexp = "en|zh") String locale,
      @NotNull @AssertTrue Boolean privacyConsent,
      @NotNull Boolean publishConsent,
      @Size(max = 200) String website,
      @NotNull java.util.UUID requestId) {}

  public record Moderation(
      @NotBlank @Pattern(regexp = "PENDING|APPROVED|REJECTED") String moderationStatus,
      @NotBlank @Pattern(regexp = "NEW|FOLLOWING|CLOSED|SPAM") String followupStatus,
      @Size(max = 80) String publicName,
      @Size(max = 4000) String publicText,
      @Size(max = 4000) String reply,
      @Size(max = 4000) String internalNote,
      @NotNull @Min(0) Integer version) {}

  public record Content(
      @NotBlank @Size(max = 100) String key,
      @Size(max = 16000) String ru,
      @NotBlank @Size(max = 16000) String en,
      @NotBlank @Size(max = 16000) String zh) {}

  public record PublicMessage(
      long id,
      String publicName,
      String message,
      String reply,
      String locale,
      LocalDateTime createdAt) {}
}
