/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ChatgptDich.config;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

public class Config {
  private final Properties props = new Properties();
  private File userConfigFile;

  public static Config load() {
    Config c = new Config();
    // 1) lớp mặc định trong classpath
    try (InputStream in = Config.class.getResourceAsStream("/app.properties")) {
      if (in != null) c.props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
    } catch (Exception ignore) {}

    // 2) file cục bộ trong project nếu có (không bắt buộc)
    File local = new File("config.local.properties");
    c.loadIfExists(local);

    // 3) file người dùng (~/.novel-translator/app.properties)
    File homeDir = new File(System.getProperty("user.home"), ".novel-translator");
    if (!homeDir.exists()) homeDir.mkdirs();
    File userFile = new File(homeDir, "app.properties");
    c.userConfigFile = userFile;
    c.loadIfExists(userFile);

    // 4) fallback từ ENV nếu có (không bắt buộc)
    c.copyIfEnvPresent("OPENAI_API_KEY");
    c.copyIfEnvPresent("DB_URL");
    c.copyIfEnvPresent("DB_USER");
    c.copyIfEnvPresent("DB_PASS");

    return c;
  }

  private void loadIfExists(File f) {
    if (f.exists() && f.isFile()) {
      try (InputStream in = Files.newInputStream(f.toPath())) {
        Properties p = new Properties();
        p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        // override
        for (String k : p.stringPropertyNames()) props.setProperty(k, p.getProperty(k));
      } catch (Exception ignore) {}
    }
  }

  private void copyIfEnvPresent(String key) {
    String v = System.getenv(key);
    if (v != null && !v.isBlank()) props.setProperty(key, v);
  }

  public String get(String key, String def) { return props.getProperty(key, def); }
  public void set(String key, String val) { props.setProperty(key, val==null? "": val); }

  public void saveUserConfig() throws IOException {
    if (userConfigFile == null) return;
    try (OutputStream out = Files.newOutputStream(userConfigFile.toPath())) {
      props.store(new OutputStreamWriter(out, StandardCharsets.UTF_8), "Novel Translator config");
    }
  }

  /** Hỏi người dùng nếu thiếu giá trị quan trọng, rồi lưu vào ~/.novel-translator/app.properties */
  public void ensureInteractive(Component parent) {
    // OPENAI_API_KEY
    if (isBlank(get("OPENAI_API_KEY", ""))) {
      String k = promptSecret(parent, "Nhập OPENAI_API_KEY (sẽ được lưu cục bộ, không commit):");
      if (k == null) throw new RuntimeException("Thiếu OPENAI_API_KEY");
      set("OPENAI_API_KEY", k.trim());
    }
    // DB_URL
    if (isBlank(get("DB_URL", ""))) {
      String def = "jdbc:mysql://localhost:3306/truyen?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh";
      String v = promptText(parent, "Nhập DB_URL:", get("DB_URL", def));
      if (v == null) throw new RuntimeException("Thiếu DB_URL");
      set("DB_URL", v.trim());
    }
    // DB_USER
    if (isBlank(get("DB_USER", ""))) {
      String v = promptText(parent, "Nhập DB_USER:", "root");
      if (v == null) throw new RuntimeException("Thiếu DB_USER");
      set("DB_USER", v.trim());
    }
    // DB_PASS
    if (get("DB_PASS", null) == null) set("DB_PASS", "");
    try { saveUserConfig(); } catch (IOException e) { /* không cản chạy */ }
  }

  private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

  private static String promptText(Component parent, String msg, String init) {
    return (String) JOptionPane.showInputDialog(parent, msg, "Cấu hình",
            JOptionPane.QUESTION_MESSAGE, null, null, init);
  }

  private static String promptSecret(Component parent, String msg) {
    JPasswordField pf = new JPasswordField(32);
    int ok = JOptionPane.showConfirmDialog(parent, pf, msg, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (ok == JOptionPane.OK_OPTION) return new String(pf.getPassword());
    return null;
  }
}