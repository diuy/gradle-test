package com.fortis.test

import groovy.sql.Sql
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongPoint
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

import java.nio.file.Paths

class Test2 {
    static void indexData(IndexWriter writer) {
        String mysqlUrl = 'jdbc:mysql://172.20.1.233/drugdb?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&serverTimezone=GMT&tinyInt1isBit=false'
        String user = 'root'
        String password = 'Pass1234'
        String driver = 'com.mysql.jdbc.Driver'
        def mysql = Sql.newInstance(mysqlUrl, user, password, driver)
        def sql = 'SELECT drug_id,cn_name,common_name,short_name1 FROM b_drug WHERE drug_id> ? ORDER BY drug_id LIMIT 1000'
        long id = 0
        while (true) {
            def results = mysql.rows(sql, id)
            if (results.isEmpty())
                break
            for (def result : results) {
                long drug_id = result.drug_id as long
                String cn_name = result.cn_name as String
                String common_name = result.common_name as String
                String short_name1 = result.short_name1 as String
                id = drug_id
                Document doc = new Document()
                doc.add(new StringField("id", drug_id as String, Field.Store.YES))
                if (cn_name) doc.add(new TextField("cn_name", cn_name, Field.Store.YES))
                if (common_name) doc.add(new TextField("common_name", common_name, Field.Store.YES))
                if (short_name1) doc.add(new TextField("short_name1", short_name1, Field.Store.YES))
                writer.addDocument(doc)
            }
        }
    }

    static void index() {
        Directory directory = FSDirectory.open(Paths.get("lucene_index"))
        // 标准分词器
        Analyzer analyzer = new StandardAnalyzer();
        //保存用于创建IndexWriter的所有配置。
        IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer)
        //实例化IndexWriter
        IndexWriter writer = new IndexWriter(directory, iwConfig)
        long t1 = System.currentTimeMillis()
        indexData(writer)
        long t2 = System.currentTimeMillis()
        println "index over use:${t2 - t1}"
        writer.close()
    }

    static void search(String q) {
        Directory dir = FSDirectory.open(Paths.get("lucene_index"))
        // 通过dir得到的路径下的所有的文件
        IndexReader reader = DirectoryReader.open(dir)
        // 建立索引查询器
        IndexSearcher is = new IndexSearcher(reader)
        // 实例化分析器
        Analyzer analyzer = new StandardAnalyzer()
        // 建立查询解析器
        /**
         * 第一个参数是要查询的字段； 第二个参数是分析器Analyzer
         */
        QueryParser parser = new QueryParser("cn_name", analyzer)
        // 根据传进来的p查找
        Query query = parser.parse(q)
        // 计算索引开始时间
        long start = System.currentTimeMillis();
        // 开始查询
        /**
         * 第一个参数是通过传过来的参数来查找得到的query； 第二个参数是要出查询的行数
         */
        TopDocs hits = is.search(query, 10)
        // 计算索引结束时间
        long end = System.currentTimeMillis()
        System.out.println("匹配 " + q + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录")

        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc)
            println doc
//            System.out.println("id:${doc.get("id")} common_name:${doc.get("common_name")}")
        }

        // 关闭reader
        reader.close()
    }


    static void main(String[] args) {
//         index()
        search(" 阿莫  头孢")
//        search(" 阿莫 ")

    }
}
