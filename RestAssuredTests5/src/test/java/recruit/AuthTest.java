package recruit;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthTest {

    private static Map<String, String> env;

    @BeforeAll
    public static void setup() throws IOException {

        env = readJsonFromFile("src/test/resources/environment.json");
        RestAssured.baseURI = env.get("baseURI");

    }


    @Test
    @Order(1)
    public void testLogin() throws IOException {
        String credentials = new String(Files.readAllBytes(Paths.get("src/test/resources/studentLogin.json")));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/login")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("authResponseSchema.json"))
                .extract().response();

        Assertions.assertEquals(200, response.getStatusCode());

        // Validating token expiration
        int issuedAt = response.path("issuedAt");
        int expiresAt = response.path("expiresAt");

        // Calculating the time difference in seconds
        int diffTime = expiresAt - issuedAt;
        //this is about 24 hours

        // Asserting that the time difference is 24 hours (86400 seconds)
        Assertions.assertEquals(86400, diffTime, "The time difference between issuedAt and expiresAt should be 24 hours.");

        env.put("token", response.path("token"));
        saveJsonToFile("src/test/resources/environment.json", env);
        //Token is saved in the environment
    }

    @Test
    @Order(2)
    public void testVerifyRequestWithToken() {
        given()
                .config(RestAssuredConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .header("Authorization", "Bearer " + env.get("token"))
                .when()
                .post("/verify")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("verifyResponseSchema.json"));
    }

    private static Map<String, String> readJsonFromFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return new com.fasterxml.jackson.databind.ObjectMapper().readValue(content, Map.class);
    }

    private static void saveJsonToFile(String filePath, Map<String, String> data) throws IOException {
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(Paths.get(filePath).toFile(), data);
    }
}

