package com.ruoyi.furniture;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.furniture.Models.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {
  private final CatalogService catalog;
  private final InquiryService inquiries;

  public PublicController(CatalogService catalog, InquiryService inquiries) {
    this.catalog = catalog;
    this.inquiries = inquiries;
  }

  @GetMapping("/products")
  public AjaxResult products() {
    return AjaxResult.success(catalog.list(true));
  }

  @GetMapping("/products/{slug}")
  public AjaxResult product(@PathVariable String slug) {
    return AjaxResult.success(catalog.publicProduct(slug));
  }

  @GetMapping("/content")
  public AjaxResult content() {
    return AjaxResult.success(catalog.content().stream().map(WebsiteContent::publicCopy).toList());
  }

  @GetMapping("/messages")
  public AjaxResult messages(
      @RequestParam(defaultValue = "en") String locale,
      @RequestParam(defaultValue = "1") int page) {
    String safeLocale = locale.equals("zh") ? "zh" : "en";
    return AjaxResult.success(inquiries.publicList(safeLocale, page));
  }

  @PostMapping("/messages")
  public AjaxResult submit(@Valid @RequestBody InquiryInput input, HttpServletRequest req) {
    return AjaxResult.success(Map.of("receipt", inquiries.submit(input, req.getRemoteAddr())));
  }
}
