/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ChatgptDich.service;

import com.mycompany.ChatgptDich.dao.ChapterDao;
import com.mycompany.ChatgptDich.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 *
 * @author trang
 */
public class TranslationService {
    private final OpenAIClient openAI;
    private final ChapterDao chapterDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // chạy tuần tự để dễ kiểm soát chi phí

    public TranslationService(OpenAIClient client, ChapterDao dao) {
        this.openAI = client; this.chapterDao = dao;
    }

    public void processChapters(long bookId, List<File> chapterFiles,
                                String guidelines, String nameTable, String model,
                                Consumer<ProgressEvent> progressCb) throws IOException, SQLException {

        int total = chapterFiles.size();
        int done = 0;

        for (File f : chapterFiles) {
            int chNo = FileUtils.extractChapterNumber(f.getName()); // parse C123
            String src = FileUtils.readUtf8(f);
            try {
                chapterDao.upsertPending(bookId, chNo, f.getName(), src.length());
                String translation = openAI.translateChapter(model, guidelines, nameTable, src);
                chapterDao.updateDone(bookId, chNo, translation, countWords(translation));
                done++;
                progressCb.accept(new ProgressEvent(done, total, "DONE C" + chNo));
                // ngủ ngắn để tôn trọng rate limit nếu cần
                Thread.sleep(300);
            } catch (Exception ex) {
                chapterDao.updateError(bookId, chNo, ex.getMessage());
                progressCb.accept(new ProgressEvent(done, total, "ERROR C" + chNo + ": " + ex.getMessage()));
            }
        }
    }
    
}
