package com.example.STOLOTO.Services;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.STOLOTO.exception.ApiException;

@Component
public class AvatarStorage {

    public static final String PATH = "/api/v1/avatars/";
    public static final DataSize MAX_SIZE = DataSize.ofMegabytes(5);

    private static final Logger log = LoggerFactory.getLogger(AvatarStorage.class);
    private static final Pattern NAME = Pattern.compile("[0-9a-f]{32}\\.(jpg|png|webp)");

    private final Path dir;
    private final String publicUrl;

    public AvatarStorage(@Value("${app.avatars.dir}") Path dir, @Value("${app.avatars.public-url:}") String publicUrl) {
        this.dir = dir.toAbsolutePath().normalize();
        this.publicUrl = publicUrl == null || publicUrl.isBlank() ? null : publicUrl.replaceAll("/+$", "");
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.validation("file", "файл не передан");
        }
        if (file.getSize() > MAX_SIZE.toBytes()) {
            throw tooLarge();
        }
        byte[] bytes;
        try (InputStream in = file.getInputStream()) {
            bytes = in.readNBytes((int) MAX_SIZE.toBytes() + 1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (bytes.length > MAX_SIZE.toBytes()) {
            throw tooLarge();
        }
        String ext = detectExtension(bytes).orElseThrow(() -> new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE", "Поддерживаются только JPG, PNG и WEBP"));

        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(name);
        try {
            Files.createDirectories(dir);
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        onRollback(() -> deleteFile(name));
        return url(name);
    }

    public void deleteAfterCommit(String avatarUrl) {
        nameFromUrl(avatarUrl).ifPresent(name -> {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deleteFile(name);
                    }
                });
            } else {
                deleteFile(name);
            }
        });
    }

    public boolean isUploaded(String avatarUrl) {
        return nameFromUrl(avatarUrl).isPresent();
    }

    public Optional<Path> find(String name) {
        if (name == null || !NAME.matcher(name).matches()) {
            return Optional.empty();
        }
        Path file = dir.resolve(name);
        return Files.isRegularFile(file) ? Optional.of(file) : Optional.empty();
    }

    public static MediaType mediaType(String name) {
        if (name.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (name.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }

    public static ApiException tooLarge() {
        return new ApiException(HttpStatus.CONTENT_TOO_LARGE, "FILE_TOO_LARGE", "Файл больше 5 МБ");
    }

    private String url(String name) {
        String base = publicUrl != null
                ? publicUrl
                : ServletUriComponentsBuilder.fromCurrentContextPath().path(PATH).build().toUriString()
                        .replaceAll("/+$", "");
        return base + "/" + name;
    }

    private static Optional<String> nameFromUrl(String avatarUrl) {
        if (avatarUrl == null) {
            return Optional.empty();
        }
        int slash = avatarUrl.lastIndexOf('/');
        String name = avatarUrl.substring(slash + 1);
        return avatarUrl.contains(PATH) && NAME.matcher(name).matches() ? Optional.of(name) : Optional.empty();
    }

    private void deleteFile(String name) {
        try {
            Files.deleteIfExists(dir.resolve(name));
        } catch (IOException e) {
            log.warn("Не удалось удалить аватар {}", name, e);
        }
    }

    private static void onRollback(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        action.run();
                    }
                }
            });
        }
    }

    static Optional<String> detectExtension(byte[] b) {
        if (startsWith(b, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of("jpg");
        }
        if (startsWith(b, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of("png");
        }
        if (startsWith(b, 0, 'R', 'I', 'F', 'F') && startsWith(b, 8, 'W', 'E', 'B', 'P')) {
            return Optional.of("webp");
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] b, int offset, int... signature) {
        if (b.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((b[offset + i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
