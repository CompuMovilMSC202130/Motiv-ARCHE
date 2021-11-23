package co.edu.javeriana.motivarche;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA3K2cEDo:APA91bFEmw4muVRToNfw26gAJxHCqYWefIItXmBFkK0sIWGGRRzKe6u1hgDmtLEldqtvkzsTzemtTOSMC5wWLCaX2nrt3viBcA0hXrsza0gLdl6Rz6rIe1LCqdfjw9Vlu_RW6Ilb9e0G"
    }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
