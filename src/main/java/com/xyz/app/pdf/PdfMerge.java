package com.xyz.app.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.xyz.app.config.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PdfMerge {

	@Autowired
	private Config config;

	public void run() {
		String src = config.getSrc();
		String out = config.getOut();
		if (src == null || "".equals(src.trim())) {
			log.error("[src]参数错误！");
			return;
		}
		if (out == null || "".equals(out.trim())) {
			log.error("[out]参数错误！");
			return;
		}
		File file = new File(src);
		if (!file.exists()) {
			log.error(MessageFormat.format("[{0}]不存在！", src));
			return;
		}
		file = new File(out);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				log.error(MessageFormat.format("[{0}]创建失败！", out));
				return;
			}
		} else {
			if (file.isFile()) {
				log.error(MessageFormat.format("[{0}]已经存在！", out));
				return;
			}
		}
		try {
			merge(src, out);
			log.info("合并成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void merge(String src, String out) throws Exception {
		File dir = new File(src);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				Document doc = null;
				PdfCopy copy = null;
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					String name = file.getName();
					String path = file.getAbsolutePath();
					if (name.matches("(?i).*\\.pdf$")) {
						log.info(MessageFormat.format("读取Pdf：{0}！", name));
						if (doc == null) {
							doc = new Document(new PdfReader(path).getPageSize(1));
							copy = new PdfCopy(doc, new FileOutputStream(out));
							doc.open();
						}
						PdfReader reader = new PdfReader(path);
						for (int j = 1; j <= reader.getNumberOfPages(); j++) {
							doc.newPage();
							copy.addPage(copy.getImportedPage(reader, j));
						}
						if (reader != null) {
							reader.close();
						}
					} else {
						log.warn(MessageFormat.format("[{0}]非Pdf！", name));
					}
				}
				if (doc != null) {
					doc.close();
				}
			} else {
				log.warn(MessageFormat.format("[{0}]空目录！", src));
			}
		} else {
			log.warn(MessageFormat.format("[{0}]非目录！", src));
		}
	}

}
