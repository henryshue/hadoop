package cn.i.search.core.elasticsearch.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	public static final String KEY_FILE_PATH = "filePath";
	public static final String KEY_FILE_NAME = "fileName";
	public static final String KEY_FILE = "file";

	public static List<File> getLocalFilesByPath(String path) {
		// 返回一个文件列表
		List<File> fileList = new ArrayList<File>();
		File file = new File(path);
		if (!file.exists() || !file.canRead()) {
			return null;
		}
		// 如果为文件就加入list
		if (file.isFile()) {
			fileList.add(file);
			return fileList;
		}
		// 文件夹列表
		LinkedList<File> folderList = new LinkedList<File>();
		// 如果为目录，加入列表
		if (file.isDirectory()) {
			folderList.add(file);
		}
		// 当为列表时
		while (!folderList.isEmpty()) {
			// 取出第一个
			file = folderList.removeFirst();
			for (File tempFile : file.listFiles()) {
				if (tempFile.exists() && tempFile.isFile()) {
					fileList.add(tempFile);
				} else if (tempFile.exists() && tempFile.isDirectory()) {
					folderList.add(tempFile);
				}
			}
		}
		if (fileList.size() > 0) {
			return fileList;
		}
		return null;
	}

	public static String encodeBase64File(File file) {
		String result = null;
		if (null != file && file.isFile()) {
			try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputStream.available());) {
				int buf_size = 1024;
				byte[] buffer = new byte[buf_size];
				int len = 0;
				while (-1 != (len = inputStream.read(buffer, 0, buf_size))) {
					outputStream.write(buffer, 0, len);
				}
				result = Base64.encodeBase64String(outputStream.toByteArray());
			} catch (IOException e) {
				logger.error("常量工具类encodeBase64File方法报错", e);
			}
		}
		return result;
	}

	public static Map<String, Object> readFileToBytes(File file) {
		Map<String, Object> result = null;
		if (file != null && file.exists()) {
			result = new HashMap<String, Object>();
			result.put("filePath", file.getAbsolutePath());
			result.put("fileName", file.getName());
			result.put("file", encodeBase64File(file));
		}
		return result;
	}

}
