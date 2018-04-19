package com.isslng.pdfreader;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class PDFReader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Mongo Connection
			Mongo mongo = new Mongo("", port);
			DB db = mongo.getDB("dbname");
			
			DBCollection collection = db.getCollection("BankStatement");
			
			
			
			// date pattern description dd/mm/yyyy
			
			String regex = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)";
			// date pattern description dd/mmm/yyyy
	    	String regexmonth= "^(0[1-9]|[12][0-9]|3[01])[- /.][a-zA-Z]{3}[- /.](19|20)\\d\\d$";

			// Read from the PDF
			PdfReader reader = new PdfReader(
					"srcLocation\\honesty.pdf",
					"Password".getBytes());
			System.out.println("This PDF has " + reader.getNumberOfPages() + " pages.");

			for (int ll = 1; ll <= reader.getNumberOfPages(); ll++) {

				String page = PdfTextExtractor.getTextFromPage(reader, ll);

				// scan the string line by line
				Scanner scanner = new Scanner(page);
				String StmtRow = "";
				Integer StmtLines = 0;
				boolean inStmtRow = false;
				String lword1 = null;
				String lword2 = null;
				String lword9 = null;
				String lword8 = null;
				String lword7 = null;

				String lNarrative = "";
				while (scanner.hasNextLine()) {
					String lineinFocus = scanner.nextLine();
					

					//
					// System.out.println("New Row Encountered");
					//
					String arr[] = lineinFocus.split(" ", 100);
					// Place the first word of each line in firstWord
					String firstWord = arr[0];

					// Check if firstWord is a date
					Matcher m = Pattern.compile(regex).matcher(firstWord);
					Matcher m2 = Pattern.compile(regexmonth).matcher(firstWord);
					
					
					if (m.find()||m2.find()) {

						if (inStmtRow) {
							//
							// Save each rows to mongo
							BasicDBObject document = new BasicDBObject();
							document.put("TransactionDate", lword1);
							document.put("ValueDate", lword2);
							document.put("TransactionDetails", lNarrative);
							document.put("Debit", lword7);
							document.put("Credit", lword8);
							document.put("Balance", lword9);
							
							collection.insert(document);

							// Print rows
							System.out.println("PostDate: " + lword1);
							System.out.println("ValueDate: " + lword2);
							System.out.println("Debit " + lword7);
							System.out.println("Credit " + lword8);
							System.out.println("Balance " + lword9);
							System.out.println("Stmt narrative:" + lNarrative);
							//
							// Save to database
							//
							StmtRow = "";
							inStmtRow = true;
							StmtLines = 0;
						}
						inStmtRow = true;
						StmtLines++;
						if (StmtLines == 1) {

							String linewords[] = lineinFocus.split(" ");
							//
							Integer kt = linewords.length;
							lword1 = linewords[0];
							lword2 = linewords[1];
							lword9 = linewords[kt - 1];
							lword8 = linewords[kt - 2];
							lword7 = linewords[kt - 3];
							//
							lNarrative = "";
							Integer ii = 2;
							while (ii < kt - 3) {

								lNarrative = lNarrative + " " + linewords[ii];
								ii++;
							}
						}
						Boolean NewRow = true;

						// Use date here
						System.out.println("");
					} 
					else {
						StmtRow.concat(lineinFocus);
						lNarrative = lNarrative + lineinFocus;
					}

					// System.out.println(newLine);
				}
//			Save the last row of each page to Mongo
				BasicDBObject document = new BasicDBObject();
				document.put("TransactionDate", lword1);
				document.put("ValueDate", lword2);
				document.put("TransactionDetails", lNarrative);
				document.put("Debit", lword7);
				document.put("Credit", lword8);
				document.put("Balance", lword9);
				
				collection.insert(document);

				
				
				System.out.println("End of Page Encountered");
				System.out.println("PostDate: " + lword1);
				System.out.println("ValueDate: " + lword2);
				System.out.println("Debit " + lword7);
				System.out.println("Credit " + lword8);
				System.out.println("Balance " + lword9);
				System.out.println("Stmt narrative:" + lNarrative);
				//
				scanner.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}