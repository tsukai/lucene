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
 * IndexSearcher用于搜索IndexWriter类创建的索引，他是连接索引的中心环节。
 * 可看做是一个以只读方式打开索引的类。他需要利用Directory实例来掌控前期创建的索引，然后才能提供大量的搜索方法。
 * 
 * Term搜索功能的基本单元。与Field对象类似，Term对象包含一对字符串元素：域名和单词。
 * 与索引操作有关。
 * 
 * Query setBoost(float) 某个子查询相对于其他子查询来说必须对最后的评分有更强贡献。
 * 
 * TermQuery基本的查询类型。
 * 用来匹配指定域中包含特定项的文档。
 * 
 * TopDocs负责展示搜索结果
 * 是一个简单的指针容器，指针一般指向前N个排名的搜索结果，搜索结果即匹配查询条件的文档。
 * TopDocs会记录前N个结果中每个结果的int docId (可以用它类恢复文档)和浮点型分数。
 * @author zukai 2015-12-07
 */
public class Searcher {
	public static void search(String indexDir,String q) throws IOException, ParseException{
		Directory dir = FSDirectory.open(new File(indexDir));//打开索引文件
		IndexSearcher is = new IndexSearcher(dir);
		QueryParser parser = new QueryParser(Version.LUCENE_30, "contents", new StandardAnalyzer(Version.LUCENE_30));//解析查询字符串
		Query query = parser.parse(q);//contents域中包含q的文档
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
