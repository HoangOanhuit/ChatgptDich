package com.mycompany.quanlytruyen.service;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

public class SeleniumChatGPTService {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String CHATGPT_URL = "https://chat.openai.com/";
    private boolean isLoggedIn = false;
    
    public void initWithProfile(String userDataDir, String profileName) {
        System.out.println("Khởi tạo Chrome cho ChatGPT...");
        
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + userDataDir);
        options.addArguments("profile-directory=" + profileName);
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--disable-notifications");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        System.out.println("Chrome đã khởi động!");
    }
    
    public boolean checkLogin() {
        try {
            driver.get(CHATGPT_URL);
            Thread.sleep(3000);
            
            // Kiểm tra textarea input
            try {
                driver.findElement(By.id("prompt-textarea"));
                isLoggedIn = true;
                System.out.println("✓ Đã đăng nhập ChatGPT");
                return true;
            } catch (NoSuchElementException e) {
                System.out.println("✗ Chưa đăng nhập ChatGPT");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String translate(String chapterContent, String translationRules, String characterTable) {
        if (!isLoggedIn) {
            throw new RuntimeException("Chưa đăng nhập ChatGPT!");
        }
        
        try {
            System.out.println("Bắt đầu dịch với ChatGPT...");
            
            // Navigate to ChatGPT
            driver.get(CHATGPT_URL);
            Thread.sleep(2000);
            
            // Find input textarea
            WebElement inputBox = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("prompt-textarea"))
            );
            
            // Build and send prompt
            String prompt = buildPrompt(chapterContent, translationRules, characterTable);
            inputBox.click();
            inputBox.sendKeys(prompt);
            
            // Send (press Enter)
            inputBox.sendKeys(Keys.RETURN);
            Thread.sleep(3000);
            
            // Wait for response
            String response = waitAndGetResponse();
            
            System.out.println("✓ Nhận được bản dịch từ ChatGPT!");
            return response;
            
        } catch (Exception e) {
            System.err.println("✗ Lỗi: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }
    
    private String waitAndGetResponse() throws InterruptedException {
        System.out.println("Đợi ChatGPT trả lời...");
        
        Thread.sleep(5000); // Đợi bắt đầu
        
        // Đợi cho đến khi không còn "Stop generating" button
        int maxWait = 180;
        int waited = 0;
        
        while (waited < maxWait) {
            try {
                // Nếu tìm thấy "Stop generating" = đang generate
                driver.findElement(By.xpath("//button[contains(., 'Stop generating')]"));
                
                if (waited % 10 == 0) {
                    System.out.println("Đang đợi... (" + waited + "s)");
                }
                
                Thread.sleep(1000);
                waited++;
            } catch (NoSuchElementException e) {
                // Không còn button = đã xong
                System.out.println("✓ ChatGPT đã hoàn thành");
                Thread.sleep(2000);
                break;
            }
        }
        
        // Extract response
        return extractResponse();
    }
    
    private String extractResponse() {
        try {
            // ChatGPT response selectors
            String[] selectors = {
                "(//div[@data-message-author-role='assistant'])[last()]//div[contains(@class, 'markdown')]",
                "(//div[@data-message-author-role='assistant'])[last()]",
                "(//div[contains(@class, 'agent-turn')])[last()]"
            };
            
            for (String selector : selectors) {
                try {
                    WebElement element = driver.findElement(By.xpath(selector));
                    String text = element.getText();
                    
                    if (text != null && !text.trim().isEmpty()) {
                        System.out.println("✓ Đã lấy response (độ dài: " + text.length() + " ký tự)");
                        return text;
                    }
                } catch (Exception e) {
                    // Try next selector
                }
            }
            
            throw new RuntimeException("Không thể trích xuất response");
            
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }
    
    private String buildPrompt(String content, String rules, String charTable) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("=== YÊU CẦU DỊCH ===\n");
        prompt.append(rules);
        prompt.append("\n\n");
        
        if (charTable != null && !charTable.trim().isEmpty()) {
            prompt.append("=== BẢNG TÊN NHÂN VẬT ===\n");
            prompt.append(charTable);
            prompt.append("\n\n");
        }
        
        prompt.append("=== NỘI DUNG CẦN DỊCH ===\n");
        prompt.append(content);
        prompt.append("\n\nHãy dịch nội dung trên sang tiếng Việt theo yêu cầu. Chỉ trả về bản dịch.");
        
        return prompt.toString();
    }
    
    public void close() {
        if (driver != null) {
            driver.quit();
            System.out.println("Đã đóng browser");
        }
    }
}