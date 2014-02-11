/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.runnables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import mailtest.MailTest;
import mailtest.SearchScene.SearchSceneController;
import mailtest.jpa.controllers.MailMessageJpaController;
import mailtest.jpa.entities.MailMessage;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;

/**
 *
 * @author v.georgiev
 */
public class SearchThread implements Runnable {
    private String fromField;
    private String toField;
    private String ccField;
    private String subjectField;
    private String bodyField;
    
    

    public SearchThread() {
    }

    public SearchThread(String fromField, String toField, String ccField, String subjectField, String bodyField) {
        this.fromField = fromField.trim();
        this.toField = toField.trim();
        this.ccField = ccField.trim();
        this.subjectField = subjectField.trim();
        this.bodyField = bodyField.trim();
    }
    
    @Override
    public void run() {
        SearchSceneController.getTable().getItems().clear();
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                SearchSceneController.getProgress().setVisible(true);
            }
        });
        int maxResults = 100;
        List<String> searchFields = new ArrayList<>();
        BooleanQuery bq = new BooleanQuery();

        if (!fromField.equals("")) {
//            sb.append("from:").append(fromField).append("~^4");
            FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("from", fromField.toLowerCase().trim()));
            fuzzyQuery.setBoost(4f);
            searchFields.add(SearchSceneController.FIELDS[0]);
            bq.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
        }
        if (!toField.equals("")) {
//            sb.append("to:").append(toField.getText()).append("~^4");
            FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("to", toField.toLowerCase().trim()));
            fuzzyQuery.setBoost(4f);
            bq.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
            searchFields.add(SearchSceneController.FIELDS[1]);
        }
        if (!ccField.equals("")) {
//            if (sb.length() != 0) {
//                sb.append(" AND ");
//            }
//            sb.append("cc:").append(toField.getText()).append("~^3");
            FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("cc", ccField.toLowerCase().trim()));
            fuzzyQuery.setBoost(3f);
            bq.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
            searchFields.add(SearchSceneController.FIELDS[2]);
        }
        if (!subjectField.equals("")) {
//            if (sb.length() != 0) {
//                sb.append(" AND ");
//            }
//            sb.append("subject:").append(toField.getText()).append("~^2");
            FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("subject", subjectField.toLowerCase().trim()));
            fuzzyQuery.setBoost(2f);
            bq.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
            searchFields.add(SearchSceneController.FIELDS[3]);
        }
        if (!bodyField.equals("")) {
//            if (sb.length() != 0) {
//                sb.append(" AND ");
//            }
//            sb.append("body:").append(toField.getText()).append("~^1");
            FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("body", bodyField.toLowerCase().trim()));
            fuzzyQuery.setBoost(1f);
            bq.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
            searchFields.add(SearchSceneController.FIELDS[4]);
        }
        try (StandardAnalyzer a = new StandardAnalyzer(Version.LUCENE_45)) {
            try (IndexReader reader = DirectoryReader.open(MailTest.getIndex())) {
                IndexSearcher searcher = new IndexSearcher(reader);
                TopScoreDocCollector collector = TopScoreDocCollector.create(maxResults, true);
                searcher.search(bq, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                MailMessageJpaController mmc = new MailMessageJpaController(MailTest.getEmf());
                for (ScoreDoc scoreDoc : hits) {
                    int docId = scoreDoc.doc;
                    int messageId = reader.document(docId).getField("messageId").numericValue().intValue();
                    final MailMessage mm = mmc.findMailMessageByMessageId(messageId);
                    String body = mm.getBody();
                    if (!fromField.equals("")) {
                        body = body.replaceAll(fromField, String.format("<span style=\"background-color:yellow\"> %s </span>", fromField));
                    }
                    if (!toField.equals("")) {
                        body = body.replaceAll(toField, String.format("<span style=\"background-color:yellow\"> %s </span>", toField));
                    }
                    if (!ccField.equals("")) {
                        body = body.replaceAll(ccField, String.format("<span style=\"background-color:yellow\"> %s </span>", ccField));
                    }
                    if (!subjectField.equals("")) {
                        body = body.replaceAll(subjectField, String.format("<span style=\"background-color:yellow\"> %s </span>", subjectField));
                    }
                    if (!bodyField.equals("")) {
                        body = body.replaceAll(bodyField, String.format("<span style=\"background-color:yellow\"> %s </span>", bodyField));
                    }
                    mm.setBody(body);
                    Platform.runLater(new Runnable() {
                        
                        @Override
                        public void run() {
                            SearchSceneController.getTable().getItems().add(mm);
                        }
                    });
                }
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        SearchSceneController.getProgress().setVisible(false);
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(SearchSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
