/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ChatgptDich.service;

import com.mycompany.ChatgptDich.dao.ChapterDao;
import com.mycompany.ChatgptDich.utils.FileUtils;
import com.mycompany.ChatgptDich.utils.PromptBuilder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TranslationRunner {
  public record Progress(int done, int total, String message) {}

  public interface ProgressListener { void onProgress(Progress p); }

  private final OpenAIClient openAI;
  private final ChapterDao chapterDao;
  private final ProgressListener listener;

  public TranslationRunner(OpenAIClient openAI, ChapterDao chapterDao, ProgressListener listener) {
    this.openAI = openAI;
    this.chapterDao = chapterDao;
    this.listener = listener;
  }

  public void processFiles(long bookId, List<File> chapterFiles, String guidelines, String nameTableBlock, String model) {
    int total = chapterFiles.size(), done = 0;
    for (File f : chapterFiles) {
      int chNo = FileUtils.extractChapterNumber(f.getName());
      try {
        chapterDao.upsertPending(bookId, chNo, f.getName());
        String src = Files.readString(f.toPath());

        String prompt = PromptBuilder.build(guidelines, nameTableBlock, src);
        String translated = callOpenAIWithRetry(model, prompt, 4);

        chapterDao.updateDone(bookId, chNo, translated, FileUtils.wordCount(translated));
        done++;
        listener.onProgress(new Progress(done, total, "DONE C" + chNo));
        Thread.sleep(200); // nho nhỏ để “dịu” rate limit
      } catch (Exception ex) {
        try { chapterDao.updateError(bookId, chNo, ex.getMessage()); } catch (Exception ignore) {}
        listener.onProgress(new Progress(done, total, "ERROR C" + chNo + " -> " + ex.getMessage()));
      }
    }
  }

  public void processPendingOrError(long bookId, String guidelines, String nameTableBlock, String model) {
    try {
      List<Integer> list = chapterDao.listPendingOrError(bookId);
      int total = list.size(), done = 0;
      for (int chNo : list) {
        try {
          // ở flow resume này, bạn có thể đọc lại file gốc nếu cần (tuỳ bạn đã lưu source ở đâu)
          // giả sử đặt cùng thư mục và tên C{SỐ}.txt; ở đây minh hoạ bỏ qua đọc file gốc.
          throw new IllegalStateException("Bạn cần cung cấp lại source để resume theo cách của bạn.");
        } catch (Exception ex) {
          try { chapterDao.updateError(bookId, chNo, ex.getMessage()); } catch (Exception ignore) {}
          listener.onProgress(new Progress(done, total, "ERROR C" + chNo + " -> " + ex.getMessage()));
        }
      }
    } catch (Exception e) {
      listener.onProgress(new Progress(0, 0, "Resume lỗi: " + e.getMessage()));
    }
  }

  private String callOpenAIWithRetry(String model, String prompt, int maxRetry) throws Exception {
    int attempt = 0;
    while (true) {
      try {
        return openAI.translate(model, prompt);
      } catch (Exception ex) {
        attempt++;
        if (attempt >= maxRetry) throw ex;
        long backoff = (long) (Math.pow(2, attempt) * 500L);
        listener.onProgress(new Progress(0, 0, "(retry " + attempt + ") " + ex.getMessage()));
        Thread.sleep(backoff);
      }
    }
  }
}