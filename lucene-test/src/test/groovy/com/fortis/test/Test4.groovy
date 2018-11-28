package com.fortis.test

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.junit.Test

class Test4 {

    private static void displayToken(String str, Analyzer analyzer){
        try {
            //将一个字符串创建成Token流
            TokenStream stream  = analyzer.tokenStream("", new StringReader(str));
            stream.reset()
            //保存相应词汇
            CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
            while(stream.incrementToken()){
                System.out.print("[" + cta + "]");
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void test1(){
        Analyzer aly1 = new StandardAnalyzer();
        String str = "阿莫西林颗粒";
        displayToken(str, aly1);
    }
    @Test
    void test2(){
        Analyzer aly1 = new SmartChineseAnalyzer();
        displayToken( "阿莫西林颗粒",aly1);
        displayToken( "我是中国人",new SmartChineseAnalyzer());
        displayToken( "尽心竭力的工作",new SmartChineseAnalyzer());

    }
}
