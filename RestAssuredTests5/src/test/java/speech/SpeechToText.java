package speech;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SpeechToText {
    private static final String API_KEY = System.getenv("KEY");

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://image-ai.portnov.com/api/Speech";
    }

    @Test
    public void testConvertSpeechToText() {
        File audioFile = new File(getClass().getClassLoader().getResource("test_speech.mp3").getFile());

        Response response = given()
                .contentType(ContentType.MULTIPART)
                .header("X-Api-Key", API_KEY)
                .multiPart("audioFile", audioFile)
                .when()
                .post("/convert-speech-to-text")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response();

        String transcript = response.path("jsonResponse.results.transcripts[0].transcript");
        assertThat(transcript, equalTo("Hello, this is a test for text to speech conversion."));

        String result = response.path("jsonResponse.status");
        assertThat(result, equalTo("COMPLETED"));
        boolean validMessage = false;
        if (!transcript.contains("next")) {
            validMessage = true;
        }
        assertThat(validMessage, equalTo(true));

        List<Integer> segments = response.path("jsonResponse.results.audio_segments[0].items");
        assertThat(segments.size(), greaterThan(10));



//        String transcript = response.path("jsonResponse.results.transcripts[0].transcript");
//        assertThat(transcript, equalTo("Hello, this is a test for text to speech conversion."));
//
//        String result = response.path("jsonResponse.status");
//        assertThat(result, equalTo("COMPLETED"));
//
//        boolean validMessage = false;
//        if(!transcript.contains("next")) {
//            validMessage = true;
//        }
//        assertThat(validMessage, equalTo(true));
//
//        List<Integer> segments = response.path("jsonResponse.results.audio_segments[0].items");
//        assertThat(segments.size(), greaterThan(10));
    }
}
