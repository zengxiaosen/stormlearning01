package com.zengxiaosen.adalgo.etl.hadoop_test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/**
 * Created by Administrator on 2016/10/22.
 */
public class MyGroup {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if(otherArgs.length != 2){
            System.err.println("Usage databaseV1 <inputpath> <outputpath>");
        }
        Job job = Job.getInstance(conf, MyGroup.class.getSimpleName() + "1");
        job.setJarByClass(MyGroup.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapperClass(MyMapper1.class);
        job.setGroupingComparatorClass(MyGroupComparator.class);
        job.setReducerClass(MyReducer1.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        job.waitForCompletion(true);
    }

    public static class MyMapper1 extends Mapper<LongWritable, Text, Text, Text>{
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] spl = value.toString().split("\t");
            context.write(new Text(spl[0].trim()), new Text(spl[1].trim()));
        }
    }
    public static class MyReducer1 extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            Long count = 0L;
            for(@SuppressWarnings("unused") Text v2 : values){
                count++;
                context.write(new Text("in--"+key), new Text(count.toString()));
            }
            context.write(new Text("out--"+key), new Text(count.toString()));
        }
    }
    public static class MyGroupComparator extends WritableComparator{
        public MyGroupComparator(){
            super(Text.class, true);
        }
        @SuppressWarnings("rawtypes")
        public int compare(WritableComparable a, WritableComparable b){
            Text p1 = (Text)a;
            Text p2 = (Text)b;
            p1.compareTo(p2);
            return 0;
        }
    }



}
