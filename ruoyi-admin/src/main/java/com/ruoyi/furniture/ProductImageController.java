package com.ruoyi.furniture;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/furniture/images")
public class ProductImageController {
  @PostMapping
  @PreAuthorize("@ss.hasPermi('furniture:product:edit')")
  public AjaxResult upload(@RequestParam("file") MultipartFile file) throws IOException {
    if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be under 5MB");
    // Decode and re-encode; filenames, extensions and content types supplied by the client are not
    // trusted.
    try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext())
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a JPEG or PNG image");
      ImageReader reader = readers.next();
      try {
        reader.setInput(input);
        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
        if (!Set.of("png", "jpeg", "jpg").contains(format))
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a JPEG or PNG image");
        int w = reader.getWidth(0), h = reader.getHeight(0);
        if (w < 1 || h < 1 || w > 8000 || h > 8000 || (long) w * h > 20000000)
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Image dimensions are too large");
        BufferedImage image = reader.read(0);
        String name = UUID.randomUUID() + "." + (format.equals("png") ? "png" : "jpg");
        Path directory = Path.of(RuoYiConfig.getProfile(), "catalog");
        Files.createDirectories(directory);
        ImageIO.write(
            image, format.equals("png") ? "png" : "jpg", directory.resolve(name).toFile());
        return AjaxResult.success(Map.of("path", "/profile/catalog/" + name));
      } finally {
        reader.dispose();
      }
    }
  }
}
