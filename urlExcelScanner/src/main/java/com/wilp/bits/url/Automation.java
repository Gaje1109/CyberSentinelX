//package com.wilp.bits.url;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.wilp.bits.email.EmailManagement;
//
////URl
//public class Automation implements RequestHandler<String,String>
//{
//	
//	String methodsName="";
////    public static void main( String[] args )
////    {
////       CyberSentinelX url = new CyberSentinelX();
////      ArrayList<String> columnvalues =url.readXLFile();
////       url.writeXLSheet(columnvalues);
////    }
//    public String handleRequest(String input, Context context) {
//    	
//    	Automation url = new Automation();
//        ArrayList<String> columnvalues =url.readXLFile();
//        url.writeXLSheet(columnvalues);
//     
//    	return "CyberSentinelX successfully completed";
//    }
//        
//        
//   
//    
//    
//    //Create Empty XL file 
//    private String createXLSheet()
//    {
//    	methodsName="CyberSentinelX.createXLSheet()";
//    	String fileName="C:/Users/DELL/Documents/MAH/Mah_20231206_1.xlsx";
//    	FileOutputStream fileoutput = null;
//    	try {
//    		fileoutput= new FileOutputStream(fileName);
//    		System.out.println("File Created");
//			
//			
//		} catch (FileNotFoundException e) {
//			System.out.println("Exception occured in "+methodsName+ " : "+e);
//		}finally
//    	{
//			try {
//				fileoutput.close();
//			} catch (IOException e) {
//				System.out.println("Exception occured in "+methodsName+ " : "+e);
//			}
//    	}
//    	return fileName;
//    }
//    
//    
//    
//    //Read Request XL File
//    private ArrayList<String> readXLFile()
//    {
//    	methodsName="CyberSentinelX.readXLFile()";
//    	String columnValues="";
//    	ArrayList<String> columnval = null;
//    	String inputfile="C:/Users/DELL/Documents/MAH/Mah.xlsx";
//    	try
//    	{
//    	FileInputStream fis= new FileInputStream(inputfile);
//    	XSSFWorkbook wb= new XSSFWorkbook(fis);
//    	XSSFSheet sheetvalue= wb.getSheet("Base");
//    	Iterator<Row> fileitr= sheetvalue.iterator();
//    	columnval= new ArrayList<String>();
//    	//iterating over excel file
//    	while(fileitr.hasNext())
//    	{
//    		Row row= fileitr.next(); 
//    		//Skipping the first row as it is header
//    		if(row.getRowNum()==0)
//    		{
//    			continue;
//    		}
//    		//Iterating over each column
//    		Iterator<Cell> columnitr = row.cellIterator();
//    		while(columnitr.hasNext())
//    		{
//    			Cell cell = columnitr.next();
//    			 columnValues= cell.getStringCellValue();
//    			//System.out.println(columnValues +"\t\t\t");
//    			columnval.add(columnValues);    			
//    		}
//    	}
//
//    	}catch(FileNotFoundException e)
//    	{
//    		System.out.println("Exception occured in "+methodsName+ " : "+e);
//    	} catch (IOException e) {
//    		System.out.println("Exception occured in "+methodsName+ " : "+e);
//		}
//    	return columnval;
//    }
//    
//   //Write to XL file
//    private void writeXLSheet(ArrayList<String> columnbasevalues)
//    {
//    	methodsName="CyberSentinelX.writeXLSheet()";
//    	String createdfile="";
//    	FileInputStream input=null;
//    	FileOutputStream output= null;
//    	XSSFWorkbook workbook;
//    	XSSFSheet sheet;
//    	Row header;
//    	Cell column=null;
//    	String columnValues="";
//    	try {
//    		//Fetching the newly created XL File
//    		 createdfile= createXLSheet();
//			 input= new FileInputStream(createdfile);
//			 workbook = new XSSFWorkbook();
//			 sheet= workbook.createSheet("MAH URLs");
//			 header= sheet.createRow(0);
//			 //converting arraylist to string
//			 columnValues= String.join(",", columnbasevalues);
//			 String[] words= columnValues.split(",");
//			  
//			for(String s : words){	
//					for(int i=1; i<words.length; i++)
//					{	
//						header= sheet.createRow(i);
//						column= header.createCell(0); 
//						column.setCellValue(words[i]);					
//					}
//					//System.out.println(s);
//			 }
//			output = new FileOutputStream(createdfile);
//			workbook.write(output);
//			output.flush();		
//			System.out.println("Request URLs successfully written");
//			
//			validateURL(createdfile);
//			sendMail();
//		
//		} catch (FileNotFoundException e) {
//			System.out.println("Exception occured in "+methodsName+ " : "+e);
//		} catch (IOException e) {
//			System.out.println("Exception occured in "+methodsName+ " : "+e);
//		}catch(Exception e)
//    	{
//			EmailManagement failed_email =new EmailManagement();
//    	}
//    	
//    }
//    
//    private void validateURL(String createdfile)
//    {
//    	ReadWriteURLSSL ssl = new ReadWriteURLSSL();
//    	try {
//			ssl.excelReadAndCheck(createdfile);
//			
//			ssl.showCertInfo(createdfile);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//    
//   private void sendMail()
//   {
//	   EmailManagement email =new EmailManagement();
//	  // email.emailConfigurations();
//	   
//	   System.out.println("Message sent");
//   }
//
//
//
//
//
//    
//    
//    
//    
//}
