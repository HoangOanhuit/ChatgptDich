/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ChatgptDich.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Comparator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
  public static List<File> listFilesSorted(File dir, Predicate<File> filter, Comparator<File> sorter) {
    File[] arr = dir.listFiles((f)->f.isFile() && (filter==null || filter.test(f)));
    if (arr==null) return List.of();
    List<File> list = new ArrayList<>(Arrays.asList(arr));
    if (sorter!=null) list.sort(sorter);
    return list;
  }

  public static int extractChapterNumber(String filename) {
    Matcher m = Pattern.compile("C(\\d+)\\.txt", Pattern.CASE_INSENSITIVE).matcher(filename);
    if (m.matches()) return Integer.parseInt(m.group(1));
    return -1;
  }

  public static int wordCount(String text) {
    if (text==null || text.isBlank()) return 0;
    return text.trim().split("\\s+").length;
  }

  public static String readUtf8(File f) throws IOException {
    return Files.readString(f.toPath());
  }
}
