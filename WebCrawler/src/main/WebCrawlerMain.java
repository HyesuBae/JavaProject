package main;

import java.util.ArrayList;

public class WebCrawlerMain {

	public static void main(String args[]) {
		long startTime = System.currentTimeMillis();

		WebTool.setProxyServer(PROXY_IP, PROXY_PORT);

		// Get HTML source code from URL
		String urlPath = "https://play.google.com/store/apps/collection/topselling_free";
		String webContents = WebTool.getWebContents(urlPath);
		System.out.println("Get HTML source done.");

		// parsing
		ArrayList<App> appList = ParsingTool.parsing(webContents);
		System.out.println("HTML source parsing done.");
		
		// download image file
		downloadAppImage(appList);
		System.out.println("Download app image done.");
		
		// Make Json file from crawling data
		ParsingTool.makeJsonFile(appList, "/home/hyesu/neon-workspace/WebCrawler/json/appList.json");
		System.out.println("Make json file done.");

		long endTime = System.currentTimeMillis();
		System.out.println("execution time : " + (endTime - startTime));
	}
	
	private static void downloadAppImage(ArrayList<App> appList){
		for(App app : appList){
			WebTool.downloadImage(app.imgUrl, app.imgLocalPath);
		}
	}
}