/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.service;
import javazoom.jl.player.Player;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 *
 * @author Admin
 */
public class FPTTTSService {
    private static final String API_URL = "https://api.fpt.ai/hmi/tts/v5";
    private static final String API_KEY = "umx1vVG11JV5SytKJhAA99HI64hE21xQ"; 

    // Hàm gọi API FPT để lấy link mp3
    private String callTTS(String text) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("api-key", API_KEY);
        conn.setRequestProperty("voice", "banmai");
        conn.setRequestProperty("speed", "0");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(text.getBytes("UTF-8"));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        JSONObject json = new JSONObject(response.toString());
        return json.getString("async");
    }

    
    private void playFromURL(String mp3Url) throws Exception {
        URL url = new URL(mp3Url);
        InputStream is = null;

        int retry = 0;
        while (retry < 5) { 
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                if (code == 200) {
                    is = conn.getInputStream();
                    break;
                } else {
                    System.out.println("File chưa sẵn sàng, code = " + code + ". Thử lại...");
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                System.out.println("Lỗi khi truy cập file: " + e.getMessage());
                Thread.sleep(1000);
            }
            retry++;
        }

        if (is == null) {
            throw new FileNotFoundException("Không thể lấy file mp3 sau khi thử nhiều lần!");
        }

        Player player = new Player(is);
        player.play();
    }

   
    public void speak(String text) {
        try {
            String mp3Url = callTTS(text);
            System.out.println("Streaming from: " + mp3Url);
            playFromURL(mp3Url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
