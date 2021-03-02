package dt.persistent.xml;

public class Question {
	private String text;
	private String text2;
	private QImage image;
	public String getText() {
		return text;
	}
	public String getText2() {
		return text2;
	}
	public void setText(String text) {
		this.text = text;
	}
	public void setText2(String text) {
		this.text2 = text;
	}
	public QImage getImage() {
		return image;
	}
	public void setImage(QImage image) {
		this.image = image;
	}
	
}
