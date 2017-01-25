package main;

public class App{
	public String title;
	public String subTitle;
	public String imgUrl;
	public String imgLocalPath;
	public String link;
	public double rating;
	public String description;

	public App(String title, String subTitle, String imgUrl, String imgLocalPath
				, String link, double rating, String description) {
		super();
		this.title = title;
		this.subTitle = subTitle;
		this.imgUrl = imgUrl;
		this.imgLocalPath = imgLocalPath;
		this.link = link;
		this.rating = rating;
		this.description = description;
	}
}
