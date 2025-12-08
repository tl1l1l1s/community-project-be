package ktb.week4.community.global.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

	private final Path uploadRoot;

	public FileStorageService(@Value("${file.upload-dir}") String uploadDir) throws IOException {
		this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
		Files.createDirectories(this.uploadRoot);
	}

	public String store(MultipartFile file, String directory) {
		if (file == null || file.isEmpty() ||
				file.getContentType() == null || file.getContentType().isEmpty() ||
				file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
			return null;
		}
		
		if(!file.getContentType().startsWith("image")) {
			throw new GeneralException(ErrorCode.FILE_FORMAT_ERROR);
		}
		
		try {
			String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
			Path targetDir = uploadRoot.resolve(directory).normalize();
			Files.createDirectories(targetDir);
			Path targetFile = targetDir.resolve(fileName);
			Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
			return "/uploads/" + directory + "/" + fileName;
		} catch (IOException e) {
			throw new GeneralException(ErrorCode.FILE_UPLOAD_ERROR);
		}
	}

	public void delete(String urlPath) {
		if (!StringUtils.hasText(urlPath)) {
			return;
		}

		String relativePath = urlPath;
		if (relativePath.startsWith("/uploads/")) {
			relativePath = relativePath.substring("/uploads/".length());
		}

		Path target = uploadRoot.resolve(relativePath).normalize();
		try {
			Files.deleteIfExists(target);
		} catch (IOException e) {
			throw new GeneralException(ErrorCode.FILE_DELETE_ERROR);
		}
	}
}
