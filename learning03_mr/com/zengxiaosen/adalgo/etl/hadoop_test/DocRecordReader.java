package com.zengxiaosen.adalgo.etl.hadoop_test;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;

import java.io.IOException;

/**
 * Created by Administrator on 2016/10/22.
 * 对文档进行切分的业务逻辑
 */
public class DocRecordReader implements RecordReader<LongWritable, Text> {
    //Reader
    private LineRecordReader reader;
    //the current line_num and lin
    private LongWritable lineKey = null;
    private Text lineValue = null;
    //Doc related
    private StringBuilder sb = new StringBuilder();
    private boolean inDoc = false;
    private final String DOC_START = "<DOC>";
    private final String DOC_END = "</DOC>";
    public DocRecordReader(JobConf job, FileSplit genericSplit) throws IOException{
        reader = new LineRecordReader(job, genericSplit);
        lineKey = reader.createKey();
        lineValue = reader.createValue();
    }

    @Override
    public boolean next(LongWritable key, Text value) throws IOException {
        while(true){
            //get current line
            if(!reader.next(lineKey, lineValue)){
                break;
            }
            if(!inDoc){
                // not in doc, check if <doc>
                if(lineValue.toString().startsWith(DOC_START)){
                    //reset doc status
                    inDoc = true;
                    //clean buff
                    sb.delete(0, sb.length());
                }else{
                    //indoc, check if </doc>
                    if(lineValue.toString().startsWith(DOC_END)){
                        //reset doc status
                        inDoc = false;
                        //set kv and return
                        key.set(key.get() + 1);
                        value.set(sb.toString());
                        return true;
                    }else{
                        if(sb.length() != 0){
                            sb.append("\n");
                        }
                        sb.append(lineValue.toString());
                    }
                }
            }

        }
        return false;
    }

    @Override
    public LongWritable createKey() {
        return null;
    }

    @Override
    public Text createValue() {
        return null;
    }

    @Override
    public long getPos() throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public float getProgress() throws IOException {
        return 0;
    }
}
