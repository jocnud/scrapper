package com.shahbaz.scrapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jaunt.Document;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.ResponseException;
import com.jaunt.SearchException;
import com.jaunt.UserAgent;

public class App {
	private static final String MORNIGSTAR_OUTPUT_CSV = "mornigstar_output.csv";

	public static void main(String[] args) throws IOException {
		try {

			String baseUrl = "https://www.morningstar.in/equities/#alphabet.aspx";

			for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {

				String url = baseUrl.replace("#alphabet", String.valueOf(alphabet));
				System.out.println("Fetching data from " + url);
				getPageData(url);
			}

		} catch (JauntException e) {
			System.err.println(e);
		}
	}

	private static void getPageData(String baseUrl) throws ResponseException, SearchException, IOException {
		UserAgent userAgent = new UserAgent();
		userAgent.visit(baseUrl);
		int numOfSubPage = Integer.parseInt(userAgent.doc.findFirst("<span id='spnTotalCount'").getText().trim());
		extractedPageData(userAgent, userAgent.doc, numOfSubPage);
		userAgent.close();

	}

	private static void extractedPageData(UserAgent userAgent, Document doc, int numOfSubPage)
			throws SearchException, ResponseException, IOException {

		for (int i = 1; i <= numOfSubPage; i++) {

			Elements eml1 = doc.findFirst("<div class='archive'>").findEach("<div class = 'td.*'>");

			int count = 0;
			String rowBuilder = "";

			for (Element e : eml1) {

				String content = e.innerHTML().trim();
				if (content.startsWith("<a")) {

					rowBuilder = rowBuilder+extractTextFromAnchortag(content)+",";
					count++;
				} else {
					rowBuilder = rowBuilder+content+",";
					count++;
				}

				if (count % 6 == 0) {
					File outputFile = new File(MORNIGSTAR_OUTPUT_CSV);
					if (!outputFile.exists())
						outputFile.createNewFile();
					
					rowBuilder = rowBuilder + System.getProperty("line.separator");
					
					Files.write(Paths.get(MORNIGSTAR_OUTPUT_CSV), rowBuilder.toString().getBytes(),
							StandardOpenOption.APPEND);
					
					rowBuilder = "";
				}

			}
			doc = userAgent.doc.getForm(0).submit("Next");

		}
	}

	private static String extractTextFromAnchortag(String content) {
		Pattern titleFinder = Pattern.compile("<a[^>]*>(.*?)</a>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = titleFinder.matcher(content);
		while (regexMatcher.find()) {
			return regexMatcher.group(1);
		}
		return null;
	}
}
