package com.mycompany.quanlytruyen.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;
import java.io.File;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * IMPROVED SeleniumClaudeService with:
 * - Retry logic
 * - Better validation
 * - Enhanced error handling
 * - Configurable timeouts
 * - Screenshot on errors
 */
public class SeleniumClaudeService {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String CLAUDE_URL = "https://claude.ai/new";
    private boolean isLoggedIn = false;
    private String projectUrl = null;
    private boolean projectOpened = false;
    
    // Configuration
    private int maxRetries = 3;
    private int retryDelaySeconds = 5;
    private int defaultTimeout = 30;
    private int responseTimeout = 180;
    private String screenshotDir = "screenshots";
    
    public SeleniumClaudeService() {
        createScreenshotDirectory();
    }
    
    // ==================== CONFIGURATION METHODS ====================
    
    public void setMaxRetries(int retries) {
        this.maxRetries = retries;
    }
    
    public void setRetryDelay(int seconds) {
        this.retryDelaySeconds = seconds;
    }
    
    public void setDefaultTimeout(int seconds) {
        this.defaultTimeout = seconds;
    }
    
    public void setResponseTimeout(int seconds) {
        this.responseTimeout = seconds;
    }
    
    public void setScreenshotDirectory(String dir) {
        this.screenshotDir = dir;
        createScreenshotDirectory();
    }
    
    private void createScreenshotDirectory() {
        try {
            Files.createDirectories(Paths.get(screenshotDir));
        } catch (Exception e) {
            System.err.println("Cannot create screenshot directory: " + e.getMessage());
        }
    }
    
    // ==================== VALIDATION METHODS ====================
    
