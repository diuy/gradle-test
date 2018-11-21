package com.fortis.test

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.index.memory.MemoryIndex
import org.apache.lucene.queryparser.classic.QueryParser

class Test1 {
    static void main(String[] args) {
        def version = "1.0"
        Analyzer analyzer = new SimpleAnalyzer();
        MemoryIndex index = new MemoryIndex();
        index.addField("content", "Readings about Salmons and other select Alaska fishing Manuals", analyzer);
        index.addField("author", "Tales of James", analyzer)
        QueryParser parser = new QueryParser("content", analyzer);
        float score = index.search(parser.parse("+author:james +salmon~ +fish* manual~"));
        if (score > 0.0f) {
            System.out.println("it's a match");
        } else {
            System.out.println("no match found");
        }
        System.out.println("indexData=" + index.toString());

    }
}
