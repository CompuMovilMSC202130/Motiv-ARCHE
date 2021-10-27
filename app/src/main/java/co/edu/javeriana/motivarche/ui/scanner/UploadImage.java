package co.edu.javeriana.motivarche.ui.scanner;

public class UploadImage {

    private String nameImage;
    private String urlImage;

    public UploadImage(){

    }

    public UploadImage(String name, String url){
        if(name.trim().equals("")){
            name = "no name";
        }
        nameImage = name;
        urlImage = url;

    }

    public String getNameImage() {
        return nameImage;
    }

    public void setNameImage(String nameImage) {
        this.nameImage = nameImage;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }
}
