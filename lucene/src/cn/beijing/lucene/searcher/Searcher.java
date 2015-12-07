/**
 * 
 */
package cn.beijing.lucene.searcher;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author zukai 2015-12-07
 */
public class Searcher {
	public static void search(String indexDir,String q) throws IOException, ParseException{
		Directory dir = FSDirectory.open(new File(indexDir));//打开索引文件
		IndexSearcher is = new IndexSearcher(dir);
		QueryParser parser = new QueryParser(Version.LUCENE_30, "contents", new StandardAnalyzer(Version.LUCENE_30));//解析查询字符串
		Query query = parser.parse(q);
		long start = System.currentTimeMillis();
		TopDocs hints = is.search(query, 10);//搜索索引
		long end = System.currentTimeMillis();
		System.err.println("Found: "+hints.totalHits+" document(s) (in "+(end-start)+" milliseconds) that matched query '"+q+"'");
		for(ScoreDoc srcDoc : hints.scoreDocs){
			Document doc = is.doc(srcDoc.doc);//返回匹配文本
			System.out.println(doc.get("fullpath"));
		}
		is.close();
	}
	public static void main(String[] args) throws IOException, ParseException {
		if(args.length != 2){
			throw new IllegalArgumentException("Usage: java "+Searcher.class.getName()+" <index dir> <query>.");
		}
		String indexDir = args[0];
		String q = args[1];
		search(indexDir, q);
		
	}
}
