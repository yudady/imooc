package com.e12e.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetInfo {
	public static void doGetInfo(int classNo, String className) throws IOException {
		File file = new File("./download/" + className + "/course_info.txt");

		file.createNewFile();

		FileWriter txtWriter = new FileWriter(file);

		Document doc = Jsoup.connect("http://www.imooc.com/view/" + classNo).get();
		String title = doc.select("h2").html();
		txtWriter.write("【课程】：" + title + "\n\n");

		String author = doc.select("span.tit a").html();
		txtWriter.write("【讲师】：" + author + "\n");

		String time = doc.select(".static-time span").first().text();
		txtWriter.write("【时长】：" + time + "\n");

		String hard = doc.select(".statics .static-item span").first().text();
		txtWriter.write("【难度】：" + hard + "\n\n\n");

		String intruc = doc.select(".auto-wrap").html();
		txtWriter.write("【课程介绍】：\n" + intruc + "\n\n\n");

		String know = doc.select(".course-info-tip .first dd").html();
		txtWriter.write("【课程须知】：\n" + know + "\n\n\n");

		String what = doc.select(".course-info-tip dd").last().html();
		txtWriter.write(
				"【老师告诉你能学到什么？】\n" + what + "\n\n\n");

		txtWriter.write("【课程提纲】：\n\n");
		Elements chapters = doc.select(".chapter-bd");
		for (Element chapter : chapters) {
			String chaptername = chapter.select("h5").html();
			txtWriter.write(chaptername + "\n");
			String chapterdesc = chapter.select("p").html();
			txtWriter.write(chapterdesc + "\n\n");
		}

		txtWriter.close();
	}
}

