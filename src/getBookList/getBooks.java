package getBookList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
 

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
 



import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

 public class getBooks extends Thread{
     
	 public static int i=0;//i th page
	 public static int threadNum=5;
	 public static int index=0;//index of current book in list
	 public static volatile boolean exit = false; 
	 public static int numOfList=12;//num of all results in final list
	 
	 public static ArrayList<String> titles=new ArrayList<String>();
	 public static ArrayList<String> scores=new ArrayList<String>();
	 public static ArrayList<String> comments=new ArrayList<String>();
	 
	 private static Lock lockForPageIndex = new ReentrantLock(); //lock for getting the page index 
	 private static Lock lockForWrite = new ReentrantLock();  //lock for writing results into arraylist
	 
     public  Document getDocument (String url){
         try {
             return Jsoup.connect(url).get();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
     
     public class Constants {  

	    public static final String URL = "https://book.douban.com/tag/%E7%BC%96%E7%A8%8B";  //url for coding books  ¡°±à³Ì¡±
	    //public static final String URL = "https://book.douban.com/tag/%E5%B0%8F%E8%AF%B4"; //url for novels "Ð¡Ëµ"
	    public static final int NUM = 20;  //numbers of books on each page
	  
	    public static final String START = "?start=";  //fenye
    	  
    }  
     
	 public getBooks(String name) { 
	        super(name); 
	 }
  
	 
     public void run(){	
    	while(!exit){
	    	 int page;
	    	 /*get page index i use mutex*/
	    	 lockForPageIndex.lock();
	    	 try{
	    		 page=i;
	    		 i++;
	    	 }finally{
	    		 lockForPageIndex.unlock();
	    	 }
	    	 
		     String url = Constants.URL + Constants.START + String.valueOf(page * Constants.NUM)+"&type=S";  		       
		     Document connection;
			try {
				//inoder to avoid the 403 error, add a header 
				connection = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2").get();
				Elements ul = connection.select("ul"); // get the ul tag
		        Iterator<Element> ulIter = ul.iterator();  
		        while (ulIter.hasNext() && !exit) {  
		             Element element = ulIter.next();  
		             Elements eleLi = element.select("li"); // ul:li  
		             Iterator<Element> liIter = eleLi.iterator();  
		             while (liIter.hasNext() &&!exit) {  
		            	 
		            	 Element liElement = liIter.next();  
			             Elements eleSpan1=liElement.select("span.rating_nums");
			             String score=eleSpan1.text();//get the score
		
			             Elements eleSpan2 = liElement.select("span.pl*");//get the comment info
			             String comment=eleSpan2.text();
		
			             Elements eleTitle = liElement.select("h2");
			             Elements eleHref = liElement.select("a[href]");  
			             String title=eleHref.attr("title");
			            
			             String regEx="[^0-9]";   //get the number of comments
			             Pattern p = Pattern.compile(regEx);   
			             Matcher m = p.matcher(comment);  
			             if(!m.replaceAll("").trim().equals("")){
			            	 int commentNum=Integer.parseInt(m.replaceAll("").trim());
			            	 if(commentNum>2000){
			            		 
			            		 /*modify index and insert this document into Arraylist*/
			            		 lockForWrite.lock();
			            		 try{
			            			 System.out.println("*********************"+this.getName()+"th thread");
			            			 index++;
			            			 System.out.println(index+"th book inserted");
				            		 titles.add(title);
				            		 scores.add(score);
				            		 comments.add(m.replaceAll("").trim());
				            		 if(index>=numOfList){
				            			 exit=true;
				            		 }
			            		 }finally{
			            			 lockForWrite.unlock();
			            		 }
			            	 }
			             }
		             }
	    	 
		         }
			
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  
		     	
			} 

     }
	     
	    
     public static void writeIntoExcel() throws IOException, RowsExceededException, WriteException{
    	 WritableWorkbook wwb = null;  
    	 wwb = Workbook.createWorkbook(new File("C:\\Users\\Administrator\\Desktop\\booksForCodingsMulti.xls"));
         WritableSheet sheet = wwb.createSheet("First Sheet",0);
         Label titleLable = new Label(0,0,"title");
         sheet.addCell(titleLable);
         Label scoreLable = new Label(1,0,"score");
         sheet.addCell(scoreLable);
         Label commentsLable = new Label(2,0,"comments");
         sheet.addCell(commentsLable);
         
         for(int p=0;p<index;p++){
        	 
    		 Label titleCon = new Label(0,p+1,titles.get(p).toString());
             sheet.addCell(titleCon);
             Label scoreCon = new Label(1,p+1,scores.get(p).toString());
             sheet.addCell(scoreCon);
             Label commentsCon = new Label(2,p+1,comments.get(p).toString());
             sheet.addCell(commentsCon);
         }
         wwb.write();
         wwb.close();
         
     }
	     
      public static void main(String[] args) throws InterruptedException{
    	  Thread thread[]=new Thread[threadNum];
    	  for(int k=0;k<threadNum;k++){
    		  thread[k]=new getBooks(String.valueOf(k+1));
    	  }
    	  for(int k=0;k<threadNum;k++){
    		  thread[k].start();
    	  }
          
          for(int k=0;k<threadNum;k++){//wait all threads to return
    		  thread[k].join();
    		  System.out.println("thread"+k+" return");
    	  }
          try {
        	  writeIntoExcel();
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          System.out.println("end");
	  }
     
 }