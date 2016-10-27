package getBookList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
 

import java.io.InputStreamReader;
import java.util.Iterator;
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

 public class getBooks {
     
     public  Document getDocument (String url){
         try {
             return Jsoup.connect(url).get();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
     
     public class Constants {  

    	    public static final String URL = "https://book.douban.com/tag/%E7%BC%96%E7%A8%8B";  
    	  
    	    public static final int NUM = 20;  //numbers of books on each page
    	  
    	    public static final String START = "?start=";  //fenye
    	  
    	}  
     
     public static void main(String[] args) throws IOException, WriteException {
    	 
    	 WritableWorkbook wwb = null;  
    	 wwb = Workbook.createWorkbook(new File("C:\\Users\\Administrator\\Desktop\\books.xls"));
         WritableSheet sheet = wwb.createSheet("First Sheet",0);
         Label titleLable = new Label(0,0,"title");
         sheet.addCell(titleLable);
         Label scoreLable = new Label(1,0,"score");
         sheet.addCell(scoreLable);
         Label commentsLable = new Label(2,0,"comments");
         sheet.addCell(commentsLable);
         
    	 int index=0;//index of current book in excel
    	 int i=0;//i th page
    	 while(index<50){  
	    	 String url = Constants.URL + Constants.START + String.valueOf(i * Constants.NUM)+"&type=S";  
	         System.out.println(url);  
	         Connection connection = Jsoup.connect(url);  
	         Document document = connection.get();  
	         
	         //getBooks t = new getBooks();
	         //Document document = t.getDocument("https://book.douban.com/tag/%E7%BC%96%E7%A8%8B?type=S");
	         Elements ul = document.select("ul"); // 得到ul标签  
	         Iterator<Element> ulIter = ul.iterator();  
//	         
//	         File writename = new File("C:\\Users\\Administrator\\Desktop\\books.txt");
//		        writename.createNewFile();
//		        BufferedWriter out = new BufferedWriter(new FileWriter(writename,true));
//		       
	         while (ulIter.hasNext()) {  
	             Element element = ulIter.next();  
	             Elements eleLi = element.select("li"); // 得到ul里的li标签  
	             Iterator<Element> liIter = eleLi.iterator();  
	             while (liIter.hasNext()) {  
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
		            	 if(commentNum>500){
		            		 index++;
		            		 System.out.println(index+"th book inserted");
		            		 //out.write(title+" "+score+" "+commentNum+"\r\n");
		            		 //out.flush();
		            		 
		            		 
		            		 Label titleCon = new Label(0,index,title);
		                     sheet.addCell(titleCon);
		                     Label scoreCon = new Label(1,index,score);
		                     sheet.addCell(scoreCon);
		                     Label commentsCon = new Label(2,index,m.replaceAll("").trim());
		                     sheet.addCell(commentsCon);
		                     
		            		 //wwb.write();
		                     
		            		 if(index >=50){
		            			 break;
		            		 }
		            		 
		            	 }	
		             }
		             
	             }
	         } 
	         
	         //out.close();
	         i++;
    	 }
    	 wwb.write();
         wwb.close();
     }
 }