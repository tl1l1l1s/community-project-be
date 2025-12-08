package ktb.week4.community.file;

import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import ktb.week4.community.global.file.FileStorageService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
	
	@TempDir
	static Path tempDir;
	
	static FileStorageService fileStorageService;
	static Path uploadRoot;
	
	@BeforeAll
	static void setUp(@TempDir Path tempDir) throws IOException {
		uploadRoot = tempDir.resolve("uploads");
		fileStorageService = new FileStorageService(uploadRoot.toString());
	}
	
	@Test
	@DisplayName("파일 저장 전 저장용 폴더가 생성된다.")
	void given_whenFileStorageServiceInit_thenCreatesFile() throws IOException {
		
		// given
		Path testDir = tempDir.resolve("init-test");
		
		// when
		new FileStorageService(testDir.toString());
		
		// then
		assertTrue(Files.exists(testDir));
		assertTrue(Files.isDirectory(testDir));
	}
	
	@Test
	@DisplayName("이미지 저장 시 /upload/directory/랜덤 UUID_filename의 형식으로 저장된다.")
	void givenValidImage_whenUploadFile_thenSucceeds() {
		
		// given
		byte[] content = "test-image".getBytes();
		MockMultipartFile mockFile = new MockMultipartFile(
				"file",
				"image.png",
				"image/png",
				content
		);
		
		// when
		String res = fileStorageService.store(mockFile, "test");
		
		//then
		assertNotNull(res);
		assertTrue(res.startsWith("/uploads"));
	}
	
	
	@Test
	@DisplayName("이미지 저장 시 이미지가 없는 경우 null을 반환한다.")
	void givenNone_whenUploadFile_thenReturnNull() {
		
		// given
		
		// when
		String res = fileStorageService.store(null, "");
		
		//then
		assertNull(res);
	}
	
	
	@Test
	@DisplayName("업로드 한 파일의 형태가 image~가 아닌 경우 FILE_FORMAT_ERROR를 받는다.")
	void givenWrongFormatFile_whenUploadFile_thenThrowsFileFormatException() {
	
		// given
		byte[] content = "test-json".getBytes();
		MockMultipartFile mockFile = new MockMultipartFile(
				"file",
				"image.json",
				"application/json",
				content
		);
		
		// when
		GeneralException exc = assertThrows(GeneralException.class,
				() -> fileStorageService.store(mockFile, "test"));
		
		//then
		assertEquals(ErrorCode.FILE_FORMAT_ERROR, exc.getErrorCode());
	}
	
	@Test
	@DisplayName("이미지 삭제 시 이미지가 삭제된다.")
	void givenValidImageUrl_whenDeleteFile_thenSucceeds() throws IOException {
		
		// given
		String directory = "test";
		String fileName = "testDelete.png";
		
		Path targetDir = uploadRoot.resolve(directory);
		Files.createDirectories(targetDir);
		Path targetFile = targetDir.resolve(fileName);
		Files.createFile(targetFile);
		assertTrue(Files.exists(targetFile));
		
		String urlPath = "/uploads/" + directory + "/" + fileName;
		
		// when
		fileStorageService.delete(urlPath);
		
		// then
		assertFalse(Files.exists(targetFile));
	}
}
