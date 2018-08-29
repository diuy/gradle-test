package com.fortis.test.calculator;

public class CalException extends Exception {
    private int line;
    private int column;
    private String msg;

    public CalException(int line,int column,String msg){
        this.line=line;
        this.column=column;
        this.msg=msg;
    }

    @Override
    public String toString() {
        return msg+",line:"+line+",column:"+column;
    }
}