package com.kars.fire.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.gson.JsonObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final String PROJECT_ID = "<project_id>";
    private static final String BASE_URL = "https://fcm.googleapis.com";
    public static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE};

    public String title;
    public String message;
    public String token;


    @RequestMapping("/hello")
    @ResponseBody
    public String hello() throws FirebaseMessagingException {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle("我是push标题")
                        .setBody("我是你push的内容")
                        .build())
                .setToken("")
                .build();
        String response = FirebaseMessaging.getInstance().send(message);
        // Response is a message ID string.
        System.out.println("Successfully sent message: " + response);
        return "112";
    }

    private static String getAccessToken() throws IOException {
        ClassPathResource cpr = new ClassPathResource("serviceAccountKey.json");
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(cpr.getInputStream())
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refreshAccessToken();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    private static HttpURLConnection getConnection() throws IOException {
        // [START use_access_token]
        URL url = new URL(BASE_URL + FCM_SEND_ENDPOINT);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        String accessToken = getAccessToken();
        System.out.println(accessToken);
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
        httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
        return httpURLConnection;
        // [END use_access_token]
    }

    private JsonObject buildNotificationMessage() {
        JsonObject jNotification = new JsonObject();
        jNotification.addProperty("title", this.title);
        jNotification.addProperty("body", this.message);

        JsonObject jMessage = new JsonObject();
        jMessage.add("notification", jNotification);
        jMessage.addProperty("token", this.token);

        JsonObject jFcm = new JsonObject();
        jFcm.add("message", jMessage);

        return jFcm;
    }

    private static void sendtoSingleDevice(JsonObject fcmMessage) throws IOException {
        HttpURLConnection connection = getConnection();
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(fcmMessage.toString());
        outputStream.flush();
        outputStream.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String response = inputstreamToString(connection.getInputStream());
            System.out.println("Message sent to Firebase for delivery, response:");
            System.out.println(response);
        } else {
            System.out.println("Unable to send message to Firebase:");
            String response = inputstreamToString(connection.getErrorStream());
            System.out.println(response);
        }
    }

    public static String inputstreamToString(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];
        for   (int  n; (n = in.read(b)) != -1;)   {
            out.append(new String(b,  0,   n));
        }
        return out.toString();
    }



}
