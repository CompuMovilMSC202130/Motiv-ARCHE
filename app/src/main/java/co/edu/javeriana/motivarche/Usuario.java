package co.edu.javeriana.motivarche;

public class Usuario {

    private String id;
    private String username;
    private String imageURL;
    private String email;
    private String provider;

    public Usuario(String id, String username, String imageURL, String email, String provider) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.email = email;
        this.provider = provider;
    }

    public Usuario() {
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
