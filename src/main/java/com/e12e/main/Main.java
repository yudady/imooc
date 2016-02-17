package com.e12e.main;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.e12e.service.DownloadFile;
import com.e12e.service.GetFile;
import com.e12e.service.GetInfo;

public class Main {
	static Integer[] classNos;
	static int curruntCount;
	static int curruntGlobalCount;
	static boolean flag;
	static List<Integer> failClassNos = new ArrayList<Integer>();

	static {
		// http://www.imooc.com/learn/601
		classNos = new Integer[] { 556, 221, 156, 203, 51 };
	}

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < classNos.length; i++) {
			int classNo = classNos[i];
			try {
				runOneProcess(classNo);
			} catch (Exception e) {
				failClassNos.add(classNo);
				e.printStackTrace();
			}
		}

		System.out.println("--------------------------------------");
		for (int i = 0; i < failClassNos.size(); i++) {
			System.out.println("失敗的有:" + failClassNos.get(i));
		}
		System.out.println("--------------------------------------");
	}

	public static void runOneProcess(int classNo) throws Exception {

		curruntCount = 0;
		curruntGlobalCount = 0;
		flag = true;

		//int classNo = GetInput.getInputClassNo();

		Document doc = Jsoup.connect("http://www.imooc.com/learn/" + classNo).get();

		String title = doc.getElementsByTag("h2").html();

		Elements videos = doc.select(".video a");
		if ((title.equals("")) && (videos.size() == 0)) {
			System.out.println("抱歉，没有该课程！\n");
			return;
		}

		int count = 0;
		for (Element video : videos) {
			String[] videoNos = video.attr("href").split("/");

			if (videoNos[1].equals("video")) {
				count++;
			}
		}
		title = "【" + classNo + "】" + title;

		System.out.print("\n要下载的课程标题为【" + title + "】，");
		System.out.println("共 " + videos.size() + " 节课程，其中视频课程有 " + count + " 共\n");

		//int videoDef = GetInput.getInputVideoDef();
		int videoDef = 0;

		String savePath = "./download/" + title + "/";
		File file = new File(savePath);

		file.mkdirs();

		GetInfo.doGetInfo(classNo, title);

		FileWriter htmlWriter = new FileWriter(savePath + "course_list.html", true);
		String startHTML = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><title>课程列表</title><link type=\"text/css\" rel=\"stylesheet\" href=\"http://apps.bdimg.com/libs/jquerymobile/1.4.5/jquery.mobile-1.4.5.min.css\" /></head><body><table  data-role=table data-mode=reflow class=\"ui-responsive table-stroke\"><thead><th>No.</th><th>课程名称</th><th>在线地址</th></thead><tbody>";

		htmlWriter.write(startHTML);
		htmlWriter.flush();

		System.out.println("\n准备开始下载，请耐心等待…\n");

		for (Element video : videos) {
			String[] videoNos = video.attr("href").split("/");
			String url = "http://www.imooc.com" + video.attr("href");

			if (flag) {
				GetFile.doGetFile(videoNos[2], title);
				flag = false;
			}

			if (!videoNos[1].equals("video")) {
				String codeName = video.html().trim();
				curruntGlobalCount += 1;
				String downloadinfo = "<tr><td>" + curruntGlobalCount + "</td><td>" + codeName + "</td><td><a href='"
						+ url + "'>去慕课网练习*</a></td></tr>\n";
				htmlWriter.write(downloadinfo);
				htmlWriter.flush();
			} else {
				String videoName = video.html().substring(0, video.html().length() - 7).trim();
				String videoNo = videoNos[2];

				Document jsonDoc = Jsoup
						.connect("http://www.imooc.com/course/ajaxmediainfo/?mid=" + videoNo + "&mode=flash").get();
				String jsonData = jsonDoc.text();

				JSONObject jsonObject = new JSONObject(jsonData);
				JSONArray mpath = jsonObject.optJSONObject("data").optJSONObject("result").optJSONArray("mpath");
				String downloadPath = mpath.getString(videoDef).trim();

				System.out.println("【" + curruntCount + "】：\t" + videoName + " \t下载開始！");

				DownloadFile.downLoadFromUrl(downloadPath, videoName + ".mp4", savePath);

				curruntCount += 1;
				System.out.println("【" + curruntCount + "】：\t" + videoName + " \t下载成功！");
				System.out.println("**********************************************************");

				String downloadinfo = "<tr><td>" + curruntCount + "</td><td>" + videoName + "</td><td><a href='" + url
						+ "'>去慕课网观看</a></td></tr>\n";
				htmlWriter.write(downloadinfo);
				htmlWriter.flush();
			}
		}
		String endHTML = "</tbody></table><script src=\"http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js\"></script><script src=\"http://apps.bdimg.com/libs/jquerymobile/1.4.5/jquery.mobile-1.4.5.min.js\"></script></body></html>";

		htmlWriter.write(endHTML);
		htmlWriter.close();
		System.out.println("\n恭喜！【" + classNo + "】【" + title
				+ "】课程的所有视频已经下载完成！！！下载的文件在该程序所在目录下&#x7684download;文件夹中。\n-------------------------------------------------------\n");

	}
}
