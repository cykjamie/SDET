package speech;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SpeechToTextAssignment {
    private static final String API_KEY = System.getenv("KEY");

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://image-ai.portnov.com/api/Speech";
    }

    @Test
    public void testConvertSpeechToText() {
        File audioFile = new File(getClass().getClassLoader().getResource("TechnicalJargon.mp3").getFile());

        Response response = given()
                .contentType(ContentType.MULTIPART)
                .header("X-Api-Key", API_KEY)
                .multiPart("audioFile", audioFile)
                .when()
                .post("convert-speech-to-text")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response();

        String jargonTranscript = response.path("jsonResponse.results.transcripts[0].transcript");
        assertThat(jargonTranscript, equalTo("The API endpoints for post and get requests are well documented with their respective status codes."));
    }
}
