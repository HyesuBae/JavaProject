package main;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ParsingTool {
	public static ArrayList<App> parsing(String webContent) {
		int startIdx;
		String startPoint = "<div class=\"card no-rationale square-cover apps small\"";

		ArrayList<App> appList = new ArrayList<>();

		while ((startIdx = webContent.indexOf(startPoint)) != -1) {
			webContent = webContent.substring(startIdx + startPoint.length());

			String title;
			String subTitle;
			String imgName;
			String imgSrcUrl = "http:";
			String link = "https://play.google.com";
			String description;
			double rating;

			String regex;
			Matcher matcher;

			regex = "<img.*?src=\"(.*?)\".*?>";
			matcher = findFirstMatchingPattern(webContent, regex);
			imgSrcUrl += matcher.group(1);
			imgName = imgSrcUrl.substring(imgSrcUrl.lastIndexOf('/') + 1, imgSrcUrl.lastIndexOf('='));

			regex = "class=\"title\".*?(title=\"(.*?)\").*?(href=\"(.*?)\").*?>";
			matcher = findFirstMatchingPattern(webContent, regex);
			title = matcher.group(2);
			link += matcher.group(4);

			regex = "class=\"subtitle\".*?(title=\"(.*?)\").*?>";
			matcher = findFirstMatchingPattern(webContent, regex);
			subTitle = matcher.group(2);

			regex = "class=\"tiny-star star-rating-non-editable-container\" "
					+ "aria-label=\".*?5.*?([0-9].[0-9]).*?\".*?>";
			matcher = findFirstMatchingPattern(webContent, regex);
			rating = Double.parseDouble(matcher.group(1));
			
			regex = "<div class=\"description\">(.*?)<span";
			matcher = findFirstMatchingPattern(webContent, regex);
			description = matcher.group(1);
			
			
			
			String imgDownloadDir = "/home/hyesu/neon-workspace/WebCrawler/download";

			appList.add(new App(title, subTitle, imgSrcUrl, imgDownloadDir + "/"+imgName
									, link, rating, description));
		} // while

		return appList;
	}

	private static Matcher findFirstMatchingPattern(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		matcher.find();

		return matcher;
	}

	public static void makeJsonFile(ArrayList<App> appList, String jsonFilePath) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			objectMapper.writeValue(new File(jsonFilePath), appList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
