package ktb.week4.community.file;

import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import ktb.week4.community.global.file.FileStorageService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
	
	@ParameterizedTest
	@ValueSource(strings = {
			"application/json",
			"text/plain",
			"application/pdf",
			"application/x-msdownload",
			"application/octet-stream"
	})
	@DisplayName("업로드 한 파일의 형태가 image~가 아닌 경우 FILE_FORMAT_ERROR를 받는다.")
	void givenWrongFormatFile_whenUploadFile_thenThrowsFileFormatException(String contentType) {
	
		// given
		byte[] content = "test-json".getBytes();
		MockMultipartFile mockFile = new MockMultipartFile(
				"file",
				"file.bin",
				contentType,
				content
		);
		
		// when
		GeneralException exc = assertThrows(GeneralException.class,
				() -> fileStorageService.store(mockFile, "test"));
		
		//then
		assertEquals(ErrorCode.FILE_FORMAT_ERROR, exc.getErrorCode());
	}
	
	@Test
	@DisplayName("파일 저장 중 예외가 발생하면 FILE_UPLOAD_ERROR를 받는다.")
	void givenIoFailure_whenUploadFile_thenThrowsFileUploadError() {
		// given
		MultipartFile brokenFile = new MultipartFile() {
			@Override
			public String getName() {
				return "file";
			}
			
			@Override
			public String getOriginalFilename() {
				return "broken.png";
			}
			
			@Override
			public String getContentType() {
				return "image/png";
			}
			
			@Override
			public boolean isEmpty() {
				return false;
			}
			
			@Override
			public long getSize() {
				return 1L;
			}
			
			@Override
			public byte[] getBytes() {
				return new byte[0];
			}
			
			@Override
			public java.io.InputStream getInputStream() throws IOException {
				throw new IOException("cannot read");
			}
			
			@Override
			public void transferTo(java.io.File dest) {
				throw new UnsupportedOperationException();
			}
		};
		
		// when
		GeneralException exc = assertThrows(
				GeneralException.class,
				() -> fileStorageService.store(brokenFile, "test")
		);
		
		// then
		assertEquals(ErrorCode.FILE_UPLOAD_ERROR, exc.getErrorCode());
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
	
	@Test
	@DisplayName("존재하지 않는 이미지 삭제 시 FILE_DELETE_ERROR를 받는다.")
	void givenInvalidImageUrl_whenDeleteFile_thenThrowsFile_Delete_Error() throws IOException {
		
		// given
		Path dir = uploadRoot.resolve("not-empty-dir");
		Files.createDirectories(dir);
		Files.createFile(dir.resolve("dummy.txt"));

		// when
		GeneralException exc = assertThrows(GeneralException.class,
				() -> fileStorageService.delete("/uploads/not-empty-dir")
		);
		
		// then
		assertEquals(ErrorCode.FILE_DELETE_ERROR, exc.getErrorCode());
	}
}
