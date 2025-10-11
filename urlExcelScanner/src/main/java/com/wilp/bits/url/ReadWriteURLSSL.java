package com.wilp.bits.url;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ReadWriteURLSSL {
	
	
	private static final Logger readWriteURLSSL = Logger.getLogger(ReadWriteURLSSL.class.getName());
	String methodsName="";
	XSSFWorkbook workbook;
	XSSFSheet sheet;
	FileInputStream input;
	FileOutputStream output;
	DataFormatter formatter ;
	CellReference cellRef;
	String text;
	String updatedUrl;
	int slno=1;
	String statusmessage = null;
	int status = 0;
	String urltest;
	String urltest1;
	URL requesturl;
	Date startdate = null ;
	Date enddate = null;
	boolean redirect =false;
	String lastlineremove;
	String firstlineremove1;
	String firstlineremove2;
	String IssueTo ;
	String riskLevel = null;
	String Domainname;
	String reupdatedUrl;
	Row row;

	String regex = "((http|https|www)://)(www.)?"
            + "[a-zA-Z0-9@:%._\\+~#?&//=]"
            + "{2,256}\\.[a-z]"
            + "{2,6}\\b([-a-zA-Z0-9@:%"
            + "._\\+~#?&//=]*)";
	
	public static void main(String[] args) throws Exception {
		
		ReadWriteURLSSL urls = new ReadWriteURLSSL();
		File inputFile = new File(args[0]);
		File updatedFile ;
		File outputFile;
		
		if(args.length < 1)
		{
			System.out.println("Usage: Input file not present");
			throw new IllegalArgumentException("Input file path must be provided as an argument by the Python script.");
		}
		if (!inputFile.exists()) {
            System.err.println("Input file not found: " + inputFile.getAbsolutePath());
            System.exit(2);
        }
		String filepath = args[0];
		File file = new File(filepath);
		
	try {
		urls.excelReadAndCheck(file);
		String jsonPath = filepath.replace("\\", "\\\\");
        String outputPathJson = "{\"output_path\": \"" + jsonPath + "\"}";
        
        System.out.println(outputPathJson); 
        
	} catch (Exception e) {
        readWriteURLSSL.severe("FATAL ERROR during URL processing: " + e.getMessage());
        // Propagate the failure by returning a non-zero exit code (optional, but good practice)
        System.exit(1); 
    }
	}

    // Helper method to safely extract the hostname for SSL check
    private String extractHostName(String urlString) {
        if (urlString == null || urlString.startsWith("N/A")) {
            return null;
        }
        try {
            // If the URL is already just a hostname/IP, try to use it directly
            if (!urlString.contains("://")) {
                return urlString;
            }
            
            URL url = new URL(urlString);
            String host = url.getHost();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (MalformedURLException e) {
            readWriteURLSSL.warning("Failed to parse URL for hostname extraction: " + urlString);
            return null;
        }
    }
	
	//Reading excel, Checking redirection, Writing into excel
	public void excelReadAndCheck(File filename)throws Exception
	{
		methodsName="excelReadAndCheck()";
		readWriteURLSSL.info("Inside "+methodsName+" -- Start ");
		
		// Use try-with-resources for FileInputStream to ensure it's closed
		try (FileInputStream input = new FileInputStream(filename)) { 
			readWriteURLSSL.info("Filename: "+filename);
			workbook = new XSSFWorkbook(input);
			sheet= workbook.getSheetAt(0);
			formatter = new DataFormatter();
			Row header = sheet.createRow(0);
			
			/* In excel column no. starts from 0. */
			// Headers
			header.createCell(0).setCellValue("REQUEST URL");
			header.createCell(1).setCellValue("INITIAL HEURISTIC VERDICT");
			header.createCell(2).setCellValue("THREAT INTELLIGENCE STATUS");
			header.createCell(3).setCellValue("ANALYSIS RATIONALE");
			header.createCell(4).setCellValue("FINAL URL (AFTER REDIRECTS)");
			header.createCell(5).setCellValue("HTTPS STATUS CODES");
			header.createCell(6).setCellValue("HTTPS STATUS MESSAGE");
			header.createCell(7).setCellValue("SSL CERTIFICATE VALID FROM");
			header.createCell(8).setCellValue("SSL CERTIFICATE EXPIRES ON");
			header.createCell(9).setCellValue("SSL CERTIFICATE ISSUER DATA");
			header.createCell(10).setCellValue("OVERALL RISK ASSESSMENT");
			 
			for( Row row: sheet)
			{
				//Skipping the first row since they are headers
				if(row.getRowNum()==0)
				{
					continue;
				}
				
				// Resetting state variables for each new row
				updatedUrl = null;
				status = 0;
				statusmessage = null;
				redirect = false;
				startdate = null;
				enddate = null;
				IssueTo = null; 
				riskLevel = null; // Resetting new variable
				lastlineremove = null;
				firstlineremove1 = null;
				
				Cell cellnum= row.getCell(0);
				cellRef = new CellReference(row.getRowNum(), cellnum.getColumnIndex());
				
				text =formatter.formatCellValue(cellnum);
				
				readWriteURLSSL.info("*****************************************CYBERSENTINELX**************************************");
				
				
				//If orginal(Request Url) is matching with Regex
				if(text.matches(regex))
				{
					HttpURLConnection conn = null; // Declare connection outside try for finally block
					try
					{
						urltest= text;
						readWriteURLSSL.info("-----------------REGEX URLs VALIDATON STARTED---------------");
							
						readWriteURLSSL.info("Request URL matching with Regex");
					
						//Redirection Status, Response Code, Redirected URL
						requesturl = new URL(urltest);
						conn= (HttpURLConnection)requesturl.openConnection();
						conn.setReadTimeout(5000);
						conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
						conn.addRequestProperty("User-Agent", "Chrome");
						conn.addRequestProperty("Referer", "google.com");
						
						readWriteURLSSL.info((slno++)+ ")"+ "Request URL : "+requesturl);
						
						//Getting the response code and response message
						status = conn.getResponseCode();
						statusmessage=conn.getResponseMessage();
					
						if (	
							status == HttpURLConnection.HTTP_MOVED_TEMP
							|| status == HttpURLConnection.HTTP_SEE_OTHER
							|| status == HttpURLConnection.HTTP_MOVED_PERM
							||status ==HttpURLConnection.HTTP_MULT_CHOICE
							||status ==HttpURLConnection.HTTP_USE_PROXY
							||status ==HttpURLConnection.HTTP_NOT_MODIFIED
							||status ==HttpURLConnection.HTTP_OK)
						{
							redirect =true;
						}
						
						
						readWriteURLSSL.info("Response Code : "+status);
						readWriteURLSSL.info("Status Message : "+statusmessage);
						
						// 💎 NEW RISK ASSESSMENT LOGIC
						if (status >= 200 && status < 300) {
							riskLevel = "Low"; // Success
						} else if (status >= 300 && status < 400) {
							riskLevel = "Low"; // Redirection
						} else if (status == 404 || status == 403) {
							riskLevel = "Medium"; // Common client errors
						} else if (status >= 400 && status < 500) {
							riskLevel = "High"; // Other client errors
						} else if (status >= 500 && status < 600) {
							riskLevel = "Critical"; // Server errors
						} else {
							riskLevel = "Unknown";
						}
						
						if(redirect)
						{// redirect start
							// close old connection before opening a new one
							conn.disconnect(); 
							
							// get redirect URL from "location" header field
							updatedUrl = conn.getHeaderField("Location");
							// get the cookie if need, for login
							String cookies = conn.getHeaderField("Set-Cookie");
							
							if (updatedUrl != null) {
								// open the new Connection again
								conn = (HttpURLConnection) new URL(updatedUrl).openConnection();
								conn.setRequestProperty("Cookie", cookies);
								conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
								conn.addRequestProperty("User-Agent", "Chrome");
								conn.addRequestProperty("Referer", "google.com");	
								readWriteURLSSL.info("REGEX REDIRECTION URL: "+updatedUrl);
							} else {
								readWriteURLSSL.info("REGEX REDIRECTION URL is null (Location header missing)");
							}

						}// redirect end
						
					}catch(Exception e)
					{
						readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
						
						status = 999; 
						statusmessage = "Connection Blocked"; // 💎 FANCY TERM
						riskLevel = "Critical"; // 💎 Set Critical Risk for connection failure
						updatedUrl = "N/A"; // Clean up response URL column
						
						// Detailed reason for 11th column (IssueTo)
						if (e instanceof java.net.UnknownHostException) {
							IssueTo = "ERROR: IP/DNS not found: " + e.getMessage();
						} else if (e instanceof java.net.SocketTimeoutException) {
							IssueTo = "ERROR: Connection Timeout";
						} else if (e instanceof java.net.MalformedURLException) {
							IssueTo = "ERROR: Invalid URL Format";
						} else {
							IssueTo = "ERROR: Unexpected Connection Error: " + e.getClass().getSimpleName();
						}
					} finally {
						// 💎 RESOURCE MANAGEMENT IMPROVEMENT
						if (conn != null) {
							conn.disconnect(); 
						}
					}
					
				}// if close
				
				else
				{// Non-Redirection else statement start
					readWriteURLSSL.info("-----------------NON-REGEX URLs VALIDATON STARTED---------------");
					HttpURLConnection conn = null; // Declare connection outside try for finally block
					
					// Conditional Protocol Prepending (to avoid http://http://)
					if (text.startsWith("http://") || text.startsWith("https://")) {
						urltest = text;
					} else {
						urltest = "http://" + text;
					}
					
					
					try
					{	
						readWriteURLSSL.info((slno++)+ ")"+ "Request URL : "+urltest);
						requesturl = new URL(urltest);
						conn= (HttpURLConnection)requesturl.openConnection();
						conn.setReadTimeout(50000);
						conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
						conn.addRequestProperty("User-Agent", "Chrome");
						conn.addRequestProperty("Referer", "google.com");
						
						//Getting the response code and response message
						status = conn.getResponseCode();
						statusmessage=conn.getResponseMessage();
						
						if (	
								status == HttpURLConnection.HTTP_MOVED_TEMP
								|| status == HttpURLConnection.HTTP_SEE_OTHER
								|| status == HttpURLConnection.HTTP_MOVED_PERM
								||status ==HttpURLConnection.HTTP_MULT_CHOICE
								||status ==HttpURLConnection.HTTP_USE_PROXY
								||status ==HttpURLConnection.HTTP_NOT_MODIFIED) {
						
								redirect =true;
							}
						
						
					
						readWriteURLSSL.info("Response Code : "+status);
						readWriteURLSSL.info("Status Message : "+statusmessage);
						
						// 💎 NEW RISK ASSESSMENT LOGIC
						if (status >= 200 && status < 300) {
							riskLevel = "Low"; // Success
						} else if (status >= 300 && status < 400) {
							riskLevel = "Low"; // Redirection
						} else if (status == 404 || status == 403) {
							riskLevel = "Medium"; // Common client errors
						} else if (status >= 400 && status < 500) {
							riskLevel = "High"; // Other client errors
						} else if (status >= 500 && status < 600) {
							riskLevel = "Critical"; // Server errors
						} else {
							riskLevel = "Unknown";
						}
						
						
						if(redirect){// redirect start
							// close old connection before opening a new one
							conn.disconnect(); 
							
							// get redirect URL from "location" header field
							updatedUrl = conn.getHeaderField("Location");
							// get the cookie if need, for login
							String cookies = conn.getHeaderField("Set-Cookie");
							
							if (updatedUrl != null) {
								// open the new Connection again
								conn = (HttpURLConnection) new URL(updatedUrl).openConnection();
								conn.setRequestProperty("Cookie", cookies);
								conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
								conn.addRequestProperty("User-Agent", "Chrome");
								conn.addRequestProperty("Referer", "google.com");	
								readWriteURLSSL.info("NON-REGEX REDIRECTION URL: "+updatedUrl);
							} else {
								readWriteURLSSL.info("NON-REGEX REDIRECTION URL is null (Location header missing)");
							}
									
							}// redirect end
					
					}catch(Exception e)
					{
						readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
						
						status = 999; 
						statusmessage = "Connection Blocked";
						riskLevel = "Critical";
						updatedUrl = "N/A";
						
						// Detailed reason for 11th column (IssueTo)
						if (e instanceof java.net.UnknownHostException) {
							IssueTo = "ERROR: IP/DNS not found: " + e.getMessage();
						} else if (e instanceof java.net.SocketTimeoutException) {
							IssueTo = "ERROR: Connection Timeout";
						} else if (e instanceof java.net.MalformedURLException) {
							IssueTo = "ERROR: Invalid URL Format";
						} else {
							IssueTo = "ERROR: Unexpected Connection Error: " + e.getClass().getSimpleName();
						}
					} finally {
						// 💎 RESOURCE MANAGEMENT IMPROVEMENT
						if (conn != null) {
							conn.disconnect(); 
						}
					}
					
				}// Non-Redirection else statement end
				
//---------------------------------------------------------------------------------------------------------			
				// Redirected URL other than 300 series execution.Ex: 200, 404,403, 500, 503, 999
				if(status == 200 || status ==403|| status ==404 || status ==503 || status ==500 || status == 999)	
					{
					readWriteURLSSL.info("-----------------200x,400x,500x,999x VALIDATON STARTED---------------");
					
						readWriteURLSSL.info("Status code not in 300 series ");
						Cell redirectedurl =row.createCell(5);
						redirectedurl.setCellValue(updatedUrl);
						
						// If the request URL was successful (200), check for SSL on the original URL
						if (urltest != null) {
							String host = extractHostName(urltest);
							if (host != null) {
								certificateConfigurtions(host);
							}
						}
						
						// 💎 Final Risk Adjustment based on SSL Check
						if (IssueTo != null && IssueTo.startsWith("ERROR:")) {
							if ("Low".equals(riskLevel) || "Medium".equals(riskLevel) || "Unknown".equals(riskLevel)) {
								riskLevel = "High - SSL Failure";
							}
						}

						writeIntoXL(filename, row);
						
						readWriteURLSSL.info("Written into excel for status code which are not in 300 series");
					}
//---------------------------------------------------------------------------------------------------------				
					//Here the 300x series redirected url are been checked for checking whether it is a real redirect.
					else if (status >= 300 && status < 400) // Ensure we only enter this for 300-399 codes
					{// 300x else statement start
						readWriteURLSSL.info("-----------------300X VALIDATON STARTED---------------");
						
						// Checking for same request and redirected URL -- start
						// Added updatedUrl != null checks
						if(updatedUrl != null && updatedUrl.startsWith("https://www.")&& updatedUrl.endsWith("/"))
						{//Validation 1 start
						readWriteURLSSL.info("-----------------------------------------------------------------------");
						readWriteURLSSL.info("-----------VALIDATION 1: https://www.----------- ");
						
							// Removing the last "/"
							lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
							readWriteURLSSL.info("/ removed from end of the URL : "+lastlineremove);
							
							//Removing "https://www."
							firstlineremove1 = lastlineremove.replace("https://www.", "");
							readWriteURLSSL.info("https://www. removed from the URL : "+firstlineremove1);
							
							//In case the updated URL contains double slash. 
							//Removing 2nd "/"
							if(firstlineremove1.endsWith("/"))
							{// if statement for 2nd / start
							firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
							readWriteURLSSL.info("/ removed from end of the URL : "+firstlineremove1);	
							}// if statement for 2nd / end
							readWriteURLSSL.info("----Protocols completely removed----");
							
							
							if(text.startsWith("www."))
							{//if statement for comparing request URL and response URL -- start
								//concatenating www. to the response URL
								firstlineremove1 = "www."+firstlineremove1;
								
								readWriteURLSSL.info("-----------VALIDATION 1.1: www.----------- " + firstlineremove1);
								// Safe string comparison
								if(text.equals(firstlineremove1)){
									readWriteURLSSL.info("Same Request and Redirected URL");
								}
							}//if statement for comparing request URL and response URL -- start
							readWriteURLSSL.info("Same request and redirected url in (https://www.) protocol  : "+firstlineremove1);
							readWriteURLSSL.info("-----------------------------------------------------------------------");
								
						}//Validation 1 end
						
//---------------------------------------------------------------------------------------------------------------------------------------------------------			
						
						else if(updatedUrl != null && updatedUrl.startsWith("https://")&& updatedUrl.endsWith("/"))
						{// Validation 2 start
							readWriteURLSSL.info("-----------------------------------------------------------------------");
							readWriteURLSSL.info("-----------VALIDATION 2: https://----------- ");
							
								// Removing the last "/"
								lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
								readWriteURLSSL.info("/ removed from end of the URL : "+lastlineremove);
								//Removing "https://"
								firstlineremove1 = lastlineremove.replace("https://", "");
								readWriteURLSSL.info("https:// removed from the URL : "+firstlineremove1);
								
								//In case the updated URL contains double slash.
								//Removing 2nd "/"
								if(firstlineremove1.endsWith("/"))
								{
									firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
									readWriteURLSSL.info("/ removed from end of the URL : "+firstlineremove1);	
								}
								
								if(text.startsWith("www."))
								{
									firstlineremove1 = "www."+firstlineremove1;
									readWriteURLSSL.info("-----------VALIDATION 2.1: www.----------- " + firstlineremove1);
									if(text.equals(firstlineremove1)){
										readWriteURLSSL.info("Same Request and Redirected URL");
									}
								}
							readWriteURLSSL.info("Same request and redirected url in (https://) protocol : "+firstlineremove1);		
							readWriteURLSSL.info("-----------------------------------------------------------------------");
						}// Validation 2 end
//---------------------------------------------------------------------------------------------------------------------------------------------------------			
						
						else if( updatedUrl != null && updatedUrl.startsWith("http://www.")&&updatedUrl.endsWith("/"))
						{//Validation 3 start
							readWriteURLSSL.info("-----------------------------------------------------------------------");
							readWriteURLSSL.info("-----------VALIDATION 3: http://www.----------- ");
							
								// Removing the last "/"
								lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
								readWriteURLSSL.info("/ removed from end of the URL : "+lastlineremove);
								//Removing "https://"
								firstlineremove1 = lastlineremove.replace("http://www.", "");
								readWriteURLSSL.info("http://www. removed from the URL : "+firstlineremove1);
								
								
								//In case the updated URL contains double slash. 
								//Removing 2nd "/"
								if(firstlineremove1.endsWith("/"))
								{
								firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
								readWriteURLSSL.info("/ removed from end of the URL : "+firstlineremove1);	
								}
								
								
								if(text.startsWith("www."))
								{
									firstlineremove1 = "www."+firstlineremove1;
									readWriteURLSSL.info("-----------VALIDATION 3.1: www.----------- " + firstlineremove1);
									if(text.equals(firstlineremove1)){
										readWriteURLSSL.info("Same Request and Redirected URL");
									}
								}

							readWriteURLSSL.info("Same request and redirected url in (http://www.) protocol : "+firstlineremove1);		
							readWriteURLSSL.info("-----------------------------------------------------------------------");
							
						}// Validation 3 end
//---------------------------------------------------------------------------------------------------------------------------------------------------------			
						

						else if(updatedUrl != null && updatedUrl.startsWith("http://")&& updatedUrl.endsWith("/"))
						{//Validation 4
							readWriteURLSSL.info("-----------------------------------------------------------------------");
							readWriteURLSSL.info("-----------VALIDATION 4: http://----------- ");
							
							// Removing the last "/"
								lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
								readWriteURLSSL.info("/ removed from end of the URL : "+lastlineremove);
							//Removing "https://"
								firstlineremove1 = lastlineremove.replace("http://", "");
								readWriteURLSSL.info("http:// removed from the URL : "+firstlineremove1);
								
								
								//Incase the updated Url contains double slash. Removing 2nd "/"
								if(firstlineremove1.endsWith("/"))
								{
								firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
								readWriteURLSSL.info("/ removed from end of the URL : "+firstlineremove1);	
								}
								
								if(text.startsWith("www."))
								{
									firstlineremove1 = "www."+firstlineremove1;
									readWriteURLSSL.info("-----------VALIDATION 4.1: www.----------- " + firstlineremove1);
									if(text.equals(firstlineremove1)){
										readWriteURLSSL.info("Same Request and Redirected URL");
									}
								}
							readWriteURLSSL.info("Same request and redirected url in (http://) protocol : "+firstlineremove1);		
							readWriteURLSSL.info("-----------------------------------------------------------------------");
					
						}// Validation 4 end
						//Else statement
						else
						{
							readWriteURLSSL.info("protocols not removed as it is redirected URL: "+updatedUrl);
							firstlineremove1=updatedUrl;
						}
						
//---------------------------------------------------------------------------------------------------------------------------------------------------------			
// Checking for same request and redirected URL -- end
						readWriteURLSSL.info(updatedUrl+ "    "+firstlineremove1);
						readWriteURLSSL.info("-----------VALIDATION COMPLETED SUCESSFULLY------------------");
						
						// Use host extraction for SSL
						String host = extractHostName(firstlineremove1);
						if (host != null) {
							certificateConfigurtions(host);
						}
						
						// Final Risk Adjustment based on SSL Check
						if (IssueTo != null && IssueTo.startsWith("ERROR:")) {
							if ("Low".equals(riskLevel) || "Medium".equals(riskLevel) || "Unknown".equals(riskLevel)) {
								riskLevel = "High - SSL Failure";
							}
						}
						
						// Use null-safe comparison
						if(text.equals(firstlineremove1) || text.equals(updatedUrl))
						{
						readWriteURLSSL.info("REQUEST URL: "+text+"------------"+"RESPONSE URL: "+firstlineremove1);
						readWriteURLSSL.info("Same REQUEST and RESPONSE/REDIRECT URL -------EXCEL NOT WRITING------");
						
						writeIntoXL(filename, row);
						}
						/*
						 * If request URL and response URL are not matching...
						 */
						else
						{
							Cell redirectedurl =row.createCell(5);
							redirectedurl.setCellValue(updatedUrl);
							
							//writing into file
							writeIntoXL(filename, row);
							readWriteURLSSL.info("Successfully written");
					}//else close 
			}		//300x statement end
			
			}// for loop end
			readWriteURLSSL.info("===========================================================");
			readWriteURLSSL.info("excelReadAndCheck method done Successfully !!!");
			readWriteURLSSL.info("Inside "+methodsName+" -- End ");
		}
	}
	
	private void certificateConfigurtions(String hostName)
	{
		methodsName="certificateConfigurtions()";
		
		// Hostname check added for safety
        if (hostName == null || hostName.isEmpty()) {
            startdate = null;
            enddate = null;
            IssueTo = "ERROR: Invalid Hostname/IP for SSL Check";
            return;
        }

		SSLSocket socket = null;
		try{
			readWriteURLSSL.info("Inside "+methodsName+" -- Start ");
			
			SocketFactory factory =SSLSocketFactory.getDefault();
			InetAddress address = InetAddress.getByName(hostName);
			
			// Use the resolved hostname for the socket to handle CNAMEs/redirection
			socket = (SSLSocket) factory.createSocket(address.getHostName(), 443);
			socket.startHandshake();
			SSLSession session = socket.getSession();
			
			// Using getPeerCertificates for robustness over getPeerCertificateChain
			java.security.cert.X509Certificate[] standardCerts = (java.security.cert.X509Certificate[]) session.getPeerCertificates();
			
			if(standardCerts != null && standardCerts.length > 0)
			{
				java.security.cert.X509Certificate cert = standardCerts[0];
				
				startdate = cert.getNotBefore();
				enddate = cert.getNotAfter();
				IssueTo= cert.getSubjectX500Principal().getName(); // This gives the CN/O/OU=... format
				
				
				readWriteURLSSL.info("Start date: "+startdate);
				readWriteURLSSL.info("End date: "+enddate);
				readWriteURLSSL.info("Certificate Name(Issue To) : "+IssueTo);
			} else {
                IssueTo = "ERROR: No Certificate/Empty Chain";
            }
		}catch(Exception e)
		{
			readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
			// Certificate check failed, so ensure the certificate fields are updated with the error
			startdate = null;
			enddate = null;
			
            // Concise SSL error reasons for the 11th column
            if (e.getMessage() != null && e.getMessage().contains("unable to find valid certification path")) {
                IssueTo = "ERROR: Invalid/Self-Signed Certificate";
            } else if (e instanceof java.net.UnknownHostException) {
                IssueTo = "ERROR: Hostname Resolution Failed for SSL";
            } else if (e instanceof java.io.IOException && e.getMessage() != null && e.getMessage().contains("handshake")) {
                 IssueTo = "ERROR: SSL Handshake Failure";
            } else {
                IssueTo = "ERROR: SSL Check Failure: " + e.getClass().getSimpleName();
            }
		} finally {
			// Ensure socket is closed
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					readWriteURLSSL.warning("Failed to close SSL socket: " + e.getMessage());
				}
			}
		}
		
	
		readWriteURLSSL.info("Inside "+methodsName+" -- End ");
	}
	
	private void writeIntoXL(File fileName, Row row)
	{	
		methodsName="writeIntoXL()";
		try (FileOutputStream fos = new FileOutputStream(fileName)) { // Use try-with-resources
			readWriteURLSSL.info("Inside "+methodsName+" -- Start ");
			readWriteURLSSL.info("----------WRITING DATA INTO XL FILE----------");
			readWriteURLSSL.info("Update URL : "+updatedUrl);
			readWriteURLSSL.info("Response Status : "+status);
			readWriteURLSSL.info("Response Message: "+statusmessage);
			
			readWriteURLSSL.info("Start date: "+startdate);
			readWriteURLSSL.info("End date: "+enddate);
			readWriteURLSSL.info("Issuer Name : "+IssueTo);
			readWriteURLSSL.info("Risk Level : "+riskLevel); // Log new variable

			Cell code = row.createCell(5);
			code.setCellValue(status);
			
			Cell message = row.createCell(6);
			message.setCellValue(statusmessage);

			// Columns 8 and 9 (Dates) are left as null if there's a 999 error
			
			Cell startdate1 = row.createCell(7);
			startdate1.setCellValue(startdate);
			
			Cell enddate1 =row.createCell(8);
			enddate1.setCellValue(enddate);
			
			Cell issuername =row.createCell(9);
			issuername.setCellValue(IssueTo);	
			
			Cell riskLevelCell =row.createCell(10);
			riskLevelCell.setCellValue(riskLevel);	
			
			workbook.write(fos);
			fos.flush();

		} catch (FileNotFoundException e) {
			readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
		} catch (IOException e) {
			readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
		}
		readWriteURLSSL.info("Inside "+methodsName+" -- End ");
		
	}
}