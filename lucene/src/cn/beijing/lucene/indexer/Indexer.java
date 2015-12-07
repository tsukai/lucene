/**
 * 
 */
package cn.beijing.lucene.indexer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author zukai 2015-12-07
 */
public class Indexer {
	private IndexWriter writer;
	public Indexer(String indexDir) throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		//创建Lucene Index Writer
		writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30), true,IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	public void close() throws IOException{
		writer.close();
	}
	
	public int index(String dataDir,FileFilter filter) throws Exception{
		File[] files = new File(dataDir).listFiles();
		for(File f : files){
			if(!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() && (filter == null || filter.accept(f))){
				indexFile(f);
			}
		}
		//返回被索引文档数
		return writer.numDocs();
	}

	private void indexFile(File f) throws Exception{
		System.out.println("Index "+f.getCanonicalPath());
		Document doc = getDocument(f);
		writer.addDocument(doc);//向lucene索引中添加文档
	}

	private Document getDocument(File f) throws Exception{
		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));//索引文件内容
		doc.add(new Field("filename", f.getName(),Field.Store.YES,Field.Index.NOT_ANALYZED));//索引文件名
		doc.add(new Field("fullpath", f.getCanonicalPath(),Field.Store.YES,Field.Index.NOT_ANALYZED));//索引文件完整路径
		return doc;
	}
	
	private static class TextFilesFilter implements FileFilter{

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().toLowerCase().endsWith(".txt");//只索引*.txt文件
		}
		
	}

	public static void main(String[] args) throws Exception{
		if(args.length != 2){
			throw new IllegalArgumentException("Usage: java "+Indexer.class.getName()+" <index dir> <data dir>.");
		}
		String indexDir = args[0];//在指定目录创建索引
		String dataDir = args[1];//对指定目录中的*.txt文件进行多用
		long start = System.currentTimeMillis();
		Indexer indexer = new Indexer(indexDir);
		int numIndexed ;
		try{
			numIndexed = indexer.index(dataDir, new TextFilesFilter());
		}finally{
			indexer.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Indexing "+numIndexed+" files took "+(end-start)+" milliseconds");
	}
}
