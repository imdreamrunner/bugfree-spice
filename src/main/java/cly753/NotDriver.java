package cly753;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class NotDriver {
    static enum RecordCounters { IMAGE_SUBMITTED, IMAGE_PROCESSED };
    
    public static final String LABEL = "%%%% NotDriver : ";

    public static Configuration conf;
    
    public static String inputPath = "";
    public static String outputPath = "";
//    public static String prefixPath = "";

    public static void main(String[] args) throws Exception {
    	conf = new Configuration();
    	
    	String uri = conf.get("fs.default.name");
        System.out.println("%%%% fs.default.name : " + uri);        
        
        System.out.println("%%%% NotDriver : System::java.library.path : " + System.getProperty("java.library.path"));
        System.out.println("%%%% NotDriver : Job::java.library.path : " + conf.get("java.library.path"));
        
    	Job job = Job.getInstance(conf, "not-a-job");
//        job.addCacheFile(new URI(uri + "/lib/libBFLoG.so#libBFLoG.so"));
//        job.createSymlink();
//        job.addCacheFile(new URI(uri + "/lib/libbflog_api.so#libbflog_api.so"));
//        job.createSymlink();
//    	URI temp = new URI(uri + "/lib/libbflog_api.so#libbflog_api.so");
//    	System.out.println(LABEL + temp.toString());
//    	DistributedCache.createSymlink(conf); DistributedCache.addCacheFile(temp, conf);

        job.setJarByClass(NotMapper.class);  // no need copy jar any more
        job.setInputFormatClass(NotInputFormat.class);
        job.setMapperClass(NotMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NotFeatureWritable.class);

        // job.setCombinerClass(NotCombiner.class);
        // job.setPartitionerClass(NotPartitioner.class);

//         job.setReducerClass(NotReducer.class);
//         job.setOutputKeyClass(Text.class);
//         job.setOutputValueClass(NotFeatureWritable.class);
        job.setOutputFormatClass(NotOutputFormat.class);

        job.setNumReduceTasks(0); // directly write to file system, without calling reducer
        
        job.setSpeculativeExecution(true);

        fillPath(args);
        
        System.out.println(LABEL + "delete old output path...");
        FileSystem fs = FileSystem.get(new Configuration());
        fs.delete(new Path(outputPath), true);
        
        FileInputFormat.addInputPath(job, new Path(inputPath)); // provide input directory
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        boolean ok = job.waitForCompletion(true);
    }
    
    private static void fillPath(String[] args) {
        for (String arg : args) {
        	if (arg.startsWith("INPUTPATH"))
        		inputPath = arg.substring(9, arg.length());
            if (arg.startsWith("OUTPUTPATH"))
            	outputPath = arg.substring(10, arg.length());
        }
        System.out.println("INPUTPATH : " + inputPath);
        System.out.println("OUTPUTPATH: " + outputPath);
    }
}

// set mapreduce.framework.name to "local" / "classic" / "yarn"


// many small input files
// http://yaseminavcular.blogspot.in/2011/03/many-small-input-files.html

// The Small Files Problem
// http://blog.cloudera.com/blog/2009/02/the-small-files-problem/