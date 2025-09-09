/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.awt.Image;
/**
 *
 * @author Admin
 */
public class ImageUtils {
     public static String saveImage(File sourceFile,String destinationFolder) throws IOException {
          String destDir = destinationFolder != null ? destinationFolder : "assets/images/";
        File destFolder = new File(destDir);
        if (!destFolder.exists()) {
            destFolder.mkdirs(); // tạo thư mục nếu chưa có
        }

        // Tạo tên file duy nhất
        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        Path destination = Paths.get(destDir + fileName);
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString(); // trả về đường dẫn ảnh đã lưu
    }

    public static File chooseImageFile(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    public static ImageIcon loadImage(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }
        try {
            // Normalize path separators to avoid platform issues
            String normalized = imagePath.replace("\\", "/");
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }            
            
            Path path = Paths.get(normalized);
            if (!path.isAbsolute()) {
                // Thử tìm tương đối theo thư mục làm việc hiện tại
                Path base = Paths.get(System.getProperty("user.dir")).resolve(path);
                if (!Files.exists(base)) {
                    // Nếu chạy từ file jar, thư mục làm việc có thể khác
                    // nên thử tìm theo vị trí của file jar
                    java.net.URL codeLocation = ImageUtils.class
                            .getProtectionDomain().getCodeSource().getLocation();
                    if (codeLocation != null) {
                        base = Paths.get(codeLocation.toURI()).getParent().resolve(path);
                    }
                }
                path = base.normalize();               
            }
            path = path.normalize();

            if (Files.exists(path)) {
                ImageIcon icon = new ImageIcon(path.toString());
                if (width > 0 && height > 0) {
                    Image img = icon.getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                }
                return icon;
            }

            java.net.URL url = ImageUtils.class.getClassLoader().getResource(normalized);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                if (width > 0 && height > 0) {
                    Image img = icon.getImage()
                            .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                }
                return icon;
            }
        } catch (Exception e) {
            // In lỗi để dễ debug khi không tải được ảnh
            e.printStackTrace(); 
        }
        return null;
    }
}
//File selectedImage = ImageUtils.chooseImageFile(this);
//if (selectedImage != null) {
//    try {
//        String savedPath = ImageUtils.saveImage(selectedImage);
//        System.out.println("Ảnh đã lưu: " + savedPath);
//        employee.setAvatar(savedPath); // gán vào model để lưu DB
//    } catch (IOException ex) {
//        ex.printStackTrace();
//        JOptionPane.showMessageDialog(this, "Lỗi lưu ảnh");
//    }
//}