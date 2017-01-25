package main;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

public class WebTool {
	private static Proxy proxy;

	public static void setProxyServer(String ipAddr, int port){
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipAddr, port));	
	}
	
	public static String getWebContents(String urlPath) {
		StringBuilder webContents = new StringBuilder();

		try {
			URL url = new URL(urlPath);
			URLConnection con = (URLConnection) url.openConnection(proxy);

			InputStreamReader ir = new InputStreamReader(con.getInputStream(), "utf-8");
			BufferedReader br = new BufferedReader(ir);

			String line;
			while ((line = br.readLine()) != null) {
				webContents.append(line);
			}

			br.close();
			ir.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return webContents.toString();
	}

	public static void downloadImage(String imageUrl, String downloadPath) {
		try {
			URL url = new URL(imageUrl);
			URLConnection con = (URLConnection) url.openConnection(proxy);

			BufferedImage bimage = ImageIO.read(con.getInputStream());
			ImageIO.write(bimage, "png", new File(downloadPath));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
