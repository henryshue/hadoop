package cn.i.search.core.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NoticeUtil {

	public static Map<String, Object> getElement(File file) {
		Map<String, Object> data = new HashMap<String, Object>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] words = line.split("=");
				StringBuilder labels = new StringBuilder("");
				switch (words[0]) {
				case "DocDate":
					if (words.length < 2) {
						data.put("modifyTimeStamp", new Date());
						break;
					}
					long date = 0;
					try {
						date = sdf.parse(words[1]).getTime();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					data.put("modifyTimeStamp", date);
					break;
				case "DocTitle":
					if (words.length < 2) {
						break;
					}
					data.put("title", words[1]);
					break;
				case "StockName":
					if (words.length < 2) {
						break;
					}
					labels.append(words[1] + "、");
					break;
				case "CompanyName":
					if (words.length < 2) {
						break;
					}
					labels.append(words[1] + "、");
					break;
				case "Investment":
					if (words.length < 2) {
						break;
					}
					labels.append(words[1] + "、");
					break;
				case "ReportType":
					if (words.length < 2) {
						break;
					}
					labels.append(words[1] + "、");
					break;
				default:
					break;
				}
				data.put("labels", labels);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

}