    /**
     * Validate user data directory exists
     */
    private void validateUserDataDir(String userDataDir) {
        if (userDataDir == null || userDataDir.trim().isEmpty()) {
            throw new IllegalArgumentException("User Data Directory không được để trống!");
        }
        
        File dir = new File(userDataDir);
        if (!dir.exists()) {
            throw new IllegalArgumentException(
                "User Data Directory không tồn tại: " + userDataDir + "\n" +
                "Vui lòng kiểm tra đường dẫn!"
            );
        }
        
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(
                "Đường dẫn không phải là thư mục: " + userDataDir
            );
        }
    }
    
    /**
     * Validate profile name
     */
    private void validateProfileName(String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile Name không được để trống!");
        }
    }
    
    /**
     * Validate translation inputs
     */
    private void validateTranslationInputs(String content, String rules, String charTable) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung chương không được để trống!");
        }
        
        if (!isUsingProject()) {
            if (rules == null || rules.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Yêu cầu dịch không được để trống! " +
                    "Hoặc sử dụng Claude Project."
                );
            }
        }
        
        if (content.length() > 500000) {
            throw new IllegalArgumentException(
                "Nội dung quá dài (" + content.length() + " ký tự). " +
                "Giới hạn 500,000 ký tự."
            );
        }
    }
    
    // ==================== PROJECT MANAGEMENT ====================
    
    public void setProjectUrl(String url) {
        /*
        if (url != null && !url.trim().isEmpty()) {
            if (!url.contains("claude.ai/project/")) {
        */
        String sanitizedUrl = (url != null) ? url.trim() : null;
        if (sanitizedUrl != null && !sanitizedUrl.isEmpty()) {
            if (!sanitizedUrl.contains("claude.ai/project/")) {
                throw new IllegalArgumentException(
                    "URL không hợp lệ! Phải là: https://claude.ai/project/..."
                );
            }
        }
        //this.projectUrl = url;
        this.projectUrl = sanitizedUrl;
        this.projectOpened = false;
        //System.out.println("✓ Đã set Project URL: " + url);
        
        if (sanitizedUrl == null) {
            System.out.println("✓ Đã xóa Project URL - sẽ dùng conversation mới");
        } else {
            System.out.println("✓ Đã set Project URL: " + sanitizedUrl);
        }
    }
    
    public boolean isUsingProject() {
        return projectUrl != null && !projectUrl.trim().isEmpty() 
               && projectUrl.contains("claude.ai/project/");
    }
    
    public void resetProject() {
        this.projectOpened = false;
        System.out.println("✓ Đã reset Project flag");
    }
    
    // ==================== BROWSER INITIALIZATION ====================
    
    /**
     * Initialize browser with validation
     */
    public void initWithProfile(String userDataDir, String profileName) {
        // Validate inputs
        validateUserDataDir(userDataDir);
        validateProfileName(profileName);
        
        System.out.println("Đang khởi tạo Chrome với profile: " + profileName);
        
        try {
            WebDriverManager.chromedriver().setup();
            
            ChromeOptions options = new ChromeOptions();
            
            // Random debug port
            int debugPort = 9222 + (int)(Math.random() * 1000);
            options.addArguments("--remote-debugging-port=" + debugPort);
            /*
            // Profile settings
            options.addArguments("user-data-dir=" + userDataDir);
            options.addArguments("profile-directory=" + profileName);
            */
            options.addArguments("--user-data-dir=" + userDataDir);
            options.addArguments("--profile-directory=" + profileName);            
            
            // Additional options
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout));
            
            System.out.println("✓ Chrome đã khởi động (debug port: " + debugPort + ")");
            
        } catch (Exception e) {
            throw new RuntimeException(
                "Không thể khởi động Chrome!\n" +
                "Lỗi: " + e.getMessage() + "\n" +
                "Hãy đảm bảo:\n" +
                "1. Đã đóng tất cả Chrome\n" +
                "2. Đường dẫn User Data Dir đúng\n" +
                "3. Profile Name đúng",
                e
            );
        }
    }
    
    // ==================== LOGIN CHECK ====================
    
    /**
     * Check login with retry
     */
    public boolean checkLogin() {
        return executeWithRetry("Kiểm tra đăng nhập", () -> {
            driver.get(CLAUDE_URL);
            Thread.sleep(3000);
            
            try {
                driver.findElement(By.cssSelector("div[contenteditable='true']"));
                isLoggedIn = true;
                System.out.println("✓ Đã đăng nhập Claude");
                return true;
            } catch (NoSuchElementException e) {
                isLoggedIn = false;
                System.out.println("✗ Chưa đăng nhập");
                return false;
            }
        }, 1); // Only retry once for login check
    }
    
    // ==================== TRANSLATION WITH RETRY ====================
    
    /**
     * Translate with retry and validation
     */
    public String translate(String chapterContent, String translationRules, String characterTable) {
        // Validate login
        if (!isLoggedIn) {
            throw new RuntimeException(
                "Chưa đăng nhập Claude!\n" +
                "Vui lòng kiểm tra đăng nhập trước khi dịch."
            );
        }
        
        // Validate inputs
        validateTranslationInputs(chapterContent, translationRules, characterTable);
        
        // Execute translation with retry
        return executeWithRetry("Dịch chương", () -> {
            return translateInternal(chapterContent, translationRules, characterTable);
        }, maxRetries);
    }
    
    /**
     * Internal translation logic
     */
    private String translateInternal(String content, String rules, String charTable) throws Exception {
        System.out.println("Bắt đầu dịch chương...");
        
        // Navigate to project or new conversation
        if (isUsingProject()) {
            if (!projectOpened) {
                System.out.println("Mở Project lần đầu: " + projectUrl);
                driver.get(projectUrl);
                Thread.sleep(3000);
                projectOpened = true;
            } else {
                System.out.println("Dùng Project đang mở");
                scrollToBottom();
                Thread.sleep(1000);
            }
        } else {
            System.out.println("Mở conversation mới");
            driver.get(CLAUDE_URL);
            Thread.sleep(3000);
        }
        
        // Build prompt
        String prompt = isUsingProject() 
            ? buildProjectPrompt(content)
            : buildPrompt(content, rules, charTable);
        
        System.out.println("Độ dài prompt: " + prompt.length() + " ký tự");
        
        // Send prompt
        sendPrompt(prompt);
        
        // Wait and get response
        String response = waitAndGetResponse();
        
        // Validate response
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("Response rỗng!");
        }
        
        if (response.startsWith("ERROR:")) {
            throw new RuntimeException(response);
        }
        
        System.out.println("✓ Đã nhận bản dịch (" + response.length() + " ký tự)");
        return response;
    }
    
    // ==================== HELPER METHODS ====================
    
    private void scrollToBottom() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        } catch (Exception e) {
            System.err.println("Cannot scroll: " + e.getMessage());
        }
    }
    
    private void sendPrompt(String prompt) throws Exception {
        // Find input box with timeout
        WebElement inputBox = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("div[contenteditable='true']")
            )
        );
        
        inputBox.click();
        Thread.sleep(500);
        
        // Copy to clipboard
        Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(new StringSelection(prompt), null);
        
        // Paste
        inputBox.sendKeys(Keys.chord(Keys.CONTROL, "v"));
        Thread.sleep(1000);
        
        // Send
        inputBox.sendKeys(Keys.RETURN);
        Thread.sleep(2000);
        
        System.out.println("✓ Đã gửi prompt!");
    }
    
    private String buildProjectPrompt(String content) {
        return "Hãy dịch chương sau sang tiếng Việt theo context đã có trong Project:\n\n" + content;
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
    
    private String waitAndGetResponse() throws InterruptedException {
        System.out.println("Đang đợi Claude trả lời...");
        Thread.sleep(3000);
        
        int waited = 0;
        while (waited < responseTimeout) {
            if (!isClaudeTyping()) {
                System.out.println("✓ Claude đã hoàn thành");
                Thread.sleep(2000);
                break;
            }
            
            if (waited % 10 == 0) {
                System.out.println("Đang đợi... (" + waited + "s/" + responseTimeout + "s)");
            }
            
            Thread.sleep(1000);
            waited++;
        }
        
        if (waited >= responseTimeout) {
            throw new RuntimeException("Timeout! Claude không phản hồi sau " + responseTimeout + "s");
        }
        
        return extractResponse();
    }
    
    private boolean isClaudeTyping() {
        String[] indicators = {
            "div.typing-indicator",
            "div[class*='loading']",
            "div[class*='generating']",
            "button[aria-label*='Stop']"
        };
        
        for (String indicator : indicators) {
            try {
                driver.findElement(By.cssSelector(indicator));
                return true;
            } catch (NoSuchElementException e) {
                // Continue checking
            }
        }
        return false;
    }
    
    private String extractResponse() {
        String[] selectors = {
            "(//div[contains(@class, 'font-claude-message')])[last()]",
            "(//div[@data-role='assistant'])[last()]",
            "(//div[contains(@class, 'assistant-message')])[last()]",
            "(//div[contains(@class, 'message-content')])[last()]"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(selector));
                if (!elements.isEmpty()) {
                    String text = elements.get(elements.size() - 1).getText();
                    if (text != null && !text.trim().isEmpty()) {
                        return text;
                    }
                }
            } catch (Exception e) {
                // Try next selector
            }
        }
        
        // Fallback
        try {
            return driver.findElement(By.tagName("body")).getText();
        } catch (Exception e) {
            throw new RuntimeException("Không thể trích xuất response!", e);
        }
    }
    
    // ==================== RETRY MECHANISM ====================
    
    /**
     * Execute operation with retry logic
     */
    private <T> T executeWithRetry(String operation, RetryableOperation<T> task, int maxRetries) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            try {
                if (attempt > 0) {
                    System.out.println("Thử lại lần " + (attempt + 1) + "/" + maxRetries + "...");
                    Thread.sleep(retryDelaySeconds * 1000);
                }
                
                return task.execute();
                
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                System.err.println("✗ " + operation + " thất bại (lần " + attempt + "): " + e.getMessage());
                
                // Take screenshot on error
                if (driver != null) {
                    String filename = String.format("%s/error_%s_attempt%d_%d.png",
                        screenshotDir,
                        operation.replaceAll("[^a-zA-Z0-9]", "_"),
                        attempt,
                        System.currentTimeMillis()
                    );
                    takeScreenshot(filename);
                }
                
                if (attempt >= maxRetries) {
                    break;
                }
            }
        }
        
        // All retries failed
        throw new RuntimeException(
            operation + " thất bại sau " + maxRetries + " lần thử!\n" +
            "Lỗi cuối: " + (lastException != null ? lastException.getMessage() : "Unknown"),
            lastException
        );
    }
    
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
    
    // ==================== SCREENSHOT ====================
    
    public void takeScreenshot(String filename) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(
                screenshot.toPath(),
                Paths.get(filename),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            System.out.println("📸 Screenshot: " + filename);
        } catch (Exception e) {
            System.err.println("Cannot take screenshot: " + e.getMessage());
        }
    }
    
    // ==================== CLEANUP ====================
    
    public void close() {
        shutdownExistingSession();
    }

    private void shutdownExistingSession() {
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("✓ Đã đóng browser");
            } catch (Exception e) {
                System.err.println("Error closing browser: " + e.getMessage());
            } finally {
                driver = null;
                wait = null;
                isLoggedIn = false;
            }
        }
    }
}